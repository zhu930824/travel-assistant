<template>
  <div class="plan-container">
    <!-- Background Effects -->
    <div class="bg-effects">
      <div class="gradient-orb gradient-orb-1"></div>
      <div class="gradient-orb gradient-orb-2"></div>
      <div class="grid-overlay"></div>
    </div>

    <!-- Header -->
    <header class="plan-header">
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

        <div class="header-center">
          <div class="plan-info" v-if="planData">
            <span class="plan-destination">{{ planData.destination }}</span>
            <span class="plan-divider">·</span>
            <span class="plan-days">{{ planData.days }}天</span>
          </div>
        </div>

        <div class="header-actions">
          <button class="btn-new" @click="startNewPlan">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 5v14M5 12h14"/>
            </svg>
            <span>新建规划</span>
          </button>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="plan-main">
      <!-- Progress Bar -->
      <div class="progress-section" v-if="isPlanning">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
        </div>
        <div class="progress-text">
          <span class="progress-stage">{{ currentStageText }}</span>
          <span class="progress-percent">{{ progressPercent }}%</span>
        </div>
      </div>

      <!-- Agent Cards Grid -->
      <div class="agents-grid">
        <!-- 景点推荐 Agent -->
        <div class="agent-card" :class="agentStatus.attraction">
          <div class="agent-header">
            <div class="agent-icon agent-icon--attraction">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/>
                <path d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/>
              </svg>
            </div>
            <div class="agent-info">
              <h3 class="agent-title">景点推荐</h3>
              <span class="agent-status">{{ getStatusText('attraction') }}</span>
            </div>
            <div class="agent-loader" v-if="agentStatus.attraction === 'loading'">
              <div class="loader-ring"></div>
            </div>
          </div>
          <div class="agent-content" v-if="agentContents.attraction">
            <div class="content-text" v-html="renderMarkdown(agentContents.attraction)"></div>
          </div>
          <div class="agent-placeholder" v-else>
            <div class="placeholder-dots">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>

        <!-- 住宿规划 Agent -->
        <div class="agent-card" :class="agentStatus.hotel">
          <div class="agent-header">
            <div class="agent-icon agent-icon--hotel">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/>
              </svg>
            </div>
            <div class="agent-info">
              <h3 class="agent-title">住宿规划</h3>
              <span class="agent-status">{{ getStatusText('hotel') }}</span>
            </div>
            <div class="agent-loader" v-if="agentStatus.hotel === 'loading'">
              <div class="loader-ring"></div>
            </div>
          </div>
          <div class="agent-content" v-if="agentContents.hotel">
            <div class="content-text" v-html="renderMarkdown(agentContents.hotel)"></div>
          </div>
          <div class="agent-placeholder" v-else>
            <div class="placeholder-dots">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>

        <!-- 交通规划 Agent -->
        <div class="agent-card" :class="agentStatus.transport">
          <div class="agent-header">
            <div class="agent-icon agent-icon--transport">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4"/>
              </svg>
            </div>
            <div class="agent-info">
              <h3 class="agent-title">交通规划</h3>
              <span class="agent-status">{{ getStatusText('transport') }}</span>
            </div>
            <div class="agent-loader" v-if="agentStatus.transport === 'loading'">
              <div class="loader-ring"></div>
            </div>
          </div>
          <div class="agent-content" v-if="agentContents.transport">
            <div class="content-text" v-html="renderMarkdown(agentContents.transport)"></div>
          </div>
          <div class="agent-placeholder" v-else>
            <div class="placeholder-dots">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
      </div>

      <!-- Supervisor Output -->
      <div class="supervisor-section" v-if="agentContents.supervisor || isPlanning">
        <div class="supervisor-header">
          <div class="supervisor-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"/>
            </svg>
          </div>
          <div class="supervisor-info">
            <h2>智能行程方案</h2>
            <p v-if="isPlanning" class="supervisor-hint">AI 正在整合各 Agent 的建议，生成完整行程...</p>
            <p v-else-if="agentContents.supervisor" class="supervisor-hint">AI 已为您生成完整行程方案</p>
          </div>
          <div class="supervisor-actions" v-if="agentContents.supervisor && !isPlanning">
            <button class="btn-copy" @click="copyToClipboard">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
                <path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/>
              </svg>
              <span>复制</span>
            </button>
            <button class="btn-export" @click="exportToPDF" :disabled="isExporting">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
                <line x1="12" y1="18" x2="12" y2="12"/>
                <line x1="9" y1="15" x2="15" y2="15"/>
              </svg>
              <span v-if="isExporting">导出中...</span>
              <span v-else>导出PDF</span>
            </button>
          </div>
        </div>
        <div class="supervisor-content" v-if="agentContents.supervisor">
          <div class="content-text" v-html="renderMarkdown(agentContents.supervisor)"></div>
        </div>
        <div class="supervisor-loading" v-else-if="isPlanning">
          <div class="loading-pulse">
            <div class="pulse-ring"></div>
            <div class="pulse-ring"></div>
            <div class="pulse-ring"></div>
          </div>
          <p>AI 正在思考中...</p>
        </div>
      </div>

      <!-- Limit Reached State -->
      <div class="limit-section" v-if="limitReached">
        <div class="limit-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
        </div>
        <h3>本月规划次数已达上限</h3>
        <p>免费版用户每月限5次规划，升级专业版可享受无限规划次数</p>
        <div class="limit-actions">
          <router-link to="/profile" class="btn-upgrade">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M13 10V3L4 14h7v7l9-11h-7z"/>
            </svg>
            升级会员
          </router-link>
          <button class="btn-back" @click="router.push('/')">返回首页</button>
        </div>
      </div>

      <!-- Error State -->
      <div class="error-section" v-else-if="error">
        <div class="error-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <path d="M12 8v4M12 16h.01"/>
          </svg>
        </div>
        <h3>规划出现问题</h3>
        <p>{{ error }}</p>
        <button class="btn-retry" @click="retryPlan">重新规划</button>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { marked } from 'marked'

const router = useRouter()

// 规划数据
const planData = ref(null)
const error = ref('')
const limitReached = ref(false) // 次数已达上限
const isExporting = ref(false) // 导出中状态

// Agent 状态
const isPlanning = ref(false)
const agentStatus = reactive({
  attraction: 'pending',
  hotel: 'pending',
  transport: 'pending',
  supervisor: 'pending'
})

const agentContents = reactive({
  attraction: '',
  hotel: '',
  transport: '',
  supervisor: ''
})

// 计算进度
const progressPercent = computed(() => {
  const stages = ['attraction', 'hotel', 'transport', 'supervisor']
  let progress = 0
  stages.forEach(stage => {
    if (agentStatus[stage] === 'complete') progress += 25
    else if (agentStatus[stage] === 'loading') progress += 12.5
  })
  return Math.round(progress)
})

const currentStageText = computed(() => {
  const stageNames = {
    attraction: '景点推荐分析中',
    hotel: '住宿规划中',
    transport: '交通规划中',
    supervisor: '生成完整行程'
  }
  return stageNames[agentStatus.supervisor === 'loading' ? 'supervisor' :
         agentStatus.transport === 'loading' ? 'transport' :
         agentStatus.hotel === 'loading' ? 'hotel' :
         agentStatus.attraction === 'loading' ? 'attraction' : '准备中'] || '准备中'
})

// 获取状态文本
const getStatusText = (stage) => {
  const statusMap = {
    pending: '等待中',
    loading: '分析中...',
    complete: '已完成',
    error: '出错了'
  }
  return statusMap[agentStatus[stage]]
}

// 渲染 Markdown
const renderMarkdown = (text) => {
  if (!text) return ''
  return marked(text)
}

// 开始规划
const startPlanning = async (formData) => {
  if (!formData) {
    router.push('/')
    return
  }

  // 检查规划权限
  const token = localStorage.getItem('token')
  if (token) {
    try {
      const checkResult = await fetch('/api/user/can-plan', {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      const checkData = await checkResult.json()
      if (checkData.code === 200 && !checkData.data.canPlan) {
        limitReached.value = true
        return
      }
    } catch (err) {
      console.warn('检查规划权限失败:', err)
    }
  }

  isPlanning.value = true
  error.value = ''
  limitReached.value = false

  // 重置状态
  Object.keys(agentStatus).forEach(key => {
    agentStatus[key] = 'pending'
    agentContents[key] = ''
  })

  const headers = {
    'Content-Type': 'application/json',
    'Accept': 'text/event-stream'
  }
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }

  try {
    const response = await fetch('/api/travel/plan/stream', {
      method: 'POST',
      headers,
      body: JSON.stringify({
        destination: formData.destination,
        days: parseInt(formData.days) || 3,
        budget: formData.budget || null,
        preferences: formData.preferences || null
      })
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        if (line.startsWith('data:')) {
          try {
            const data = JSON.parse(line.substring(5).trim())
            handleMessage(data)
          } catch (e) {
            console.warn('Failed to parse SSE data:', line)
          }
        } else if (line.startsWith('event:')) {
          const eventType = line.substring(6).trim()
          if (eventType === 'complete') {
            handleComplete()
          }
        }
      }
    }
  } catch (err) {
    console.error('Planning error:', err)
    error.value = err.message || '规划失败，请重试'
    isPlanning.value = false
  }
}

// 处理消息
const handleMessage = (data) => {
  if (!data) return

  if (data.sessionId) return // 会话开始
  if (data.error) {
    error.value = data.error
    return
  }

  if (data.stage && data.hasContent) {
    const stage = data.stage

    // 更新状态
    if (agentStatus[stage] !== 'complete') {
      agentStatus[stage] = 'loading'
    }

    // 更新内容
    if (data.contentType === 'delta') {
      agentContents[stage] = data.content
    }

    // 如果是 supervisor 阶段，同步更新显示
    if (stage === 'supervisor') {
      agentContents.supervisor = data.content
    }
  }
}

// 处理完成
const handleComplete = () => {
  isPlanning.value = false
  Object.keys(agentStatus).forEach(key => {
    if (agentStatus[key] === 'loading') {
      agentStatus[key] = 'complete'
    }
  })
}

// 复制到剪贴板
const copyToClipboard = async () => {
  try {
    await navigator.clipboard.writeText(agentContents.supervisor)
    alert('已复制到剪贴板')
  } catch (err) {
    console.error('Copy failed:', err)
  }
}

// 导出PDF
const exportToPDF = async () => {
  if (!agentContents.supervisor) return

  isExporting.value = true

  try {
    // 动态导入 html2pdf.js
    const html2pdf = (await import('html2pdf.js')).default

    // 创建临时容器用于PDF导出
    const container = document.createElement('div')
    container.style.cssText = `
      position: absolute;
      left: -9999px;
      top: 0;
      width: 800px;
      background: #ffffff;
      padding: 40px;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
    `

    // 添加标题
    const header = document.createElement('div')
    header.innerHTML = `
      <div style="text-align: center; margin-bottom: 30px; padding-bottom: 20px; border-bottom: 2px solid #6366f1;">
        <h1 style="font-size: 28px; color: #1a1a2e; margin: 0 0 10px 0;">
          ${planData.value?.destination || '旅行'}${planData.value?.days || ''}日游规划方案
        </h1>
        <p style="font-size: 14px; color: #666; margin: 0;">
          由 TravelAI 智能生成 · ${new Date().toLocaleDateString('zh-CN')}
        </p>
      </div>
    `
    container.appendChild(header)

    // 添加内容
    const content = document.createElement('div')
    content.style.cssText = 'font-size: 14px; line-height: 1.8; color: #333;'
    content.innerHTML = renderMarkdown(agentContents.supervisor)
    container.appendChild(content)

    // 添加页脚
    const footer = document.createElement('div')
    footer.innerHTML = `
      <div style="margin-top: 40px; padding-top: 20px; border-top: 1px solid #eee; text-align: center;">
        <p style="font-size: 12px; color: #999; margin: 0;">
          本方案由 TravelAI 基于人工智能技术生成，仅供参考
        </p>
      </div>
    `
    container.appendChild(footer)

    document.body.appendChild(container)

    // 配置html2pdf选项
    const opt = {
      margin: [10, 10, 10, 10],
      filename: `${planData.value?.destination || '旅行'}规划方案_${new Date().toLocaleDateString('zh-CN').replace(/\//g, '-')}.pdf`,
      image: { type: 'jpeg', quality: 0.98 },
      html2canvas: {
        scale: 2,
        useCORS: true,
        logging: false
      },
      jsPDF: {
        unit: 'mm',
        format: 'a4',
        orientation: 'portrait'
      }
    }

    // 生成PDF
    await html2pdf().set(opt).from(container).save()

    // 清理临时容器
    document.body.removeChild(container)
  } catch (err) {
    console.error('Export PDF failed:', err)
    alert('导出PDF失败，请重试')
  } finally {
    isExporting.value = false
  }
}

// 新建规划
const startNewPlan = () => {
  sessionStorage.removeItem('planFormData')
  router.push('/')
}

// 重试
const retryPlan = () => {
  if (planData.value) {
    startPlanning(planData.value)
  }
}

// 生命周期
onMounted(() => {
  const savedData = sessionStorage.getItem('planFormData')
  if (savedData) {
    planData.value = JSON.parse(savedData)
    startPlanning(planData.value)
  } else {
    router.push('/')
  }
})

onUnmounted(() => {
  // 清理
})
</script>

<style scoped>
/* Container */
.plan-container {
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
  width: 800px;
  height: 800px;
  background: radial-gradient(circle, #4f46e5 0%, transparent 70%);
  top: -300px;
  left: -300px;
}

.gradient-orb-2 {
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, #7c3aed 0%, transparent 70%);
  bottom: -200px;
  right: -200px;
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
.plan-header {
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
  max-width: 1400px;
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

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.plan-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 24px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 100px;
}

.plan-destination {
  font-size: 1rem;
  font-weight: 600;
  color: #fff;
}

.plan-divider {
  color: rgba(255, 255, 255, 0.3);
}

.plan-days {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.7);
}

.btn-new {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 10px;
  color: #fff;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-new:hover {
  background: rgba(255, 255, 255, 0.15);
}

.btn-new svg {
  width: 16px;
  height: 16px;
}

/* Main Content */
.plan-main {
  position: relative;
  z-index: 1;
  max-width: 1400px;
  margin: 0 auto;
  padding: 100px 24px 60px;
}

/* Progress Section */
.progress-section {
  margin-bottom: 40px;
  padding: 20px 24px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 16px;
}

.progress-bar {
  height: 6px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 12px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #6366f1, #8b5cf6, #a855f7);
  border-radius: 3px;
  transition: width 0.5s ease;
}

.progress-text {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-stage {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.7);
}

.progress-percent {
  font-size: 0.9rem;
  font-weight: 600;
  color: #a5b4fc;
}

/* Agents Grid */
.agents-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 40px;
}

/* Agent Card */
.agent-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  padding: 24px;
  min-height: 280px;
  max-height: 1000px;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
}

.agent-card.loading {
  border-color: rgba(99, 102, 241, 0.3);
  box-shadow: 0 0 30px rgba(99, 102, 241, 0.1);
}

.agent-card.complete {
  border-color: rgba(34, 197, 94, 0.3);
}

.agent-card.error {
  border-color: rgba(239, 68, 68, 0.3);
}

.agent-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.agent-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.agent-icon svg {
  width: 24px;
  height: 24px;
}

.agent-icon--attraction {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.25), rgba(139, 92, 246, 0.25));
  color: #a5b4fc;
}

.agent-icon--hotel {
  background: linear-gradient(135deg, rgba(236, 72, 153, 0.25), rgba(244, 63, 94, 0.25));
  color: #f9a8d4;
}

.agent-icon--transport {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.25), rgba(239, 68, 68, 0.25));
  color: #fcd34d;
}

.agent-info {
  flex: 1;
}

.agent-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 4px;
}

.agent-status {
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.5);
}

.agent-loader {
  width: 32px;
  height: 32px;
}

.loader-ring {
  width: 100%;
  height: 100%;
  border: 2px solid rgba(99, 102, 241, 0.2);
  border-top-color: #6366f1;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.agent-content {
  flex: 1;
  overflow: auto;
}

.agent-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-dots {
  display: flex;
  gap: 8px;
}

.placeholder-dots span {
  width: 8px;
  height: 8px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  animation: pulse-dot 1.5s ease-in-out infinite;
}

.placeholder-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.placeholder-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes pulse-dot {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.2); opacity: 1; }
}

/* Supervisor Section */
.supervisor-section {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1), rgba(139, 92, 246, 0.1));
  border: 1px solid rgba(139, 92, 246, 0.2);
  border-radius: 24px;
  padding: 32px;
}

.supervisor-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
}

.supervisor-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 30px rgba(99, 102, 241, 0.3);
}

.supervisor-icon svg {
  width: 28px;
  height: 28px;
  color: #fff;
}

.supervisor-info {
  flex: 1;
}

.supervisor-info h2 {
  font-size: 1.5rem;
  font-weight: 700;
  margin-bottom: 4px;
}

.supervisor-hint {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.6);
}

.supervisor-actions {
  display: flex;
  gap: 12px;
}

.btn-copy {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 10px;
  color: #fff;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-copy:hover {
  background: rgba(255, 255, 255, 0.15);
}

.btn-copy svg {
  width: 16px;
  height: 16px;
}

.btn-export {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border: none;
  border-radius: 10px;
  color: #fff;
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-export:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(99, 102, 241, 0.4);
}

.btn-export:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-export svg {
  width: 16px;
  height: 16px;
}

.supervisor-content {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 16px;
  padding: 24px;
}

.supervisor-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
}

.loading-pulse {
  position: relative;
  width: 80px;
  height: 80px;
  margin-bottom: 20px;
}

.pulse-ring {
  position: absolute;
  inset: 0;
  border: 2px solid rgba(99, 102, 241, 0.3);
  border-radius: 50%;
  animation: pulse-expand 2s ease-out infinite;
}

.pulse-ring:nth-child(2) {
  animation-delay: 0.5s;
}

.pulse-ring:nth-child(3) {
  animation-delay: 1s;
}

@keyframes pulse-expand {
  0% { transform: scale(0.5); opacity: 1; }
  100% { transform: scale(1.5); opacity: 0; }
}

.supervisor-loading p {
  font-size: 0.95rem;
  color: rgba(255, 255, 255, 0.6);
}

/* Content Text */
.content-text {
  font-size: 0.95rem;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.9);
}

.content-text :deep(h1),
.content-text :deep(h2),
.content-text :deep(h3) {
  color: #fff;
  margin-top: 1.5em;
  margin-bottom: 0.75em;
}

.content-text :deep(h1) { font-size: 1.4rem; }
.content-text :deep(h2) { font-size: 1.2rem; }
.content-text :deep(h3) { font-size: 1.1rem; }

.content-text :deep(p) {
  margin-bottom: 1em;
}

.content-text :deep(ul),
.content-text :deep(ol) {
  margin-left: 1.5em;
  margin-bottom: 1em;
}

.content-text :deep(li) {
  margin-bottom: 0.5em;
}

.content-text :deep(strong) {
  color: #fff;
  font-weight: 600;
}

.content-text :deep(code) {
  background: rgba(255, 255, 255, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.85em;
}

/* Error Section */
.error-section {
  text-align: center;
  padding: 60px 24px;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  border-radius: 20px;
}

.error-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 20px;
  background: rgba(239, 68, 68, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.error-icon svg {
  width: 32px;
  height: 32px;
  color: #f87171;
}

.error-section h3 {
  font-size: 1.25rem;
  margin-bottom: 8px;
}

.error-section p {
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 24px;
}

.btn-retry {
  padding: 12px 32px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border: none;
  border-radius: 12px;
  color: #fff;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-retry:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(99, 102, 241, 0.4);
}

/* Limit Section */
.limit-section {
  text-align: center;
  padding: 60px 24px;
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.1), rgba(234, 179, 8, 0.08));
  border: 1px solid rgba(245, 158, 11, 0.2);
  border-radius: 20px;
}

.limit-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 20px;
  background: rgba(245, 158, 11, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.limit-icon svg {
  width: 32px;
  height: 32px;
  color: #fbbf24;
}

.limit-section h3 {
  font-size: 1.25rem;
  margin-bottom: 8px;
}

.limit-section p {
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 24px;
}

.limit-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.btn-upgrade {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 32px;
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  border: none;
  border-radius: 12px;
  color: #0a0a0f;
  font-size: 1rem;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.3s;
}

.btn-upgrade svg {
  width: 20px;
  height: 20px;
}

.btn-upgrade:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(251, 191, 36, 0.4);
}

.btn-back {
  padding: 12px 32px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  color: #fff;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-back:hover {
  background: rgba(255, 255, 255, 0.15);
}

/* Responsive */
@media (max-width: 1024px) {
  .agents-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .header-center {
    display: none;
  }

  .plan-main {
    padding: 80px 16px 40px;
  }

  .supervisor-section {
    padding: 24px;
  }

  .supervisor-header {
    flex-wrap: wrap;
  }

  .supervisor-actions {
    width: 100%;
    margin-top: 16px;
  }
}
</style>
