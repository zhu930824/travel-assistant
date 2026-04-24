import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: 'TravelAI - 智能旅游规划' }
  },
  {
    path: '/plan',
    name: 'Plan',
    component: () => import('@/views/Plan.vue'),
    meta: { title: '规划详情 - TravelAI' }
  },
  {
    path: '/autogen',
    name: 'AutoGenChat',
    component: () => import('@/views/AutoGenChat.vue'),
    meta: { title: 'AutoGen 人机协作 - TravelAI' }
  },
  {
    path: '/guide/:id',
    name: 'GuideDetail',
    component: () => import('@/views/GuideDetail.vue'),
    meta: { title: '攻略详情 - TravelAI' }
  },
  {
    path: '/guide/create',
    name: 'GuideEditor',
    component: () => import('@/components/guide/GuideEditor.vue'),
    meta: { title: '发布攻略 - TravelAI', requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { title: '个人中心 - TravelAI', requiresAuth: true }
  },
  {
    path: '/payment',
    name: 'Payment',
    component: () => import('@/views/Payment.vue'),
    meta: { title: '会员支付 - TravelAI', requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title || 'TravelAI'

  // 需要登录的页面
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('token')
    if (!token) {
      next('/')
      return
    }
  }

  next()
})

export default router
