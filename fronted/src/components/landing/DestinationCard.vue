<template>
  <div class="destination-card" :style="cardStyle">
    <div class="destination-image">
      <img :src="image" :alt="name" loading="lazy" />
      <div class="destination-overlay">
        <span class="destination-badge">{{ tag }}</span>
      </div>
    </div>
    <div class="destination-content">
      <div class="destination-header">
        <h3 class="destination-name">{{ name }}</h3>
        <div class="destination-rating">
          <span>⭐</span>
          <span>{{ rating }}</span>
        </div>
      </div>
      <p class="destination-desc">{{ description }}</p>
      <div class="destination-meta">
        <span class="destination-price">
          <span class="price-label">起</span>
          <span class="price-value">¥{{ price }}</span>
        </span>
        <span class="destination-duration">
          <span>📅</span>
          <span>{{ duration }}天</span>
        </span>
      </div>
      <button class="destination-cta" @click="$emit('select', id)">
        立即探索
        <span class="cta-arrow">→</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  id: String,
  name: String,
  image: String,
  tag: String,
  rating: String,
  description: String,
  price: Number,
  duration: Number,
  color: String
})

defineEmits(['select'])

const cardStyle = computed(() => ({
  '--accent-color': props.color
}))
</script>

<style scoped>
.destination-card {
  position: relative;
  border-radius: 24px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.destination-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.4),
              0 0 40px var(--accent-color);
}

.destination-image {
  position: relative;
  height: 220px;
  overflow: hidden;
}

.destination-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s ease;
}

.destination-card:hover .destination-image img {
  transform: scale(1.1);
}

.destination-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    transparent 0%,
    transparent 50%,
    rgba(0, 0, 0, 0.6) 100%
  );
}

.destination-badge {
  position: absolute;
  top: 16px;
  left: 16px;
  padding: 6px 14px;
  background: var(--accent-color);
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  color: white;
  backdrop-filter: blur(10px);
}

.destination-content {
  padding: 20px;
}

.destination-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.destination-name {
  font-size: 1.25rem;
  font-weight: 700;
  color: white;
}

.destination-rating {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #fbbf24;
  font-weight: 600;
  font-size: 0.9rem;
}

.destination-desc {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.875rem;
  line-height: 1.6;
  margin-bottom: 16px;
}

.destination-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.destination-price {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.price-label {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.6);
}

.price-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--accent-color);
}

.destination-duration {
  display: flex;
  align-items: center;
  gap: 6px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.875rem;
}

.destination-cta {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, var(--accent-color), color-mix(in srgb, var(--accent-color) 70%, white));
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.destination-cta:hover {
  transform: scale(1.02);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.3);
}

.cta-arrow {
  transition: transform 0.3s ease;
}

.destination-cta:hover .cta-arrow {
  transform: translateX(4px);
}
</style>
