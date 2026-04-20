<template>
  <div :class="['agent-card', { active: isActive }]">
    <!-- Header -->
    <div class="card-header">
      <span class="agent-icon">{{ agent.icon }}</span>
      <div class="agent-info">
        <h4 class="agent-name">{{ agent.name }}</h4>
        <p class="agent-desc">{{ agent.description }}</p>
      </div>
      <span :class="['status-badge', statusClass]">
        {{ statusText }}
      </span>
    </div>

    <!-- Content -->
    <div ref="contentRef" class="card-content scroll-content">
      <div v-if="content" class="markdown-content" v-html="renderedContent"></div>
      <div v-else-if="status === 'loading'" class="loading-state">
        <div class="typing-dots">
          <span></span>
          <span></span>
          <span></span>
        </div>
        <p>正在规划中...</p>
      </div>
      <div v-else class="empty-state">
        <p>等待开始...</p>
      </div>
    </div>

    <!-- Progress Bar -->
    <div v-if="status === 'loading'" class="progress-wrapper">
      <div class="progress-bar">
        <div class="progress-bar-fill"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch, nextTick } from 'vue'
import { marked } from 'marked'

const props = defineProps({
  agent: {
    type: Object,
    required: true
  },
  isActive: {
    type: Boolean,
    default: false
  },
  content: {
    type: String,
    default: ''
  },
  status: {
    type: String,
    default: 'pending'
  }
})

const contentRef = ref(null)

const statusClass = computed(() => {
  switch (props.status) {
    case 'complete': return 'status-badge--complete'
    case 'loading': return 'status-badge--loading'
    default: return 'status-badge--pending'
  }
})

const statusText = computed(() => {
  switch (props.status) {
    case 'complete': return '已完成'
    case 'loading': return '处理中'
    default: return '等待中'
  }
})

const renderedContent = computed(() => {
  if (!props.content) return ''
  try {
    return marked(props.content)
  } catch (e) {
    return props.content.replace(/\n/g, '<br>')
  }
})

watch(() => props.content, async () => {
  await nextTick()
  if (contentRef.value) {
    contentRef.value.scrollTop = contentRef.value.scrollHeight
  }
})
</script>

<style scoped>
.agent-card {
  padding: 24px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  transition: all 0.3s ease;
}

.agent-card:hover {
  background: rgba(255, 255, 255, 0.05);
}

.agent-card.active {
  border-color: rgba(99, 102, 241, 0.5);
  box-shadow: 0 0 30px rgba(99, 102, 241, 0.2);
}

.card-header {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  margin-bottom: 16px;
}

.agent-icon {
  font-size: 2rem;
  flex-shrink: 0;
}

.agent-info {
  flex: 1;
  min-width: 0;
}

.agent-name {
  font-size: 1rem;
  font-weight: 700;
  color: #fff;
  margin-bottom: 4px;
}

.agent-desc {
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.5);
}

.card-content {
  height: 280px;
  overflow-y: auto;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: rgba(255, 255, 255, 0.4);
}

.loading-state p,
.empty-state p {
  margin-top: 12px;
  font-size: 0.85rem;
}

.typing-dots {
  display: flex;
  gap: 6px;
}

.typing-dots span {
  width: 8px;
  height: 8px;
  background: #6366f1;
  border-radius: 50%;
  animation: typing-dot 1.4s infinite ease-in-out both;
}

.typing-dots span:nth-child(1) { animation-delay: -0.32s; }
.typing-dots span:nth-child(2) { animation-delay: -0.16s; }

@keyframes typing-dot {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

.progress-wrapper {
  margin-top: 16px;
}

.progress-bar {
  height: 4px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  overflow: hidden;
}

.progress-bar-fill {
  height: 100%;
  width: 60%;
  background: linear-gradient(90deg, #6366f1, #8b5cf6, #d946ef);
  background-size: 200% 100%;
  animation: progress-shimmer 1.5s linear infinite;
}

@keyframes progress-shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}
</style>
