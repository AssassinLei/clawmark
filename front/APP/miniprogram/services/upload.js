import { env } from '../config/env'
import { getToken } from '../utils/auth'

function parseResponse(dataText) {
  if (!dataText) return {}
  if (typeof dataText === 'object') return dataText
  try {
    return JSON.parse(dataText)
  } catch (e) {
    throw new Error('上传响应解析失败')
  }
}

export const uploadApi = {
  uploadFile({ filePath, imageType = 'original' }) {
    return new Promise((resolve, reject) => {
      const token = getToken()
      wx.uploadFile({
        url: `${env.apiBaseUrl}/upload/file`,
        filePath,
        name: 'file',
        formData: {
          image_type: imageType
        },
        header: {
          ...(token ? { Authorization: `Bearer ${token}` } : {})
        },
        success: (res) => {
          const body = parseResponse(res.data)
          if (!body || body.code !== 0) {
            reject(new Error((body && body.message) || (res && res.errMsg) || '上传失败'))
            return
          }

          const data = body.data || {}
          resolve({
            fileUrl: data.file_url || data.fileUrl || '',
            imageType: data.image_type || data.imageType || imageType,
            fileSize: data.file_size || data.fileSize || 0
          })
        },
        fail: (err) => reject(err)
      })
    })
  }
}
