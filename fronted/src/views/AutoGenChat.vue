<template>
  <div class="autogen-container">
    <!-- Background Effects -->
    <div class="bg-effects">
      <div class="gradient-orb gradient-orb-1"></div>
      <div class="gradient-orb gradient-orb-2"></div>
      <div class="grid-overlay"></div>
    </div>

    <!-- Header -->
    <header class="autogen-header">
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
          <span class="mode-badge">AutoGen 人机协作</span>
        </div>

        <div class="header-actions">
          <button class="btn-back" @click="router.push('/')">返回首页</button>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="autogen-main">
      <!-- 初始表单 -->
      <div class="setup-section" v-if="!sessionId">
        <div class="setup-card">
          <div class="setup-header">
            <div class="setup-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M17 8h2a2 2 0 012 2v6a2 2 0 01-2 2h-2v4l-4-4H9a1.994 1.994 0 01-1.414-.586m0 0L11 14h4a2 2 0 002-2V6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2v4l.586-.586z"/>
              </svg>
            </div>
            <h2>AutoGen 人机协作规划</h2>
            <p>通过多轮对话，与 AI 专家协作完成旅行规划</p>
          </div>

          <div class="setup-form">
            <div class="form-group">
              <label>目的地</label>
              <input v-model="formData.destination" type="text" placeholder="例如：北京、上海、成都" />
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>天数</label>
                <select v-model="formData.days">
                  <option :value="1">1天</option>
                  <option :value="2">2天</option>
                  <option :value="3">3天</option>
                  <option :value="5">5天</option>
                  <option :value="7">7天</option>
                </select>
              </div>
              <div class="form-group">
                <label>预算</label>
                <select v-model="formData.budget">
                  <option value="经济">经济实惠</option>
                  <option value="中等">中等预算</option>
                  <option value="高端">高端豪华</option>
                </select>
              </div>
            </div>

            <div class="form-group">
              <label>偏好</label>
              <input v-model="formData.preferences" type="text" placeholder="例如：文化历史、美食、自然风光" />
            </div>

            <div class="form-group">
              <label>交互模式</label>
              <div class="mode-options">
                <button class="mode-btn" :class="{ active: formData.humanInputMode === 'ALWAYS' }"
                  @click="formData.humanInputMode = 'ALWAYS'">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
                  </svg>
                  <span>全程参与</span>
                  <small>每轮都需确认</small>
                </button>
                <button class="mode-btn" :class="{ active: formData.humanInputMode === 'TERMINATE' }"
                  @click="formData.humanInputMode = 'TERMINATE'">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                  </svg>
                  <span>结果确认</span>
                  <small>仅最终确认</small>
                </button>
                <button class="mode-btn" :class="{ active: formData.humanInputMode === 'NEVER' }"
                  @click="formData.humanInputMode = 'NEVER'">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M13 10V3L4 14h7v7l9-11h-7z"/>
                  </svg>
                  <span>全自动</span>
                  <small>无需人工干预</small>
                </button>
              </div>
            </div>

            <button class="btn-start" @click="startConversation" :disabled="!formData.destination">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z"/>
                <path d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
              </svg>
              开始对话
            </button>
          </div>
        </div>
      </div>

      <!-- 对话区域 -->
      <div class="chat-section" v-else>
        <!-- 对话信息栏 -->
        <div class="chat-info">
          <div class="chat-info-left">
            <span class="chat-destination">{{ formData.destination }}</span>
            <span class="chat-meta">{{ formData.days }}天 · {{ formData.budget }} · {{ modeLabel }}</span>
          </div>
          <div class="chat-info-right">
            <span class="chat-round" v-if="currentRound > 0">第 {{ currentRound }} 轮</span>
            <span class="chat-status" :class="chatStatus">{{ statusLabel }}</span>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="messages" ref="messagesContainer">
          <div v-for="(msg, index) in messages" :key="index"
            class="message" :class="getMessageClass(msg)">
            <div class="message-avatar" :class="getAvatarClass(msg)">
              <span>{{ getAgentEmoji(msg) }}</span>
            </div>
            <div class="message-body">
              <div class="message-header">
                <span class="message-name">{{ getAgentName(msg) }}</span>
                <span class="message-time">{{ formatTime(msg.timestamp) }}</span>
              </div>
              <div class="message-content" v-html="renderContent(msg.content)"></div>
            </div>
          </div>

          <!-- 加载中 -->
          <div class="message system" v-if="chatStatus === 'running' && !waitingForInput">
            <div class="message-avatar system-avatar">
              <span>...</span>
            </div>
            <div class="message-body">
              <div class="typing-indicator">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 用户输入区域 -->
        <div class="input-section" v-if="waitingForInput">
          <div class="input-hint">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/>
            </svg>
            <span>AI 请求您的输入</span>
          </div>
          <div class="input-box">
            <textarea v-model="userInput" @keydown.enter.exact="sendInput" placeholder="输入您的反馈，或输入 TERMINATE 结束对话..." rows="2"></textarea>
            <div class="input-actions">
              <button class="btn-terminate" @click="terminateConversation">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                  <path d="M9 10a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1v-4z"/>
                </svg>
                结束对话
              </button>
              <button class="btn-send" @click="sendInput" :disabled="!userInput.trim()">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"/>
                </svg>
                发送
              </button>
            </div>
          </div>
        </div>

        <!-- 完成区域 -->
        <div class="complete-section" v-if="chatStatus === 'completed'">
          <div class="complete-card">
            <div class="complete-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
              </svg>
            </div>
            <h3>对话已完成</h3>
            <p>共 {{ currentRound }} 轮对话，耗时 {{ duration }}秒</p>
            <div class="complete-actions">
              <button class="btn-copy" @click="copyResult">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
                  <path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/>
                </svg>
                复制结果
              </button>
              <button class="btn-restart" @click="resetConversation">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
                </svg>
                重新开始
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { marked } from 'marked'

const router = useRouter()

// 表单数据
const formData = reactive({
  destination: '',
  days: 3,
  budget: '中等',
  preferences: '',
  humanInputMode: 'TERMINATE'
})

// 会话状态
const sessionId = ref('')
const chatStatus = ref('idle') // idle, running, waiting_input, completed, error
const currentRound = ref(0)
const duration = ref(0)
const waitingForInput = ref(false)
const userInput = ref('')
const messages = ref([])
const messagesContainer = ref(null)
let eventSource = null

const modeLabel = computed(() => {
  const map = { ALWAYS: '全程参与', TERMINATE: '结果确认', NEVER: '全自动' }
  return map[formData.humanInputMode] || ''
})

const statusLabel = computed(() => {
  const map = {
    idle: '准备中',
    running: '对话中',
    waiting_input: '等待输入',
    completed: '已完成',
    error: '出错了'
  }
  return map[chatStatus.value] || ''
})

// 启动对话
const startConversation = async () => {
  if (!formData.destination) return

  try {
    const response = await fetch('/api/autogen/start', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(formData)
    })

    const data = await response.json()
    sessionId.value = data.sessionId
    chatStatus.value = 'running'

    // 启动 SSE 流
    connectStream()
  } catch (err) {
    console.error('启动对话失败:', err)
    chatStatus.value = 'error'
  }
}

// 连接 SSE 流
const connectStream = () => {
  const url = `/api/autogen/stream/${sessionId.value}`
  eventSource = new EventSource(url)

  eventSource.addEventListener('message', (event) => {
    try {
      const msg = JSON.parse(event.data)
      addMessage(msg)
    } catch (e) {
      console.warn('解析消息失败:', event.data)
    }
  })

  eventSource.addEventListener('input_required', (event) => {
    try {
      const data = JSON.parse(event.data)
      addSystemMessage(data.prompt)
      waitingForInput.value = true
      chatStatus.value = 'waiting_input'
    } catch (e) {
      console.warn('解析输入请求失败:', event.data)
    }
  })

  eventSource.addEventListener('complete', (event) => {
    try {
      const data = JSON.parse(event.data)
      currentRound.value = data.totalRounds || 0
      duration.value = Math.round((data.durationMs || 0) / 1000)
      chatStatus.value = 'completed'
      waitingForInput.value = false
    } catch (e) {
      console.warn('解析完成事件失败:', event.data)
    }
    eventSource.close()
  })

  eventSource.addEventListener('error', (event) => {
    console.error('SSE error:', event)
    chatStatus.value = 'error'
    waitingForInput.value = false
    eventSource.close()
  })

  eventSource.onerror = () => {
    chatStatus.value = 'error'
    waitingForInput.value = false
  }
}

// 发送用户输入
const sendInput = async () => {
  if (!userInput.value.trim()) return

  const input = userInput.value.trim()
  addMessage({ agentName: 'user_proxy', content: input, type: 'HUMAN_INPUT', timestamp: new Date().toISOString() })

  userInput.value = ''
  waitingForInput.value = false
  chatStatus.value = 'running'

  try {
    await fetch(`/api/autogen/input/${sessionId.value}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ input })
    })
  } catch (err) {
    console.error('发送输入失败:', err)
  }
}

// 终止对话
const terminateConversation = async () => {
  try {
    await fetch(`/api/autogen/terminate/${sessionId.value}`, { method: 'POST' })
    chatStatus.value = 'completed'
    waitingForInput.value = false
    if (eventSource) eventSource.close()
  } catch (err) {
    console.error('终止对话失败:', err)
  }
}

// 重置
const resetConversation = () => {
  sessionId.value = ''
  chatStatus.value = 'idle'
  messages.value = []
  currentRound.value = 0
  duration.value = 0
  waitingForInput.value = false
  if (eventSource) eventSource.close()
}

// 复制结果
const copyResult = async () => {
  const result = messages.value
    .filter(m => m.type === 'TEXT' || m.type === 'HUMAN_INPUT')
    .map(m => `【${getAgentName(m)}】${m.content}`)
    .join('\n\n')
  try {
    await navigator.clipboard.writeText(result)
    alert('已复制到剪贴板')
  } catch (err) {
    console.error('复制失败:', err)
  }
}

// 添加消息
const addMessage = (msg) => {
  messages.value.push(msg)
  currentRound.value = Math.floor(messages.value.length / 5) + 1
  nextTick(scrollToBottom)
}

const addSystemMessage = (content) => {
  messages.value.push({
    agentName: 'system',
    content: content,
    type: 'SYSTEM',
    timestamp: new Date().toISOString()
  })
  nextTick(scrollToBottom)
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 消息辅助方法
const getAgentName = (msg) => {
  const names = {
    user_proxy: '用户',
    attraction_expert: '景点专家',
    hotel_expert: '住宿专家',
    transport_expert: '交通专家',
    coordinator: '协调者',
    system: '系统'
  }
  return names[msg.agentName] || msg.agentName
}

const getAgentEmoji = (msg) => {
  const emojis = {
    user_proxy: '👤',
    attraction_expert: '🏛️',
    hotel_expert: '🏨',
    transport_expert: '🚄',
    coordinator: '🎯',
    system: '💬'
  }
  return emojis[msg.agentName] || '🤖'
}

const getMessageClass = (msg) => {
  if (msg.type === 'HUMAN_INPUT') return 'user'
  if (msg.type === 'SYSTEM') return 'system'
  if (msg.agentName === 'coordinator') return 'coordinator'
  return 'agent'
}

const getAvatarClass = (msg) => {
  if (msg.type === 'HUMAN_INPUT') return 'user-avatar'
  if (msg.agentName === 'coordinator') return 'coordinator-avatar'
  if (msg.agentName === 'system') return 'system-avatar'
  return 'agent-avatar'
}

const formatTime = (ts) => {
  if (!ts) return ''
  const d = new Date(ts)
  return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const renderContent = (content) => {
  if (!content) return ''
  return marked(content)
}
</script>

<style scoped>
/* Container */
.autogen-container {
  min-height: 100vh;
  background: #0a0a0f;
  color: #fff;
  position: relative;
  overflow-x: hidden;
}

/* Background */
.bg-effects { position: fixed; inset: 0; pointer-events: none; z-index: 0; }
.gradient-orb { position: absolute; border-radius: 50%; filter: blur(120px); opacity: 0.4; }
.gradient-orb-1 { width: 800px; height: 800px; background: radial-gradient(circle, #6366f1 0%, transparent 70%); top: -300px; left: -300px; }
.gradient-orb-2 { width: 600px; height: 600px; background: radial-gradient(circle, #10b981 0%, transparent 70%); bottom: -200px; right: -200px; }
.grid-overlay { position: absolute; inset: 0; background-image: linear-gradient(rgba(255,255,255,0.02) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.02) 1px, transparent 1px); background-size: 60px 60px; }

/* Header */
.autogen-header {
  position: fixed; top: 0; left: 0; right: 0; z-index: 100;
  padding: 16px 24px; background: rgba(10,10,15,0.9);
  backdrop-filter: blur(20px); border-bottom: 1px solid rgba(255,255,255,0.05);
}
.header-inner { max-width: 1200px; margin: 0 auto; display: flex; align-items: center; justify-content: space-between; }
.logo { display: flex; align-items: center; gap: 12px; text-decoration: none; color: #fff; }
.logo-icon { width: 36px; height: 36px; background: linear-gradient(135deg, #6366f1, #8b5cf6); border-radius: 10px; display: flex; align-items: center; justify-content: center; }
.logo-icon svg { width: 20px; height: 20px; color: #fff; }
.logo-text { font-size: 1.25rem; font-weight: 700; }
.mode-badge { padding: 8px 20px; background: linear-gradient(135deg, rgba(16,185,129,0.2), rgba(99,102,241,0.2)); border: 1px solid rgba(16,185,129,0.3); border-radius: 100px; font-size: 0.9rem; color: #6ee7b7; font-weight: 500; }
.btn-back { padding: 10px 20px; background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.15); border-radius: 10px; color: #fff; font-size: 0.9rem; cursor: pointer; transition: all 0.2s; }
.btn-back:hover { background: rgba(255,255,255,0.15); }

/* Main */
.autogen-main { position: relative; z-index: 1; max-width: 1200px; margin: 0 auto; padding: 100px 24px 60px; }

/* Setup Section */
.setup-section { display: flex; justify-content: center; padding-top: 40px; }
.setup-card { width: 100%; max-width: 600px; background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.08); border-radius: 24px; padding: 40px; }
.setup-header { text-align: center; margin-bottom: 32px; }
.setup-icon { width: 64px; height: 64px; margin: 0 auto 20px; background: linear-gradient(135deg, #6366f1, #10b981); border-radius: 18px; display: flex; align-items: center; justify-content: center; }
.setup-icon svg { width: 32px; height: 32px; color: #fff; }
.setup-header h2 { font-size: 1.5rem; font-weight: 700; margin-bottom: 8px; }
.setup-header p { color: rgba(255,255,255,0.6); font-size: 0.95rem; }

/* Form */
.form-group { margin-bottom: 20px; }
.form-group label { display: block; font-size: 0.9rem; font-weight: 500; margin-bottom: 8px; color: rgba(255,255,255,0.8); }
.form-group input, .form-group select { width: 100%; padding: 12px 16px; background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.12); border-radius: 12px; color: #fff; font-size: 0.95rem; outline: none; transition: border-color 0.2s; }
.form-group input:focus, .form-group select:focus { border-color: #6366f1; }
.form-group select option { background: #1a1a2e; color: #fff; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }

/* Mode Options */
.mode-options { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.mode-btn { display: flex; flex-direction: column; align-items: center; gap: 6px; padding: 16px 12px; background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.1); border-radius: 14px; color: rgba(255,255,255,0.7); cursor: pointer; transition: all 0.2s; }
.mode-btn:hover { background: rgba(255,255,255,0.06); }
.mode-btn.active { border-color: #6366f1; background: rgba(99,102,241,0.1); color: #a5b4fc; }
.mode-btn svg { width: 24px; height: 24px; }
.mode-btn span { font-size: 0.9rem; font-weight: 600; }
.mode-btn small { font-size: 0.75rem; color: rgba(255,255,255,0.4); }

.btn-start { width: 100%; display: flex; align-items: center; justify-content: center; gap: 10px; padding: 16px; background: linear-gradient(135deg, #6366f1, #8b5cf6); border: none; border-radius: 14px; color: #fff; font-size: 1rem; font-weight: 600; cursor: pointer; transition: all 0.3s; margin-top: 8px; }
.btn-start:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 10px 30px rgba(99,102,241,0.4); }
.btn-start:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-start svg { width: 20px; height: 20px; }

/* Chat Section */
.chat-section { display: flex; flex-direction: column; height: calc(100vh - 120px); }

.chat-info { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.08); border-radius: 14px; margin-bottom: 16px; }
.chat-destination { font-size: 1.1rem; font-weight: 600; }
.chat-meta { font-size: 0.85rem; color: rgba(255,255,255,0.5); margin-left: 12px; }
.chat-round { padding: 4px 12px; background: rgba(99,102,241,0.15); border-radius: 100px; font-size: 0.8rem; color: #a5b4fc; margin-right: 12px; }
.chat-status { padding: 4px 12px; border-radius: 100px; font-size: 0.8rem; }
.chat-status.running { background: rgba(16,185,129,0.15); color: #6ee7b7; }
.chat-status.waiting_input { background: rgba(245,158,11,0.15); color: #fcd34d; }
.chat-status.completed { background: rgba(99,102,241,0.15); color: #a5b4fc; }
.chat-status.error { background: rgba(239,68,68,0.15); color: #f87171; }

/* Messages */
.messages { flex: 1; overflow-y: auto; padding: 8px 0; display: flex; flex-direction: column; gap: 16px; }
.message { display: flex; gap: 12px; animation: fadeIn 0.3s ease; }
.message.user { flex-direction: row-reverse; }

.message-avatar { width: 40px; height: 40px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; font-size: 1.2rem; }
.user-avatar { background: linear-gradient(135deg, #3b82f6, #1d4ed8); }
.agent-avatar { background: linear-gradient(135deg, #6366f1, #8b5cf6); }
.coordinator-avatar { background: linear-gradient(135deg, #10b981, #059669); }
.system-avatar { background: rgba(255,255,255,0.1); }

.message-body { max-width: 75%; }
.message.user .message-body { text-align: right; }

.message-header { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.message.user .message-header { justify-content: flex-end; }
.message-name { font-size: 0.8rem; font-weight: 600; color: rgba(255,255,255,0.7); }
.message-time { font-size: 0.7rem; color: rgba(255,255,255,0.3); }

.message-content { padding: 14px 18px; border-radius: 16px; font-size: 0.92rem; line-height: 1.7; }
.message.agent .message-content { background: rgba(99,102,241,0.1); border: 1px solid rgba(99,102,241,0.15); border-top-left-radius: 4px; }
.message.coordinator .message-content { background: rgba(16,185,129,0.1); border: 1px solid rgba(16,185,129,0.15); border-top-left-radius: 4px; }
.message.user .message-content { background: rgba(59,130,246,0.15); border: 1px solid rgba(59,130,246,0.2); border-top-right-radius: 4px; }
.message.system .message-content { background: rgba(255,255,255,0.03); border: 1px dashed rgba(255,255,255,0.1); font-style: italic; color: rgba(255,255,255,0.5); }

.message-content :deep(h1), .message-content :deep(h2), .message-content :deep(h3) { color: #fff; margin: 0.8em 0 0.4em; }
.message-content :deep(h2) { font-size: 1.1rem; }
.message-content :deep(h3) { font-size: 1rem; }
.message-content :deep(ul), .message-content :deep(ol) { margin-left: 1.5em; }
.message-content :deep(li) { margin-bottom: 0.3em; }
.message-content :deep(strong) { color: #e0e7ff; }

/* Typing Indicator */
.typing-indicator { display: flex; gap: 6px; padding: 14px 18px; }
.typing-indicator span { width: 8px; height: 8px; background: rgba(99,102,241,0.5); border-radius: 50%; animation: typing 1.4s ease-in-out infinite; }
.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing { 0%, 100% { transform: scale(1); opacity: 0.5; } 50% { transform: scale(1.3); opacity: 1; } }
@keyframes fadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }

/* Input Section */
.input-section { padding-top: 16px; border-top: 1px solid rgba(255,255,255,0.06); }
.input-hint { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; padding: 10px 16px; background: rgba(245,158,11,0.1); border: 1px solid rgba(245,158,11,0.2); border-radius: 10px; }
.input-hint svg { width: 18px; height: 18px; color: #fcd34d; }
.input-hint span { font-size: 0.85rem; color: #fcd34d; font-weight: 500; }

.input-box { background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.1); border-radius: 16px; padding: 16px; }
.input-box textarea { width: 100%; background: transparent; border: none; color: #fff; font-size: 0.95rem; line-height: 1.6; resize: none; outline: none; }
.input-box textarea::placeholder { color: rgba(255,255,255,0.3); }
.input-actions { display: flex; justify-content: space-between; margin-top: 12px; }

.btn-terminate { display: flex; align-items: center; gap: 6px; padding: 10px 18px; background: rgba(239,68,68,0.1); border: 1px solid rgba(239,68,68,0.2); border-radius: 10px; color: #f87171; font-size: 0.85rem; cursor: pointer; transition: all 0.2s; }
.btn-terminate:hover { background: rgba(239,68,68,0.2); }
.btn-terminate svg { width: 16px; height: 16px; }

.btn-send { display: flex; align-items: center; gap: 6px; padding: 10px 22px; background: linear-gradient(135deg, #6366f1, #8b5cf6); border: none; border-radius: 10px; color: #fff; font-size: 0.9rem; font-weight: 500; cursor: pointer; transition: all 0.2s; }
.btn-send:hover:not(:disabled) { box-shadow: 0 4px 15px rgba(99,102,241,0.4); }
.btn-send:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-send svg { width: 16px; height: 16px; }

/* Complete Section */
.complete-section { padding-top: 20px; }
.complete-card { text-align: center; padding: 32px; background: rgba(16,185,129,0.05); border: 1px solid rgba(16,185,129,0.15); border-radius: 20px; }
.complete-icon { width: 56px; height: 56px; margin: 0 auto 16px; background: rgba(16,185,129,0.2); border-radius: 50%; display: flex; align-items: center; justify-content: center; }
.complete-icon svg { width: 28px; height: 28px; color: #6ee7b7; }
.complete-card h3 { font-size: 1.2rem; margin-bottom: 8px; }
.complete-card p { color: rgba(255,255,255,0.5); font-size: 0.9rem; margin-bottom: 20px; }
.complete-actions { display: flex; justify-content: center; gap: 12px; }
.btn-copy, .btn-restart { display: flex; align-items: center; gap: 8px; padding: 10px 20px; border-radius: 10px; font-size: 0.9rem; cursor: pointer; transition: all 0.2s; }
.btn-copy { background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.15); color: #fff; }
.btn-copy:hover { background: rgba(255,255,255,0.15); }
.btn-copy svg, .btn-restart svg { width: 16px; height: 16px; }
.btn-restart { background: linear-gradient(135deg, #6366f1, #8b5cf6); border: none; color: #fff; }
.btn-restart:hover { box-shadow: 0 4px 15px rgba(99,102,241,0.4); }

/* Responsive */
@media (max-width: 768px) {
  .setup-card { padding: 24px; }
  .mode-options { grid-template-columns: 1fr; }
  .form-row { grid-template-columns: 1fr; }
  .message-body { max-width: 85%; }
}
</style>
