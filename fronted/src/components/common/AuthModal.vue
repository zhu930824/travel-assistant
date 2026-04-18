<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="modal-overlay" @click.self="handleClose">
        <div class="modal-container">
          <button class="modal-close" @click="handleClose">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 6L6 18M6 6l12 12"/>
            </svg>
          </button>

          <!-- 手机号验证码登录 -->
          <template v-if="!showWechat">
            <div class="modal-header">
              <h2>登录 / 注册</h2>
              <p>手机号验证即自动注册，开启智能旅行</p>
            </div>

            <form class="modal-form" @submit.prevent="handleSmsLogin">
              <div class="form-group">
                <label>手机号</label>
                <input
                  v-model="form.phone"
                  type="tel"
                  placeholder="请输入手机号"
                  maxlength="11"
                  autocomplete="tel"
                />
              </div>

              <div class="form-group">
                <label>验证码</label>
                <div class="code-input-row">
                  <input
                    v-model="form.code"
                    type="text"
                    placeholder="请输入验证码"
                    maxlength="6"
                    autocomplete="one-time-code"
                  />
                  <button
                    type="button"
                    class="code-btn"
                    :disabled="countdown > 0 || sendingCode"
                    @click="handleSendCode"
                  >
                    <span v-if="sendingCode" class="mini-spinner"></span>
                    <span v-else-if="countdown > 0">{{ countdown }}s</span>
                    <span v-else>获取验证码</span>
                  </button>
                </div>
                <div v-if="mockCode" class="mock-code-tip">
                  验证码: {{ mockCode }}（模拟模式）
                </div>
              </div>

              <div v-if="errorMsg" class="error-message">{{ errorMsg }}</div>

              <button type="submit" class="submit-btn" :disabled="loading">
                <span v-if="loading" class="loading-spinner"></span>
                <span v-else>登录 / 注册</span>
              </button>
            </form>

            <div class="divider-section">
              <span class="divider-line"></span>
              <span class="divider-text">或</span>
              <span class="divider-line"></span>
            </div>

            <button class="wechat-btn" @click="showWechatLogin">
              <svg class="wechat-icon" viewBox="0 0 24 24" fill="currentColor">
                <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 01.213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 00.167-.054l1.903-1.114a.864.864 0 01.717-.098 10.16 10.16 0 002.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.153-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 01-1.162 1.178A1.17 1.17 0 014.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 01-1.162 1.178 1.17 1.17 0 01-1.162-1.178c0-.651.52-1.18 1.162-1.18zm3.636 4.35c-2.084 0-3.986.834-5.326 2.16-1.24 1.228-1.976 2.86-1.976 4.57 0 .594.085 1.175.247 1.728.863 2.916 3.558 4.501 6.564 4.501.67 0 1.32-.086 1.94-.244a.522.522 0 01.436.06l1.284.752a.198.198 0 00.102.033.18.18 0 00.177-.18c0-.043-.017-.087-.029-.13l-.264-1.003a.357.357 0 01.13-.403c1.326-.967 2.236-2.297 2.236-3.864 0-3.34-2.792-5.97-5.519-5.97zm-1.736 3.09c.39 0 .707.322.707.718a.713.713 0 01-.707.718.713.713 0 01-.707-.718c0-.396.317-.718.707-.718zm3.472 0c.39 0 .707.322.707.718a.713.713 0 01-.707.718.713.713 0 01-.707-.718c0-.396.317-.718.707-.718z"/>
              </svg>
              <span>微信扫码登录</span>
            </button>
          </template>

          <!-- 微信扫码登录 -->
          <template v-else>
            <div class="modal-header">
              <h2>微信扫码登录</h2>
              <p>请使用微信扫描二维码登录</p>
            </div>

            <div class="wechat-qrcode-section">
              <div class="qrcode-wrapper" v-if="!wechatLoading">
                <canvas ref="wechatQrCanvas"></canvas>
              </div>
              <div class="qrcode-loading" v-else>
                <div class="loading-spinner"></div>
                <p>正在获取二维码...</p>
              </div>
              <p class="qrcode-tip">打开微信扫一扫，快速登录</p>
            </div>

            <div v-if="wechatErrorMsg" class="error-message">{{ wechatErrorMsg }}</div>

            <button class="back-phone-btn" @click="showWechat = false">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                <path d="M19 12H5M12 19l-7-7 7-7"/>
              </svg>
              返回手机号登录
            </button>
          </template>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, reactive, watch, nextTick, onUnmounted } from 'vue'
import { authApi } from '../../services/api'
import QRCode from 'qrcode'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close', 'login-success'])

const loading = ref(false)
const sendingCode = ref(false)
const errorMsg = ref('')
const countdown = ref(0)
const mockCode = ref('')

// 微信登录相关
const showWechat = ref(false)
const wechatLoading = ref(false)
const wechatErrorMsg = ref('')
const wechatQrCanvas = ref(null)
const wechatState = ref('')
let wechatCheckInterval = null

const form = reactive({
  phone: '',
  code: ''
})

let countdownTimer = null

// 重置表单
watch(() => props.visible, (newVal) => {
  if (newVal) {
    resetForm()
  }
})

function resetForm() {
  form.phone = ''
  form.code = ''
  errorMsg.value = ''
  mockCode.value = ''
  countdown.value = 0
  showWechat.value = false
  wechatErrorMsg.value = ''
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
  if (wechatCheckInterval) {
    clearInterval(wechatCheckInterval)
    wechatCheckInterval = null
  }
}

function handleClose() {
  emit('close')
}

// 发送验证码
async function handleSendCode() {
  errorMsg.value = ''

  if (!form.phone.trim()) {
    errorMsg.value = '请输入手机号'
    return
  }

  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    errorMsg.value = '手机号格式不正确'
    return
  }

  sendingCode.value = true
  try {
    const data = await authApi.sendSmsCode(form.phone)

    // 模拟模式下显示验证码
    if (data.mockCode) {
      mockCode.value = data.mockCode
    }

    // 开始倒计时
    countdown.value = 60
    countdownTimer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(countdownTimer)
        countdownTimer = null
      }
    }, 1000)
  } catch (error) {
    errorMsg.value = error.message || '发送验证码失败'
  } finally {
    sendingCode.value = false
  }
}

// 手机号验证码登录
async function handleSmsLogin() {
  errorMsg.value = ''

  if (!form.phone.trim()) {
    errorMsg.value = '请输入手机号'
    return
  }

  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    errorMsg.value = '手机号格式不正确'
    return
  }

  if (!form.code.trim()) {
    errorMsg.value = '请输入验证码'
    return
  }

  if (!/^\d{6}$/.test(form.code)) {
    errorMsg.value = '验证码格式不正确'
    return
  }

  loading.value = true
  try {
    const data = await authApi.smsLogin(form.phone, form.code)
    // 保存token
    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(data))
    emit('login-success', data)
    handleClose()
  } catch (error) {
    errorMsg.value = error.message || '登录失败，请重试'
  } finally {
    loading.value = false
  }
}

// 显示微信登录
async function showWechatLogin() {
  showWechat.value = true
  wechatLoading.value = true
  wechatErrorMsg.value = ''

  try {
    const data = await authApi.getWechatQrcode()
    wechatState.value = data.state

    await nextTick()

    // 生成二维码
    if (wechatQrCanvas.value && data.url) {
      await QRCode.toCanvas(wechatQrCanvas.value, data.url, {
        width: 200,
        margin: 2,
        color: {
          dark: '#000000',
          light: '#ffffff'
        }
      })
    }

    // 开始轮询检查登录状态
    startWechatPolling()
  } catch (error) {
    wechatErrorMsg.value = error.message || '获取微信二维码失败'
  } finally {
    wechatLoading.value = false
  }
}

// 轮询检查微信登录状态
function startWechatPolling() {
  if (wechatCheckInterval) {
    clearInterval(wechatCheckInterval)
  }

  wechatCheckInterval = setInterval(async () => {
    try {
      const data = await authApi.checkWechatLogin(wechatState.value)

      if (data.loggedIn) {
        // 登录成功
        clearInterval(wechatCheckInterval)
        wechatCheckInterval = null

        localStorage.setItem('token', data.token)
        const userInfo = {
          token: data.token,
          userId: data.userId,
          username: data.username,
          nickname: data.nickname,
          avatar: data.avatar,
          memberLevel: data.memberLevel,
          memberLevelName: data.memberLevelName,
        }
        localStorage.setItem('userInfo', JSON.stringify(userInfo))
        emit('login-success', userInfo)
        handleClose()
      }
    } catch (error) {
      // 状态过期或其他错误
      clearInterval(wechatCheckInterval)
      wechatCheckInterval = null
      wechatErrorMsg.value = error.message || '微信登录已过期，请重新获取二维码'
    }
  }, 3000)
}

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
  if (wechatCheckInterval) {
    clearInterval(wechatCheckInterval)
  }
})
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-container {
  position: relative;
  width: 100%;
  max-width: 420px;
  background: linear-gradient(135deg, rgba(30, 30, 40, 0.95), rgba(20, 20, 30, 0.98));
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.5);
}

.modal-close {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.05);
  border: none;
  border-radius: 10px;
  color: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.modal-close:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.modal-close svg {
  width: 18px;
  height: 18px;
}

.modal-header {
  text-align: center;
  margin-bottom: 32px;
}

.modal-header h2 {
  font-size: 1.75rem;
  font-weight: 700;
  color: #fff;
  margin-bottom: 8px;
}

.modal-header p {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.5);
}

.modal-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 0.9rem;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.8);
}

.form-group input {
  padding: 14px 18px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  font-size: 1rem;
  color: #fff;
  transition: all 0.2s;
}

.form-group input::placeholder {
  color: rgba(255, 255, 255, 0.3);
}

.form-group input:focus {
  outline: none;
  border-color: rgba(99, 102, 241, 0.5);
  background: rgba(255, 255, 255, 0.08);
}

/* 验证码输入行 */
.code-input-row {
  display: flex;
  gap: 12px;
}

.code-input-row input {
  flex: 1;
}

.code-btn {
  flex-shrink: 0;
  padding: 14px 16px;
  background: rgba(99, 102, 241, 0.2);
  border: 1px solid rgba(99, 102, 241, 0.3);
  border-radius: 12px;
  color: #a5b4fc;
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  min-width: 110px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.code-btn:hover:not(:disabled) {
  background: rgba(99, 102, 241, 0.3);
  border-color: rgba(99, 102, 241, 0.5);
}

.code-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.mock-code-tip {
  padding: 8px 12px;
  background: rgba(34, 197, 94, 0.1);
  border: 1px solid rgba(34, 197, 94, 0.2);
  border-radius: 8px;
  font-size: 0.8rem;
  color: #86efac;
  text-align: center;
}

.error-message {
  padding: 12px 16px;
  background: rgba(239, 68, 68, 0.15);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 10px;
  font-size: 0.9rem;
  color: #fca5a5;
}

.submit-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border: none;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  color: #fff;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 8px;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(99, 102, 241, 0.4);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* 分割线 */
.divider-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin: 24px 0;
}

.divider-line {
  flex: 1;
  height: 1px;
  background: rgba(255, 255, 255, 0.08);
}

.divider-text {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.3);
}

/* 微信登录按钮 */
.wechat-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 14px;
  background: rgba(7, 193, 96, 0.15);
  border: 1px solid rgba(7, 193, 96, 0.3);
  border-radius: 12px;
  color: #07c160;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.wechat-btn:hover {
  background: rgba(7, 193, 96, 0.25);
  border-color: rgba(7, 193, 96, 0.5);
  transform: translateY(-1px);
}

.wechat-icon {
  width: 22px;
  height: 22px;
}

/* 微信二维码区域 */
.wechat-qrcode-section {
  text-align: center;
  padding: 20px 0;
}

.qrcode-wrapper {
  display: inline-block;
  padding: 16px;
  background: #fff;
  border-radius: 16px;
  border: 3px solid #07c160;
}

.qrcode-wrapper canvas {
  display: block;
}

.qrcode-loading {
  padding: 40px 0;
  color: rgba(255, 255, 255, 0.5);
}

.qrcode-loading .loading-spinner {
  margin: 0 auto 16px;
}

.qrcode-tip {
  margin-top: 16px;
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.5);
}

/* 返回手机号登录 */
.back-phone-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s;
  margin-top: 16px;
}

.back-phone-btn:hover {
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
}

/* Loading Spinners */
.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.mini-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(165, 179, 252, 0.3);
  border-top-color: #a5b4fc;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.qrcode-loading .loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(255, 255, 255, 0.1);
  border-top-color: #6366f1;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Transition */
.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-container,
.modal-leave-to .modal-container {
  transform: scale(0.95) translateY(20px);
}
</style>
