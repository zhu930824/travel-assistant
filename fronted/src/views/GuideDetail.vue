<template>
  <div class="detail-container">
    <div class="detail-header">
      <div class="header-inner">
        <router-link to="/" class="logo">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 16V8a2 2 0 00-1-1.73l-7-4a2 2 0 00-2 0l-7 4A2 2 0 003 8v8a2 2 0 001 1.73l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z"/>
            </svg>
          </div>
          <span class="logo-text">TravelAI</span>
        </router-link>
        <router-link to="/" class="back-btn">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M15 19l-7-7 7-7"/>
          </svg>
          <span>返回首页</span>
        </router-link>
      </div>
    </div>

    <main class="detail-main" v-if="guide">
      <article class="guide-article">
        <div class="article-hero">
          <img v-if="guide.coverImage" :src="guide.coverImage" :alt="guide.title" class="hero-image" />
          <div class="hero-overlay">
            <span class="destination-badge">{{ guide.destination }}</span>
            <h1 class="article-title">{{ guide.title }}</h1>
          </div>
        </div>

        <div class="article-meta">
          <div class="author-info">
            <div class="author-avatar">{{ guide.username?.charAt(0)?.toUpperCase() }}</div>
            <div>
              <div class="author-name">{{ guide.username }}</div>
              <div class="publish-time">{{ guide.createTimeStr }}</div>
            </div>
          </div>
          <div class="article-stats">
            <span class="stat-item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                <path d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
              </svg>
              {{ guide.viewCount || 0 }} 阅读
            </span>
            <span class="stat-item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
              </svg>
              {{ guide.likeCount || 0 }} 点赞
            </span>
            <span class="stat-item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z"/>
              </svg>
              {{ guide.favoriteCount || 0 }} 收藏
            </span>
          </div>
        </div>

        <div class="article-tags" v-if="guide.tags?.length">
          <span v-for="tag in guide.tags" :key="tag" class="tag-item">{{ tag }}</span>
        </div>

        <div class="article-content" v-html="guide.content"></div>

        <div class="article-actions">
          <button class="action-btn" :class="{ active: guide.isLiked }" @click="handleLike">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
            </svg>
            <span>{{ guide.isLiked ? '已点赞' : '点赞' }}</span>
          </button>
          <button class="action-btn" :class="{ active: guide.isFavorited }" @click="handleFavorite">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z"/>
            </svg>
            <span>{{ guide.isFavorited ? '已收藏' : '收藏' }}</span>
          </button>
        </div>
      </article>
    </main>

    <div class="detail-loading" v-else>
      <p>加载中...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { guideApi } from '@/services/api'

const route = useRoute()
const guide = ref(null)

const loadGuide = async () => {
  try {
    const data = await guideApi.getGuideDetail(route.params.id)
    guide.value = data
  } catch (error) {
    console.error('加载攻略失败:', error)
  }
}

const handleLike = async () => {
  try {
    const result = await guideApi.likeGuide(guide.value.id)
    guide.value.isLiked = result.liked
    guide.value.likeCount += result.liked ? 1 : -1
  } catch (error) {
    console.error('操作失败:', error)
  }
}

const handleFavorite = async () => {
  try {
    const result = await guideApi.favoriteGuide(guide.value.id)
    guide.value.isFavorited = result.favorited
    guide.value.favoriteCount += result.favorited ? 1 : -1
  } catch (error) {
    console.error('操作失败:', error)
  }
}

onMounted(() => {
  loadGuide()
})
</script>

<style scoped>
.detail-container {
  min-height: 100vh;
  background: #0a0a0f;
  color: #fff;
}

.detail-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  padding: 16px 24px;
  background: rgba(10, 10, 15, 0.9);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  color: #fff;
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-icon svg {
  width: 20px;
  height: 20px;
  color: #fff;
}

.logo-text {
  font-size: 1.25rem;
  font-weight: 700;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  font-size: 0.9rem;
  transition: color 0.2s;
}

.back-btn:hover {
  color: #fff;
}

.back-btn svg {
  width: 18px;
  height: 18px;
}

.detail-main {
  max-width: 800px;
  margin: 0 auto;
  padding: 80px 24px 60px;
}

.article-hero {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  margin-bottom: 32px;
}

.hero-image {
  width: 100%;
  max-height: 400px;
  object-fit: cover;
}

.hero-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 40px 32px 32px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.8));
}

.destination-badge {
  display: inline-block;
  padding: 6px 14px;
  background: rgba(99, 102, 241, 0.5);
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  margin-bottom: 12px;
}

.article-title {
  font-size: 2rem;
  font-weight: 800;
  line-height: 1.3;
}

.article-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
  font-weight: 600;
}

.author-name {
  font-weight: 600;
  font-size: 0.95rem;
}

.publish-time {
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.5);
}

.article-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.6);
}

.stat-item svg {
  width: 16px;
  height: 16px;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 24px;
}

.tag-item {
  padding: 6px 14px;
  background: rgba(99, 102, 241, 0.15);
  border-radius: 20px;
  font-size: 0.8rem;
  color: #a5b4fc;
}

.article-content {
  font-size: 1.05rem;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.85);
}

.article-content :deep(h1),
.article-content :deep(h2),
.article-content :deep(h3) {
  margin-top: 24px;
  margin-bottom: 12px;
  font-weight: 700;
  color: #fff;
}

.article-content :deep(p) {
  margin-bottom: 16px;
}

.article-content :deep(img) {
  max-width: 100%;
  border-radius: 12px;
  margin: 16px 0;
}

.article-content :deep(a) {
  color: #818cf8;
  text-decoration: underline;
}

.article-content :deep(ul),
.article-content :deep(ol) {
  padding-left: 24px;
  margin-bottom: 16px;
}

.article-content :deep(blockquote) {
  border-left: 4px solid #6366f1;
  padding-left: 16px;
  margin: 16px 0;
  color: rgba(255, 255, 255, 0.7);
}

.article-actions {
  display: flex;
  gap: 16px;
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 100px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.action-btn.active {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.2), rgba(139, 92, 246, 0.2));
  border-color: rgba(139, 92, 246, 0.4);
  color: #a5b4fc;
}

.action-btn svg {
  width: 18px;
  height: 18px;
}

.detail-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  color: rgba(255, 255, 255, 0.5);
}

@media (max-width: 768px) {
  .article-hero {
    border-radius: 12px;
    margin: -80px -24px 24px;
  }

  .article-title {
    font-size: 1.5rem;
  }

  .article-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
}
</style>