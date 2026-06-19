import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue'), meta: { requiresAuth: false } },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue'), meta: { requiresAuth: false } },
  { path: '/', name: 'Home', component: () => import('../views/Home.vue'), meta: { requiresAuth: false } },
  { path: '/movies', name: 'MovieList', component: () => import('../views/MovieList.vue'), meta: { requiresAuth: false } },
  { path: '/movie/:id', name: 'MovieDetail', component: () => import('../views/MovieDetail.vue'), meta: { requiresAuth: false } },
  { path: '/rankings', name: 'Rankings', component: () => import('../views/Rankings.vue'), meta: { requiresAuth: false } },
  { path: '/crew', name: 'CrewSearch', component: () => import('../views/CrewSearch.vue'), meta: { requiresAuth: false } },
  { path: '/crew/:id', name: 'CrewDetail', component: () => import('../views/CrewDetail.vue'), meta: { requiresAuth: false } },
  { path: '/user', name: 'UserCenter', component: () => import('../views/UserCenter.vue'), meta: { requiresAuth: true } },
  { path: '/payment', name: 'Payment', component: () => import('../views/Payment.vue'), meta: { requiresAuth: true } },
  { path: '/payment/result', name: 'PaymentResult', component: () => import('../views/PaymentResult.vue'), meta: { requiresAuth: true } },
  { path: '/statistics', name: 'Statistics', component: () => import('../views/Statistics.vue'), meta: { requiresAuth: false } },
  { path: '/graph', name: 'Graph', component: () => import('../views/Graph.vue'), meta: { requiresAuth: false } },
  { path: '/play/:id', name: 'VideoPlayer', component: () => import('../views/VideoPlayer.vue'), meta: { requiresAuth: false } },
  { path: '/admin', component: () => import('../views/Admin.vue'), meta: { requiresAuth: true }, children: [
    { path: '', redirect: '/admin/dashboard' },
    { path: 'dashboard', name: 'AdminDashboard', component: () => import('../views/admin/Dashboard.vue') },
    { path: 'movies', name: 'AdminMovies', component: () => import('../views/admin/MovieManage.vue') },
    { path: 'users', name: 'AdminUsers', component: () => import('../views/admin/UserManage.vue') }
  ]}
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else { next() }
})

export default router
