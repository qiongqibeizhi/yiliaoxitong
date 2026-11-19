import request from '../utils/request'

/** 患者下单购药 */
export function purchase(data) {
  return request.post('/api/purchases', data)
}

/** 购药订单分页：患者查本人订单，管理员查全部 */
export function pagePurchases(params) {
  return request.get('/api/purchases', { params })
}

/** 虚拟收银台支付 */
export function payPurchase(id, payPassword, payChannel = 'WECHAT', bankCardNo = '') {
  return request.put(`/api/purchases/${id}/pay`, { payPassword, payChannel, bankCardNo })
}
