<template>
  <div class="feature-card">
    <div class="feature-icon-wrapper">
      <span class="feature-icon">{{ icon }}</span>
    </div>
    <h3 class="feature-title">{{ title }}</h3>
    <p class="feature-desc">{{ description }}</p>
  </div>
</template>

<script setup>
defineProps({
  icon: {
    type: String,
    required: true
  },
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    required: true
  }
})
</script>

<style scoped>
.feature-card {
  padding: 32px;
  background: rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  text-align: center;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.feature-card:hover {
  transform: translateY(-8px);
  background: rgba(255, 255, 255, 0.1);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
}

.feature-icon-wrapper {
  width: 64px;
  height: 64px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.3), rgba(139, 92, 246, 0.3));
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.feature-icon {
  font-size: 2rem;
}

.feature-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: white;
  margin-bottom: 12px;
}

.feature-desc {
  color: rgba(255, 255, 255, 0.6);
  line-height: 1.6;
  font-size: 0.9rem;
}
</style>
