import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import relativeTime from 'dayjs/plugin/relativeTime'

dayjs.locale('zh-cn')
dayjs.extend(relativeTime)

/**
 * 格式化日期
 * @param {string|Date} date 日期
 * @param {string} format 格式
 * @returns {string}
 */
export function formatDate(date, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!date) return ''
  return dayjs(date).format(format)
}

/**
 * 获取相对时间
 * @param {string|Date} date 日期
 * @returns {string}
 */
export function fromNow(date) {
  if (!date) return ''
  return dayjs(date).fromNow()
}

/**
 * 格式化数字（添加千分位）
 * @param {number} num 数字
 * @returns {string}
 */
export function formatNumber(num) {
  if (num === null || num === undefined) return '0'
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

/**
 * 截断文本
 * @param {string} text 文本
 * @param {number} length 长度
 * @returns {string}
 */
export function truncate(text, length = 100) {
  if (!text) return ''
  if (text.length <= length) return text
  return text.slice(0, length) + '...'
}

/**
 * 获取阅读时间
 * @param {string} content 内容
 * @param {number} wordsPerMinute 每分钟字数
 * @returns {string}
 */
export function getReadTime(content, wordsPerMinute = 300) {
  if (!content) return '1 分钟'
  const words = content.length
  const minutes = Math.ceil(words / wordsPerMinute)
  return `${minutes} 分钟`
}
