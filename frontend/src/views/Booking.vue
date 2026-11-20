<template>
  <div class="page-card">
    <el-page-header content="挂号预约" @back="$router.back()" class="back" />
    <h2 class="page-title">确认预约信息</h2>
    <p class="page-desc">请核对就诊人与排班信息，提交后号源立即占用；如需退号可在挂号记录中取消。</p>

    <el-alert
      v-if="user && user.role !== 'PATIENT'"
      type="warning"
      :closable="false"
      show-icon
      title="当前账号不是患者账号"
      description="挂号接口仅对患者角色开放，请退出后使用 patient1 / 123456 登录再预约。"
      class="role-alert"
    />

    <el-row :gutter="20">
      <el-col :span="14">
        <el-card shadow="never" class="info-card">
          <template #header><span class="card-title">就诊信息</span></template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="就诊人">{{ user?.realName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="账号">{{ user?.username || '—' }}</el-descriptions-item>
            <el-descriptions-item label="患者编号">{{ user?.patientId || '未绑定患者档案' }}</el-descriptions-item>
            <el-descriptions-item label="就诊科室">{{ departmentName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="出诊医生">{{ doctor.name || '—' }}（{{ doctor.title || '医师' }}）</el-descriptions-item>
            <el-descriptions-item label="就诊日期">{{ schedule.scheduleDate || '—' }} {{ schedule.timeSlot || '' }}</el-descriptions-item>
            <el-descriptions-item label="挂号费">
              <span class="fee">{{ schedule.regFee ?? '—' }} 元</span>
            </el-descriptions-item>
            <el-descriptions-item label="剩余号源">
              {{ Math.max((schedule.totalSlots || 0) - (schedule.bookedSlots || 0), 0) }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card shadow="never" class="info-card confirm-card">
          <template #header><span class="card-title">预约须知</span></template>
          <ul class="tips">
            <li>同一患者同一天重复挂同一医生的号，系统会予以提示。</li>
            <li>号源已满时提示“号源不足”，不会产生挂号记录。</li>
            <li>提交后号源立即锁定；如预约失败，号源不受影响。</li>
            <li>您的个人信息将严格保密，仅在就诊时使用。</li>
          </ul>
          <el-button
            type="primary"
            size="large"
            class="confirm-btn"
            :loading="submitting"
            :disabled="!canSubmit"
            @click="submit"
          >
            确认挂号
          </el-button>
          <div v-if="!canSubmit" class="disabled-hint">
            {{ user?.role !== 'PATIENT' ? '仅患者账号可提交挂号' : '缺少患者编号，无法挂号' }}
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" title="挂号成功" width="460px" class="success-dialog">
      <div class="success-body">
        <el-result icon="success" title="挂号成功" :sub-title="`挂号编号 ${registration.regNo || ''}`">
          <template #extra>
            <el-descriptions :column="1" border size="small" class="result-desc">
              <el-descriptions-item label="就诊日期">{{ registration.visitDate }}</el-descriptions-item>
              <el-descriptions-item label="时段">{{ registration.timeSlot }}</el-descriptions-item>
              <el-descriptions-item label="挂号费">{{ registration.regFee }} 元</el-descriptions-item>
              <el-descriptions-item label="状态">{{ statusText(registration.status) }}</el-descriptions-item>
            </el-descriptions>
          </template>
        </el-result>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">留在本页</el-button>
        <el-button type="primary" @click="goDoctors">继续预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSchedule } from '../api/schedule'
import { getDoctor } from '../api/doctor'
import { getDepartment } from '../api/department'
import { register } from '../api/registration'
import { getUser } from '../utils/auth'

const route = useRoute()
const router = useRouter()
const user = ref(getUser())
const doctor = ref({})
const schedule = ref({})
const departmentName = ref('')
const submitting = ref(false)
const dialogVisible = ref(false)
const registration = ref({})

const canSubmit = computed(() => user.value?.role === 'PATIENT' && user.value?.patientId)

const STATUS_TEXT = { 0: '待就诊', 1: '已完成', 2: '已取消' }
function statusText(status) {
  return STATUS_TEXT[status] ?? status
}

async function load() {
  const scheduleId = route.query.scheduleId
  if (!scheduleId) {
    ElMessage.warning('请从医生详情页选择排班后再预约')
    return
  }
  schedule.value = await getSchedule(scheduleId)
  if (schedule.value.doctorId) {
    doctor.value = await getDoctor(schedule.value.doctorId)
    if (doctor.value.departmentId) {
      const dept = await getDepartment(doctor.value.departmentId).catch(() => null)
      departmentName.value = dept ? dept.deptName : ''
    }
  }
}

async function submit() {
  submitting.value = true
  try {
    const data = await register({
      patientId: user.value.patientId,
      scheduleId: schedule.value.id
    })
    registration.value = data
    dialogVisible.value = true
    // 本地同步最新号源，避免用户返回后看到旧数据
    schedule.value.bookedSlots = (schedule.value.bookedSlots || 0) + 1
  } catch (e) {
    // 业务异常（重复挂号、号源已满）由拦截器统一提示
  } finally {
    submitting.value = false
  }
}

function goDoctors() {
  dialogVisible.value = false
  router.push(`/doctors/${doctor.value.id}`)
}

onMounted(load)
</script>

<style scoped>
.back {
  margin-bottom: 16px;
}
.role-alert {
  margin-bottom: 18px;
}
.card-title {
  font-weight: 600;
  font-size: 15px;
}
.confirm-card {
  height: 100%;
}
.tips {
  color: #40536b;
  font-size: 13px;
  line-height: 2;
  padding-left: 18px;
}
.fee {
  color: #e6543a;
  font-weight: 600;
}
.confirm-btn {
  width: 100%;
  margin-top: 10px;
}
.disabled-hint {
  margin-top: 8px;
  font-size: 12px;
  color: #9aa8ba;
  text-align: center;
}
.result-desc {
  margin-top: 8px;
}
</style>
