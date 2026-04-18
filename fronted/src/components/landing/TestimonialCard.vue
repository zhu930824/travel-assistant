<template>
  <div class="testimonial-card">
    <div class="stars">
      <span v-for="i in 5" :key="i" class="star" :class="{ filled: i <= rating }">★</span>
    </div>
    <p class="text">"{{ text }}"</p>
    <div class="author">
      <div class="avatar" :style="{ background: avatarColor }">
        {{ avatarInitial }}
      </div>
      <div class="info">
        <span class="name">{{ name }}</span>
        <span class="trip">{{ trip }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  name: String,
  text: String,
  rating: Number,
  trip: String,
  avatarColor: String
})

const avatarInitial = computed(() => props.name.charAt(0))
</script>

<style scoped>
.testimonial-card {
  padding: 28px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  transition: all 0.3s ease;
}

.testimonial-card:hover {
  background: rgba(255, 255, 255, 0.05);
  transform: translateY(-4px);
}

.stars {
  display: flex;
  gap: 2px;
  margin-bottom: 16px;
}

.star {
  font-size: 1rem;
  color: rgba(255, 255, 255, 0.2);
}

.star.filled {
  color: #fbbf24;
}

.text {
  font-size: 1rem;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.85);
  margin-bottom: 20px;
  font-style: italic;
}

.author {
  display: flex;
  align-items: center;
  gap: 14px;
}

.avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
  font-weight: 700;
  color: #fff;
}

.info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.name {
  font-weight: 600;
  font-size: 0.95rem;
  color: #fff;
}

.trip {
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.5);
}
</style>
