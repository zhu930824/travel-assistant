// API 基础配置（留空走 Vite 代理，避免 CORS）
const API_BASE_URL = ''

// 通用请求方法
async function request(url, options = {}) {
  const token = localStorage.getItem('token')

  const defaultHeaders = {
    'Content-Type': 'application/json',
  }

  if (token) {
    defaultHeaders['Authorization'] = `Bearer ${token}`
  }

  const config = {
    ...options,
    headers: {
      ...defaultHeaders,
      ...options.headers,
    },
  }

  const response = await fetch(`${API_BASE_URL}${url}`, config)
  const data = await response.json()

  if (!response.ok || data.code !== 200) {
    throw new Error(data.message || '请求失败')
  }

  return data.data
}

// 用户相关API
export const userApi = {
  // 登录（已废弃，保留兼容）
  login(username, password) {
    return request('/api/user/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    })
  },

  // 注册（已废弃，保留兼容）
  register(username, password, nickname) {
    return request('/api/user/register', {
      method: 'POST',
      body: JSON.stringify({ username, password, nickname }),
    })
  },

  // 获取用户信息
  getUserInfo() {
    return request('/api/user/info')
  },

  // 检查是否可以规划
  checkCanPlan() {
    return request('/api/user/can-plan')
  },

  // 获取规划记录列表
  getPlanRecords(limit = 10) {
    return request(`/api/user/plan-records?limit=${limit}`)
  },

  // 升级会员
  upgradeMember(memberLevel, months = 1) {
    return request('/api/user/upgrade', {
      method: 'POST',
      body: JSON.stringify({ memberLevel, months }),
    })
  },
}

// 认证相关API（短信验证码 + 微信登录）
export const authApi = {
  // 发送短信验证码
  sendSmsCode(phone) {
    return request('/api/auth/sms/send', {
      method: 'POST',
      body: JSON.stringify({ phone }),
    })
  },

  // 手机号验证码登录
  smsLogin(phone, code) {
    return request('/api/auth/sms/login', {
      method: 'POST',
      body: JSON.stringify({ phone, code }),
    })
  },

  // 获取微信登录二维码
  getWechatQrcode() {
    return request('/api/auth/wechat/qrcode')
  },

  // 检查微信登录状态
  checkWechatLogin(state) {
    return request(`/api/auth/wechat/check?state=${state}`)
  },

  // 微信登录（通过code）
  wechatLogin(code, state) {
    return request('/api/auth/wechat/login', {
      method: 'POST',
      body: JSON.stringify({ code, state }),
    })
  },
}

// 支付相关API
export const paymentApi = {
  // 获取可用的支付方式列表
  getPaymentMethods() {
    return request('/api/payment/methods')
  },

  // 创建支付订单
  createOrder(memberLevel, months = 1, paymentMethod = 'WECHAT') {
    return request('/api/payment/create', {
      method: 'POST',
      body: JSON.stringify({ memberLevel, months, paymentMethod }),
    })
  },

  // 查询订单状态
  queryOrder(orderNo) {
    return request(`/api/payment/status/${orderNo}`)
  },

  // 获取用户订单列表
  getUserOrders() {
    return request('/api/payment/orders')
  },
}

// 攻略相关API
export const guideApi = {
  // 获取推荐攻略列表
  getRecommendList(page = 0, size = 10) {
    return request(`/api/guide/recommend?page=${page}&size=${size}`)
  },

  // 获取攻略详情
  getGuideDetail(id) {
    return request(`/api/guide/${id}`)
  },

  // 创建攻略
  createGuide(data) {
    return request('/api/guide', {
      method: 'POST',
      body: JSON.stringify(data),
    })
  },

  // 点赞攻略
  likeGuide(id) {
    return request(`/api/guide/${id}/like`, {
      method: 'POST',
    })
  },

  // 收藏攻略
  favoriteGuide(id) {
    return request(`/api/guide/${id}/favorite`, {
      method: 'POST',
    })
  },

  // 获取我的攻略
  getMyGuides(page = 0, size = 10) {
    return request(`/api/guide/my?page=${page}&size=${size}`)
  },

  // 删除攻略
  deleteGuide(id) {
    return request(`/api/guide/${id}`, {
      method: 'DELETE',
    })
  },

  // 上传图片
  uploadImage(formData) {
    const token = localStorage.getItem('token')
    return fetch(`${API_BASE_URL}/api/upload/image`, {
      method: 'POST',
      headers: token ? { 'Authorization': `Bearer ${token}` } : {},
      body: formData,
    }).then(res => res.json()).then(data => {
      if (data.code !== 200) {
        throw new Error(data.message || '上传失败')
      }
      return data.data
    })
  },
}
