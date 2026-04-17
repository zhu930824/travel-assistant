<template>
  <div class="aurora-bg">
    <!-- Floating Particles -->
    <div class="particles">
      <div v-for="i in 20" :key="i" class="particle" :style="getParticleStyle(i)"></div>
    </div>

    <!-- Header -->
    <header class="fixed top-0 left-0 right-0 z-50 nav-glass">
      <nav class="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center">
            <span class="text-xl">✈️</span>
          </div>
          <span class="text-xl font-bold text-white">TravelAI</span>
        </div>
        <div class="hidden md:flex items-center gap-8">
          <a href="#destinations" class="text-white/70 hover:text-white transition-colors text-sm">热门目的地</a>
          <a href="#features" class="text-white/70 hover:text-white transition-colors text-sm">功能特性</a>
          <a href="#testimonials" class="text-white/70 hover:text-white transition-colors text-sm">用户评价</a>
          <a href="#pricing" class="text-white/70 hover:text-white transition-colors text-sm">定价方案</a>
        </div>
        <div class="flex items-center gap-4">
          <button class="text-white/80 hover:text-white text-sm hidden md:block">登录</button>
          <button class="btn-primary text-sm px-5 py-2.5">免费开始</button>
        </div>
      </nav>
    </header>

    <!-- Hero Section -->
    <section class="relative z-10 pt-32 pb-20 px-4">
      <div class="max-w-7xl mx-auto">
        <div class="text-center mb-16">
          <div class="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-white/5 border border-white/10 mb-6">
            <span class="animate-pulse">✨</span>
            <span class="text-sm text-white/80">AI驱动的智能旅游规划</span>
          </div>
          <h1 class="text-5xl md:text-7xl font-extrabold text-white mb-6 leading-tight">
            让AI为您规划<br />
            <span class="gradient-text">完美旅程</span>
          </h1>
          <p class="text-xl text-white/60 max-w-2xl mx-auto mb-10">
            多Agent协作系统，智能分析景点、住宿、交通，为您打造专属旅行方案
          </p>
          <div class="flex flex-col sm:flex-row gap-4 justify-center">
            <button class="px-8 py-4 bg-gradient-to-r from-amber-400 to-orange-500 text-gray-900 font-bold rounded-xl hover:shadow-lg hover:shadow-orange-500/30 transition-all hover:-translate-y-1">
              立即开始规划
            </button>
            <button class="px-8 py-4 bg-white/10 text-white font-medium rounded-xl border border-white/20 hover:bg-white/15 transition-all">
              观看演示视频
            </button>
          </div>
        </div>

        <!-- Main Planning Area -->
        <div class="grid lg:grid-cols-2 gap-8 items-start max-w-6xl mx-auto">
          <!-- Travel Form -->
          <TravelForm @submit="handlePlanSubmit" :isPlanning="isPlanning" />

          <!-- Booking Preview -->
          <BookingPreview
            :destinations="destinations"
            @submit="handlePlanSubmit"
          />
        </div>

        <!-- Planning Results Panel -->
        <div v-if="isPlanning || hasResult" class="max-w-4xl mx-auto mt-8">
          <PlanningPanel
            :agentStatus="agentStatus"
            :agentContents="agentContents"
            :currentStage="currentStage"
            :finalPlan="finalPlan"
            :isPlanning="isPlanning"
          />
        </div>
      </div>
    </section>

    <!-- Destinations Showcase -->
    <section id="destinations" class="relative z-10 py-24 px-4">
      <div class="max-w-7xl mx-auto">
        <h2 class="section-title">
          探索<span class="gradient-text-purple">热门目的地</span>
        </h2>
        <p class="section-subtitle">
          精选全球最受欢迎的旅游目的地，让您的旅行充满惊喜
        </p>

        <div class="grid sm:grid-cols-2 lg:grid-cols-4 gap-6">
          <DestinationCard
            v-for="dest in destinations"
            :key="dest.id"
            v-bind="dest"
            @select="handleDestSelect"
          />
        </div>

        <div class="text-center mt-12">
          <button class="px-8 py-3 bg-white/10 text-white rounded-xl border border-white/20 hover:bg-white/15 transition-all">
            查看更多目的地 →
          </button>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section id="features" class="relative z-10 py-24 px-4">
      <div class="max-w-7xl mx-auto">
        <h2 class="section-title">
          为什么选择<span class="gradient-text">TravelAI</span>
        </h2>
        <p class="section-subtitle">
          我们的多Agent系统为您提供全方位的旅行规划服务
        </p>

        <div class="grid md:grid-cols-3 gap-8">
          <div class="glass-card p-8 glass-card-hover">
            <div class="w-14 h-14 rounded-2xl bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center text-2xl mb-6">
              🏔️
            </div>
            <h3 class="text-xl font-bold text-white mb-3">智能景点推荐</h3>
            <p class="text-white/60 leading-relaxed">
              基于您的目的地、天数、偏好，AI智能分析并推荐最佳景点和游览路线
            </p>
          </div>

          <div class="glass-card p-8 glass-card-hover">
            <div class="w-14 h-14 rounded-2xl bg-gradient-to-br from-pink-500 to-rose-600 flex items-center justify-center text-2xl mb-6">
              🏨
            </div>
            <h3 class="text-xl font-bold text-white mb-3">住宿智能规划</h3>
            <p class="text-white/60 leading-relaxed">
              根据景点位置、预算和偏好，自动推荐最优住宿区域和酒店选择
            </p>
          </div>

          <div class="glass-card p-8 glass-card-hover">
            <div class="w-14 h-14 rounded-2xl bg-gradient-to-br from-amber-500 to-orange-600 flex items-center justify-center text-2xl mb-6">
              🚗
            </div>
            <h3 class="text-xl font-bold text-white mb-3">交通方案优化</h3>
            <p class="text-white/60 leading-relaxed">
              自动规划城际和市内交通，提供多种出行方案对比，节省时间和费用
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- Testimonials -->
    <section id="testimonials" class="relative z-10 py-24 px-4">
      <div class="max-w-7xl mx-auto">
        <h2 class="section-title">
          用户<span class="gradient-text-purple">真实评价</span>
        </h2>
        <p class="section-subtitle">
          超过10,000名旅行者的信赖选择
        </p>

        <div class="grid md:grid-cols-3 gap-6">
          <TestimonialCard
            v-for="testimonial in testimonials"
            :key="testimonial.name"
            v-bind="testimonial"
          />
        </div>
      </div>
    </section>

    <!-- Pricing Section -->
    <section id="pricing" class="relative z-10 py-24 px-4">
      <div class="max-w-5xl mx-auto">
        <h2 class="section-title">
          选择适合您的<span class="gradient-text">方案</span>
        </h2>
        <p class="section-subtitle">
          灵活的定价方案，满足不同需求
        </p>

        <div class="grid md:grid-cols-3 gap-6">
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
      </div>
    </section>

    <!-- Trip Planner CTA -->
    <section class="relative z-10 py-24 px-4">
      <div class="max-w-5xl mx-auto">
        <TripPlannerCTA
          @start="scrollToPlanner"
          @demo="showDemo"
        />
      </div>
    </section>

    <!-- Trust Badges -->
    <section class="relative z-10 py-16 px-4">
      <div class="max-w-4xl mx-auto">
        <div class="glass-card p-10">
          <p class="text-white/50 text-center mb-8">值得信赖的旅行规划平台</p>
          <div class="flex flex-wrap justify-center items-center gap-12">
            <TrustBadge icon="⭐" text="4.9分好评" />
            <TrustBadge icon="🔒" text="数据安全" />
            <TrustBadge icon="⚡" text="秒级响应" />
            <TrustBadge icon="🎯" text="精准推荐" />
          </div>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <footer class="relative z-10 py-12 px-4 border-t border-white/5">
      <div class="max-w-7xl mx-auto">
        <div class="grid md:grid-cols-4 gap-8 mb-8">
          <div>
            <div class="flex items-center gap-3 mb-4">
              <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center">
                <span class="text-xl">✈️</span>
              </div>
              <span class="text-xl font-bold text-white">TravelAI</span>
            </div>
            <p class="text-white/50 text-sm">
              基于AI的智能旅游规划平台，让每一次旅行都精彩难忘
            </p>
          </div>
          <div>
            <h4 class="font-semibold text-white mb-4">产品</h4>
            <ul class="space-y-2 text-white/50 text-sm">
              <li><a href="#" class="hover:text-white transition-colors">功能介绍</a></li>
              <li><a href="#" class="hover:text-white transition-colors">定价方案</a></li>
              <li><a href="#" class="hover:text-white transition-colors">更新日志</a></li>
            </ul>
          </div>
          <div>
            <h4 class="font-semibold text-white mb-4">支持</h4>
            <ul class="space-y-2 text-white/50 text-sm">
              <li><a href="#" class="hover:text-white transition-colors">帮助中心</a></li>
              <li><a href="#" class="hover:text-white transition-colors">联系我们</a></li>
              <li><a href="#" class="hover:text-white transition-colors">API文档</a></li>
            </ul>
          </div>
          <div>
            <h4 class="font-semibold text-white mb-4">关注我们</h4>
            <div class="flex gap-4">
              <a href="#" class="w-10 h-10 rounded-xl bg-white/5 flex items-center justify-center text-white/60 hover:bg-white/10 hover:text-white transition-all">
                📱
              </a>
              <a href="#" class="w-10 h-10 rounded-xl bg-white/5 flex items-center justify-center text-white/60 hover:bg-white/10 hover:text-white transition-all">
                💬
              </a>
              <a href="#" class="w-10 h-10 rounded-xl bg-white/5 flex items-center justify-center text-white/60 hover:bg-white/10 hover:text-white transition-all">
                📧
              </a>
            </div>
          </div>
        </div>
        <div class="text-center text-white/40 text-sm pt-8 border-t border-white/5">
          <p>&copy; 2024 TravelAI. 基于Spring AI Alibaba构建 · 让AI助力您的每一次旅行</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import TravelForm from './components/planning/TravelForm.vue'
import PlanningPanel from './components/planning/PlanningPanel.vue'
import DestinationCard from './components/landing/DestinationCard.vue'
import BookingPreview from './components/landing/BookingPreview.vue'
import TestimonialCard from './components/landing/TestimonialCard.vue'
import TripPlannerCTA from './components/landing/TripPlannerCTA.vue'
import PricingCard from './components/common/PricingCard.vue'
import TrustBadge from './components/common/TrustBadge.vue'
import { usePlanning } from './composables/usePlanning'

const {
  isPlanning,
  currentStage,
  agentStatus,
  agentContents,
  finalPlan,
  hasResult,
  startPlanning
} = usePlanning()

const destinations = ref([
  {
    id: 'tokyo',
    name: '东京',
    image: 'https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?w=400&h=300&fit=crop',
    tag: '热门推荐',
    rating: '4.9',
    description: '现代与传统完美融合的都市，体验日本文化精髓',
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
    description: '艺术与浪漫的代名词，感受法式优雅生活',
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
    description: '纯净海岛天堂，享受极致放松的度假时光',
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
    description: '蓝白交织的爱琴海明珠，浪漫日落令人沉醉',
    price: 9999,
    duration: 6,
    color: '#3b82f6'
  }
])

const testimonials = ref([
  {
    name: '王小明',
    text: 'AI规划的行程非常合理，景点安排恰到好处，时间安排也很科学。强烈推荐！',
    rating: 5,
    trip: '东京5日游',
    avatarColor: 'linear-gradient(135deg, #6366f1, #8b5cf6)'
  },
  {
    name: '李雪',
    text: '作为一个选择困难症患者，这个工具简直是救星！住宿和交通推荐都很贴心。',
    rating: 5,
    trip: '巴黎7日游',
    avatarColor: 'linear-gradient(135deg, #ec4899, #f43f5e)'
  },
  {
    name: '张伟',
    text: '带着家人出行，AI考虑到了老人和孩子的需求，规划的节奏很舒适。点赞！',
    rating: 5,
    trip: '马尔代夫5日游',
    avatarColor: 'linear-gradient(135deg, #06b6d4, #0891b2)'
  }
])

const handlePlanSubmit = (formData) => {
  startPlanning(formData)
}

const handleDestSelect = (destId) => {
  console.log('Selected destination:', destId)
}

const scrollToPlanner = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const showDemo = () => {
  console.log('Show demo')
}

const getParticleStyle = (index) => {
  const left = Math.random() * 100
  const delay = Math.random() * 15
  const duration = 15 + Math.random() * 10
  return {
    left: `${left}%`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`
  }
}
</script>
