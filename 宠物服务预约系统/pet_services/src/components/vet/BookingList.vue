<template>
  <div>
    <el-card shadow="hover" style="margin-bottom: 16px;">
      <div slot="header" style="font-weight: 600; display:flex; justify-content:space-between; align-items:center;">
        <span>我的预约列表</span>
        <span style="color:#909399;font-size:12px;">当前登录兽医：{{ currentVetId || '未获取' }}</span>
      </div>
      <el-row :gutter="12">
        <el-col :span="8">
          <el-input v-model.trim="keyword" clearable placeholder="按预约编号/宠物ID/宠物名筛选"></el-input>
        </el-col>
        <el-col :span="6">
          <el-select v-model="flag" clearable placeholder="预约状态" style="width: 100%;">
            <el-option label="预约中" :value="1"></el-option>
            <el-option label="已服务" :value="2"></el-option>
            <el-option label="已评价" :value="3"></el-option>
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-empty v-if="!tableData.length" description="当前没有分配给你的预约"></el-empty>
    <el-table v-else :data="tableData" border stripe style="width: 100%;">
      <el-table-column prop="bookingId" label="预约编号" width="100"></el-table-column>
      <el-table-column prop="bookingDate" label="预约时间" width="130">
        <template slot-scope="scope">{{ formatDate(scope.row.bookingDate) }}</template>
      </el-table-column>
      <el-table-column prop="name" label="预约人" width="120"></el-table-column>
      <el-table-column prop="phone" label="电话" width="140"></el-table-column>
      <el-table-column label="宠物ID" width="120">
        <template slot-scope="scope">
          {{ scope.row.petId || '未知' }}
        </template>
      </el-table-column>
      <el-table-column prop="serveName" label="服务名称"></el-table-column>
      <el-table-column prop="flag" label="状态" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.flag == 1">预约中</el-tag>
          <el-tag v-if="scope.row.flag == 2" type="success">已服务</el-tag>
          <el-tag v-if="scope.row.flag == 3" type="info">已评价</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template slot-scope="scope">
          <el-button
            v-if="scope.row.flag == 1"
            type="primary"
            size="mini"
            @click="markDone(scope.row.bookingId)">
            标记已服务
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import { getVetBooking, updateBookingFlag } from '@/api/BookingAPI.js';

export default {
  name: 'VetBookingList',
  data() {
    return {
      tableData: [],
      keyword: '',
      flag: '',
      currentVetId: ''
    };
  },
  async created() {
    await this.loadData();
  },
  methods: {
    formatDate(value) {
      if (!value) return '';
      const d = new Date(value);
      if (Number.isNaN(d.getTime())) return String(value).slice(0, 10);
      const y = d.getFullYear();
      const m = String(d.getMonth() + 1).padStart(2, '0');
      const day = String(d.getDate()).padStart(2, '0');
      return `${y}-${m}-${day}`;
    },
    async loadData() {
      const vet = JSON.parse(localStorage.getItem('vet') || '{}');
      const vetId = vet.userId || vet.vetId;
      this.currentVetId = vetId || '';
      if (!vetId) {
        this.$message.warning('登录状态已失效，请重新登录');
        this.$router.push('/login');
        return;
      }
      const rows = await getVetBooking(vetId);
      let list = Array.isArray(rows) ? rows : [];
      if (this.flag !== '' && this.flag !== null && this.flag !== undefined) {
        list = list.filter(item => Number(item.flag) === Number(this.flag));
      }
      if (this.keyword) {
        const k = this.keyword.trim();
        list = list.filter(item => {
          const text = `${item.bookingId || ''} ${item.petId || ''} ${item.petName || ''}`;
          return text.includes(k);
        });
      }
      this.tableData = list;
      console.log('vet booking loaded', { vetId, rows: list });
    },
    async markDone(bookingId) {
      const result = await updateBookingFlag(bookingId, 2);
      this.tableData = Array.isArray(result?.records) ? result.records : [];
      this.$message.success('已标记为已服务');
      await this.loadData();
    }
  }
};
</script>