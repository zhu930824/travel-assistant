<template>
  <div class="travel-form">
    <div class="form-header">
      <div class="form-icon">🗺️</div>
      <h2>规划您的旅行</h2>
      <p>输入基本信息，AI将为您生成专属行程</p>
    </div>

    <form @submit.prevent="handleSubmit" class="form-body">
      <div class="form-group">
        <label>目的地</label>
        <div class="input-wrapper">
          <input
            v-model="formData.destination"
            type="text"
            placeholder="输入城市或地区名称"
            required
          />
          <span class="input-icon">📍</span>
        </div>
      </div>

      <div class="form-row">
        <div class="form-group">
          <label>行程天数</label>
          <div class="input-wrapper">
            <input
              v-model.number="formData.days"
              type="number"
              min="1"
              max="30"
              placeholder="3"
            />
            <span class="input-icon">📅</span>
          </div>
        </div>

        <div class="form-group">
          <label>出行人数</label>
          <div class="input-wrapper">
            <input
              v-model.number="formData.guests"
              type="number"
              min="1"
              max="20"
              placeholder="2"
            />
            <span class="input-icon">👥</span>
          </div>
        </div>
      </div>

      <div class="form-group">
        <label>预算范围</label>
        <div class="budget-selector">
          <button
            v-for="budget in budgetOptions"
            :key="budget.value"
            type="button"
            :class="['budget-btn', { active: formData.budget === budget.value }]"
            @click="formData.budget = budget.value"
          >
            {{ budget.label }}
          </button>
        </div>
      </div>

      <div class="form-group">
        <label>旅行偏好</label>
        <div class="preference-tags">
          <button
            v-for="pref in preferenceOptions"
            :key="pref"
            type="button"
            :class="['pref-tag', { active: formData.preferences.includes(pref) }]"
            @click="togglePreference(pref)"
          >
            {{ pref }}
          </button>
        </div>
      </div>

      <button
        type="submit"
        :disabled="isPlanning || !formData.destination"
        class="submit-btn"
      >
        <template v-if="isPlanning">
          <span class="typing-dots">
            <span></span>
            <span></span>
            <span></span>
          </span>
          <span>AI正在规划...</span>
        </template>
        <template v-else>
          <span>✨</span>
          <span>开始智能规划</span>
        </template>
      </button>
    </form>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const props = defineProps({
  isPlanning: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['submit'])

const formData = reactive({
  destination: '',
  days: 3,
  guests: 2,
  budget: 'medium',
  preferences: []
})

const budgetOptions = [
  { label: '经济', value: 'economy' },
  { label: '舒适', value: 'medium' },
  { label: '豪华', value: 'luxury' }
]

const preferenceOptions = [
  '美食探索', '自然风光', '历史文化', '购物休闲', '户外冒险', '网红打卡'
]

const togglePreference = (pref) => {
  const index = formData.preferences.indexOf(pref)
  if (index > -1) {
    formData.preferences.splice(index, 1)
  } else {
    formData.preferences.push(pref)
  }
}

const handleSubmit = () => {
  if (!formData.destination) return

  emit('submit', {
    destination: formData.destination,
    days: formData.days || 3,
    guests: formData.guests || 2,
    budget: formData.budget,
    preferences: formData.preferences
  })
}
</script>

<style scoped>
.travel-form {
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(30px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 24px;
  padding: 32px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.form-header {
  text-align: center;
  margin-bottom: 28px;
}

.form-icon {
  font-size: 2.5rem;
  margin-bottom: 12px;
}

.form-header h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: white;
  margin-bottom: 8px;
}

.form-header p {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.9rem;
}

.form-body {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.input-wrapper {
  position: relative;
}

.input-wrapper input {
  width: 100%;
  padding: 14px 44px 14px 16px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  color: white;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.input-wrapper input:focus {
  outline: none;
  border-color: rgba(99, 102, 241, 0.5);
  background: rgba(255, 255, 255, 0.15);
}

.input-wrapper input::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

.input-icon {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  pointer-events: none;
}

.budget-selector {
  display: flex;
  gap: 8px;
}

.budget-btn {
  flex: 1;
  padding: 12px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.budget-btn:hover {
  background: rgba(255, 255, 255, 0.15);
}

.budget-btn.active {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-color: transparent;
  color: white;
  font-weight: 600;
}

.preference-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.pref-tag {
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 20px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.pref-tag:hover {
  background: rgba(255, 255, 255, 0.12);
}

.pref-tag.active {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.3), rgba(139, 92, 246, 0.3));
  border-color: rgba(139, 92, 246, 0.5);
  color: white;
}

.submit-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6, #d946ef);
  background-size: 200% 200%;
  animation: gradient-shift 3s ease infinite;
  border: none;
  border-radius: 14px;
  color: white;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: all 0.3s ease;
  margin-top: 8px;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 15px 30px rgba(99, 102, 241, 0.4);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@keyframes gradient-shift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.typing-dots {
  display: flex;
  gap: 4px;
}

.typing-dots span {
  width: 8px;
  height: 8px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out both;
}

.typing-dots span:nth-child(1) { animation-delay: -0.32s; }
.typing-dots span:nth-child(2) { animation-delay: -0.16s; }

@keyframes typing {
  0%, 80%, 100% { transform: scale(0); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}
</style>
