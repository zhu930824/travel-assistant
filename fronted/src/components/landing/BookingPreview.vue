<template>
  <div class="booking-preview">
    <div class="booking-header">
      <h3>快速预订</h3>
      <p>选择目的地，开启您的旅程</p>
    </div>

    <div class="booking-form">
      <div class="form-group">
        <label>目的地</label>
        <div class="select-wrapper">
          <select v-model="form.destination">
            <option value="">选择目的地</option>
            <option v-for="dest in destinations" :key="dest.id" :value="dest.id">
              {{ dest.name }}
            </option>
          </select>
          <span class="select-icon">📍</span>
        </div>
      </div>

      <div class="form-row">
        <div class="form-group">
          <label>出发日期</label>
          <div class="input-wrapper">
            <input type="date" v-model="form.date" />
            <span class="input-icon">📅</span>
          </div>
        </div>

        <div class="form-group">
          <label>出行人数</label>
          <div class="input-wrapper">
            <input type="number" v-model="form.guests" min="1" max="10" />
            <span class="input-icon">👥</span>
          </div>
        </div>
      </div>

      <div class="form-group">
        <label>行程天数</label>
        <div class="days-selector">
          <button
            v-for="day in [3, 5, 7, 10, 14]"
            :key="day"
            :class="['day-btn', { active: form.days === day }]"
            @click="form.days = day"
          >
            {{ day }}天
          </button>
        </div>
      </div>

      <div class="booking-summary" v-if="estimatedPrice">
        <div class="summary-row">
          <span>预估费用</span>
          <span class="summary-price">¥{{ estimatedPrice.toLocaleString() }}</span>
        </div>
        <div class="summary-note">* 实际价格以最终方案为准</div>
      </div>

      <button class="booking-submit" @click="$emit('submit', form)">
        <span>开始规划</span>
        <span class="submit-icon">✨</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'

const props = defineProps({
  destinations: {
    type: Array,
    default: () => []
  }
})

defineEmits(['submit'])

const form = reactive({
  destination: '',
  date: '',
  guests: 2,
  days: 5
})

const estimatedPrice = computed(() => {
  if (!form.destination) return 0
  const basePrice = 1500
  return basePrice * form.days * form.guests
})
</script>

<style scoped>
.booking-preview {
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(30px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 24px;
  padding: 32px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.booking-header {
  text-align: center;
  margin-bottom: 28px;
}

.booking-header h3 {
  font-size: 1.5rem;
  font-weight: 700;
  color: white;
  margin-bottom: 8px;
}

.booking-header p {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
}

.booking-form {
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

.select-wrapper,
.input-wrapper {
  position: relative;
}

.select-wrapper select,
.input-wrapper input {
  width: 100%;
  padding: 14px 44px 14px 16px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  color: white;
  font-size: 1rem;
  appearance: none;
  cursor: pointer;
  transition: all 0.3s ease;
}

.select-wrapper select:focus,
.input-wrapper input:focus {
  outline: none;
  border-color: rgba(99, 102, 241, 0.5);
  background: rgba(255, 255, 255, 0.15);
}

.select-icon,
.input-icon {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  pointer-events: none;
}

.days-selector {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.day-btn {
  flex: 1;
  min-width: 60px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.day-btn:hover {
  background: rgba(255, 255, 255, 0.15);
}

.day-btn.active {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-color: transparent;
  color: white;
  font-weight: 600;
}

.booking-summary {
  padding: 16px;
  background: rgba(99, 102, 241, 0.15);
  border-radius: 12px;
  margin-top: 8px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-price {
  font-size: 1.5rem;
  font-weight: 700;
  color: #a5b4fc;
}

.summary-note {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 8px;
}

.booking-submit {
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

.booking-submit:hover {
  transform: translateY(-2px);
  box-shadow: 0 15px 30px rgba(99, 102, 241, 0.4);
}

@keyframes gradient-shift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}
</style>
