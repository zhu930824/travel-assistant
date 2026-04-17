<template>
  <div class="trust-badge">
    <span class="badge-icon">{{ icon }}</span>
    <span class="badge-text">{{ text }}</span>
  </div>
</template>

<script setup>
defineProps({
  icon: {
    type: String,
    required: true
  },
  text: {
    type: String,
    required: true
  }
})
</script>

<style scoped>
.trust-badge {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 24px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  transition: all 0.3s ease;
}

.trust-badge:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px);
}

.badge-icon {
  font-size: 1.75rem;
}

.badge-text {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
}
</style>
