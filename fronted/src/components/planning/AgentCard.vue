<template>
  <div
    :class="[
      'glass-card p-6 glass-card-hover transition-all duration-300',
      { 'ring-2 ring-indigo-500/50': isActive }
    ]"
  >
    <!-- 卡片头部 -->
    <div class="flex items-center gap-3 mb-4">
      <span class="text-3xl">{{ agent.icon }}</span>
      <div class="flex-1">
        <h3 class="font-bold text-white">{{ agent.name }}</h3>
        <p class="text-white/60 text-sm">{{ agent.description }}</p>
      </div>
      <span :class="['status-badge', getStatusClass]">
        {{ getStatusText }}
      </span>
    </div>

    <!-- 内容区域 -->
    <div
      ref="contentRef"
      class="scroll-content min-h-[150px] max-h-[300px] pr-2"
    >
      <!-- 有内容时显示 -->
      <div
        v-if="content"
        class="markdown-content text-white/90"
        v-html="renderedContent"
      ></div>

      <!-- 加载状态 -->
      <div v-else-if="status === 'loading'" class="flex items-center justify-center h-32">
        <div class="text-center">
          <div class="typing-dots justify-center mb-3">
            <span></span>
            <span></span>
            <span></span>
          </div>
          <p class="text-white/60 text-sm">正在规划中...</p>
        </div>
      </div>

      <!-- 等待状态 -->
      <div v-else class="flex items-center justify-center h-32">
        <p class="text-white/40 text-sm">等待开始...</p>
      </div>
    </div>

    <!-- 进度条 -->
    <div v-if="status === 'loading'" class="mt-4">
      <div class="progress-bar">
        <div class="progress-bar-fill" style="width: 60%"></div>
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

const getStatusClass = computed(() => {
  switch (props.status) {
    case 'complete':
      return 'status-badge--complete'
    case 'loading':
      return 'status-badge--loading'
    default:
      return 'status-badge--pending'
  }
})

const getStatusText = computed(() => {
  switch (props.status) {
    case 'complete':
      return '已完成'
    case 'loading':
      return '处理中'
    default:
      return '等待中'
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

// 自动滚动到底部
watch(() => props.content, async () => {
  await nextTick()
  if (contentRef.value) {
    contentRef.value.scrollTop = contentRef.value.scrollHeight
  }
})
</script>
