<template>
  <div class="home-container">
    <!-- Background Effects -->
    <div class="bg-effects">
      <div class="gradient-orb gradient-orb-1"></div>
      <div class="gradient-orb gradient-orb-2"></div>
      <div class="gradient-orb gradient-orb-3"></div>
      <div class="grid-overlay"></div>
    </div>

    <!-- Header -->
    <header class="app-header">
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
          <a href="#destinations">目的地</a>
          <a href="#guides">攻略</a>
          <a href="#features">功能</a>
          <a href="#pricing">定价</a>
        </nav>

        <div class="header-actions">
          <template v-if="isLoggedIn">
            <div class="user-info">
              <router-link to="/guide/create" class="btn-publish-guide">发布攻略</router-link>
              <router-link to="/profile" class="user-link">
                <div class="user-avatar">{{ userInfo?.username?.charAt(0).toUpperCase() }}</div>
                <span class="user-name">{{ userInfo?.username }}</span>
              </router-link>
              <button class="btn-ghost" @click="handleLogout">退出</button>
            </div>
          </template>
          <template v-else>
            <button class="btn-ghost" @click="showAuthModal = true">登录</button>
            <button class="btn-primary" @click="showAuthModal = true">免费试用</button>
          </template>
        </div>
      </div>
    </header>

    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-content">
        <!-- Hero Title Block -->
        <div class="hero-title-block">
          <div class="hero-badge">
            <span class="badge-dot"></span>
            <span>AI驱动的智能旅游规划</span>
          </div>

          <h1 class="hero-title">
            <span class="title-line">一键规划</span>
            <span class="title-highlight-wrapper">
              <span class="title-highlight">完美旅程</span>
              <svg class="title-underline" viewBox="0 0 300 12" fill="none">
                <path d="M2 8C50 4 100 2 150 4C200 6 250 4 298 8" stroke="url(#gradient)" stroke-width="4" stroke-linecap="round"/>
                <defs>
                  <linearGradient id="gradient" x1="0%" y1="0%" x2="100%" y2="0%">
                    <stop offset="0%" stop-color="#fbbf24"/>
                    <stop offset="50%" stop-color="#f59e0b"/>
                    <stop offset="100%" stop-color="#ef4444"/>
                  </linearGradient>
                </defs>
              </svg>
            </span>
          </h1>

          <p class="hero-desc">
            智能分析景点、住宿、交通，多Agent协作为您打造专属旅行方案
          </p>
        </div>

        <!-- Main Form -->
        <div class="form-wrapper">
          <TravelForm @submit="handlePlanSubmit" :isPlanning="false" />
        </div>

        <!-- Quick Tips -->
        <div class="quick-tips">
          <div class="tip-item">
            <span class="tip-icon">💡</span>
            <span>输入目的地即可开始规划</span>
          </div>
          <div class="tip-item">
            <span class="tip-icon">⚡</span>
            <span>AI实时生成行程方案</span>
          </div>
          <div class="tip-item">
            <span class="tip-icon">🎯</span>
            <span>根据偏好智能推荐</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Stats Section -->
    <section class="stats-section">
      <div class="stats-grid">
        <div class="stat-item">
          <span class="stat-value">50K+</span>
          <span class="stat-label">行程已规划</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">98%</span>
          <span class="stat-label">用户满意度</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">200+</span>
          <span class="stat-label">热门目的地</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">24/7</span>
          <span class="stat-label">在线服务</span>
        </div>
      </div>
    </section>

    <!-- Destinations Section -->
    <section id="destinations" class="destinations-section">
      <div class="section-header">
        <h2 class="section-title">热门目的地</h2>
        <p class="section-desc">精选全球最受欢迎的旅游目的地</p>
      </div>

      <div class="destinations-grid">
        <DestinationCard
          v-for="dest in destinations"
          :key="dest.id"
          v-bind="dest"
          @select="handleDestSelect"
        />
      </div>

      <div class="section-more">
        <button v-if="hasMoreDestinations" class="more-btn" @click="toggleMoreDestinations">
          <span>{{ showMoreDestinations ? '收起目的地' : '查看更多目的地' }}</span>
          <svg :class="{ 'rotate': showMoreDestinations }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M5 12h14M12 5l7 7-7 7"/>
          </svg>
        </button>
      </div>
    </section>

    <!-- Guides Section -->
    <section id="guides" class="guides-section-wrapper">
      <GuideSection />
    </section>

    <!-- Features Section -->
    <section id="features" class="features-section">
      <div class="section-header">
        <h2 class="section-title">核心功能</h2>
        <p class="section-desc">多Agent系统为您提供全方位旅行规划服务</p>
      </div>

      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-card-bg"></div>
          <div class="feature-icon feature-icon--purple">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/>
              <path d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/>
            </svg>
          </div>
          <h3 class="feature-title">智能景点推荐</h3>
          <p class="feature-desc">AI分析您的偏好，推荐最佳景点和游览路线</p>
          <ul class="feature-tags">
            <li>个性化路线</li>
            <li>热门景点</li>
          </ul>
        </div>

        <div class="feature-card">
          <div class="feature-card-bg"></div>
          <div class="feature-icon feature-icon--pink">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/>
            </svg>
          </div>
          <h3 class="feature-title">住宿智能规划</h3>
          <p class="feature-desc">根据景点位置和预算，推荐最优住宿</p>
          <ul class="feature-tags">
            <li>位置优选</li>
            <li>价格对比</li>
          </ul>
        </div>

        <div class="feature-card">
          <div class="feature-card-bg"></div>
          <div class="feature-icon feature-icon--amber">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4"/>
            </svg>
          </div>
          <h3 class="feature-title">交通方案优化</h3>
          <p class="feature-desc">自动规划交通，节省时间和费用</p>
          <ul class="feature-tags">
            <li>路线对比</li>
            <li>费用预估</li>
          </ul>
        </div>

        <div class="feature-card">
          <div class="feature-card-bg"></div>
          <div class="feature-icon feature-icon--cyan">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
            </svg>
          </div>
          <h3 class="feature-title">预算智能分配</h3>
          <p class="feature-desc">智能分配各项开支，让钱花在刀刃上</p>
          <ul class="feature-tags">
            <li>智能分配</li>
            <li>省钱建议</li>
          </ul>
        </div>
      </div>
    </section>

    <!-- Testimonials Section -->
    <section id="testimonials" class="testimonials-section">
      <div class="section-header">
        <h2 class="section-title">用户评价</h2>
        <p class="section-desc">超过10,000名旅行者的信赖选择</p>
      </div>

      <div class="testimonials-grid">
        <TestimonialCard
          v-for="testimonial in testimonials"
          :key="testimonial.name"
          v-bind="testimonial"
        />
      </div>
    </section>

    <!-- Pricing Section -->
    <section id="pricing" class="pricing-section">
      <div class="section-header">
        <h2 class="section-title">简单透明的定价</h2>
        <p class="section-desc">选择适合您的方案，开启智能旅行</p>
      </div>

      <div class="pricing-grid">
        <PricingCard
          title="免费版"
          price="¥0"
          :features="['每月5次规划', '基础景点推荐', '邮件支持']"
        />
        <PricingCard
          title="专业版"
          price="¥29/月"
          :features="['无限规划次数', '高级AI分析', '优先支持', '行程导出PDF', '专属客服']"
          :highlighted="true"
        />
        <PricingCard
          title="企业版"
          price="联系我们"
          :features="['定制化方案', 'API接入', '专属客服', '团队协作', '私有部署']"
        />
      </div>
    </section>

    <!-- CTA Section -->
    <section class="cta-section">
      <div class="cta-card">
        <h2 class="cta-title">准备好开始您的旅行了吗？</h2>
        <p class="cta-desc">让AI助手为您量身定制专属行程</p>
        <button class="cta-button" @click="scrollToTop">
          <span>立即开始</span>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M5 12h14M12 5l7 7-7 7"/>
          </svg>
        </button>
      </div>
    </section>

    <!-- Footer -->
    <footer class="app-footer">
      <div class="footer-content">
        <div class="footer-brand">
          <div class="logo">
            <div class="logo-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 16V8a2 2 0 00-1-1.73l-7-4a2 2 0 00-2 0l-7 4A2 2 0 003 8v8a2 2 0 001 1.73l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z"/>
              </svg>
            </div>
            <span class="logo-text">TravelAI</span>
          </div>
          <p class="footer-tagline">让AI助力您的每一次旅行</p>
        </div>

        <div class="footer-links">
          <div class="footer-col">
            <h4>产品</h4>
            <a href="#">功能介绍</a>
            <a href="#">定价方案</a>
            <a href="#">更新日志</a>
          </div>
          <div class="footer-col">
            <h4>支持</h4>
            <a href="#">帮助中心</a>
            <a href="#">联系我们</a>
            <a href="#">API文档</a>
          </div>
          <div class="footer-col">
            <h4>关于</h4>
            <a href="#">关于我们</a>
            <a href="#">隐私政策</a>
            <a href="#">服务条款</a>
          </div>
        </div>
      </div>

      <div class="footer-bottom">
        <p>&copy; 2024 TravelAI. 基于Spring AI Alibaba构建</p>
      </div>
    </footer>

    <!-- Auth Modal -->
    <AuthModal
      :visible="showAuthModal"
      @close="showAuthModal = false"
      @login-success="handleLoginSuccess"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import TravelForm from '@/components/planning/TravelForm.vue'
import DestinationCard from '@/components/landing/DestinationCard.vue'
import TestimonialCard from '@/components/landing/TestimonialCard.vue'
import PricingCard from '@/components/common/PricingCard.vue'
import AuthModal from '@/components/common/AuthModal.vue'
import GuideSection from '@/components/guide/GuideSection.vue'

const router = useRouter()

// 登录相关状态
const showAuthModal = ref(false)
const isLoggedIn = ref(false)
const userInfo = ref(null)

// 检查登录状态
onMounted(() => {
  const savedUserInfo = localStorage.getItem('userInfo')
  if (savedUserInfo) {
    try {
      userInfo.value = JSON.parse(savedUserInfo)
      isLoggedIn.value = true
    } catch (e) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
})

// 处理登录成功
const handleLoginSuccess = (data) => {
  userInfo.value = data
  isLoggedIn.value = true
}

// 退出登录
const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  userInfo.value = null
  isLoggedIn.value = false
}

// 所有目的地数据
const allDestinations = [
  {
    id: 'tokyo',
    name: '东京',
    image: 'https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?w=400&h=300&fit=crop',
    tag: '热门推荐',
    rating: '4.9',
    description: '现代与传统完美融合的都市',
    price: 5999,
    duration: 5,
    color: '#ec4899'
  },
  {
    id: 'paris',
    name: '巴黎',
    image: 'https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=400&h=300&fit=crop',
    tag: '浪漫之都',
    rating: '4.8',
    description: '艺术与浪漫的代名词',
    price: 8999,
    duration: 7,
    color: '#8b5cf6'
  },
  {
    id: 'maldives',
    name: '马尔代夫',
    image: 'https://images.unsplash.com/photo-1514282401047-d79a71a590e8?w=400&h=300&fit=crop',
    tag: '海岛度假',
    rating: '4.9',
    description: '纯净海岛天堂',
    price: 12999,
    duration: 5,
    color: '#06b6d4'
  },
  {
    id: 'santorini',
    name: '圣托里尼',
    image: 'https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff?w=400&h=300&fit=crop',
    tag: '网红打卡',
    rating: '4.7',
    description: '蓝白交织的爱琴海明珠',
    price: 9999,
    duration: 6,
    color: '#3b82f6'
  },
  {
    id: 'newyork',
    name: '纽约',
    image: 'https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?w=400&h=300&fit=crop',
    tag: ' 都市风情',
    rating: '4.8',
    description: '世界之都，永不眠的城市',
    price: 11999,
    duration: 7,
    color: '#f97316'
  },
  {
    id: 'bali',
    name: '巴厘岛',
    image: 'https://images.unsplash.com/photo-1537996194471-e657df975ab4?w=400&h=300&fit=crop',
    tag: '度假胜地',
    rating: '4.9',
    description: '神明之岛，热带天堂',
    price: 6999,
    duration: 6,
    color: '#10b981'
  },
  {
    id: 'london',
    name: '伦敦',
    image: 'https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?w=400&h=300&fit=crop',
    tag: '文化之旅',
    rating: '4.7',
    description: '古老与现代交织的英伦风情',
    price: 10999,
    duration: 6,
    color: '#6366f1'
  },
  {
    id: 'sydney',
    name: '悉尼',
    image: 'https://images.unsplash.com/photo-1506973035872-a4ec16bd875a?w=400&h=300&fit=crop',
    tag: '南半球明珠',
    rating: '4.8',
    description: '阳光海港，澳洲风情',
    price: 15999,
    duration: 8,
    color: '#14b8a6'
  },
  {
    id: 'dubai',
    name: '迪拜',
    image: 'https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400&h=300&fit=crop',
    tag: '奢华体验',
    rating: '4.6',
    description: '沙漠中的奇迹之城',
    price: 13999,
    duration: 5,
    color: '#eab308'
  },
  {
    id: 'rome',
    name: '罗马',
    image: 'https://images.unsplash.com/photo-1552832230-c0197dd311b5?w=400&h=300&fit=crop',
    tag: '历史名城',
    rating: '4.8',
    description: '永恒之城，历史沉淀',
    price: 9999,
    duration: 6,
    color: '#dc2626'
  },
  {
    id: 'bangkok',
    name: '曼谷',
    image: 'https://images.unsplash.com/photo-1508009603885-50cf7c57903f?w=400&h=300&fit=crop',
    tag: '东南亚明珠',
    rating: '4.7',
    description: '寺庙与现代的完美融合',
    price: 4999,
    duration: 5,
    color: '#f59e0b'
  },
  {
    id: 'hokkaido',
    name: '北海道',
    image: 'https://images.unsplash.com/photo-1490704287038-3033351a81b7?w=400&h=300&fit=crop',
    tag: '自然风光',
    rating: '4.9',
    description: '雪国仙境，温泉之乡',
    price: 8999,
    duration: 6,
    color: '#0ea5e9'
  }
]

// 显示更多目的地状态
const showMoreDestinations = ref(false)

// 当前显示的目的地（计算属性）
const destinations = computed(() => {
  return showMoreDestinations.value ? allDestinations : allDestinations.slice(0, 4)
})

// 是否还有更多目的地
const hasMoreDestinations = computed(() => allDestinations.length > 4)

// 切换显示更多
const toggleMoreDestinations = () => {
  showMoreDestinations.value = !showMoreDestinations.value
}

const testimonials = ref([
  {
    name: '王小明',
    text: 'AI规划的行程非常合理，景点安排恰到好处，强烈推荐！',
    rating: 5,
    trip: '东京5日游',
    avatarColor: 'linear-gradient(135deg, #6366f1, #8b5cf6)'
  },
  {
    name: '李雪',
    text: '作为一个选择困难症患者，这个工具简直是救星！',
    rating: 5,
    trip: '巴黎7日游',
    avatarColor: 'linear-gradient(135deg, #ec4899, #f43f5e)'
  },
  {
    name: '张伟',
    text: '带着家人出行，AI考虑到了老人和孩子的需求，点赞！',
    rating: 5,
    trip: '马尔代夫5日游',
    avatarColor: 'linear-gradient(135deg, #06b6d4, #0891b2)'
  }
])

// 提交规划 - 跳转到规划页面
const handlePlanSubmit = (formData) => {
  // 将表单数据存储到 sessionStorage
  sessionStorage.setItem('planFormData', JSON.stringify(formData))
  // 跳转到规划页面
  router.push('/plan')
}

const handleDestSelect = (destId) => {
  // 查找目的地信息
  const dest = allDestinations.find(d => d.id === destId)
  if (dest) {
    // 创建表单数据并跳转到规划页面
    const formData = {
      destination: dest.name,
      days: dest.duration,
      guests: 2,
      budget: 'medium',
      preferences: []
    }
    sessionStorage.setItem('planFormData', JSON.stringify(formData))
    router.push('/plan')
  }
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<style scoped>
/* Container */
.home-container {
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
  filter: blur(100px);
  opacity: 0.5;
}

.gradient-orb-1 {
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, #4f46e5 0%, transparent 70%);
  top: -200px;
  left: -200px;
  animation: float-orb 20s ease-in-out infinite;
}

.gradient-orb-2 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, #7c3aed 0%, transparent 70%);
  top: 30%;
  right: -150px;
  animation: float-orb 25s ease-in-out infinite reverse;
}

.gradient-orb-3 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, #ec4899 0%, transparent 70%);
  bottom: 10%;
  left: 20%;
  animation: float-orb 30s ease-in-out infinite;
}

@keyframes float-orb {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -30px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.95); }
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
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  padding: 16px 24px;
  background: rgba(10, 10, 15, 0.8);
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
  letter-spacing: -0.02em;
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
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
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
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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

.btn-primary {
  padding: 10px 24px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border: none;
  border-radius: 10px;
  color: #fff;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(99, 102, 241, 0.4);
}

.btn-publish-guide {
  padding: 8px 20px;
  background: linear-gradient(135deg, rgba(251, 191, 36, 0.2), rgba(245, 158, 11, 0.2));
  border: 1px solid rgba(251, 191, 36, 0.3);
  border-radius: 10px;
  color: #fcd34d;
  font-size: 0.85rem;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.3s;
}

.btn-publish-guide:hover {
  background: linear-gradient(135deg, rgba(251, 191, 36, 0.3), rgba(245, 158, 11, 0.3));
  transform: translateY(-1px);
}

.guides-section-wrapper {
  position: relative;
  z-index: 1;
  background: rgba(255, 255, 255, 0.02);
}

/* Hero Section */
.hero-section {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 120px 24px 80px;
}

.hero-content {
  max-width: 960px;
  width: 100%;
  text-align: center;
}

/* Hero Title Block */
.hero-title-block {
  margin-bottom: 48px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 18px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.15), rgba(139, 92, 246, 0.15));
  border: 1px solid rgba(139, 92, 246, 0.3);
  border-radius: 100px;
  font-size: 0.9rem;
  color: #c4b5fd;
  margin-bottom: 28px;
}

.badge-dot {
  width: 8px;
  height: 8px;
  background: linear-gradient(135deg, #6366f1, #a855f7);
  border-radius: 50%;
  animation: pulse-dot 2s ease-in-out infinite;
}

@keyframes pulse-dot {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(1.2); }
}

.hero-title {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.title-line {
  font-size: 4rem;
  font-weight: 800;
  line-height: 1.1;
  letter-spacing: -0.04em;
  color: #fff;
}

.title-highlight-wrapper {
  position: relative;
  display: inline-block;
}

.title-highlight {
  font-size: 4rem;
  font-weight: 800;
  line-height: 1.1;
  letter-spacing: -0.04em;
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 40%, #ef4444 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
}

.title-underline {
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  height: 12px;
  opacity: 0.8;
}

.hero-desc {
  font-size: 1.25rem;
  color: rgba(255, 255, 255, 0.6);
  line-height: 1.7;
  margin-top: 24px;
  max-width: 500px;
  margin-left: auto;
  margin-right: auto;
}

.form-wrapper {
  position: relative;
}

.quick-tips {
  display: flex;
  justify-content: center;
  gap: 32px;
  margin-top: 28px;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.5);
}

.tip-icon {
  font-size: 1rem;
}

/* Stats Section */
.stats-section {
  position: relative;
  z-index: 1;
  padding: 48px 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  background: rgba(255, 255, 255, 0.02);
}

.stats-grid {
  max-width: 900px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 2.25rem;
  font-weight: 800;
  background: linear-gradient(135deg, #fff, #a5b4fc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.5);
}

/* Section Common */
.section-header {
  text-align: center;
  margin-bottom: 48px;
}

.section-title {
  font-size: 2.5rem;
  font-weight: 800;
  letter-spacing: -0.02em;
  margin-bottom: 12px;
}

.section-desc {
  font-size: 1.1rem;
  color: rgba(255, 255, 255, 0.6);
}

.section-more {
  text-align: center;
  margin-top: 40px;
}

.more-btn {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 16px 32px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 100px;
  color: #fff;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.more-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

.more-btn svg {
  width: 20px;
  height: 20px;
  transition: transform 0.3s ease;
}

.more-btn:hover svg {
  transform: translateX(4px);
}

.more-btn svg.rotate {
  transform: rotate(180deg);
}

.more-btn:hover svg.rotate {
  transform: rotate(180deg);
}

/* Destinations Section */
.destinations-section {
  position: relative;
  z-index: 1;
  padding: 100px 24px;
}

.destinations-grid {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

/* Features Section */
.features-section {
  position: relative;
  z-index: 1;
  padding: 100px 24px;
  background: rgba(255, 255, 255, 0.02);
}

.features-grid {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.feature-card {
  position: relative;
  padding: 32px 28px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 24px;
  transition: all 0.4s ease;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.feature-card-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, transparent 0%, rgba(255,255,255,0.02) 100%);
  opacity: 0;
  transition: opacity 0.4s ease;
}

.feature-card:hover .feature-card-bg {
  opacity: 1;
}

.feature-card:hover {
  border-color: rgba(255, 255, 255, 0.15);
  transform: translateY(-6px);
}

.feature-icon {
  width: 64px;
  height: 64px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-bottom: 20px;
}

.feature-icon svg {
  width: 32px;
  height: 32px;
}

.feature-icon--purple {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.25), rgba(139, 92, 246, 0.25));
  color: #a5b4fc;
}

.feature-icon--pink {
  background: linear-gradient(135deg, rgba(236, 72, 153, 0.25), rgba(244, 63, 94, 0.25));
  color: #f9a8d4;
}

.feature-icon--amber {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.25), rgba(239, 68, 68, 0.25));
  color: #fcd34d;
}

.feature-icon--cyan {
  background: linear-gradient(135deg, rgba(6, 182, 212, 0.25), rgba(34, 211, 238, 0.25));
  color: #67e8f9;
}

.feature-content {
  flex: 1;
}

.feature-title {
  font-size: 1.35rem;
  font-weight: 700;
  margin-bottom: 12px;
  color: #fff;
}

.feature-desc {
  color: rgba(255, 255, 255, 0.6);
  line-height: 1.7;
  margin-bottom: 20px;
}

.feature-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  list-style: none;
}

.feature-tags li {
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 100px;
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.7);
}

/* Testimonials Section */
.testimonials-section {
  position: relative;
  z-index: 1;
  padding: 100px 24px;
}

.testimonials-grid {
  max-width: 1000px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

/* Pricing Section */
.pricing-section {
  position: relative;
  z-index: 1;
  padding: 100px 24px;
  background: rgba(255, 255, 255, 0.02);
}

.pricing-grid {
  max-width: 900px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  align-items: center;
}

/* CTA Section */
.cta-section {
  position: relative;
  z-index: 1;
  padding: 100px 24px;
}

.cta-card {
  max-width: 600px;
  margin: 0 auto;
  padding: 48px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.15), rgba(139, 92, 246, 0.15));
  border: 1px solid rgba(139, 92, 246, 0.3);
  border-radius: 24px;
  text-align: center;
}

.cta-title {
  font-size: 2rem;
  font-weight: 800;
  margin-bottom: 12px;
}

.cta-desc {
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 32px;
}

.cta-button {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 18px 36px;
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  border: none;
  border-radius: 14px;
  color: #0a0a0f;
  font-size: 1.1rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s;
}

.cta-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 15px 40px rgba(251, 191, 36, 0.4);
}

.cta-button svg {
  width: 20px;
  height: 20px;
  transition: transform 0.3s;
}

.cta-button:hover svg {
  transform: translateX(4px);
}

/* Footer */
.app-footer {
  position: relative;
  z-index: 1;
  padding: 60px 24px 30px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  gap: 60px;
  margin-bottom: 40px;
}

.footer-tagline {
  margin-top: 12px;
  color: rgba(255, 255, 255, 0.5);
  font-size: 0.9rem;
}

.footer-links {
  display: flex;
  gap: 60px;
}

.footer-col h4 {
  font-size: 0.9rem;
  font-weight: 600;
  margin-bottom: 16px;
  color: #fff;
}

.footer-col a {
  display: block;
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.5);
  text-decoration: none;
  margin-bottom: 10px;
  transition: color 0.2s;
}

.footer-col a:hover {
  color: #fff;
}

.footer-bottom {
  padding-top: 30px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  text-align: center;
}

.footer-bottom p {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.4);
}

/* Responsive */
@media (max-width: 1024px) {
  .destinations-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .nav-links {
    display: none;
  }

  .title-line,
  .title-highlight {
    font-size: 2.75rem;
  }

  .hero-desc {
    font-size: 1.1rem;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .destinations-grid,
  .testimonials-grid,
  .pricing-grid {
    grid-template-columns: 1fr;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }

  .footer-content {
    flex-direction: column;
    gap: 40px;
  }

  .footer-links {
    flex-wrap: wrap;
    gap: 40px;
  }

  .cta-card {
    padding: 32px 24px;
  }

  .quick-tips {
    flex-direction: column;
    gap: 12px;
  }
}
</style>
