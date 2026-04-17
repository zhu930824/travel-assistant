<template>
  <div class="mt-8">
    <!-- 进度指示器 -->
    <div class="glass-card p-4 mb-6">
      <div class="flex items-center justify-between">
        <div
          v-for="stage in stages"
          :key="stage.id"
          class="flex items-center"
        >
          <div class="flex items-center">
            <div
              :class="[
                'w-10 h-10 rounded-full flex items-center justify-center text-lg transition-all',
                getStageClass(stage.id)
              ]"
            >
              {{ stage.icon }}
            </div>
            <span class="ml-2 text-white/80 text-sm hidden md:inline">{{ stage.name }}</span>
          </div>
          <div
            v-if="stage.id !== 'supervisor'"
            class="w-8 md:w-16 h-1 mx-2 rounded transition-all"
            :class="getLineColor(stage.id)"
          ></div>
        </div>
      </div>
    </div>

    <!-- Agent卡片网格 -->
    <div class="grid md:grid-cols-3 gap-6 mb-6">
      <AgentCard
        v-for="agent in agents"
        :key="agent.id"
        :agent="agent"
        :isActive="currentStage === agent.id"
        :content="agentContents[agent.id]"
        :status="agentStatus[agent.id]"
      />
    </div>

    <!-- 最终整合结果 -->
    <div v-if="finalPlan" class="glass-card p-6">
      <div class="flex items-center gap-3 mb-4">
        <span class="text-2xl">📋</span>
        <h3 class="text-xl font-bold text-white">完整旅行方案</h3>
        <span
          :class="[
            'status-badge ml-auto',
            isPlanning ? 'status-badge--loading' : 'status-badge--complete'
          ]"
        >
          {{ isPlanning ? '整合中...' : '已完成' }}
        </span>
      </div>

      <div
        class="markdown-content scroll-content max-h-96 pr-2"
        v-html="renderedFinalPlan"
      ></div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import AgentCard from './AgentCard.vue'
import { marked } from 'marked'

const props = defineProps({
  agentStatus: {
    type: Object,
    required: true
  },
  agentContents: {
    type: Object,
    required: true
  },
  currentStage: {
    type: String,
    default: ''
  },
  finalPlan: {
    type: String,
    default: ''
  },
  isPlanning: {
    type: Boolean,
    default: false
  }
})

const stages = [
  { id: 'attraction', name: '景点推荐', icon: '🏔️' },
  { id: 'hotel', name: '住宿规划', icon: '🏨' },
  { id: 'transport', name: '交通安排', icon: '🚗' },
  { id: 'supervisor', name: '方案整合', icon: '📋' }
]

const agents = [
  { id: 'attraction', name: '景点推荐专家', icon: '🏔️', description: '为您推荐热门景点和游览路线' },
  { id: 'hotel', name: '住宿规划专家', icon: '🏨', description: '精选住宿推荐' },
  { id: 'transport', name: '交通规划专家', icon: '🚗', description: '最优交通方案' }
]

const getStageClass = (stageId) => {
  const status = props.agentStatus[stageId]
  if (status === 'complete') {
    return 'bg-green-500/30 text-white'
  } else if (status === 'loading' || props.currentStage === stageId) {
    return 'bg-indigo-500/30 text-white animate-pulse'
  }
  return 'bg-white/10 text-white/50'
}

const getLineColor = (stageId) => {
  const status = props.agentStatus[stageId]
  if (status === 'complete') {
    return 'bg-green-500'
  } else if (status === 'loading') {
    return 'bg-indigo-500'
  }
  return 'bg-white/20'
}

const renderedFinalPlan = computed(() => {
  if (!props.finalPlan) return ''
  try {
    return marked(props.finalPlan)
  } catch (e) {
    return props.finalPlan
  }
})
</script>
