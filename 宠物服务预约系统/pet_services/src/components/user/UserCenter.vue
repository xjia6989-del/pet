<template>
  <div class="user-center">
    <div class="hero">
      <div class="hero-left">
        <h2>个人中心</h2>
        <p>管理您的资料、通知与客服沟通记录</p>
      </div>
      <el-button type="primary" plain icon="el-icon-refresh" @click="refreshAll">刷新数据</el-button>
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="10">
        <el-card shadow="hover" class="card profile-card">
          <div slot="header" class="card-title">个人资料</div>
          <el-form :model="profile" label-width="80px" class="modern-form">
            <el-form-item label="用户名">
              <el-input v-model="profile.username" disabled />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model="profile.name" />
            </el-form-item>
            <el-form-item label="电话">
              <el-input v-model="profile.phone" />
            </el-form-item>
            <el-form-item label="地址">
              <el-input v-model="profile.address" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveProfile">保存资料</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="14">
        <el-card shadow="hover" class="card notice-card">
          <div slot="header" class="card-title">系统通知</div>
          <el-timeline v-if="notifications.length">
            <el-timeline-item
              v-for="(item, idx) in notifications"
              :key="idx"
              :timestamp="item.time"
              :type="item.type">
              {{ item.text }}
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无通知" :image-size="80"></el-empty>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="card contact-card" style="margin-top: 16px;">
      <div slot="header" class="card-title">联系客服</div>
      <el-row :gutter="16" class="contact-grid">
        <el-col :xs="24" :md="8"><div class="contact-item"><i class="el-icon-phone-outline"></i> 400-800-1234</div></el-col>
        <el-col :xs="24" :md="8"><div class="contact-item"><i class="el-icon-chat-line-round"></i> pet-service-help</div></el-col>
        <el-col :xs="24" :md="8"><div class="contact-item"><i class="el-icon-message"></i> support@pet-service.com</div></el-col>
      </el-row>

      <el-input
        type="textarea"
        :rows="4"
        placeholder="请输入您要反馈的问题..."
        v-model="contactText" />
      <div style="margin-top:12px; display:flex; justify-content:flex-end;">
        <el-button type="primary" @click="submitContact">提交留言</el-button>
      </div>

      <el-divider></el-divider>
      <div class="history-title">历史留言</div>
      <el-empty v-if="!contactHistory.length" description="暂无留言记录" :image-size="80"></el-empty>
      <div v-for="item in contactHistory" :key="item.messageId" class="history-item">
        <div>
          <el-tag size="mini" :type="item.status === 1 ? 'success' : 'warning'" style="margin-right:8px;">
            {{ item.status === 1 ? '已处理' : '未处理' }}
          </el-tag>
          {{ item.content }}
        </div>
        <div v-if="item.reply" class="reply-text">客服回复：{{ item.reply }}</div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { updateMyInfo, submitContactMessage, listMyContactMessages } from '@/api/UserAPI';
import { getMyBooking } from '@/api/BookingAPI';
import { getHealthReminders } from '@/api/PetAPI';

export default {
  name: 'UserCenter',
  data() {
    return {
      profile: {
        userId: null,
        username: '',
        name: '',
        phone: '',
        address: '',
        avatar: '',
        age: null,
        sex: null,
        password: ''
      },
      notifications: [],
      contactText: '',
      contactHistory: []
    };
  },
  async created() {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    this.profile = { ...this.profile, ...user };
    await this.loadNotifications();
    await this.loadContactHistory();
  },
  methods: {
    async refreshAll() {
      await this.loadNotifications();
      await this.loadContactHistory();
      this.$message.success('已刷新最新数据');
    },
    async saveProfile() {
      const { data: res } = await updateMyInfo(this.profile);
      if (res && res.success) {
        localStorage.setItem('user', JSON.stringify(this.profile));
        this.$message.success('资料保存成功');
      } else {
        this.$message.error((res && res.msg) || '资料保存失败');
      }
    },
    async loadNotifications() {
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      const userId = user.userId || 1;
      const list = [];
      list.push({
        time: new Date().toLocaleString(),
        type: 'primary',
        text: `欢迎回来，${user.name || user.username || '用户'}。`
      });

      try {
        const bookings = await getMyBooking(userId);
        if (Array.isArray(bookings) && bookings.length) {
          list.push({
            time: new Date().toLocaleString(),
            type: 'warning',
            text: `您当前共有 ${bookings.length} 条预约记录，请及时查看状态。`
          });
        }
      } catch (e) {}

      try {
        const reminders = await getHealthReminders(userId, 'display');
        if (Array.isArray(reminders) && reminders.length) {
          list.push({
            time: new Date().toLocaleString(),
            type: 'success',
            text: `健康提醒：近期有 ${reminders.length} 条待关注记录。`
          });
        }
      } catch (e) {}

      this.notifications = list;
    },
    async loadContactHistory() {
      try {
        const user = JSON.parse(localStorage.getItem('user') || '{}');
        const userId = user.userId || 1;
        const rows = await listMyContactMessages(userId);
        this.contactHistory = Array.isArray(rows) ? rows : [];
      } catch (e) {
        this.contactHistory = [];
      }
    },
    async submitContact() {
      const content = (this.contactText || '').trim();
      if (!content) {
        this.$message.warning('请先填写留言内容');
        return;
      }
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      const payload = {
        userId: user.userId || 1,
        username: user.username || '',
        name: user.name || '',
        phone: user.phone || '',
        content
      };
      const ok = await submitContactMessage(payload);
      if (ok) {
        this.$message.success('留言已提交，客服会尽快联系您');
        this.contactText = '';
        await this.loadContactHistory();
      } else {
        this.$message.error('留言提交失败，请稍后重试');
      }
    }
  }
};
</script>

<style scoped>
.user-center { padding: 12px; }
.hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 16px 18px;
  border-radius: 12px;
  background: linear-gradient(135deg, #eef4ff, #f8fbff);
  border: 1px solid #e7efff;
}
.hero h2 { font-size: 22px; color: #2d3a4b; margin-bottom: 4px; }
.hero p { color: #7a869a; font-size: 13px; }
.card-title { font-weight: 600; color: #303133; }
.contact-grid { margin-bottom: 14px; }
.contact-item {
  padding: 10px 12px;
  background: #f7faff;
  border-radius: 10px;
  border: 1px solid #edf2ff;
  margin-bottom: 10px;
}
.history-title { font-weight: 600; margin-bottom: 10px; }
.history-item {
  padding: 12px;
  border: 1px solid #edf1f8;
  border-radius: 10px;
  background: #fcfdff;
  margin-bottom: 10px;
}
.reply-text { color: #409EFF; margin-top: 8px; }
</style>
