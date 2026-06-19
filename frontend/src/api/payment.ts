import request from './index'

export function getProducts() {
  return request.get('/payment/products')
}

export function createOrder(productId: string) {
  return request.post('/payment/create', { productId })
}

export function getPaymentStatus(orderNo: string) {
  return request.get(`/payment/status/${orderNo}`)
}
