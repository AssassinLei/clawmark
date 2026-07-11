import { couplesApi } from '../../services/couples'
import { usersApi } from '../../services/users'
import { clearToken } from '../../utils/auth'
import { ensureLogin } from '../../utils/page-guard'

Page({
  data: {
    loading: false,
    user: null,
    inviteCode: '',
    inviteExpiresAt: '',
    inviteInput: '',
    operating: false,
    error: ''
  },

  async onShow() {
    await this.loadMe()
  },

  async onPullDownRefresh() {
    await this.loadMe()
    wx.stopPullDownRefresh()
  },

  async loadMe() {
    if (!ensureLogin()) return
    this.setData({ loading: true, error: '' })
    try {
      const me = await usersApi.me()
      this.setData({ user: me })
    } catch (e) {
      const message = e instanceof Error ? e.message : '加载失败'
      this.setData({ error: message })
      wx.showToast({ title: message, icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  async onCreateInvite() {
    if (this.data.operating) return
    this.setData({ operating: true })
    try {
      const data = await couplesApi.createInvite()
      this.setData({ inviteCode: data.invite_code, inviteExpiresAt: data.expires_at })
      wx.showToast({ title: '邀请码已生成', icon: 'success' })
    } catch (e) {
      const message = e instanceof Error ? e.message : '操作失败'
      wx.showToast({ title: message, icon: 'none' })
    } finally {
      this.setData({ operating: false })
    }
  },

  onCopyInvite() {
    if (!this.data.inviteCode) {
      wx.showToast({ title: '暂无邀请码', icon: 'none' })
      return
    }
    wx.setClipboardData({ data: this.data.inviteCode })
  },

  onInviteInput(e) {
    this.setData({ inviteInput: (e.detail.value || '').trim() })
  },

  async onBindInvite() {
    if (this.data.operating) return
    const code = this.data.inviteInput
    if (!code) {
      wx.showToast({ title: '请输入邀请码', icon: 'none' })
      return
    }

    this.setData({ operating: true })
    try {
      await couplesApi.bind(code)
      wx.showToast({ title: '绑定成功', icon: 'success' })
      this.setData({ inviteInput: '', inviteCode: '', inviteExpiresAt: '' })
      await this.loadMe()
    } catch (e) {
      const message = e instanceof Error ? e.message : '绑定失败'
      wx.showToast({ title: message, icon: 'none' })
    } finally {
      this.setData({ operating: false })
    }
  },

  async onUnbind() {
    if (this.data.operating) return

    const modal = await wx.showModal({
      title: '确认解绑',
      content: '解绑后操作不可逆，是否继续？',
      confirmColor: '#d03050'
    })
    if (!modal.confirm) return

    this.setData({ operating: true })
    try {
      await couplesApi.unbind()
      wx.showToast({ title: '解绑成功', icon: 'success' })
      this.setData({ inviteCode: '', inviteExpiresAt: '', inviteInput: '' })
      await this.loadMe()
    } catch (e) {
      const message = e instanceof Error ? e.message : '解绑失败'
      wx.showToast({ title: message, icon: 'none' })
    } finally {
      this.setData({ operating: false })
    }
  },

  onLogout() {
    clearToken()
    const app = getApp()
    app.globalData.token = ''
    wx.navigateTo({ url: '/pages/auth-login/index' })
  }
})
