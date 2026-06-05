import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  // ========== 用户端 ==========
  {
    path: '/',
    component: () => import('../views/user/Layout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('../views/user/Home.vue') },
      { path: 'products/:id', name: 'ProductDetail', component: () => import('../views/user/ProductDetail.vue') },
      { path: 'cart', name: 'Cart', component: () => import('../views/user/Cart.vue'), meta: { auth: true } },
      { path: 'checkout', name: 'Checkout', component: () => import('../views/user/Checkout.vue'), meta: { auth: true } },
      { path: 'orders', name: 'Orders', component: () => import('../views/user/Orders.vue'), meta: { auth: true } },
      { path: 'orders/:id', name: 'OrderDetail', component: () => import('../views/user/OrderDetail.vue'), meta: { auth: true } },
      { path: 'profile', name: 'Profile', component: () => import('../views/user/Profile.vue'), meta: { auth: true } },
      { path: 'addresses', name: 'Addresses', component: () => import('../views/user/Addresses.vue'), meta: { auth: true } },
    ]
  },
  { path: '/login', name: 'Login', component: () => import('../views/user/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/user/Register.vue') },

  // ========== 商家端 ==========
  {
    path: '/merchant',
    component: () => import('../views/merchant/Layout.vue'),
    meta: { auth: true, role: 'MERCHANT' },
    children: [
      { path: '', name: 'MerchantDashboard', component: () => import('../views/merchant/Dashboard.vue') },
      { path: 'products', name: 'MerchantProducts', component: () => import('../views/merchant/Products.vue') },
      { path: 'orders', name: 'MerchantOrders', component: () => import('../views/merchant/Orders.vue') },
      { path: 'refunds', name: 'MerchantRefunds', component: () => import('../views/merchant/Refunds.vue') },
    ]
  },

  // ========== 管理员端 ==========
  {
    path: '/admin',
    component: () => import('../views/admin/Layout.vue'),
    meta: { auth: true, role: 'ADMIN' },
    children: [
      { path: '', name: 'AdminDashboard', component: () => import('../views/admin/Dashboard.vue') },
      { path: 'users', name: 'AdminUsers', component: () => import('../views/admin/Users.vue') },
      { path: 'merchants', name: 'AdminMerchants', component: () => import('../views/admin/Merchants.vue') },
      { path: 'categories', name: 'AdminCategories', component: () => import('../views/admin/Categories.vue') },
      { path: 'config', name: 'AdminConfig', component: () => import('../views/admin/Config.vue') },
    ]
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || 'null')

  if (to.meta.auth && !token) {
    next('/login')
    return
  }

  if (to.meta.role && user?.role !== to.meta.role) {
    if (user?.role === 'MERCHANT') next('/merchant')
    else if (user?.role === 'ADMIN') next('/admin')
    else next('/')
    return
  }

  next()
})

export default router
