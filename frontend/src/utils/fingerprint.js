import { getFingerprint } from '@guardhivefraudshield/device-fingerprint'

const COOKIE_NAME = 'visitor_id'
const COOKIE_EXPIRY_DAYS = 365 // Cookie 有效期1年
let visitorId = null

/**
 * 设置 Cookie
 * @param {string} name - Cookie 名称
 * @param {string} value - Cookie 值
 * @param {number} days - 过期天数
 */
function setCookie(name, value, days) {
  const expires = new Date(Date.now() + days * 864e5).toUTCString()
  document.cookie = `${name}=${encodeURIComponent(value)}; expires=${expires}; path=/; SameSite=Lax`
}

/**
 * 获取 Cookie
 * @param {string} name - Cookie 名称
 * @returns {string|null} Cookie 值
 */
function getCookie(name) {
  const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'))
  return match ? decodeURIComponent(match[2]) : null
}

/**
 * 初始化设备指纹并生成访客ID（优先从 Cookie 读取）
 * @returns {Promise<string>} 访客设备指纹ID
 */
export async function getVisitorId() {
  // 如果内存中已有，直接返回
  if (visitorId) {
    return visitorId
  }

  // 尝试从 Cookie 读取
  const cachedId = getCookie(COOKIE_NAME)
  if (cachedId) {
    visitorId = cachedId
    console.log('[Fingerprint] Visitor ID loaded from cookie:', visitorId)
    return visitorId
  }

  try {
    // 生成新的设备指纹
    const result = await getFingerprint()
    visitorId = result.hash || result.fingerprint || generateFallbackId()
    
    // 保存到 Cookie
    setCookie(COOKIE_NAME, visitorId, COOKIE_EXPIRY_DAYS)
    
    console.log('[Fingerprint] New visitor ID generated:', visitorId)
    return visitorId
  } catch (error) {
    console.error('[Fingerprint] Failed to generate fingerprint:', error)
    // 降级方案：生成 UUID
    visitorId = generateFallbackId()
    setCookie(COOKIE_NAME, visitorId, COOKIE_EXPIRY_DAYS)
    console.warn('[Fingerprint] Using fallback ID:', visitorId)
    return visitorId
  }
}

/**
 * 生成降级的UUID（当指纹库失败时使用）
 * @returns {string} UUID v4
 */
function generateFallbackId() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0
    const v = c === 'x' ? r : (r & 0x3 | 0x8)
    return v.toString(16)
  })
}

/**
 * 清除缓存的访客ID和Cookie（用于测试或重新生成）
 */
export function clearVisitorId() {
  visitorId = null
  // 清除 Cookie
  document.cookie = `${COOKIE_NAME}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`
}
