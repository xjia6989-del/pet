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

    <el-row :gutter="16" style="margin-top:16px;">
      <el-col :xs="24" :lg="10">
        <el-card shadow="hover" class="card">
          <div slot="header" class="card-title">重点关注宠物</div>
          <el-empty v-if="!focusPets.length" description="暂无需要重点关注的宠物" :image-size="80"></el-empty>
          <div v-for="item in focusPets" :key="item.petId" class="focus-item">
            <div class="focus-name">{{ item.petName }}</div>
            <div class="focus-desc">{{ item.remark }}</div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="14">
        <el-card shadow="hover" class="card">
          <div slot="header" class="card-title">即将到期提醒</div>
          <el-empty v-if="!upcomingReminders.length" description="暂无即将到期的提醒" :image-size="80"></el-empty>
          <div v-for="(item, idx) in upcomingReminders" :key="idx" class="reminder-item">
            <div style="display:flex;align-items:center;gap:8px;flex-wrap:wrap;">
              <el-tag size="mini" type="warning">{{ item.reminderType }}</el-tag>
              <el-tag size="mini" :type="item.level === 'overdue' ? 'danger' : 'success'">
                {{ item.level === 'overdue' ? '已过期' : '待提醒' }}
              </el-tag>
              <span>{{ item.petName }}：{{ item.reminderText }}（{{ item.dueDate }}）</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="card rating-card" style="margin-top: 16px;">
      <div slot="header" class="card-title">我的预约评价</div>
      <el-empty v-if="!completedBookings.length" description="暂无可评价的预约" :image-size="80"></el-empty>
      <div v-for="item in completedBookings" :key="item.bookingId" class="rating-item">
        <div class="rating-top">
          <div>
            <div class="rating-title">{{ item.serveName || '预约服务' }}</div>
            <div class="rating-sub">宠物：{{ item.petName || '未知' }} ｜ 时间：{{ item.bookingDate || '' }}</div>
          </div>
          <el-rate v-model="item.localStar" :max="5" show-score score-template="{value} 分"></el-rate>
        </div>
        <el-input
          type="textarea"
          :rows="3"
          placeholder="写下你的评价吧"
          v-model="item.localAppraise"
          style="margin-top:10px;">
        </el-input>
        <div style="margin-top:10px; display:flex; justify-content:flex-end; gap:8px;">
          <el-button size="mini" @click="resetRating(item)">重置</el-button>
          <el-button size="mini" type="primary" @click="saveRating(item)">提交评价</el-button>
        </div>
      </div>
    </el-card>

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
        <div style="display:flex;justify-content:space-between;align-items:flex-start;gap:12px;">
          <div>
            <el-tag size="mini" :type="item.status === 1 ? 'success' : 'warning'" style="margin-right:8px;">
              {{ item.status === 1 ? '已处理' : '未处理' }}
            </el-tag>
            {{ item.content }}
          </div>
          <el-button size="mini" type="text" style="color:#f56c6c;" @click="removeContact(item)">删除</el-button>
        </div>
        <div v-if="item.reply" class="reply-text">客服回复：{{ item.reply }}</div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { updateMyInfo, submitContactMessage, listMyContactMessages, deleteMyContactMessage } from '@/api/UserAPI';
import { getMyBooking, updateBookingComment } from '@/api/BookingAPI';
import { getHealthReminders, getPetList } from '@/api/PetAPI';

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
      upcomingReminders: [],
      focusPets: [],
      completedBookings: [],
      contactText: '',
      contactHistory: []
    };
  },
  async created() {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    this.profile = { ...this.profile, ...user };
    await this.loadNotifications();
    await this.loadRatingBookings();
    await this.loadContactHistory();
  },
  methods: {
    async refreshAll() {
      await this.loadNotifications();
      await this.loadRatingBookings();
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
        this.upcomingReminders = Array.isArray(reminders)
          ? reminders.filter(item => item.level === 'upcoming' || item.level === 'overdue').slice(0, 5)
          : [];
        if (this.upcomingReminders.length) {
          const overdue = this.upcomingReminders.filter(item => item.level === 'overdue').length;
          const upcoming = this.upcomingReminders.filter(item => item.level === 'upcoming').length;
          list.push({
            time: new Date().toLocaleString(),
            type: overdue > 0 ? 'danger' : 'success',
            text: overdue > 0
              ? `健康提醒：有 ${overdue} 条记录已过期，请优先处理。`
              : `健康提醒：近期有 ${upcoming} 条记录即将到期，请留意。`
          });
        }
      } catch (e) {
        this.upcomingReminders = [];
      }

      try {
        const pets = await getPetList(userId);
        this.focusPets = Array.isArray(pets)
          ? pets.filter(item => Number(item.status) !== 1).map(item => ({
              petId: item.petId,
              petName: item.petName,
              remark: '该宠物当前处于停用状态，历史档案仍可查看。'
            }))
          : [];
      } catch (e) {
        this.focusPets = [];
      }

      this.notifications = list;
    },
    getBookingText(item) {
      const flag = Number(item.flag);
      if (flag === 1) return '预约中';
      if (flag === 2) return '已服务';
      if (flag === 3) return '已评价';
      if (flag === 4) return '已取消';
      return '未知';
    },
    getBookingTagType(item) {
      const flag = Number(item.flag);
      if (flag === 1) return 'warning';
      if (flag === 2) return 'success';
      if (flag === 3) return 'info';
      if (flag === 4) return 'danger';
      return '';
    },
    canCancelBooking(item) {
      const flag = Number(item.flag);
      if (flag !== 1) return false;
      if (!item.bookingDate) return false;
      const time = new Date(item.bookingDate).getTime();
      if (Number.isNaN(time)) return false;
      return time - Date.now() > 2 * 60 * 60 * 1000;
    },
    async cancelBookingItem(item) {
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      const userId = user.userId || 1;
      const ok = await cancelBooking(item.bookingId, userId);
      if (ok) {
        this.$message.success('取消成功');
        await this.loadRatingBookings();
      } else {
        this.$message.error('当前预约无法取消');
      }
    },
    async loadAllBookings() {
      try {
        const user = JSON.parse(localStorage.getItem('user') || '{}');
        const userId = user.userId || 1;
        const rows = await getMyBooking(userId);
        const arr = Array.isArray(rows) ? rows : [];
        this.myBookings = arr;
        this.completedBookings = arr
          .filter(item => Number(item.flag) === 2 || Number(item.flag) === 3)
          .map(item => ({
            ...item,
            localStar: Number(item.star || 0),
            localAppraise: item.appraise || ''
          }));
      } catch (e) {
        this.myBookings = [];
        this.completedBookings = [];
      }
    },
    async loadRatingBookings() {
      await this.loadAllBookings();
    },
    resetRating(item) {
      item.localStar = Number(item.star || 0);
      item.localAppraise = item.appraise || '';
    },
    async saveRating(item) {
      if (!item.bookingId) {
        this.$message.warning('预约信息异常');
        return;
      }
      if (!item.localStar) {
        this.$message.warning('请先选择评分');
        return;
      }
      const payload = {
        bookingId: item.bookingId,
        star: item.localStar,
        appraise: item.localAppraise,
        flag: 3
      };
      const ok = await updateBookingComment(payload);
      if (ok) {
        this.$message.success('评价提交成功');
        await this.loadRatingBookings();
      } else {
        this.$message.error('评价提交失败');
      }
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
    async removeContact(item) {
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      const userId = user.userId || 1;
      this.$confirm('确定删除这条留言吗？', '提示', { type: 'warning' }).then(async () => {
        const ok = await deleteMyContactMessage(item.messageId, userId);
        if (ok) {
          this.$message.success('删除成功');
          await this.loadContactHistory();
        } else {
          this.$message.error('删除失败');
        }
      }).catch(() => {});
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
.focus-item {
  padding: 12px;
  border: 1px solid #f0e7d6;
  border-radius: 10px;
  background: #fffaf0;
  margin-bottom: 10px;
}
.focus-name { font-weight: 600; color: #8a5a00; margin-bottom: 4px; }
.focus-desc { color: #8c7a52; font-size: 13px; }
.reminder-item {
  padding: 10px 12px;
  border: 1px solid #e9eef7;
  border-radius: 10px;
  background: #fcfdff;
  margin-bottom: 10px;
}
.reply-text { color: #409EFF; margin-top: 8px; }
</style>
