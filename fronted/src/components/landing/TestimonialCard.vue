<template>
  <div class="testimonial-card">
    <div class="testimonial-stars">
      <span v-for="i in 5" :key="i" class="star" :class="{ filled: i <= rating }">★</span>
    </div>
    <p class="testimonial-text">"{{ text }}"</p>
    <div class="testimonial-author">
      <div class="author-avatar" :style="{ background: avatarColor }">
        {{ avatarInitial }}
      </div>
      <div class="author-info">
        <span class="author-name">{{ name }}</span>
        <span class="author-trip">{{ trip }}</span>
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
  background: rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  transition: all 0.3s ease;
}

.testimonial-card:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-4px);
}

.testimonial-stars {
  display: flex;
  gap: 4px;
  margin-bottom: 16px;
}

.star {
  font-size: 1.25rem;
  color: rgba(255, 255, 255, 0.3);
  transition: color 0.3s ease;
}

.star.filled {
  color: #fbbf24;
}

.testimonial-text {
  font-size: 1rem;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.85);
  margin-bottom: 20px;
  font-style: italic;
}

.testimonial-author {
  display: flex;
  align-items: center;
  gap: 14px;
}

.author-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  font-weight: 700;
  color: white;
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-name {
  font-weight: 600;
  color: white;
}

.author-trip {
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.6);
}
</style>
