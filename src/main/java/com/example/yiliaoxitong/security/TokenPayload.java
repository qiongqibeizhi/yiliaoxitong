package com.example.yiliaoxitong.security;

/**
 * JWT 解析后的载荷，随请求在线程内传递。
 *
 * @param username  登录名
 * @param role      角色（ADMIN / DOCTOR / PATIENT）
 * @param patientId 患者账号关联的患者编号，医生与管理员为 null
 * @param doctorId  医生账号关联的医生编号，患者与管理员为 null
 * @param jti       令牌唯一标识，注销时用于加入黑名单
 * @param expiresAt 过期时间（秒级时间戳）
 */
public record TokenPayload(String username, String role, Long patientId, Long doctorId,
                           String jti, long expiresAt) {
}
