/**
 * Client ID utility for presence tracking
 * Generates and persists a unique identifier per browser (shared across tabs)
 */

const CLIENT_ID_KEY = 'clientId'

/**
 * Get existing clientId from localStorage or create a new one
 * @returns {string} The client ID
 */
export function getOrCreateClientId() {
  let clientId = localStorage.getItem(CLIENT_ID_KEY)
  if (!clientId) {
    clientId = generateClientId()
    localStorage.setItem(CLIENT_ID_KEY, clientId)
  }
  return clientId
}

/**
 * Get existing clientId (read-only, returns null if not exists)
 * @returns {string|null}
 */
export function getClientId() {
  return localStorage.getItem(CLIENT_ID_KEY)
}

/**
 * Generate a unique client ID
 * @returns {string}
 */
function generateClientId() {
  // Use crypto.randomUUID() if available (modern browsers)
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  // Fallback: generate UUID-like string with Math.random()
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}
