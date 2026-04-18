<template>
  <div class="profile-container">
    <!-- Background Effects -->
    <div class="bg-effects">
      <div class="gradient-orb gradient-orb-1"></div>
      <div class="gradient-orb gradient-orb-2"></div>
      <div class="grid-overlay"></div>
    </div>

    <!-- Header -->
    <header class="profile-header">
      <div class="header-inner">
        <router-link to="/" class="logo">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 16V8a2 2 0 00-1-1.73l-7-4a2 2 0 00-2 0l-7 4A2 2 0 003 8v8a2 2 0 001 1.73l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z"/>
              <polyline points="7.5 4.21 12 6.81 16.5 4.21"/>
              <polyline points="7.5 19.79 7.5 14.6 3 12"/>
              <polyline points="21 12 16.5 14.6 16.5 19.79"/>
              <polyline points="3.27 6.96 12 12.08 20.73 6.96"/>
              <line x1="12" y1="22.08" x2="12" y2="12"/>
            </svg>
          </div>
          <span class="logo-text">TravelAI</span>
        </router-link>

        <nav class="nav-links">
          <router-link to="/">首页</router-link>
          <router-link to="/plan">规划</router-link>
        </nav>

        <div class="header-actions">
          <template v-if="isLoggedIn">
            <router-link to="/profile" class="user-link">
              <div class="user-avatar">{{ userInfo?.username?.charAt(0).toUpperCase() }}</div>
              <span class="user-name">{{ userInfo?.username }}</span>
            </router-link>
            <button class="btn-ghost" @click="handleLogout">退出</button>
          </template>
          <template v-else>
            <button class="btn-ghost" @click="$router.push('/')">登录</button>
          </template>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="profile-main">
      <!-- User Card -->
      <div class="user-card">
        <div class="user-card-bg"></div>
        <div class="user-avatar-large">
          {{ userInfo?.username?.charAt(0).toUpperCase() }}
        </div>
        <div class="user-info">
          <h1 class="user-nickname">{{ userInfo?.nickname || userInfo?.username }}</h1>
          <p class="user-username">@{{ userInfo?.username }}</p>
        </div>
        <div class="member-badge" :class="memberLevelClass">
          <span class="member-icon">{{ memberLevelIcon }}</span>
          <span class="member-name">{{ userInfo?.memberLevelName || '免费版' }}</span>
        </div>
      </div>

      <!-- Stats Cards -->
      <div class="stats-cards">
        <div class="stat-card">
          <div class="stat-icon stat-icon--plans">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-value">
              <span class="used">{{ userInfo?.usedPlans || 0 }}</span>
              <span class="divider">/</span>
              <span class="limit">{{ planLimitText }}</span>
            </div>
            <div class="stat-label">本月规划次数</div>
          </div>
          <div class="stat-progress">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: planProgress + '%' }"></div>
            </div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon stat-icon--days">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
              <line x1="16" y1="2" x2="16" y2="6"/>
              <line x1="8" y1="2" x2="8" y2="6"/>
              <line x1="3" y1="10" x2="21" y2="10"/>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ memberDaysLeft }}</div>
            <div class="stat-label">会员剩余天数</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon stat-icon--status">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-value" :class="canPlanClass">{{ canPlanText }}</div>
            <div class="stat-label">规划状态</div>
          </div>
        </div>
      </div>

      <!-- Member Benefits -->
      <div class="benefits-section">
        <h2 class="section-title">会员权益</h2>
        <div class="benefits-grid">
          <div class="benefit-card" v-for="benefit in currentBenefits" :key="benefit.title">
            <div class="benefit-icon">{{ benefit.icon }}</div>
            <h3 class="benefit-title">{{ benefit.title }}</h3>
            <p class="benefit-desc">{{ benefit.desc }}</p>
            <span class="benefit-status" :class="{ active: benefit.active }">
              {{ benefit.active ? '已开通' : '未开通' }}
            </span>
          </div>
        </div>
      </div>

      <!-- Upgrade Section -->
      <div class="upgrade-section" v-if="userInfo?.memberLevel === 'FREE'">
        <h2 class="section-title">升级会员</h2>
        <div class="upgrade-cards">
          <div class="upgrade-card">
            <div class="upgrade-header">
              <span class="upgrade-badge">推荐</span>
              <h3 class="upgrade-name">专业版</h3>
              <div class="upgrade-price">
                <span class="price">¥29</span>
                <span class="unit">/月</span>
              </div>
            </div>
            <ul class="upgrade-features">
              <li><span class="check">✓</span> 无限规划次数</li>
              <li><span class="check">✓</span> 高级AI分析</li>
              <li><span class="check">✓</span> 行程导出PDF</li>
              <li><span class="check">✓</span> 优先客服支持</li>
            </ul>
            <div class="upgrade-duration">
              <label>选择时长：</label>
              <div class="duration-options">
                <button
                  class="duration-btn"
                  :class="{ active: proDuration === 1 }"
                  @click="proDuration = 1"
                >1个月</button>
                <button
                  class="duration-btn"
                  :class="{ active: proDuration === 3 }"
                  @click="proDuration = 3"
                >3个月</button>
                <button
                  class="duration-btn"
                  :class="{ active: proDuration === 6 }"
                  @click="proDuration = 6"
                >6个月</button>
                <button
                  class="duration-btn"
                  :class="{ active: proDuration === 12 }"
                  @click="proDuration = 12"
                >12个月</button>
              </div>
              <div class="total-price">
                总价：<span class="price-value">¥{{ proDuration * 29 }}</span>
              </div>
            </div>
            <div class="payment-method-section">
              <label>支付方式：</label>
              <div class="payment-method-options">
                <button
                  class="method-btn disabled"
                  disabled
                  title="微信支付暂未开放"
                >
                  <span class="method-icon">💚</span>
                  <span class="method-name">微信支付</span>
                  <span class="method-status">暂不可用</span>
                </button>
                <button
                  class="method-btn"
                  :class="{ active: paymentMethod === 'ALIPAY' }"
                  @click="paymentMethod = 'ALIPAY'"
                >
                  <span class="method-icon">💙</span>
                  <span class="method-name">支付宝</span>
                </button>
              </div>
            </div>
            <button class="upgrade-btn" @click="upgradeToPro">
              立即升级
            </button>
          </div>

          <div class="upgrade-card upgrade-card--enterprise">
            <div class="upgrade-header">
              <h3 class="upgrade-name">企业版</h3>
              <div class="upgrade-price">
                <span class="price">联系我们</span>
              </div>
            </div>
            <ul class="upgrade-features">
              <li><span class="check">✓</span> 无限规划次数</li>
              <li><span class="check">✓</span> API接口接入</li>
              <li><span class="check">✓</span> 团队协作功能</li>
              <li><span class="check">✓</span> 私有化部署</li>
            </ul>
            <button class="upgrade-btn upgrade-btn--contact" @click="contactUs">
              联系我们
            </button>
          </div>
        </div>
      </div>

      <!-- Recent Plans -->
      <div class="recent-section">
        <h2 class="section-title">最近规划</h2>
        <div class="recent-empty" v-if="recentPlans.length === 0">
          <div class="empty-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/>
            </svg>
          </div>
          <p>暂无规划记录</p>
          <router-link to="/" class="btn-start">开始规划</router-link>
        </div>
        <div class="recent-list" v-else>
          <div class="recent-item" v-for="plan in recentPlans" :key="plan.id">
            <div class="recent-status" :class="plan.status === 1 ? 'status-success' : 'status-failed'">
              {{ plan.status === 1 ? '✓' : '✗' }}
            </div>
            <div class="recent-content">
              <div class="recent-destination">{{ plan.destination }}</div>
              <div class="recent-meta">
                <span class="recent-days">{{ plan.days }}天</span>
                <span class="recent-divider">·</span>
                <span class="recent-date">{{ plan.date }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '@/services/api'

const router = useRouter()

const isLoggedIn = ref(false)
const userInfo = ref(null)

// 会员等级样式
const memberLevelClass = computed(() => {
  const level = userInfo.value?.memberLevel
  return {
    'member-free': level === 'FREE' || !level,
    'member-pro': level === 'PRO',
    'member-enterprise': level === 'ENTERPRISE'
  }
})

// 会员图标
const memberLevelIcon = computed(() => {
  const level = userInfo.value?.memberLevel
  if (level === 'PRO') return '⭐'
  if (level === 'ENTERPRISE') return '👑'
  return '🆓'
})

// 规划次数限制文本
const planLimitText = computed(() => {
  const limit = userInfo.value?.planLimit
  return limit >= 9999 ? '∞' : limit
})

// 规划进度
const planProgress = computed(() => {
  const used = userInfo.value?.usedPlans || 0
  const limit = userInfo.value?.planLimit || 5
  if (limit >= 9999) return 0
  return Math.min((used / limit) * 100, 100)
})

// 会员剩余天数
const memberDaysLeft = computed(() => {
  const expireTime = userInfo.value?.memberExpireTime
  if (!expireTime) return '-'
  const expire = new Date(expireTime)
  const now = new Date()
  const days = Math.ceil((expire - now) / (1000 * 60 * 60 * 24))
  return days > 0 ? `${days}天` : '已过期'
})

// 规划状态
const canPlanText = computed(() => {
  return userInfo.value?.canPlan ? '可规划' : '已达上限'
})

const canPlanClass = computed(() => {
  return userInfo.value?.canPlan ? 'status-ok' : 'status-limit'
})

// 当前权益
const currentBenefits = computed(() => {
  const level = userInfo.value?.memberLevel
  const isPro = level === 'PRO'
  const isEnterprise = level === 'ENTERPRISE'

  return [
    {
      icon: '📊',
      title: '规划次数',
      desc: isPro || isEnterprise ? '无限次数' : '每月5次',
      active: true
    },
    {
      icon: '🎯',
      title: '高级AI分析',
      desc: '深度行程优化',
      active: isPro || isEnterprise
    },
    {
      icon: '📄',
      title: 'PDF导出',
      desc: '导出完整行程',
      active: isPro || isEnterprise
    },
    {
      icon: '⚡',
      title: '优先支持',
      desc: '专属客服通道',
      active: isPro || isEnterprise
    },
    {
      icon: '🔌',
      title: 'API接入',
      desc: '开发者接口',
      active: isEnterprise
    },
    {
      icon: '🏢',
      title: '私有部署',
      desc: '企业独立部署',
      active: isEnterprise
    }
  ]
})

// 最近规划记录
const recentPlans = ref([])

// 升级相关
const proDuration = ref(1)
const paymentMethod = ref('ALIPAY') // 默认使用支付宝，微信支付暂时不可用

// 升级专业版 - 跳转到支付页面
const upgradeToPro = () => {
  router.push({
    path: '/payment',
    query: {
      memberLevel: 'PRO',
      months: proDuration.value,
      paymentMethod: paymentMethod.value
    }
  })
}

// 联系我们
const contactUs = () => {
  alert('请联系客服：support@travelai.com')
}

// 退出登录
const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/')
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const data = await userApi.getUserInfo()
    userInfo.value = data
    isLoggedIn.value = true

    // 加载规划记录
    await loadPlanRecords()
  } catch (error) {
    console.error('获取用户信息失败:', error)
    router.push('/')
  }
}

// 加载规划记录
const loadPlanRecords = async () => {
  try {
    const records = await userApi.getPlanRecords(10)
    recentPlans.value = records.map(record => ({
      id: record.id,
      destination: record.destination,
      days: record.days,
      date: record.dateStr,
      status: record.status
    }))
  } catch (error) {
    console.error('获取规划记录失败:', error)
  }
}

onMounted(() => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/')
    return
  }
  loadUserInfo()
})
</script>

<style scoped>
/* Container */
.profile-container {
  min-height: 100vh;
  background: #0a0a0f;
  color: #fff;
  position: relative;
  overflow-x: hidden;
}

/* Background Effects */
.bg-effects {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(120px);
  opacity: 0.4;
}

.gradient-orb-1 {
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, #4f46e5 0%, transparent 70%);
  top: -200px;
  right: -200px;
}

.gradient-orb-2 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, #7c3aed 0%, transparent 70%);
  bottom: -150px;
  left: -150px;
}

.grid-overlay {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255,255,255,0.02) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,0.02) 1px, transparent 1px);
  background-size: 60px 60px;
}

/* Header */
.profile-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  padding: 16px 24px;
  background: rgba(10, 10, 15, 0.9);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  color: #fff;
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-icon svg {
  width: 20px;
  height: 20px;
  color: #fff;
}

.logo-text {
  font-size: 1.25rem;
  font-weight: 700;
}

.nav-links {
  display: flex;
  gap: 32px;
}

.nav-links a {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  transition: color 0.2s;
}

.nav-links a:hover {
  color: #fff;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-link {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  color: #fff;
}

.user-avatar {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.85rem;
  font-weight: 600;
  color: #fff;
}

.user-name {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.9);
}

.btn-ghost {
  padding: 10px 20px;
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.9rem;
  cursor: pointer;
  transition: color 0.2s;
}

.btn-ghost:hover {
  color: #fff;
}

/* Main Content */
.profile-main {
  position: relative;
  z-index: 1;
  max-width: 1000px;
  margin: 0 auto;
  padding: 100px 24px 60px;
}

/* User Card */
.user-card {
  position: relative;
  padding: 40px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.15), rgba(139, 92, 246, 0.1));
  border: 1px solid rgba(139, 92, 246, 0.2);
  border-radius: 24px;
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 32px;
  overflow: hidden;
}

.user-card-bg {
  position: absolute;
  top: -50%;
  right: -20%;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.2) 0%, transparent 70%);
  pointer-events: none;
}

.user-avatar-large {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  font-weight: 700;
  color: #fff;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
}

.user-nickname {
  font-size: 1.75rem;
  font-weight: 700;
  margin-bottom: 4px;
}

.user-username {
  font-size: 0.95rem;
  color: rgba(255, 255, 255, 0.5);
}

.member-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 100px;
  font-size: 0.9rem;
  font-weight: 600;
}

.member-free {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.8);
}

.member-pro {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.3), rgba(139, 92, 246, 0.3));
  color: #a5b4fc;
}

.member-enterprise {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.3), rgba(234, 179, 8, 0.3));
  color: #fcd34d;
}

/* Stats Cards */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 40px;
}

.stat-card {
  padding: 24px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon svg {
  width: 24px;
  height: 24px;
}

.stat-icon--plans {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.25), rgba(139, 92, 246, 0.25));
  color: #a5b4fc;
}

.stat-icon--days {
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.25), rgba(16, 185, 129, 0.25));
  color: #86efac;
}

.stat-icon--status {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.25), rgba(234, 179, 8, 0.25));
  color: #fcd34d;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  margin-bottom: 4px;
}

.stat-value .used {
  color: #fff;
}

.stat-value .divider {
  color: rgba(255, 255, 255, 0.3);
  margin: 0 4px;
}

.stat-value .limit {
  color: rgba(255, 255, 255, 0.5);
}

.stat-value.status-ok {
  color: #86efac;
}

.stat-value.status-limit {
  color: #f87171;
}

.stat-label {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.5);
}

.stat-progress {
  margin-top: auto;
}

.progress-bar {
  height: 4px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  border-radius: 2px;
  transition: width 0.5s ease;
}

/* Section Title */
.section-title {
  font-size: 1.25rem;
  font-weight: 700;
  margin-bottom: 20px;
}

/* Benefits Section */
.benefits-section {
  margin-bottom: 40px;
}

.benefits-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.benefit-card {
  padding: 20px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  text-align: center;
}

.benefit-icon {
  font-size: 2rem;
  margin-bottom: 12px;
}

.benefit-title {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 8px;
}

.benefit-desc {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.5);
  margin-bottom: 12px;
}

.benefit-status {
  display: inline-block;
  padding: 4px 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 100px;
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.4);
}

.benefit-status.active {
  background: rgba(34, 197, 94, 0.15);
  color: #86efac;
}

/* Upgrade Section */
.upgrade-section {
  margin-bottom: 40px;
}

.upgrade-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.upgrade-card {
  position: relative;
  padding: 32px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 20px;
}

.upgrade-card--enterprise {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.08), rgba(234, 179, 8, 0.05));
  border-color: rgba(245, 158, 11, 0.2);
}

.upgrade-badge {
  position: absolute;
  top: -8px;
  right: 24px;
  padding: 4px 12px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 100px;
  font-size: 0.75rem;
  font-weight: 600;
}

.upgrade-name {
  font-size: 1.25rem;
  font-weight: 700;
  margin-bottom: 12px;
}

.upgrade-price {
  margin-bottom: 20px;
}

.upgrade-price .price {
  font-size: 2rem;
  font-weight: 800;
}

.upgrade-price .unit {
  font-size: 1rem;
  color: rgba(255, 255, 255, 0.5);
}

.upgrade-features {
  list-style: none;
  margin-bottom: 24px;
}

.upgrade-features li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  font-size: 0.95rem;
  color: rgba(255, 255, 255, 0.8);
}

.upgrade-features .check {
  color: #86efac;
}

.upgrade-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border: none;
  border-radius: 12px;
  color: #fff;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.upgrade-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(99, 102, 241, 0.4);
}

.upgrade-btn--contact {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.8), rgba(234, 179, 8, 0.8));
}

.upgrade-duration {
  margin-bottom: 20px;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.upgrade-duration label {
  display: block;
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 12px;
}

.duration-options {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.duration-btn {
  flex: 1;
  min-width: 60px;
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s;
}

.duration-btn:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.2);
}

.duration-btn.active {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.3), rgba(139, 92, 246, 0.3));
  border-color: rgba(139, 92, 246, 0.5);
  color: #fff;
}

.total-price {
  margin-top: 12px;
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.7);
}

.total-price .price-value {
  font-size: 1.25rem;
  font-weight: 700;
  color: #a5b4fc;
}

/* Payment Method Section */
.payment-method-section {
  margin-top: 16px;
  padding-top: 16px;
  margin-bottom: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.payment-method-section label {
  display: block;
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 12px;
}

.payment-method-options {
  display: flex;
  gap: 12px;
}

.method-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s;
}

.method-btn:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.2);
}

.method-btn.active {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.2), rgba(139, 92, 246, 0.2));
  border-color: rgba(139, 92, 246, 0.5);
  color: #fff;
}

.method-btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: rgba(255, 255, 255, 0.03);
  border-color: rgba(255, 255, 255, 0.05);
}

.method-btn.disabled:hover {
  background: rgba(255, 255, 255, 0.03);
  border-color: rgba(255, 255, 255, 0.05);
}

.method-status {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.4);
  margin-left: 4px;
}

.method-icon {
  font-size: 1.25rem;
}

.method-name {
  font-weight: 500;
}

/* Recent Section */
.recent-section {
  margin-bottom: 40px;
}

.recent-empty {
  text-align: center;
  padding: 60px 24px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 16px;
}

.empty-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  color: rgba(255, 255, 255, 0.2);
}

.empty-icon svg {
  width: 100%;
  height: 100%;
}

.recent-empty p {
  color: rgba(255, 255, 255, 0.5);
  margin-bottom: 20px;
}

.btn-start {
  display: inline-block;
  padding: 12px 32px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 12px;
  color: #fff;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.3s;
}

.btn-start:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(99, 102, 241, 0.4);
}

.recent-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recent-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  transition: all 0.2s;
}

.recent-item:hover {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(255, 255, 255, 0.12);
}

.recent-status {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.85rem;
  font-weight: 600;
  flex-shrink: 0;
}

.status-success {
  background: rgba(34, 197, 94, 0.2);
  color: #86efac;
}

.status-failed {
  background: rgba(239, 68, 68, 0.2);
  color: #f87171;
}

.recent-content {
  flex: 1;
  min-width: 0;
}

.recent-destination {
  font-weight: 600;
  margin-bottom: 4px;
}

.recent-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.recent-days {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.6);
}

.recent-divider {
  color: rgba(255, 255, 255, 0.3);
}

.recent-date {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.4);
}

/* Responsive */
@media (max-width: 768px) {
  .nav-links {
    display: none;
  }

  .user-card {
    flex-direction: column;
    text-align: center;
  }

  .stats-cards {
    grid-template-columns: 1fr;
  }

  .benefits-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .upgrade-cards {
    grid-template-columns: 1fr;
  }

  .duration-options {
    flex-wrap: wrap;
  }

  .duration-btn {
    min-width: calc(50% - 4px);
  }
}
</style>
