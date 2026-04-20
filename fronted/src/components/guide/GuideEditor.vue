<template>
  <div class="editor-container">
    <!-- Background Effects -->
    <div class="bg-effects">
      <div class="gradient-orb gradient-orb-1"></div>
      <div class="gradient-orb gradient-orb-2"></div>
      <div class="grid-overlay"></div>
    </div>

    <!-- Header -->
    <header class="app-header">
      <div class="header-inner">
        <router-link to="/" class="logo">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 16V8a2 2 0 00-1-1.73l-7-4a2 2 0 00-2 0l-7 4A2 2 0 003 8v8a2 2 0 001 1.73l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z"/>
            </svg>
          </div>
          <span class="logo-text">TravelAI</span>
        </router-link>

        <nav class="nav-links">
          <router-link to="/">首页</router-link>
          <router-link to="/plan">规划</router-link>
        </nav>

        <div class="header-actions">
          <template v-if="isLoggedIn">
            <router-link to="/profile" class="user-link">
              <div class="user-avatar">{{ userInfo?.username?.charAt(0).toUpperCase() }}</div>
              <span class="user-name">{{ userInfo?.username }}</span>
            </router-link>
            <button class="btn-ghost" @click="handleLogout">退出</button>
          </template>
          <template v-else>
            <button class="btn-ghost" @click="$router.push('/')">登录</button>
          </template>
        </div>
      </div>
    </header>

    <!-- Page Header -->
    <div class="page-header">
      <h1 class="page-title">发布攻略</h1>
      <p class="page-desc">分享你的旅行经验，帮助更多旅行者</p>
    </div>

    <form class="editor-form" @submit.prevent="handleSubmit">
      <div class="form-group">
        <label class="form-label">攻略标题</label>
        <input
          v-model="form.title"
          type="text"
          class="form-input"
          placeholder="给你的攻略起个吸引人的标题"
          maxlength="100"
          required
        />
      </div>

      <div class="form-group">
        <label class="form-label">目的地</label>
        <input
          v-model="form.destination"
          type="text"
          class="form-input"
          placeholder="攻略目的地，如：东京、巴黎"
          maxlength="100"
          required
        />
      </div>

      <div class="form-group">
        <label class="form-label">封面图片</label>
        <div class="cover-upload">
          <div class="cover-preview" v-if="form.coverImage">
            <img :src="form.coverImage" alt="封面" />
            <button type="button" class="remove-btn" @click="form.coverImage = ''">×</button>
          </div>
          <label class="upload-area" v-else>
            <input type="file" accept="image/*" @change="handleCoverUpload" hidden />
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/>
            </svg>
            <span>上传封面图片</span>
          </label>
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">攻略内容</label>
        <div class="editor-wrapper">
          <div ref="editorRef" class="quill-editor"></div>
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">标签</label>
        <div class="tags-input">
          <div class="tag-list">
            <span v-for="(tag, index) in form.tags" :key="index" class="tag-item">
              {{ tag }}
              <button type="button" @click="removeTag(index)">×</button>
            </span>
          </div>
          <input
            v-model="tagInput"
            type="text"
            class="tag-input"
            placeholder="输入标签后按回车添加"
            @keydown.enter.prevent="addTag"
          />
        </div>
      </div>

      <div class="form-actions">
        <button type="button" class="btn-save" @click="saveDraft">保存草稿</button>
        <button type="submit" class="btn-publish" :disabled="submitting">
          {{ submitting ? '发布中...' : '发布攻略' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { guideApi } from '@/services/api'

const router = useRouter()
const editorRef = ref(null)
const tagInput = ref('')
const submitting = ref(false)
let quillInstance = null

const isLoggedIn = ref(false)
const userInfo = ref(null)

const form = ref({
  title: '',
  destination: '',
  coverImage: '',
  content: '',
  tags: [],
  status: 1
})

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/')
}

const addTag = () => {
  const tag = tagInput.value.trim()
  if (tag && !form.value.tags.includes(tag) && form.value.tags.length < 5) {
    form.value.tags.push(tag)
    tagInput.value = ''
  }
}

const removeTag = (index) => {
  form.value.tags.splice(index, 1)
}

const handleCoverUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  try {
    const formData = new FormData()
    formData.append('file', file)
    const result = await guideApi.uploadImage(formData)
    form.value.coverImage = result.url
  } catch (error) {
    console.error('上传封面失败:', error)
    alert('上传失败，请重试')
  }
}

const saveDraft = () => {
  form.value.status = 0
  handleSubmit()
}

const handleSubmit = async () => {
  if (!form.value.title || !form.value.destination) {
    alert('请填写标题和目的地')
    return
  }

  if (quillInstance) {
    form.value.content = quillInstance.root.innerHTML
  }

  if (!form.value.content || form.value.content === '<p><br></p>') {
    alert('请填写攻略内容')
    return
  }

  submitting.value = true
  try {
    await guideApi.createGuide(form.value)
    alert('发布成功！')
    router.push('/profile')
  } catch (error) {
    console.error('发布失败:', error)
    alert('发布失败：' + error.message)
  } finally {
    submitting.value = false
  }
}

const initQuill = async () => {
  if (typeof window !== 'undefined' && editorRef.value) {
    const Quill = (await import('quill')).default
    await import('quill/dist/quill.snow.css')

    quillInstance = new Quill(editorRef.value, {
      theme: 'snow',
      placeholder: '写下你的旅行攻略...',
      modules: {
        toolbar: [
          [{ header: [1, 2, 3, false] }],
          ['bold', 'italic', 'underline', 'strike'],
          [{ color: [] }, { background: [] }],
          [{ list: 'ordered' }, { list: 'bullet' }],
          [{ align: [] }],
          ['link', 'image'],
          ['clean']
        ]
      }
    })

    quillInstance.getModule('toolbar').addHandler('image', async () => {
      const input = document.createElement('input')
      input.setAttribute('type', 'file')
      input.setAttribute('accept', 'image/*')
      input.click()

      input.onchange = async () => {
        const file = input.files[0]
        if (!file) return

        try {
          const formData = new FormData()
          formData.append('file', file)
          const result = await guideApi.uploadImage(formData)
          const range = quillInstance.getSelection(true)
          quillInstance.insertEmbed(range.index, 'image', result.url)
        } catch (error) {
          console.error('上传图片失败:', error)
        }
      }
    })
  }
}

onMounted(() => {
  const savedUserInfo = localStorage.getItem('userInfo')
  if (savedUserInfo) {
    try {
      userInfo.value = JSON.parse(savedUserInfo)
      isLoggedIn.value = true
    } catch (e) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
  initQuill()
})

onBeforeUnmount(() => {
  if (quillInstance) {
    quillInstance = null
  }
})
</script>

<style scoped>
.editor-container {
  min-height: 100vh;
  background: #0a0a0f;
  color: #fff;
  position: relative;
  overflow-x: hidden;
}

/* Background Effects */
.bg-effects {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(120px);
  opacity: 0.4;
}

.gradient-orb-1 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, #4f46e5 0%, transparent 70%);
  top: -150px;
  right: -150px;
}

.gradient-orb-2 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, #7c3aed 0%, transparent 70%);
  bottom: -100px;
  left: -100px;
}

.grid-overlay {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255,255,255,0.02) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,0.02) 1px, transparent 1px);
  background-size: 60px 60px;
}

/* Header */
.app-header {
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

.nav-links {
  display: flex;
  gap: 32px;
}

.nav-links a {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  transition: color 0.2s;
}

.nav-links a:hover {
  color: #fff;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-link {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  color: #fff;
}

.user-avatar {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.85rem;
  font-weight: 600;
  color: #fff;
}

.user-name {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.9);
}

.btn-ghost {
  padding: 10px 20px;
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.9rem;
  cursor: pointer;
  transition: color 0.2s;
}

.btn-ghost:hover {
  color: #fff;
}

/* Page Header */
.page-header {
  position: relative;
  z-index: 1;
  max-width: 800px;
  margin: 0 auto;
  padding: 100px 24px 24px;
}

.page-title {
  font-size: 2rem;
  font-weight: 800;
  margin-bottom: 8px;
}

.page-desc {
  color: rgba(255, 255, 255, 0.6);
}

.editor-form {
  position: relative;
  z-index: 1;
  max-width: 800px;
  margin: 0 auto;
  padding: 0 24px 60px;
}

.form-group {
  margin-bottom: 24px;
}

.form-label {
  display: block;
  font-size: 0.9rem;
  font-weight: 600;
  margin-bottom: 8px;
  color: rgba(255, 255, 255, 0.9);
}

.form-input {
  width: 100%;
  padding: 14px 16px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: #fff;
  font-size: 1rem;
  transition: all 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: rgba(139, 92, 246, 0.5);
  background: rgba(255, 255, 255, 0.08);
}

.form-input::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

.cover-upload {
  position: relative;
}

.cover-preview {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
}

.cover-preview img {
  width: 100%;
  max-height: 300px;
  object-fit: cover;
}

.remove-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 32px;
  height: 32px;
  background: rgba(0, 0, 0, 0.6);
  border: none;
  border-radius: 50%;
  color: #fff;
  font-size: 1.25rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px;
  background: rgba(255, 255, 255, 0.03);
  border: 2px dashed rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.upload-area:hover {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(139, 92, 246, 0.3);
}

.upload-area svg {
  width: 48px;
  height: 48px;
  color: rgba(255, 255, 255, 0.3);
  margin-bottom: 12px;
}

.upload-area span {
  color: rgba(255, 255, 255, 0.5);
}

.editor-wrapper {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.quill-editor {
  min-height: 400px;
}

.tags-input {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.2), rgba(139, 92, 246, 0.2));
  border-radius: 20px;
  font-size: 0.85rem;
  color: #a5b4fc;
}

.tag-item button {
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  font-size: 1rem;
  line-height: 1;
}

.tag-input {
  flex: 1;
  min-width: 120px;
  background: transparent;
  border: none;
  color: #fff;
  font-size: 0.9rem;
  outline: none;
}

.tag-input::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

.form-actions {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
  margin-top: 32px;
}

.btn-save {
  padding: 14px 32px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  color: #fff;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-save:hover {
  background: rgba(255, 255, 255, 0.15);
}

.btn-publish {
  padding: 14px 32px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border: none;
  border-radius: 12px;
  color: #fff;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-publish:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(99, 102, 241, 0.4);
}

.btn-publish:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

:deep(.ql-toolbar.ql-snow) {
  border: none;
  border-bottom: 1px solid #e5e7eb;
  background: #f9fafb;
}

:deep(.ql-container.ql-snow) {
  border: none;
  min-height: 350px;
}

:deep(.ql-editor) {
  min-height: 350px;
  font-size: 1rem;
  line-height: 1.7;
  color: #1f2937;
}

:deep(.ql-editor.ql-blank::before) {
  color: #9ca3af;
  font-style: normal;
}

@media (max-width: 768px) {
  .nav-links {
    display: none;
  }

  .page-title {
    font-size: 1.5rem;
  }
}
</style>