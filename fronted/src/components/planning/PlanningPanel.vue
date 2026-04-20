<template>
  <div class="planning-panel">
    <!-- Progress Steps -->
    <div class="progress-steps">
      <div
        v-for="(stage, index) in stages"
        :key="stage.id"
        class="step-item"
      >
        <div :class="['step-circle', getStepStatus(stage.id)]">
          <span>{{ stage.icon }}</span>
        </div>
        <span class="step-label">{{ stage.name }}</span>
        <div v-if="index < stages.length - 1" :class="['step-line', getLineStatus(stage.id)]"></div>
      </div>
    </div>

    <!-- Agent Cards -->
    <div class="agents-grid">
      <AgentCard
        v-for="agent in agents"
        :key="agent.id"
        :agent="agent"
        :isActive="currentStage === agent.id"
        :content="agentContents[agent.id]"
        :status="agentStatus[agent.id]"
      />
    </div>

    <!-- Final Plan -->
    <div v-if="finalPlan" class="final-plan">
      <div class="final-header">
        <span class="final-icon">📋</span>
        <h3>完整旅行方案</h3>
        <span :class="['status-badge', isPlanning ? 'status-badge--loading' : 'status-badge--complete']">
          {{ isPlanning ? '整合中...' : '已完成' }}
        </span>
      </div>
      <div class="final-content scroll-content" v-html="renderedFinalPlan"></div>
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
  { id: 'attraction', name: '景点', icon: '🏔️' },
  { id: 'hotel', name: '住宿', icon: '🏨' },
  { id: 'transport', name: '交通', icon: '🚗' },
  { id: 'supervisor', name: '整合', icon: '📋' }
]

const agents = [
  { id: 'attraction', name: '景点推荐专家', icon: '🏔️', description: '推荐热门景点和游览路线' },
  { id: 'hotel', name: '住宿规划专家', icon: '🏨', description: '精选住宿推荐' },
  { id: 'transport', name: '交通规划专家', icon: '🚗', description: '最优交通方案' }
]

const getStepStatus = (stageId) => {
  const status = props.agentStatus[stageId]
  if (status === 'complete') return 'complete'
  if (status === 'loading' || props.currentStage === stageId) return 'active'
  return 'pending'
}

const getLineStatus = (stageId) => {
  const status = props.agentStatus[stageId]
  if (status === 'complete') return 'complete'
  if (status === 'loading') return 'active'
  return 'pending'
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

<style scoped>
.planning-panel {
  margin-top: 32px;
}

.progress-steps {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  padding: 24px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  margin-bottom: 24px;
}

.step-item {
  display: flex;
  align-items: center;
}

.step-circle {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  transition: all 0.3s ease;
}

.step-circle.pending {
  background: rgba(255, 255, 255, 0.05);
  opacity: 0.5;
}

.step-circle.active {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.3), rgba(139, 92, 246, 0.3));
  animation: pulse-glow 2s ease-in-out infinite;
}

.step-circle.complete {
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.3), rgba(74, 222, 128, 0.3));
}

@keyframes pulse-glow {
  0%, 100% { box-shadow: 0 0 0 0 rgba(99, 102, 241, 0.4); }
  50% { box-shadow: 0 0 0 10px rgba(99, 102, 241, 0); }
}

.step-label {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.7);
  margin-left: 10px;
  margin-right: 16px;
}

.step-line {
  width: 40px;
  height: 3px;
  border-radius: 2px;
  margin-right: 16px;
}

.step-line.pending {
  background: rgba(255, 255, 255, 0.1);
}

.step-line.active {
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  animation: shimmer 1.5s ease-in-out infinite;
}

.step-line.complete {
  background: linear-gradient(90deg, #22c55e, #4ade80);
}

@keyframes shimmer {
  0% { opacity: 0.5; }
  50% { opacity: 1; }
  100% { opacity: 0.5; }
}

.agents-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.final-plan {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  padding: 28px;
}

.final-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.final-icon {
  font-size: 1.5rem;
}

.final-header h3 {
  font-size: 1.25rem;
  font-weight: 700;
  color: #fff;
  flex: 1;
}

.final-content {
  max-height: 400px;
  overflow-y: auto;
}

.final-content::-webkit-scrollbar {
  width: 6px;
}

.final-content::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 3px;
}

.final-content::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}

.final-content::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}

@media (max-width: 768px) {
  .progress-steps {
    flex-wrap: wrap;
    gap: 16px;
  }

  .step-label {
    display: none;
  }

  .step-line {
    display: none;
  }

  .agents-grid {
    grid-template-columns: 1fr;
  }
}
</style>
