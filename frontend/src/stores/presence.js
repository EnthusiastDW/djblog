import { defineStore } from 'pinia'

export const usePresenceStore = defineStore('presence', {
  state: () => ({
    onlineCount: 0,
    connected: false
  }),

  actions: {
    setOnlineCount(count) {
      this.onlineCount = count
    },

    setConnected(isConnected) {
      this.connected = isConnected
    },

    reset() {
      this.onlineCount = 0
      this.connected = false
    }
  }
})
