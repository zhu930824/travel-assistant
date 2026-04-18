<template>
  <div class="form-container">
    <div class="form-glow"></div>
    <div class="form-card">
      <!-- Header -->
      <div class="form-header">
        <div class="header-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/>
            <path d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/>
          </svg>
        </div>
        <div class="header-text">
          <h2>规划您的旅行</h2>
          <p>输入信息，AI为您定制专属行程</p>
        </div>
      </div>

      <form @submit.prevent="handleSubmit" class="form-body">
        <!-- Top Row: Destination + Days + Guests -->
        <div class="form-top-row">
          <!-- Destination -->
          <div class="form-group form-group--flex">
            <label>
              <span class="label-icon">📍</span>
              <span>目的地</span>
            </label>
            <div class="input-box">
              <input
                v-model="formData.destination"
                type="text"
                placeholder="输入城市名称..."
                required
              />
            </div>
            <div class="quick-dests">
              <span class="quick-label">热门：</span>
              <button
                v-for="dest in quickDestinations"
                :key="dest"
                type="button"
                class="quick-dest-btn"
                @click="formData.destination = dest"
              >
                {{ dest }}
              </button>
            </div>
          </div>

          <!-- Days -->
          <div class="form-group form-group--flex">
            <label>
              <span class="label-icon">📅</span>
              <span>天数</span>
            </label>
            <div class="days-selector">
              <button
                v-for="day in dayOptions"
                :key="day"
                type="button"
                :class="['day-btn', { active: formData.days === day && !showCustomDays }]"
                @click="showCustomDays = false; formData.days = day"
              >
                {{ day }}天
              </button>
              <button
                type="button"
                :class="['day-btn', 'day-btn--custom', { active: showCustomDays }]"
                @click="showCustomDays = true"
              >
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/>
                  <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/>
                </svg>
              </button>
            </div>
            <div v-if="showCustomDays" class="custom-days">
              <input
                v-model.number="formData.days"
                type="number"
                min="1"
                max="30"
                placeholder="自定义天数"
                class="custom-input"
              />
              <span class="custom-unit">天</span>
            </div>
          </div>

          <!-- Guests -->
          <div class="form-group form-group--flex">
            <label>
              <span class="label-icon">👥</span>
              <span>人数</span>
            </label>
            <div class="guests-selector">
              <button type="button" class="guest-btn" @click="formData.guests = Math.max(1, formData.guests - 1)">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M5 12h14"/>
                </svg>
              </button>
              <span class="guest-value">{{ formData.guests }}人</span>
              <button type="button" class="guest-btn" @click="formData.guests = Math.min(20, formData.guests + 1)">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 5v14M5 12h14"/>
                </svg>
              </button>
            </div>
          </div>
        </div>

        <!-- Budget -->
        <div class="form-group">
          <label>
            <span class="label-icon">💰</span>
            <span>预算范围</span>
          </label>
          <div class="budget-cards">
            <button
              v-for="budget in budgetOptions"
              :key="budget.value"
              type="button"
              :class="['budget-card', { active: formData.budget === budget.value }]"
              @click="formData.budget = budget.value"
            >
              <span class="budget-icon">{{ budget.icon }}</span>
              <div class="budget-info">
                <span class="budget-label">{{ budget.label }}</span>
                <span class="budget-range">{{ budget.range }}</span>
              </div>
            </button>
          </div>
        </div>

        <!-- Preferences -->
        <div class="form-group">
          <label>
            <span class="label-icon">🎯</span>
            <span>旅行偏好</span>
            <span class="label-hint">可多选</span>
          </label>
          <div class="pref-grid">
            <button
              v-for="pref in preferenceOptions"
              :key="pref.value"
              type="button"
              :class="['pref-item', { active: formData.preferences.includes(pref.value) }]"
              @click="togglePreference(pref.value)"
            >
              <span class="pref-icon">{{ pref.icon }}</span>
              <span class="pref-label">{{ pref.label }}</span>
            </button>
          </div>
        </div>

        <!-- Submit Button -->
        <button
          type="submit"
          :disabled="isPlanning || !formData.destination"
          class="submit-btn"
        >
          <template v-if="isPlanning">
            <span class="btn-spinner"></span>
            <span>AI正在规划...</span>
          </template>
          <template v-else>
            <span class="btn-icon">✨</span>
            <span class="btn-text">开始智能规划</span>
            <span class="btn-arrow">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M5 12h14M12 5l7 7-7 7"/>
              </svg>
            </span>
          </template>
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'

const props = defineProps({
  isPlanning: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['submit'])

const showCustomDays = ref(false)

const formData = reactive({
  destination: '',
  days: 3,
  guests: 2,
  budget: 'medium',
  preferences: []
})

const quickDestinations = ['东京', '巴黎', '马尔代夫', '三亚']

const dayOptions = [3, 5, 7, 10]

const budgetOptions = [
  { label: '经济', value: 'economy', icon: '🎒', range: '经济实惠' },
  { label: '舒适', value: 'medium', icon: '⭐', range: '性价比高' },
  { label: '豪华', value: 'luxury', icon: '👑', range: '尊享体验' }
]

const preferenceOptions = [
  { label: '美食', value: '美食探索', icon: '🍜' },
  { label: '自然', value: '自然风光', icon: '🏔️' },
  { label: '文化', value: '历史文化', icon: '🏛️' },
  { label: '购物', value: '购物休闲', icon: '🛍️' },
  { label: '冒险', value: '户外冒险', icon: '🎿' },
  { label: '打卡', value: '网红打卡', icon: '📸' }
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
.form-container {
  position: relative;
  width: 100%;
}

.form-glow {
  position: absolute;
  inset: -2px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6, #d946ef, #ec4899);
  border-radius: 30px;
  opacity: 0.4;
  filter: blur(25px);
  z-index: -1;
  animation: glow-pulse 4s ease-in-out infinite;
}

@keyframes glow-pulse {
  0%, 100% { opacity: 0.25; transform: scale(1); }
  50% { opacity: 0.45; transform: scale(1.01); }
}

.form-card {
  background: linear-gradient(180deg, rgba(25, 25, 40, 0.97), rgba(15, 15, 25, 0.99));
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 26px;
  padding: 40px 48px;
  backdrop-filter: blur(20px);
}

/* Header */
.form-header {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 36px;
  padding-bottom: 28px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.header-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 30px rgba(99, 102, 241, 0.4);
}

.header-icon svg {
  width: 30px;
  height: 30px;
  color: #fff;
}

.header-text h2 {
  font-size: 1.6rem;
  font-weight: 800;
  color: #fff;
  margin-bottom: 4px;
  letter-spacing: -0.02em;
}

.header-text p {
  font-size: 0.95rem;
  color: rgba(255, 255, 255, 0.5);
}

/* Form Body */
.form-body {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

/* Top Row Layout */
.form-top-row {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 24px;
  align-items: start;
}

.form-group--flex {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form-group--large {
  min-width: 280px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form-group label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.95rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
}

.label-icon {
  font-size: 1rem;
}

.label-hint {
  font-size: 0.8rem;
  font-weight: 400;
  color: rgba(255, 255, 255, 0.4);
  margin-left: auto;
}

/* Input */
.input-box input {
  width: 100%;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 14px;
  color: #fff;
  font-size: 1.05rem;
  transition: all 0.3s ease;
}

.input-box input:hover {
  border-color: rgba(255, 255, 255, 0.2);
}

.input-box input:focus {
  outline: none;
  border-color: rgba(99, 102, 241, 0.6);
  background: rgba(255, 255, 255, 0.08);
  box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.1);
}

.input-box input::placeholder {
  color: rgba(255, 255, 255, 0.35);
}

/* Quick Destinations */
.quick-dests {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
  flex-wrap: wrap;
}

.quick-label {
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.4);
}

.quick-dest-btn {
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 100px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.quick-dest-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

/* Days Selector */
.days-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.day-btn {
  padding: 14px 16px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 60px;
}

.day-btn:hover {
  background: rgba(255, 255, 255, 0.08);
}

.day-btn.active {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.25), rgba(139, 92, 246, 0.25));
  border-color: rgba(139, 92, 246, 0.5);
  color: #fff;
}

.day-btn--custom {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 14px;
}

.day-btn--custom svg {
  width: 18px;
  height: 18px;
}

.custom-days {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}

.custom-input {
  width: 100px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(99, 102, 241, 0.5);
  border-radius: 12px;
  color: #fff;
  font-size: 1rem;
  text-align: center;
}

.custom-input:focus {
  outline: none;
  background: rgba(255, 255, 255, 0.08);
}

.custom-unit {
  font-size: 0.95rem;
  color: rgba(255, 255, 255, 0.6);
}

/* Guests Selector */
.guests-selector {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 20px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 14px;
  min-width: 160px;
}

.guest-btn {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.08);
  border: none;
  border-radius: 10px;
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.guest-btn:hover {
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
}

.guest-btn svg {
  width: 18px;
  height: 18px;
}

.guest-value {
  font-size: 1.15rem;
  font-weight: 600;
  color: #fff;
  min-width: 50px;
  text-align: center;
}

/* Budget Cards */
.budget-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.budget-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px 20px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 18px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: left;
}

.budget-card:hover {
  background: rgba(255, 255, 255, 0.08);
  transform: translateY(-3px);
}

.budget-card.active {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.2), rgba(139, 92, 246, 0.2));
  border-color: rgba(139, 92, 246, 0.6);
  box-shadow: 0 8px 25px rgba(99, 102, 241, 0.2);
}

.budget-icon {
  font-size: 2rem;
}

.budget-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.budget-label {
  font-size: 1.1rem;
  font-weight: 700;
  color: #fff;
}

.budget-range {
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.5);
}

.budget-card.active .budget-range {
  color: rgba(255, 255, 255, 0.7);
}

/* Preference Grid */
.pref-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}

.pref-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px 12px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.pref-item:hover {
  background: rgba(255, 255, 255, 0.08);
  transform: translateY(-3px);
}

.pref-item.active {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.18), rgba(139, 92, 246, 0.18));
  border-color: rgba(139, 92, 246, 0.5);
}

.pref-icon {
  font-size: 1.75rem;
  transition: transform 0.3s ease;
}

.pref-item.active .pref-icon {
  transform: scale(1.15);
}

.pref-label {
  font-size: 0.9rem;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.8);
}

.pref-item.active .pref-label {
  color: #fff;
}

/* Submit Button */
.submit-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  width: 100%;
  padding: 22px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6, #a855f7);
  background-size: 200% 200%;
  border: none;
  border-radius: 18px;
  color: #fff;
  font-size: 1.2rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.4s ease;
  position: relative;
  overflow: hidden;
  margin-top: 8px;
}

.submit-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255,255,255,0.15), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.submit-btn:hover:not(:disabled)::before {
  opacity: 1;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-4px);
  box-shadow: 0 16px 40px rgba(99, 102, 241, 0.5);
  background-position: 100% 50%;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-icon {
  font-size: 1.35rem;
}

.btn-arrow {
  display: flex;
  align-items: center;
  transition: transform 0.3s ease;
}

.btn-arrow svg {
  width: 24px;
  height: 24px;
}

.submit-btn:hover:not(:disabled) .btn-arrow {
  transform: translateX(8px);
}

.btn-spinner {
  width: 24px;
  height: 24px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Responsive */
@media (max-width: 900px) {
  .form-card {
    padding: 32px 28px;
  }

  .form-top-row {
    grid-template-columns: 1fr;
    gap: 28px;
  }

  .pref-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 600px) {
  .form-card {
    padding: 28px 20px;
  }

  .budget-cards {
    grid-template-columns: 1fr;
  }

  .budget-card {
    padding: 18px 16px;
  }

  .pref-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }

  .pref-item {
    padding: 16px 10px;
  }

  .days-selector {
    flex-wrap: wrap;
  }

  .guests-selector {
    width: 100%;
    justify-content: center;
  }

  .custom-days {
    width: 100%;
    justify-content: center;
  }
}
</style>
