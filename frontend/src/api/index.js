import request from '../utils/request'

// ============ 文件上传 ============
export const uploadFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// ============ 认证 ============
export const login = (data) => request.post('/auth/login', data)
export const register = (data) => request.post('/auth/register', data)
export const getCurrentUser = () => request.get('/auth/me')

// ============ 商品 ============
export const getProducts = (params) => request.get('/products', { params })
export const getProductDetail = (id) => request.get(`/products/${id}`)
export const saveProduct = (data) => request.post('/products', data)
export const updateProduct = (data) => request.put('/products', data)
export const updateProductStatus = (id, status) => request.put(`/products/${id}/status`, null, { params: { status } })
export const deleteProduct = (id) => request.delete(`/products/${id}`)

// ============ 分类 ============
export const getCategories = () => request.get('/categories')
export const saveCategory = (data) => request.post('/categories', data)
export const updateCategory = (data) => request.put('/categories', data)
export const deleteCategory = (id) => request.delete(`/categories/${id}`)

// ============ 购物车 ============
export const getCart = () => request.get('/cart')
export const addToCart = (data) => request.post('/cart', data)
export const updateCartQuantity = (id, quantity) => request.put(`/cart/${id}/quantity`, null, { params: { quantity } })
export const updateCartSelected = (id, selected) => request.put(`/cart/${id}/selected`, null, { params: { selected } })
export const removeCartItem = (id) => request.delete(`/cart/${id}`)
export const clearCart = () => request.delete('/cart/clear')

// ============ 订单 ============
export const submitOrder = (data) => request.post('/orders', data)
export const getUserOrders = (params) => request.get('/orders', { params })
export const getMerchantOrders = (params) => request.get('/orders/merchant', { params })
export const getOrderDetail = (id) => request.get(`/orders/${id}`)
export const cancelOrder = (id) => request.put(`/orders/${id}/cancel`)
export const confirmReceive = (id) => request.put(`/orders/${id}/confirm`)

// ============ 支付 ============
export const payOrder = (data) => request.post('/payment/pay', data)

// ============ 退款 ============
export const applyRefund = (data) => request.post('/refunds', data)
export const getUserRefunds = () => request.get('/refunds/user')
export const getMerchantRefunds = () => request.get('/refunds/merchant')
export const handleRefund = (data) => request.post('/refunds/handle', data)

// ============ 用户 ============
export const getUserProfile = () => request.get('/user/profile')
export const updateUserProfile = (data) => request.put('/user/profile', data)
export const getAddresses = () => request.get('/user/addresses')
export const saveAddress = (data) => request.post('/user/addresses', data)
export const updateAddress = (data) => request.put('/user/addresses', data)
export const deleteAddress = (id) => request.delete(`/user/addresses/${id}`)

// ============ 商家 ============
export const shipOrder = (data) => request.post('/merchant/ship', data)
export const getMerchantStats = () => request.get('/merchant/stats')

// ============ 管理员 ============
export const getUsers = (params) => request.get('/admin/users', { params })
export const updateUserStatus = (data) => request.put('/admin/users/status', data)
export const getMerchants = (params) => request.get('/admin/merchants', { params })
export const updateMerchantStatus = (data) => request.put('/admin/merchants/status', data)
export const getAdminStats = () => request.get('/admin/stats')
export const getChartData = (type) => request.get('/admin/chart', { params: { type } })

// ============ 系统配置 ============
export const getConfigs = () => request.get('/config')
export const updateConfig = (id, value) => request.put(`/config/${id}`, null, { params: { value } })
