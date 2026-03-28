/**
 * 本地存储工具
 */
export const storage = {
  get(key) {
    const value = localStorage.getItem(key)
    try {
      return value ? JSON.parse(value) : null
    } catch {
      return value
    }
  },

  set(key, value) {
    if (typeof value === 'object') {
      localStorage.setItem(key, JSON.stringify(value))
    } else {
      localStorage.setItem(key, value)
    }
  },

  remove(key) {
    localStorage.removeItem(key)
  },

  clear() {
    localStorage.clear()
  }
}

/**
 * Session 存储工具
 */
export const session = {
  get(key) {
    const value = sessionStorage.getItem(key)
    try {
      return value ? JSON.parse(value) : null
    } catch {
      return value
    }
  },

  set(key, value) {
    if (typeof value === 'object') {
      sessionStorage.setItem(key, JSON.stringify(value))
    } else {
      sessionStorage.setItem(key, value)
    }
  },

  remove(key) {
    sessionStorage.removeItem(key)
  },

  clear() {
    sessionStorage.clear()
  }
}
