package com.example.yiliaoxitong.security;

/**
 * 系统角色。用于 JWT 载荷与 @RequireRole 权限校验。
 */
public enum Role {
    /** 管理员：可维护科室、医生、患者、排班等基础数据 */
    ADMIN,
    /** 医生：可接诊、开具处方、查看患者档案 */
    DOCTOR,
    /** 患者：可预约挂号、查询排班与公开信息 */
    PATIENT
}
