import { ref, reactive, computed } from 'vue'
import { useEventSource } from './useEventSource'

const API_BASE_URL = '/api'

/**
 * 旅游规划Hook
 * 管理整个规划流程的状态和数据
 */
export function usePlanning() {
  const isPlanning = ref(false)
  const currentStage = ref('')
  const sessionId = ref('')

  const agentStatus = reactive({
    attraction: 'pending',
    hotel: 'pending',
    transport: 'pending',
    supervisor: 'pending'
  })

  const agentContents = reactive({
    attraction: '',
    hotel: '',
    transport: '',
    supervisor: ''
  })

  const finalPlan = ref('')
  const hasResult = ref(false)

  const { connect, disconnect } = useEventSource()

  const startPlanning = async (formData) => {
    // 重置状态
    resetState()
    isPlanning.value = true
    hasResult.value = true

    // 构建请求参数
    const params = new URLSearchParams({
      destination: formData.destination,
      days: formData.days || '3'
    })

    if (formData.budget) {
      params.append('budget', formData.budget)
    }
    if (formData.preferences) {
      params.append('preferences', formData.preferences)
    }

    // 由于EventSource只支持GET，我们需要使用fetch POST + 手动处理SSE
    try {
      // 获取token
      const token = localStorage.getItem('token')
      const headers = {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream'
      }
      if (token) {
        headers['Authorization'] = `Bearer ${token}`
      }

      const response = await fetch(`${API_BASE_URL}/travel/plan/stream`, {
        method: 'POST',
        headers,
        body: JSON.stringify({
          destination: formData.destination,
          days: parseInt(formData.days) || 3,
          budget: formData.budget || null,
          preferences: formData.preferences || null
        })
      })

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      const reader = response.body.getReader()
      const decoder = new TextDecoder()

      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()

        if (done) {
          break
        }

        buffer += decoder.decode(value, { stream: true })

        // 解析SSE事件
        const lines = buffer.split('\n')
        buffer = lines.pop() || '' // 保留最后一个不完整的行

        for (const line of lines) {
          if (line.startsWith('data:')) {
            try {
              const data = JSON.parse(line.substring(5).trim())
              handleMessage(data)
            } catch (e) {
              console.warn('Failed to parse SSE data:', line)
            }
          } else if (line.startsWith('event:')) {
            // 处理事件类型
            const eventType = line.substring(6).trim()
            if (eventType === 'complete') {
              handleComplete()
            } else if (eventType === 'error') {
              // 错误会在下一条data消息中
            }
          }
        }
      }

    } catch (err) {
      console.error('Planning error:', err)
      isPlanning.value = false
      agentStatus.supervisor = 'error'
    }
  }

  const handleMessage = (data) => {
    if (!data) return

    // 处理会话开始
    if (data.sessionId) {
      sessionId.value = data.sessionId
      return
    }

    // 处理错误
    if (data.error) {
      console.error('Agent error:', data.error)
      return
    }

    // 处理Agent输出
    if (data.stage && data.hasContent) {
      currentStage.value = data.stage

      // 更新Agent状态
      if (agentStatus[data.stage] !== 'complete') {
        agentStatus[data.stage] = 'loading'
      }

      // 更新内容（如果是delta类型，替换为累积内容）
      if (data.contentType === 'delta') {
        agentContents[data.stage] = data.content
      }

      // 如果是supervisor阶段，更新最终方案
      if (data.stage === 'supervisor') {
        finalPlan.value = data.content
      }
    }
  }

  const handleComplete = () => {
    isPlanning.value = false

    // 标记所有正在loading的Agent为完成
    Object.keys(agentStatus).forEach(key => {
      if (agentStatus[key] === 'loading') {
        agentStatus[key] = 'complete'
      }
    })

    console.log('Planning complete')
  }

  const resetState = () => {
    Object.keys(agentStatus).forEach(key => {
      agentStatus[key] = 'pending'
      agentContents[key] = ''
    })
    currentStage.value = ''
    finalPlan.value = ''
    sessionId.value = ''
  }

  const stopPlanning = () => {
    disconnect()
    isPlanning.value = false
  }

  return {
    isPlanning,
    currentStage,
    agentStatus,
    agentContents,
    finalPlan,
    hasResult,
    sessionId,
    startPlanning,
    stopPlanning,
    resetState
  }
}
