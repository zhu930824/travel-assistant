import { ref, onUnmounted } from 'vue'

/**
 * SSE连接Hook
 * 处理与后端的Server-Sent Events连接
 */
export function useEventSource() {
  const eventSource = ref(null)
  const isConnected = ref(false)
  const error = ref(null)

  const contentAccumulators = {
    attraction: '',
    hotel: '',
    transport: '',
    supervisor: ''
  }

  const connect = (url, onMessage, onComplete, onError) => {
    // 关闭已有连接
    if (eventSource.value) {
      eventSource.value.close()
    }

    // 重置累积内容
    Object.keys(contentAccumulators).forEach(key => {
      contentAccumulators[key] = ''
    })

    eventSource.value = new EventSource(url)
    isConnected.value = true
    error.value = null

    // 会话开始事件
    eventSource.value.addEventListener('session_start', (event) => {
      console.log('Session started:', JSON.parse(event.data))
    })

    // 消息事件
    eventSource.value.addEventListener('message', (event) => {
      try {
        const data = JSON.parse(event.data)

        if (data.hasContent && data.stage) {
          // 累加内容
          contentAccumulators[data.stage] += data.content

          // 回调通知
          onMessage?.({
            stage: data.stage,
            content: contentAccumulators[data.stage],
            delta: data.content,
            contentType: data.contentType,
            agent: data.agent,
            node: data.node
          })
        }
      } catch (e) {
        console.error('Failed to parse SSE message:', e)
      }
    })

    // 完成事件
    eventSource.value.addEventListener('complete', (event) => {
      console.log('Session complete:', JSON.parse(event.data))
      disconnect()
      onComplete?.()
    })

    // 错误事件
    eventSource.value.addEventListener('error', (event) => {
      try {
        const data = JSON.parse(event.data)
        error.value = data.error
        onError?.(data.error)
      } catch (e) {
        console.error('SSE Error:', event)
      }
    })

    // 连接错误
    eventSource.value.onerror = (err) => {
      console.error('SSE Connection Error:', err)
      error.value = 'Connection error'
      onError?.('Connection error')
      isConnected.value = false
    }
  }

  const disconnect = () => {
    if (eventSource.value) {
      eventSource.value.close()
      eventSource.value = null
    }
    isConnected.value = false
  }

  onUnmounted(() => {
    disconnect()
  })

  return {
    connect,
    disconnect,
    isConnected,
    error,
    contentAccumulators
  }
}
