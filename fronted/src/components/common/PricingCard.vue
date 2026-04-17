<template>
  <div
    :class="[
      'pricing-card',
      highlighted ? 'pricing-card--highlighted' : ''
    ]"
  >
    <div v-if="highlighted" class="popular-badge">最受欢迎</div>
    <h3 class="pricing-title">{{ title }}</h3>
    <div class="pricing-price">
      <span class="price-value">{{ price }}</span>
    </div>
    <ul class="pricing-features">
      <li
        v-for="feature in features"
        :key="feature"
        class="feature-item"
      >
        <span class="feature-check">✓</span>
        <span>{{ feature }}</span>
      </li>
    </ul>
    <button
      :class="[
        'pricing-cta',
        highlighted ? 'pricing-cta--primary' : ''
      ]"
    >
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
  background: rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  text-align: center;
  transition: all 0.4s ease;
}

.pricing-card:hover {
  transform: translateY(-8px);
  background: rgba(255, 255, 255, 0.08);
}

.pricing-card--highlighted {
  background: linear-gradient(
    135deg,
    rgba(99, 102, 241, 0.2) 0%,
    rgba(139, 92, 246, 0.2) 50%,
    rgba(217, 70, 239, 0.2) 100%
  );
  border: 2px solid rgba(139, 92, 246, 0.5);
  transform: scale(1.05);
}

.pricing-card--highlighted:hover {
  transform: scale(1.05) translateY(-8px);
}

.popular-badge {
  position: absolute;
  top: -12px;
  left: 50%;
  transform: translateX(-50%);
  padding: 6px 16px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  color: white;
}

.pricing-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: white;
  margin-bottom: 16px;
}

.pricing-price {
  margin-bottom: 24px;
}

.price-value {
  font-size: 2.5rem;
  font-weight: 800;
  color: white;
}

.pricing-card--highlighted .price-value {
  background: linear-gradient(135deg, #a5b4fc, #c4b5fd);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.pricing-features {
  list-style: none;
  margin-bottom: 28px;
}

.feature-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 10px 0;
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.feature-item:last-child {
  border-bottom: none;
}

.feature-check {
  color: #86efac;
  font-weight: bold;
}

.pricing-cta {
  width: 100%;
  padding: 14px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 14px;
  color: white;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.pricing-cta:hover {
  background: rgba(255, 255, 255, 0.15);
}

.pricing-cta--primary {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border: none;
}

.pricing-cta--primary:hover {
  box-shadow: 0 10px 30px rgba(99, 102, 241, 0.4);
  transform: translateY(-2px);
}
</style>
