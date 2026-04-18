<template>
  <div class="destination-card" :style="cardStyle">
    <div class="card-image">
      <img :src="image" :alt="name" loading="lazy" />
      <div class="card-overlay">
        <span class="card-tag">{{ tag }}</span>
        <span class="card-rating">⭐ {{ rating }}</span>
      </div>
    </div>
    <div class="card-content">
      <h3 class="card-name">{{ name }}</h3>
      <p class="card-desc">{{ description }}</p>
      <div class="card-footer">
        <span class="card-price">
          <span class="price-label">起</span>
          <span class="price-value">¥{{ price }}</span>
        </span>
        <span class="card-duration">{{ duration }}天</span>
      </div>
      <button class="card-btn" @click="$emit('select', id)">
        立即探索
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M5 12h14M12 5l7 7-7 7"/>
        </svg>
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
  border-radius: 20px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.destination-card:hover {
  transform: translateY(-8px);
  border-color: rgba(255, 255, 255, 0.15);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3), 0 0 30px var(--accent-color);
}

.card-image {
  position: relative;
  height: 180px;
  overflow: hidden;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.destination-card:hover .card-image img {
  transform: scale(1.1);
}

.card-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 40%, rgba(0, 0, 0, 0.7) 100%);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 12px;
}

.card-tag {
  align-self: flex-start;
  padding: 4px 12px;
  background: var(--accent-color);
  border-radius: 100px;
  font-size: 0.75rem;
  font-weight: 600;
  color: #fff;
}

.card-rating {
  align-self: flex-end;
  font-size: 0.85rem;
  font-weight: 600;
  color: #fbbf24;
}

.card-content {
  padding: 20px;
}

.card-name {
  font-size: 1.25rem;
  font-weight: 700;
  color: #fff;
  margin-bottom: 8px;
}

.card-desc {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 16px;
  line-height: 1.5;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.card-price {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.price-label {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.5);
}

.price-value {
  font-size: 1.5rem;
  font-weight: 800;
  color: var(--accent-color);
}

.card-duration {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.7);
}

.card-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, var(--accent-color), color-mix(in srgb, var(--accent-color) 70%, white));
  border: none;
  border-radius: 12px;
  color: #fff;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.card-btn:hover {
  transform: scale(1.02);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.3);
}

.card-btn svg {
  width: 18px;
  height: 18px;
  transition: transform 0.3s;
}

.card-btn:hover svg {
  transform: translateX(4px);
}
</style>
