import { Client } from '@stomp/stompjs'
import { getOrCreateClientId } from '@/utils/clientId'
import { usePresenceStore } from '@/stores/presence'

const isDev = import.meta.env.DEV
let stompClient = null
let isInitialized = false
let reconnectAttempt = 0
const MAX_RECONNECT_DELAY = 30000
const BASE_RECONNECT_DELAY = 1000

function buildBrokerUrl() {
  const protocol = window.location.protocol
  const wsProtocol = protocol === 'https:' ? 'wss://' : 'ws://'
  return `${wsProtocol}${window.location.host}/ws`
}

function scheduleReconnect() {
  if (reconnectAttempt >= 10) {
    console.warn('[Presence] Max reconnect attempts reached, stopping')
    return
  }

  const delay = Math.min(BASE_RECONNECT_DELAY * Math.pow(2, reconnectAttempt), MAX_RECONNECT_DELAY)
  reconnectAttempt++

  if (isDev) {
    console.log(`[Presence] Reconnecting in ${delay}ms (attempt ${reconnectAttempt})`)
  }

  setTimeout(() => {
    connect()
  }, delay)
}

function connect() {
  if (stompClient?.connected) {
    return
  }

  const brokerUrl = buildBrokerUrl()
  if (isDev) {
    console.log('[Presence] Connecting to:', brokerUrl)
  }

  const clientId = getOrCreateClientId()

  stompClient = new Client({
    brokerURL: brokerUrl,
    connectHeaders: {
      clientId: clientId
    },
    heartbeatIncoming: 30000,
    heartbeatOutgoing: 30000,
    reconnectDelay: 0,
    debug: isDev ? (str) => console.log('[STOMP]', str) : () => {},
    onConnect: (frame) => {
      if (isDev) {
        console.log('[Presence] Connected:', frame)
      }
      reconnectAttempt = 0

      const presenceStore = usePresenceStore()
      presenceStore.setConnected(true)

      stompClient.subscribe('/topic/online-count', (message) => {
        try {
          const data = JSON.parse(message.body)
          const count = typeof data.count === 'number' ? data.count : parseInt(data.count, 10)
          if (!isNaN(count)) {
            presenceStore.setOnlineCount(count)
          }
        } catch (e) {
          console.error('[Presence] Failed to parse online count:', e)
        }
      })

      stompClient.publish({
        destination: '/app/presence/count',
        body: JSON.stringify({})
      })
    },
    onDisconnect: (frame) => {
      if (isDev) {
        console.log('[Presence] Disconnected:', frame)
      }
      const presenceStore = usePresenceStore()
      presenceStore.setConnected(false)
      presenceStore.setOnlineCount(0)

      if (!stompClient?.connected) {
        scheduleReconnect()
      }
    },
    onStompError: (frame) => {
      console.error('[Presence] STOMP error:', frame.headers['message'], frame.body)
    },
    onWebSocketError: (event) => {
      console.error('[Presence] WebSocket error:', event)
    }
  })

  stompClient.activate()
}

function disconnect() {
  if (stompClient?.connected) {
    if (isDev) {
      console.log('[Presence] Sending DISCONNECT frame')
    }
    stompClient.deactivate()
  }
  stompClient = null
  isInitialized = false
  reconnectAttempt = 0
}

function handlePageHide() {
  if (stompClient?.connected) {
    navigator.sendBeacon('/ws/disconnect', '')
    stompClient.deactivate()
  }
}

export function initPresence() {
  if (isDev && isInitialized) {
    console.log('[Presence] Already initialized, skipping')
    return
  }

  isInitialized = true
  connect()

  window.addEventListener('pagehide', handlePageHide)
  window.addEventListener('beforeunload', handlePageHide)
}

export function destroyPresence() {
  window.removeEventListener('pagehide', handlePageHide)
  window.removeEventListener('beforeunload', handlePageHide)
  disconnect()
}