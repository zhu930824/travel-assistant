<template>
  <section class="guide-section">
    <div class="section-header">
      <h2 class="section-title">精选攻略</h2>
      <p class="section-desc">发现热门旅行攻略，启发你的下一次旅程</p>
    </div>

    <div class="guide-tabs">
      <button class="tab-btn active">推荐</button>
      <button class="tab-btn">最新</button>
      <button class="tab-btn">热门</button>
    </div>

    <div class="guide-grid" v-if="guides.length > 0">
      <GuideCard
        v-for="guide in guides"
        :key="guide.id"
        :guide="guide"
        @click="handleGuideClick"
      />
    </div>

    <div class="guide-empty" v-else>
      <div class="empty-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V9a2 2 0 012-2h2a2 2 0 012 2v9a2 2 0 01-2 2zM14 10h.01M17 10h.01M17 13h.01"/>
        </svg>
      </div>
      <p>暂无攻略内容</p>
    </div>

    <div class="guide-more" v-if="hasMore">
      <button class="more-btn" @click="loadMore">
        <span>查看更多攻略</span>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 9l-7 7-7-7"/>
        </svg>
      </button>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import GuideCard from './GuideCard.vue'
import { guideApi } from '@/services/api'

const router = useRouter()
const guides = ref([])
const page = ref(0)
const hasMore = ref(true)
const loading = ref(false)

const loadGuides = async () => {
  if (loading.value) return
  loading.value = true
  try {
    const result = await guideApi.getRecommendList(page.value, 8)
    guides.value = [...guides.value, ...result.list]
    hasMore.value = result.list.length >= 8
  } catch (error) {
    console.error('加载攻略失败:', error)
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  page.value++
  loadGuides()
}

const handleGuideClick = (guide) => {
  router.push(`/guide/${guide.id}`)
}

onMounted(() => {
  loadGuides()
})
</script>

<style scoped>
.guide-section {
  position: relative;
  z-index: 1;
  padding: 80px 24px;
}

.section-header {
  text-align: center;
  margin-bottom: 32px;
}

.section-title {
  font-size: 2rem;
  font-weight: 800;
  letter-spacing: -0.02em;
  margin-bottom: 8px;
}

.section-desc {
  font-size: 1rem;
  color: rgba(255, 255, 255, 0.6);
}

.guide-tabs {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 32px;
}

.tab-btn {
  padding: 10px 24px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 100px;
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  transition: all 0.2s;
}

.tab-btn:hover {
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.8);
}

.tab-btn.active {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.2), rgba(139, 92, 246, 0.2));
  border-color: rgba(139, 92, 246, 0.4);
  color: #a5b4fc;
}

.guide-grid {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.guide-empty {
  text-align: center;
  padding: 60px 24px;
}

.empty-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  color: rgba(255, 255, 255, 0.2);
}

.empty-icon svg {
  width: 100%;
  height: 100%;
}

.guide-empty p {
  color: rgba(255, 255, 255, 0.5);
}

.guide-more {
  text-align: center;
  margin-top: 40px;
}

.more-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 32px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 100px;
  color: #fff;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.more-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

.more-btn svg {
  width: 16px;
  height: 16px;
}

@media (max-width: 1024px) {
  .guide-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .guide-grid {
    grid-template-columns: 1fr;
  }
}
</style>