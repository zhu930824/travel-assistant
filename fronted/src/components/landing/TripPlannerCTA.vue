<template>
  <div class="cta-section">
    <div class="cta-glow"></div>
    <div class="cta-content">
      <div class="cta-badge">
        <span>🎯</span>
        <span>AI智能规划</span>
      </div>
      <h2 class="cta-title">
        准备好开启您的
        <span class="highlight">梦想之旅</span>
        了吗？
      </h2>
      <p class="cta-desc">
        让我们的AI助手为您量身定制专属行程，从景点推荐到住宿安排，一站式解决您的旅行规划难题
      </p>
      <div class="cta-features">
        <div class="cta-feature">
          <span class="feature-icon">🗺️</span>
          <span>智能路线规划</span>
        </div>
        <div class="cta-feature">
          <span class="feature-icon">🏨</span>
          <span>精选住宿推荐</span>
        </div>
        <div class="cta-feature">
          <span class="feature-icon">💰</span>
          <span>预算智能优化</span>
        </div>
      </div>
      <div class="cta-actions">
        <button class="cta-primary" @click="$emit('start')">
          <span>立即开始规划</span>
          <span class="btn-arrow">→</span>
        </button>
        <button class="cta-secondary" @click="$emit('demo')">
          <span>观看演示</span>
          <span class="btn-icon">▶</span>
        </button>
      </div>
      <div class="cta-stats">
        <div class="stat-item">
          <span class="stat-value">50K+</span>
          <span class="stat-label">行程已规划</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-value">98%</span>
          <span class="stat-label">满意度</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-value">200+</span>
          <span class="stat-label">目的地</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineEmits(['start', 'demo'])
</script>

<style scoped>
.cta-section {
  position: relative;
  padding: 80px 40px;
  background: linear-gradient(
    135deg,
    rgba(99, 102, 241, 0.2) 0%,
    rgba(139, 92, 246, 0.2) 50%,
    rgba(217, 70, 239, 0.2) 100%
  );
  border-radius: 32px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.cta-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(
    circle at center,
    rgba(139, 92, 246, 0.3) 0%,
    transparent 50%
  );
  animation: rotate-glow 20s linear infinite;
}

@keyframes rotate-glow {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.cta-content {
  position: relative;
  z-index: 1;
  text-align: center;
}

.cta-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 24px;
}

.cta-title {
  font-size: 2.5rem;
  font-weight: 800;
  color: white;
  margin-bottom: 16px;
  line-height: 1.3;
}

.cta-title .highlight {
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.cta-desc {
  font-size: 1.1rem;
  color: rgba(255, 255, 255, 0.75);
  max-width: 600px;
  margin: 0 auto 32px;
  line-height: 1.7;
}

.cta-features {
  display: flex;
  justify-content: center;
  gap: 32px;
  margin-bottom: 40px;
  flex-wrap: wrap;
}

.cta-feature {
  display: flex;
  align-items: center;
  gap: 10px;
  color: rgba(255, 255, 255, 0.85);
  font-size: 0.95rem;
}

.feature-icon {
  font-size: 1.25rem;
}

.cta-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 48px;
  flex-wrap: wrap;
}

.cta-primary {
  padding: 18px 36px;
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  border: none;
  border-radius: 14px;
  color: #1a1a2e;
  font-size: 1.1rem;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  transition: all 0.3s ease;
}

.cta-primary:hover {
  transform: translateY(-3px);
  box-shadow: 0 15px 30px rgba(251, 191, 36, 0.4);
}

.btn-arrow {
  transition: transform 0.3s ease;
}

.cta-primary:hover .btn-arrow {
  transform: translateX(4px);
}

.cta-secondary {
  padding: 18px 28px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 14px;
  color: white;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  transition: all 0.3s ease;
}

.cta-secondary:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-2px);
}

.cta-stats {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: white;
}

.stat-label {
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.6);
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
}

@media (max-width: 768px) {
  .cta-title {
    font-size: 1.75rem;
  }

  .cta-features {
    gap: 16px;
  }

  .stat-divider {
    display: none;
  }
}
</style>
