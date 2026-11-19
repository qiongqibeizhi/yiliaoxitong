import request from '../utils/request'

/** 查询本人钱包余额 */
export function getBalance() {
  return request.get('/api/wallet')
}

/** 沙箱充值：自定义金额 + 付款密码（123456） */
export function recharge(data) {
  return request.post('/api/wallet/recharge', data)
}
