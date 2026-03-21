<template>
  <div class="my-booking">
    <div class="page-hero">
      <div>
        <h2>我的预约记录</h2>
        <p>查看待处理预约、历史记录，并在可取消时段内快速操作</p>
      </div>
      <el-button round type="primary" icon="el-icon-refresh" @click="fetchBookings">刷新</el-button>
    </div>

    <el-card class="panel-card" shadow="hover">
      <el-tabs v-model="activeTab" @tab-click="onTabChange" class="booking-tabs">
        <el-tab-pane label="待处理预约" name="pending"></el-tab-pane>
        <el-tab-pane label="历史预约" name="history"></el-tab-pane>
      </el-tabs>

      <el-table :data="displayBookings" border style="width: 100%">
        <el-table-column prop="bookingId" label="预约编号" width="100" />
        <el-table-column prop="serveName" label="服务项目" />
        <el-table-column prop="bookingDate" label="预约时间" width="180" />
        <el-table-column prop="flag" label="状态" width="140">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.flag === 1" type="warning">待处理</el-tag>
            <el-tag v-else-if="scope.row.flag === 2" type="success">已服务</el-tag>
            <el-tag v-else-if="scope.row.flag === 3" type="info">已评价</el-tag>
            <el-tag v-else-if="scope.row.flag === 4" type="danger">已取消</el-tag>
            <el-tag v-else>未知状态</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template slot-scope="scope">
            <el-button type="text" @click="cancel(scope.row)" :disabled="!canCancel(scope.row)">取消预约</el-button>
            <el-button type="text" @click="updateOpen(scope.row.bookingId)" :disabled="scope.row.flag !== 2">去评价</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog title="评价服务" :visible.sync="updateVisible" width="520px">
      <el-form :model="booking">
        <el-form-item label="评分" :label-width="formLabelWidth">
          <el-rate v-model="booking.star" :max="5" />
        </el-form-item>
        <el-form-item label="评价内容" :label-width="formLabelWidth">
          <el-input v-model="booking.appraise" type="textarea" :rows="3" placeholder="请输入评价内容" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="updateVisible = false">取消</el-button>
        <el-button type="primary" @click="updateSubmit">提交评价</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getMyBooking, updateBookingComment, cancelBooking } from '@/api/BookingAPI.js'

export default {
  name: 'PetServicesMyBooking',
  data() {
    return {
      tableData: [],
      activeTab: 'pending',
      booking: {
        bookingId: '',
        appraise: '',
        star: 0
      },
      updateVisible: false,
      formLabelWidth: '120px'
    };
  },
  computed: {
    displayBookings() {
      if (this.activeTab === 'pending') {
        return this.tableData.filter(item => item.flag === 1);
      }
      return this.tableData.filter(item => [2, 3, 4].includes(item.flag));
    }
  },
  methods: {
    async fetchBookings() {
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      const userId = user.userId;
      if (!userId) {
        this.$message.warning('登录状态已失效，请重新登录');
        this.$router.push('/login');
        return;
      }
      const bookings = await getMyBooking(userId);
      this.tableData = Array.isArray(bookings) ? bookings : [];
    },
    canCancel(row) {
      if (row.flag !== 1 || !row.bookingDate) return false;
      const diff = new Date(row.bookingDate).getTime() - Date.now();
      return diff >= 2 * 60 * 60 * 1000;
    },
    async cancel(row) {
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      const userId = user.userId;
      if (!userId) {
        this.$message.warning('登录状态已失效，请重新登录');
        this.$router.push('/login');
        return;
      }
      const ok = await cancelBooking(row.bookingId, userId);
      if (ok) {
        this.$message.success('取消成功');
        await this.fetchBookings();
      } else {
        this.$message.error('取消失败：仅支持预约前2小时以上取消');
      }
    },
    async updateSubmit() {
      let isEmpty = this.$infoRule(this.booking);
      if (isEmpty == true) {
        this.$message.error('评价信息不完整!');
        return;
      }
      const result = await updateBookingComment(this.booking);
      if (Array.isArray(result)) {
        this.$message.success('感谢您珍贵的评价!');
        this.tableData = result;
        this.updateVisible = false;
        return;
      }
      this.$message.error('评价提交失败');
    },
    updateOpen(bookingId) {
      this.booking.bookingId = bookingId;
      this.booking.appraise = '';
      this.booking.star = 0;
      this.updateVisible = true;
    },
    onTabChange() {}
  },
  async created() {
    await this.fetchBookings();
  },
  async activated() {
    await this.fetchBookings();
  }
};
</script>

<style scoped>
.my-booking { padding: 8px; }
.page-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
  padding: 16px 18px;
  border-radius: 14px;
  background: linear-gradient(135deg, #fff3eb, #ffe8dd);
  border: 1px solid #ffd9c7;
}
.page-hero h2 { color: #2f3f63; margin-bottom: 4px; }
.page-hero p { color: #7c8eaf; font-size: 13px; }
.panel-card { border-radius: 14px; }
.booking-tabs /deep/ .el-tabs__item.is-active { color: #ff8e5a; }
.booking-tabs /deep/ .el-tabs__active-bar { background-color: #ff8e5a; }
</style>
