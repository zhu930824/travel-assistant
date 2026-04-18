<template>
  <div :class="['pricing-card', { highlighted }]">
    <div v-if="highlighted" class="badge">最受欢迎</div>
    <h3 class="title">{{ title }}</h3>
    <div class="price">
      <span class="price-value">{{ price }}</span>
    </div>
    <ul class="features">
      <li v-for="feature in features" :key="feature" class="feature-item">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M5 12l5 5L20 7"/>
        </svg>
        <span>{{ feature }}</span>
      </li>
    </ul>
    <button :class="['cta-btn', { primary: highlighted }]">
      {{ highlighted ? '立即开始' : '选择方案' }}
    </button>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  price: {
    type: String,
    required: true
  },
  features: {
    type: Array,
    required: true
  },
  highlighted: {
    type: Boolean,
    default: false
  }
})
</script>

<style scoped>
.pricing-card {
  position: relative;
  padding: 32px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  text-align: center;
  transition: all 0.3s ease;
}

.pricing-card:hover {
  transform: translateY(-4px);
  background: rgba(255, 255, 255, 0.05);
}

.pricing-card.highlighted {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.15), rgba(139, 92, 246, 0.15));
  border: 2px solid rgba(139, 92, 246, 0.5);
  transform: scale(1.02);
}

.pricing-card.highlighted:hover {
  transform: scale(1.02) translateY(-4px);
}

.badge {
  position: absolute;
  top: -12px;
  left: 50%;
  transform: translateX(-50%);
  padding: 6px 16px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 100px;
  font-size: 0.75rem;
  font-weight: 600;
  color: #fff;
}

.title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #fff;
  margin-bottom: 12px;
}

.price {
  margin-bottom: 24px;
}

.price-value {
  font-size: 2.5rem;
  font-weight: 800;
  color: #fff;
}

.pricing-card.highlighted .price-value {
  background: linear-gradient(135deg, #a5b4fc, #c4b5fd);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.features {
  list-style: none;
  margin-bottom: 28px;
}

.feature-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 12px 0;
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.feature-item:last-child {
  border-bottom: none;
}

.feature-item svg {
  width: 16px;
  height: 16px;
  color: #86efac;
  flex-shrink: 0;
}

.cta-btn {
  width: 100%;
  padding: 14px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: #fff;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cta-btn:hover {
  background: rgba(255, 255, 255, 0.12);
}

.cta-btn.primary {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border: none;
}

.cta-btn.primary:hover {
  box-shadow: 0 10px 30px rgba(99, 102, 241, 0.4);
  transform: translateY(-2px);
}
</style>
