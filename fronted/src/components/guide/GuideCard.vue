<template>
  <div class="guide-card" @click="$emit('click', guide)">
    <div class="guide-cover">
      <img v-if="guide.coverImage" :src="guide.coverImage" :alt="guide.title" />
      <div v-else class="guide-cover-placeholder">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/>
        </svg>
      </div>
      <div class="guide-destination-tag">{{ guide.destination }}</div>
    </div>
    <div class="guide-content">
      <h3 class="guide-title">{{ guide.title }}</h3>
      <div class="guide-author">
        <div class="author-avatar">{{ guide.username?.charAt(0)?.toUpperCase() }}</div>
        <span class="author-name">{{ guide.username }}</span>
      </div>
      <div class="guide-stats">
        <span class="stat-item">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
            <path d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
          </svg>
          {{ guide.viewCount || 0 }}
        </span>
        <span class="stat-item">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
          </svg>
          {{ guide.likeCount || 0 }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  guide: {
    type: Object,
    required: true
  }
})

defineEmits(['click'])
</script>

<style scoped>
.guide-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
}

.guide-card:hover {
  transform: translateY(-4px);
  border-color: rgba(139, 92, 246, 0.3);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

.guide-cover {
  position: relative;
  aspect-ratio: 16 / 10;
  overflow: hidden;
}

.guide-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.guide-card:hover .guide-cover img {
  transform: scale(1.05);
}

.guide-cover-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.2), rgba(139, 92, 246, 0.2));
  display: flex;
  align-items: center;
  justify-content: center;
}

.guide-cover-placeholder svg {
  width: 48px;
  height: 48px;
  color: rgba(255, 255, 255, 0.3);
}

.guide-destination-tag {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 6px 12px;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(8px);
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  color: #fff;
}

.guide-content {
  padding: 16px;
}

.guide-title {
  font-size: 1rem;
  font-weight: 600;
  color: #fff;
  margin-bottom: 12px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.guide-author {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.author-avatar {
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
  font-weight: 600;
  color: #fff;
}

.author-name {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.6);
}

.guide-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.5);
}

.stat-item svg {
  width: 14px;
  height: 14px;
}
</style>
