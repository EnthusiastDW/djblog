/**
 * Cookie 工具函数
 */

/**
 * 设置 Cookie
 * @param {string} name - Cookie 名称
 * @param {string} value - Cookie 值
 * @param {number} days - 过期天数
 */
export function setCookie(name, value, days = 7) {
  const date = new Date()
  date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000))
  const expires = `expires=${date.toUTCString()}`
  document.cookie = `${name}=${encodeURIComponent(value)}; ${expires}; path=/`
}

/**
 * 获取 Cookie
 * @param {string} name - Cookie 名称
 * @returns {string|null} Cookie 值，不存在返回 null
 */
export function getCookie(name) {
  const nameEQ = `${name}=`
  const ca = document.cookie.split(';')
  for (let i = 0; i < ca.length; i++) {
    let c = ca[i]
    while (c.charAt(0) === ' ') c = c.substring(1, c.length)
    if (c.indexOf(nameEQ) === 0) {
      return decodeURIComponent(c.substring(nameEQ.length))
    }
  }
  return null
}

/**
 * 删除 Cookie
 * @param {string} name - Cookie 名称
 */
export function removeCookie(name) {
  document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/`
}
