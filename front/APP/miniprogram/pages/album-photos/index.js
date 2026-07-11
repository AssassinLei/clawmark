import { photosApi } from '../../services/photos'
import { uploadApi } from '../../services/upload'
import { usersApi } from '../../services/users'
import { ensureLogin } from '../../utils/page-guard'

const THUMBNAIL_MAX_SIZE = 200 * 1024
const UPLOAD_CONCURRENCY = 3

function todayString() {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function chooseImage() {
  return new Promise((resolve, reject) => {
    wx.chooseImage({
      count: 9,
      sizeType: ['original'],
      sourceType: ['album', 'camera'],
      success: (res) => resolve(res),
      fail: (err) => reject(err)
    })
  })
}

function getImageInfo(src) {
  return new Promise((resolve, reject) => {
    wx.getImageInfo({
      src,
      success: (res) => resolve(res),
      fail: (err) => reject(err)
    })
  })
}

function getFileInfo(filePath) {
  return new Promise((resolve) => {
    const fs = wx.getFileSystemManager()
    fs.getFileInfo({
      filePath,
      success: (res) => resolve(res),
      fail: () => resolve({ size: 0 })
    })
  })
}

function compressImage(src, quality = 35) {
  return new Promise((resolve) => {
    wx.compressImage({
      src,
      quality,
      success: (res) => resolve((res && res.tempFilePath) || src),
      fail: () => resolve(src)
    })
  })
}

async function buildThumbnailPath(originalPath) {
  const qualities = [35, 25, 15]
  let lastPath = originalPath
  for (let i = 0; i < qualities.length; i += 1) {
    const path = await compressImage(originalPath, qualities[i])
    const info = await getFileInfo(path)
    lastPath = path
    if ((Number(info.size) || 0) <= THUMBNAIL_MAX_SIZE) {
      return path
    }
  }
  return lastPath
}

function errMessage(e, fallback) {
  return (e && e.message) ? e.message : fallback
}

async function mapWithConcurrency(items, limit, handler) {
  const safeLimit = Math.max(1, Number(limit) || 1)
  const results = new Array(items.length)
  let cursor = 0

  async function worker() {
    while (true) {
      const current = cursor
      cursor += 1
      if (current >= items.length) return
      results[current] = await handler(items[current], current)
    }
  }

  const workers = []
  const workerCount = Math.min(safeLimit, items.length)
  for (let i = 0; i < workerCount; i += 1) {
    workers.push(worker())
  }
  await Promise.all(workers)
  return results
}

Page({
  data: {
    loading: false,
    loadingMore: false,
    uploading: false,
    albumId: '',
    albumTitle: '',
    albumType: '',
    shotDate: todayString(),
    year: '',
    photos: [],
    hasMore: false,
    nextCursor: '',
    selectedImages: [],
    deletingPhotoId: '',
    error: ''
  },

  onLoad(options) {
    const albumId = decodeURIComponent((options && options.albumId) || '')
    const albumTitle = decodeURIComponent((options && options.albumTitle) || '')
    const albumType = decodeURIComponent((options && options.albumType) || '')
    this.setData({
      albumId,
      albumTitle,
      albumType
    })
    wx.setNavigationBarTitle({ title: albumTitle || '相册照片' })
  },

  async onShow() {
    await this.reload()
  },

  async onPullDownRefresh() {
    await this.reload()
    wx.stopPullDownRefresh()
  },

  async onReachBottom() {
    if (this.data.loadingMore || !this.data.hasMore) return
    await this.loadPhotos(false)
  },

  onYearInput(e) {
    this.setData({ year: (e.detail.value || '').trim() })
  },

  onShotDateChange(e) {
    this.setData({ shotDate: e.detail.value || todayString() })
  },



  async onChooseImage() {
    try {
      const picked = await chooseImage()
      const paths = (picked && picked.tempFilePaths) || []
      if (!paths.length) return

      const selectedImages = await Promise.all(paths.map(async (path) => {
        const [info, file] = await Promise.all([getImageInfo(path), getFileInfo(path)])
        return {
          path,
          width: Number(info.width) || 0,
          height: Number(info.height) || 0,
          size: Number(file.size) || 0
        }
      }))

      this.setData({ selectedImages })
    } catch (e) {
      const message = e instanceof Error ? e.message : '选择图片失败'
      wx.showToast({ title: message, icon: 'none' })
    }
  },

  async onUploadPhoto() {
    if (!ensureLogin() || this.data.uploading) return
    if (!this.data.selectedImages || this.data.selectedImages.length === 0) {
      wx.showToast({ title: '请先选择照片', icon: 'none' })
      return
    }

    this.setData({ uploading: true })
    try {
      const me = await usersApi.me()
      const coupleId = me.couple_id || ''
      if (!coupleId || !this.data.albumId) {
        throw new Error('未找到情侣或相册信息')
      }

      const batchItems = await mapWithConcurrency(this.data.selectedImages, UPLOAD_CONCURRENCY, async (image) => {
        const originalPath = image.path
        const thumbnailPath = await buildThumbnailPath(originalPath)

        const originalUploaded = await uploadApi.uploadFile({
          filePath: originalPath,
          imageType: 'original'
        })

        let thumbnailUrl = originalUploaded.fileUrl
        try {
          const thumbnailUploaded = await uploadApi.uploadFile({
            filePath: thumbnailPath,
            imageType: 'thumbnail'
          })
          thumbnailUrl = thumbnailUploaded.fileUrl || thumbnailUrl
        } catch (thumbnailError) {
          console.warn('thumbnail upload failed, fallback to original', thumbnailError)
        }

        return {
          original_url: originalUploaded.fileUrl,
          thumbnail_url: thumbnailUrl,
          shot_date: this.data.shotDate,
          width: image.width || undefined,
          height: image.height || undefined
        }
      })


      try {
        await photosApi.batchCreate(coupleId, {
          album_ids: [this.data.albumId],
          photos: batchItems
        })
      } catch (createError) {
        console.error('[photo-batch-create-failed]', {
          coupleId,
          albumId: this.data.albumId,
          albumType: this.data.albumType,
          batchSize: batchItems.length,
          error: createError
        })
        throw new Error(`入库失败：${errMessage(createError, '未知错误')}`)
      }

      this.setData({ selectedImages: [] })
      wx.showToast({ title: `上传成功 ${batchItems.length} 张`, icon: 'success' })
      await this.reload()
    } catch (e) {
      const message = errMessage(e, '上传失败')
      console.error('[onUploadPhoto-failed]', e)
      wx.showToast({ title: message, icon: 'none' })
    } finally {

      this.setData({ uploading: false })
    }
  },

  async onApplyFilter() {
    await this.reload()
  },

  async onClearFilter() {
    this.setData({ year: '' })
    await this.reload()
  },

  onPreviewImage(e) {
    const src = (e.currentTarget.dataset && e.currentTarget.dataset.src) || ''
    if (!src) return
    const urls = this.data.photos.map((p) => p.original_url || p.thumbnail_url).filter(Boolean)
    if (urls.indexOf(src) < 0) {
      urls.unshift(src)
    }
    wx.previewImage({ current: src, urls })
  },

  async onDeletePhoto(e) {
    const photoId = (e.currentTarget.dataset && e.currentTarget.dataset.id) || ''
    if (!photoId || this.data.deletingPhotoId || this.data.uploading) return
    if (!ensureLogin()) return

    const confirm = await wx.showModal({
      title: '删除照片',
      content: '确认删除这张照片吗？该操作不可恢复。',
      confirmColor: '#d03050'
    })
    if (!confirm.confirm) return

    this.setData({ deletingPhotoId: photoId })
    try {
      const me = await usersApi.me()
      const coupleId = me.couple_id || ''
      if (!coupleId) {
        throw new Error('未找到情侣信息')
      }
      await photosApi.delete(coupleId, photoId)
      const nextPhotos = this.data.photos.filter((p) => p.photo_id !== photoId)
      this.setData({ photos: nextPhotos })
      wx.showToast({ title: '删除成功', icon: 'success' })
    } catch (e1) {
      const message = e1 instanceof Error ? e1.message : '删除失败'
      wx.showToast({ title: message, icon: 'none' })
    } finally {
      this.setData({ deletingPhotoId: '' })
    }
  },

  async reload() {
    this.setData({ photos: [], nextCursor: '', hasMore: false })
    await this.loadPhotos(true)
  },

  async loadPhotos(reset) {
    if (!ensureLogin()) return
    const stageKey = reset ? 'loading' : 'loadingMore'
    this.setData({ [stageKey]: true, error: '' })

    try {
      const me = await usersApi.me()
      const coupleId = me.couple_id || ''
      if (!coupleId || !this.data.albumId) {
        this.setData({ photos: [], hasMore: false, nextCursor: '' })
        return
      }

      const params = {
        album_id: this.data.albumId,
        year: this.data.year || undefined,
        cursor: reset ? '' : this.data.nextCursor,
        limit: 20
      }

      const resp = await photosApi.list(coupleId, params)
      const incoming = (resp && resp.photos) || []
      const photos = reset ? incoming : this.data.photos.concat(incoming)

      this.setData({
        photos,
        nextCursor: (resp && resp.next_cursor) || '',
        hasMore: !!(resp && resp.has_more)
      })
    } catch (e) {
      const message = e instanceof Error ? e.message : '加载失败'
      this.setData({ error: message })
      wx.showToast({ title: message, icon: 'none' })
    } finally {
      this.setData({ loading: false, loadingMore: false })
    }
  }
})
