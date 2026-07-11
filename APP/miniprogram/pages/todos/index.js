import { todosApi } from '../../services/todos'
import { usersApi } from '../../services/users'
import { ensureLogin } from '../../utils/page-guard'

const typeOptions = ['all', 'mine', 'partner', 'shared']
const statusOptions = ['all', 'pending', 'done', 'expired']
const createTypeOptions = ['mine', 'partner', 'shared']

Page({
  data: {
    loading: false,
    todos: [],
    total: 0,
    page: 1,
    pageSize: 20,
    typeOptions,
    statusOptions,
    typeIndex: 0,
    statusIndex: 0,
    coupleId: '',
    operating: false,
    error: ''
  },

  async onShow() {
    await this.loadTodos(true)
  },

  async onPullDownRefresh() {
    await this.loadTodos(true)
    wx.stopPullDownRefresh()
  },

  async onTypeChange(e) {
    this.setData({ typeIndex: Number(e.detail.value || 0), page: 1 })
    await this.loadTodos(true)
  },

  async onStatusChange(e) {
    this.setData({ statusIndex: Number(e.detail.value || 0), page: 1 })
    await this.loadTodos(true)
  },

  async onCreateTodo() {
    if (!this.data.coupleId || this.data.operating) return

    const titleModal = await wx.showModal({
      title: '新建代办',
      editable: true,
      placeholderText: '请输入代办标题（1-50字）'
    })
    if (!titleModal.confirm) return

    const title = (titleModal.content || '').trim()
    if (!title) {
      wx.showToast({ title: '请输入标题', icon: 'none' })
      return
    }

    const typeSheet = await wx.showActionSheet({
      itemList: ['mine（我的）', 'partner（派发给TA）', 'shared（共同）']
    })
    const type = createTypeOptions[typeSheet.tapIndex]

    this.setData({ operating: true })
    try {
      await todosApi.create(this.data.coupleId, { title, type })
      wx.showToast({ title: '创建成功', icon: 'success' })
      await this.loadTodos(true)
    } catch (err) {
      const message = err instanceof Error ? err.message : '创建失败'
      wx.showToast({ title: message, icon: 'none' })
    } finally {
      this.setData({ operating: false })
    }
  },

  async onEdit(e) {
    const todoId = e.currentTarget.dataset.id
    const currentTitle = e.currentTarget.dataset.title
    if (!todoId || !this.data.coupleId || this.data.operating) return

    const modal = await wx.showModal({
      title: '编辑代办标题',
      editable: true,
      placeholderText: currentTitle || '请输入新标题'
    })
    if (!modal.confirm) return

    const title = (modal.content || '').trim()
    if (!title) {
      wx.showToast({ title: '标题不能为空', icon: 'none' })
      return
    }

    this.setData({ operating: true })
    try {
      await todosApi.update(this.data.coupleId, todoId, { title })
      wx.showToast({ title: '已更新', icon: 'success' })
      await this.loadTodos(false)
    } catch (err) {
      const message = err instanceof Error ? err.message : '更新失败'
      wx.showToast({ title: message, icon: 'none' })
    } finally {
      this.setData({ operating: false })
    }
  },

  async onDelete(e) {
    const todoId = e.currentTarget.dataset.id
    if (!todoId || !this.data.coupleId || this.data.operating) return

    const confirm = await wx.showModal({ title: '删除代办', content: '确认删除该代办吗？', confirmColor: '#d03050' })
    if (!confirm.confirm) return

    this.setData({ operating: true })
    try {
      await todosApi.delete(this.data.coupleId, todoId)
      wx.showToast({ title: '已删除', icon: 'success' })
      await this.loadTodos(true)
    } catch (err) {
      const message = err instanceof Error ? err.message : '删除失败'
      wx.showToast({ title: message, icon: 'none' })
    } finally {
      this.setData({ operating: false })
    }
  },

  async onComplete(e) {
    const todoId = e.currentTarget.dataset.id
    if (!todoId || !this.data.coupleId) return
    try {
      await todosApi.complete(this.data.coupleId, todoId)
      wx.showToast({ title: '已完成', icon: 'success' })
      await this.loadTodos(false)
    } catch (err) {
      const message = err instanceof Error ? err.message : '操作失败'
      wx.showToast({ title: message, icon: 'none' })
    }
  },

  async onUrge(e) {
    const todoId = e.currentTarget.dataset.id
    if (!todoId || !this.data.coupleId) return
    try {
      await todosApi.urge(this.data.coupleId, todoId)
      wx.showToast({ title: '催办已发送', icon: 'success' })
      await this.loadTodos(false)
    } catch (err) {
      const message = err instanceof Error ? err.message : '操作失败'
      wx.showToast({ title: message, icon: 'none' })
    }
  },

  async loadTodos(reset = false) {
    if (!ensureLogin()) return
    this.setData({ loading: true, error: '' })
    try {
      let coupleId = this.data.coupleId
      if (!coupleId || reset) {
        const me = await usersApi.me()
        coupleId = me.couple_id || ''
      }
      if (!coupleId) {
        this.setData({ todos: [], total: 0, coupleId: '' })
        return
      }

      const type = this.data.typeOptions[this.data.typeIndex]
      const status = this.data.statusOptions[this.data.statusIndex]
      const data = await todosApi.list(coupleId, type, status, this.data.page, this.data.pageSize)
      this.setData({
        todos: data.todos || [],
        total: data.total || 0,
        coupleId
      })
    } catch (e) {
      const message = e instanceof Error ? e.message : '加载失败'
      this.setData({ error: message })
      wx.showToast({ title: message, icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  }
})
