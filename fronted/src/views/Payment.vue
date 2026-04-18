<template>
  <div class="payment-container">
    <!-- Background Effects -->
    <div class="bg-effects">
      <div class="gradient-orb gradient-orb-1"></div>
      <div class="gradient-orb gradient-orb-2"></div>
      <div class="grid-overlay"></div>
    </div>

    <!-- Header -->
    <header class="payment-header">
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

        <router-link to="/profile" class="back-link">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
          返回个人中心
        </router-link>
      </div>
    </header>

    <!-- Main Content -->
    <main class="payment-main">
      <!-- Loading State -->
      <div class="loading-state" v-if="loading">
        <div class="loading-spinner"></div>
        <p>正在创建订单...</p>
      </div>

      <!-- Error State -->
      <div class="error-state" v-else-if="error">
        <div class="error-icon">⚠️</div>
        <h2>创建订单失败</h2>
        <p>{{ error }}</p>
        <router-link to="/profile" class="btn-retry">返回重试</router-link>
      </div>

      <!-- Payment Card -->
      <div class="payment-card" v-else>
        <div class="payment-header-info">
          <h1 class="payment-title">会员升级</h1>
          <p class="payment-subtitle">{{ paymentMethodTip }}</p>
        </div>

        <!-- Order Info -->
        <div class="order-info">
          <div class="order-item">
            <span class="order-label">会员等级</span>
            <span class="order-value">{{ memberLevelText }}</span>
          </div>
          <div class="order-item">
            <span class="order-label">购买时长</span>
            <span class="order-value">{{ orderInfo?.months }}个月</span>
          </div>
          <div class="order-item">
            <span class="order-label">支付方式</span>
            <span class="order-value">{{ paymentMethodName }}</span>
          </div>
          <div class="order-item">
            <span class="order-label">订单金额</span>
            <span class="order-value amount">¥{{ (orderInfo?.amount || 0) / 100 }}</span>
          </div>
        </div>

        <!-- QR Code -->
        <div class="qr-section">
          <div class="qr-code" :class="paymentMethodClass">
            <canvas ref="qrCanvas"></canvas>
          </div>
          <p class="qr-tip">{{ qrTipText }}</p>
        </div>

        <!-- Countdown -->
        <div class="countdown-section">
          <div class="countdown-label">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
            <span>支付剩余时间</span>
          </div>
          <div class="countdown-value" :class="{ urgent: countdown < 300 }">
            {{ countdownText }}
          </div>
        </div>

        <!-- Payment Status -->
        <div class="status-section">
          <div class="status-indicator" :class="{ checking: isChecking }">
            <span class="status-dot"></span>
            <span class="status-text">{{ paymentStatusText }}</span>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { paymentApi } from '@/services/api'
import QRCode from 'qrcode'

const router = useRouter()
const route = useRoute()

const loading = ref(true)
const error = ref(null)
const orderInfo = ref(null)
const qrCanvas = ref(null)
const isChecking = ref(false)
const paymentStatus = ref('pending') // pending, paid, expired, cancelled

let statusCheckInterval = null
let countdownInterval = null
const countdown = ref(30 * 60) // 30分钟倒计时（秒）

// 支付方式
const currentPaymentMethod = ref('WECHAT')

// 会员等级文本
const memberLevelText = computed(() => {
  const level = orderInfo.value?.memberLevel
  if (level === 'PRO') return '专业版'
  if (level === 'ENTERPRISE') return '企业版'
  return ''
})

// 支付方式名称
const paymentMethodName = computed(() => {
  const method = orderInfo.value?.paymentMethod || currentPaymentMethod.value
  return method === 'ALIPAY' ? '支付宝' : '微信支付'
})

// 支付方式提示
const paymentMethodTip = computed(() => {
  const method = orderInfo.value?.paymentMethod || currentPaymentMethod.value
  return method === 'ALIPAY' ? '请使用支付宝扫码支付' : '请使用微信扫码支付'
})

// 二维码提示
const qrTipText = computed(() => {
  const method = orderInfo.value?.paymentMethod || currentPaymentMethod.value
  return method === 'ALIPAY' ? '打开支付宝扫一扫，完成支付' : '打开微信扫一扫，完成支付'
})

// 支付方式样式类
const paymentMethodClass = computed(() => {
  const method = orderInfo.value?.paymentMethod || currentPaymentMethod.value
  return method === 'ALIPAY' ? 'alipay' : 'wechat'
})

// 支付状态文本
const paymentStatusText = computed(() => {
  switch (paymentStatus.value) {
    case 'pending':
      return '等待支付中...'
    case 'paid':
      return '支付成功'
    case 'expired':
      return '订单已过期'
    case 'cancelled':
      return '订单已取消'
    default:
      return ''
  }
})

// 倒计时文本
const countdownText = computed(() => {
  const minutes = Math.floor(countdown.value / 60)
  const seconds = countdown.value % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})

// 生成二维码
const generateQRCode = async (text) => {
  if (!qrCanvas.value) return

  try {
    await QRCode.toCanvas(qrCanvas.value, text, {
      width: 220,
      margin: 2,
      color: {
        dark: '#000000',
        light: '#ffffff'
      }
    })
  } catch (err) {
    console.error('生成二维码失败:', err)
  }
}

// 检查支付状态
const checkPaymentStatus = async () => {
  if (!orderInfo.value?.orderNo) return

  isChecking.value = true
  try {
    const result = await paymentApi.queryOrder(orderInfo.value.orderNo)

    if (result.status === 1) {
      // 支付成功
      paymentStatus.value = 'paid'
      clearInterval(statusCheckInterval)
      clearInterval(countdownInterval)

      // 显示成功提示并跳转
      setTimeout(() => {
        router.push('/profile')
      }, 1500)
    }
  } catch (err) {
    console.error('查询支付状态失败:', err)
  } finally {
    isChecking.value = false
  }
}

// 创建订单
const createOrder = async () => {
  const memberLevel = route.query.memberLevel || 'PRO'
  const months = parseInt(route.query.months) || 1
  const paymentMethod = route.query.paymentMethod || 'WECHAT'

  currentPaymentMethod.value = paymentMethod

  try {
    loading.value = true
    const data = await paymentApi.createOrder(memberLevel, months, paymentMethod)
    orderInfo.value = data
    loading.value = false

    // 等待 DOM 更新后再生成二维码（canvas 需要先挂载到 DOM）
    await nextTick()
    await generateQRCode(data.qrCodeUrl)

    // 开始轮询支付状态
    startStatusCheck()

    // 开始倒计时
    startCountdown()

  } catch (err) {
    loading.value = false
    error.value = err.message || '创建订单失败'
  }
}

// 开始轮询支付状态
const startStatusCheck = () => {
  statusCheckInterval = setInterval(checkPaymentStatus, 3000)
}

// 开始倒计时
const startCountdown = () => {
  countdownInterval = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      paymentStatus.value = 'expired'
      clearInterval(countdownInterval)
      clearInterval(statusCheckInterval)
    }
  }, 1000)
}

onMounted(() => {
  // 检查登录状态
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/')
    return
  }

  createOrder()
})

onUnmounted(() => {
  if (statusCheckInterval) {
    clearInterval(statusCheckInterval)
  }
  if (countdownInterval) {
    clearInterval(countdownInterval)
  }
})
</script>

<style scoped>
/* Container */
.payment-container {
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
.payment-header {
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

.back-link {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  font-size: 0.9rem;
  transition: color 0.2s;
}

.back-link:hover {
  color: #fff;
}

.back-link svg {
  width: 18px;
  height: 18px;
}

/* Main Content */
.payment-main {
  position: relative;
  z-index: 1;
  max-width: 480px;
  margin: 0 auto;
  padding: 100px 24px 60px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  min-height: calc(100vh - 80px);
}

/* Loading State */
.loading-state {
  text-align: center;
  padding: 60px 24px;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 3px solid rgba(255, 255, 255, 0.1);
  border-top-color: #6366f1;
  border-radius: 50%;
  margin: 0 auto 20px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-state p {
  color: rgba(255, 255, 255, 0.6);
}

/* Error State */
.error-state {
  text-align: center;
  padding: 60px 24px;
}

.error-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.error-state h2 {
  font-size: 1.5rem;
  margin-bottom: 12px;
}

.error-state p {
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 24px;
}

.btn-retry {
  display: inline-block;
  padding: 12px 32px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 12px;
  color: #fff;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.3s;
}

.btn-retry:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(99, 102, 241, 0.4);
}

/* Payment Card */
.payment-card {
  width: 100%;
  padding: 40px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 24px;
  backdrop-filter: blur(20px);
}

.payment-header-info {
  text-align: center;
  margin-bottom: 32px;
}

.payment-title {
  font-size: 1.75rem;
  font-weight: 700;
  margin-bottom: 8px;
}

.payment-subtitle {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.95rem;
}

/* Order Info */
.order-info {
  background: rgba(255, 255, 255, 0.03);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 32px;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.order-item:last-child {
  border-bottom: none;
}

.order-label {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.95rem;
}

.order-value {
  font-weight: 600;
  font-size: 1rem;
}

.order-value.amount {
  color: #86efac;
  font-size: 1.25rem;
}

/* QR Code Section */
.qr-section {
  text-align: center;
  margin-bottom: 32px;
}

.qr-code {
  display: inline-block;
  padding: 16px;
  background: #fff;
  border-radius: 16px;
  margin-bottom: 16px;
  position: relative;
}

.qr-code.wechat {
  border: 3px solid #07c160;
}

.qr-code.alipay {
  border: 3px solid #1677ff;
}

.qr-code canvas {
  display: block;
}

.qr-tip {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.9rem;
}

/* Countdown */
.countdown-section {
  text-align: center;
  padding: 20px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  margin-bottom: 24px;
}

.countdown-label {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.85rem;
  margin-bottom: 8px;
}

.countdown-label svg {
  width: 16px;
  height: 16px;
}

.countdown-value {
  font-size: 2rem;
  font-weight: 700;
  color: #fff;
  font-family: 'Courier New', monospace;
}

.countdown-value.urgent {
  color: #f87171;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

/* Status Section */
.status-section {
  text-align: center;
}

.status-indicator {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 100px;
}

.status-indicator.checking {
  animation: pulse 1s infinite;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #fbbf24;
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.status-text {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.8);
}

/* Responsive */
@media (max-width: 480px) {
  .payment-card {
    padding: 24px;
  }

  .payment-title {
    font-size: 1.5rem;
  }

  .countdown-value {
    font-size: 1.5rem;
  }
}
</style>
