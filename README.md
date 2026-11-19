# 仁和医疗系统（yiliaoxitong）

Java Web 课程综合项目：Spring Boot 3 + MyBatis-Plus + Vue 3 全栈医疗挂号系统，覆盖
**患者自助注册 → 科室/医生导诊 → 在线挂号 → 医生接诊/开方 → 患者购药 → 沙箱支付** 的完整业务闭环。

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Spring Boot 3.3、MyBatis-Plus、JWT 认证（含注销黑名单）、Bean Validation |
| 数据库 | MySQL 8（`sql/medical_db.sql` 全量建表 + 演示数据） |
| 前端 | Vue 3 + Vite 6、Element Plus、Vue Router、Axios |
| 测试 | JUnit 5（CRUD/安全/事务用例） |

## 启动步骤

1. 建库导入：`mysql -uroot -p < sql/medical_db.sql`（库名 `yiliaoxitong`）。
2. 后端（默认 8080，数据库口令在 `src/main/resources/application-local.properties`）：
   ```bash
   ./mvnw spring-boot:run -DskipTests
   ```
3. 前端（默认 5173，已配置代理与 CORS）：
   ```bash
   cd frontend && npm install && npm run dev
   ```

## 角色与功能矩阵

| 功能 | 患者 | 医生 | 管理员 |
|---|---|---|---|
| 自助注册（建档+开通账号） | ✅ | 预置账号 | 预置账号 |
| 科室/医生/排班/药品浏览 | ✅ | ✅ | ✅ |
| 在线挂号 / 退号本人单 | ✅ | — | ✅ 全部 |
| 查看挂号记录 | 仅本人 | 仅自己接诊的 | 全部 |
| 查看患者档案 | — | 仅挂过自己号的患者（身份证/手机号脱敏） | 全部（完整信息，可按身份证查询） |
| 完成就诊（处理病症） | — | ✅ 本人接诊的 | ✅ |
| 开处方（扣库存事务） | — | ✅ 仅本人接诊的挂号 | ✅ |
| 查看处方 | 仅本人 | 仅本人开具 | 全部 |
| 单独购药 + 虚拟支付 | ✅（钱包余额支付） | — | ✅（查看订单） |
| 钱包充值（自定义金额 + 付款密码） | ✅ | — | — |
| 支付处方药费（医生开单后凭余额付款） | ✅ | — | — |
| 取消预约 | 仅非当天 | — | ✅ 全部 |

数据权限统一在 Controller/Service 层按 JWT 中的角色与 `patientId`/`doctorId` 收窄，
越权访问返回 403/400，并记录日志。

## 沙箱约定（教学演示）

- 演示账号（口令均 `123456`）：`admin` / `doctor1` / `patient1`；患者也可在登录页自助注册。
- 虚拟收银台不产生真实扣款，统一**付款密码 `123456`**，支持**微信支付 / 支付宝 / 银行卡**三种渠道（银行卡需 13-19 位卡号）。
- 演示药品数据（含 DRG019）仅用于教学，非真实药品信息。

## 核心事务与一致性设计

- **挂号事务**：校验 → 条件更新原子扣号源 → 写挂号单，并发抢号防超卖；取消挂号同步释放号源。
- **开方事务**：写处方主表 → 逐条扣库存 → 写明细，任一药品库存不足全部回滚。
- **购药事务**：校验在售 → 条件更新原子扣库存 → 写订单（待支付），支付需虚拟收银台密码校验。
- 库存/号源扣减均使用 `UPDATE ... WHERE stock/slots >= n` 条件更新，杜绝负数。

## 目录结构

```
src/main/java/com/example/yiliaoxitong
├── controller   # 9 个 REST 控制器（auth/patient/department/doctor/schedule/
│                #   registration/prescription/drug/purchase）
├── service      # 业务接口与事务实现
├── mapper       # MyBatis-Plus Mapper（条件更新在注解 SQL 中）
├── entity/dto   # 实体与请求参数
├── security     # JWT、角色注解、AuthContext
├── config       # 拦截器（认证+角色）、CORS、MyBatis-Plus 配置
└── exception    # 业务异常与全局异常处理
frontend/src
├── api          # Axios 接口封装
├── views        # 按角色划分的 16 个页面
└── router       # 路由 + 登录/角色双重守卫
```
