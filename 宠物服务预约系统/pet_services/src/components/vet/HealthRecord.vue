<template>
  <div>
    <el-breadcrumb separator="/" style="height: 35px;" separator-class="el-icon-arrow-right">
      <el-breadcrumb-item :to="'/vetHome'">首页</el-breadcrumb-item>
      <el-breadcrumb-item>{{ this.$route.meta.title }}</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card shadow="hover" style="margin-bottom: 16px;">
      <div slot="header" style="font-weight: 600; display:flex; justify-content:space-between; align-items:center;">
        <span>健康记录录入</span>
        <span style="color:#909399;font-size:12px;">当前兽医：{{ vet.name || vet.username || vet.userId }}</span>
      </div>
      <el-row :gutter="12">
        <el-col :span="8">
          <el-select v-model="selectedBookingId" filterable clearable placeholder="选择已分配预约" style="width: 100%;" @change="handleBookingChange">
            <el-option
              v-for="item in bookingOptions"
              :key="item.bookingId"
              :label="buildBookingLabel(item)"
              :value="item.bookingId">
            </el-option>
          </el-select>
        </el-col>
        <el-col :span="5">
          <el-input v-model.trim="keyword" clearable placeholder="预约/宠物/用户搜索" @keyup.enter.native="filterBookings"></el-input>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" icon="el-icon-search" @click="filterBookings">筛选</el-button>
        </el-col>
        <el-col :span="4">
          <el-button icon="el-icon-refresh" @click="loadBookings">刷新预约</el-button>
        </el-col>
      </el-row>
      <div v-if="selectedBooking.bookingId" style="margin-top: 12px; color:#606266;">
        当前预约：{{ selectedBooking.bookingId }} / {{ selectedBooking.name || '未知用户' }} / 宠物：{{ selectedBooking.petName || selectedBooking.petId || '未知' }} / 服务：{{ selectedBooking.serveName || '未知服务' }}
      </div>
    </el-card>

    <el-card shadow="hover" style="margin-bottom: 16px;">
      <div slot="header" style="font-weight: 600;">{{ form.recordId ? '修改健康记录' : '新增健康记录' }}</div>
      <el-form :model="form" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="6">
            <el-form-item label="宠物ID">
              <el-input v-model="form.petId" disabled placeholder="选择预约后带出"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="记录类型">
              <el-select v-model="form.recordType" placeholder="记录类型" style="width: 100%;">
                <el-option label="体检" value="体检"></el-option>
                <el-option label="疫苗" value="疫苗"></el-option>
                <el-option label="驱虫" value="驱虫"></el-option>
                <el-option label="复查" value="复查"></el-option>
                <el-option label="治疗" value="治疗"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="记录标签">
              <el-input v-model="form.recordTag" placeholder="可自动生成"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="记录日期">
              <el-date-picker v-model="form.recordDate" type="date" value-format="yyyy-MM-dd" placeholder="记录日期" style="width: 100%;"></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="下次复查">
              <el-date-picker v-model="form.nextDate" type="date" value-format="yyyy-MM-dd" placeholder="下次复查/提醒日期" style="width: 100%;"></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="提醒状态">
              <el-select v-model="form.reminderStatus" placeholder="提醒状态" style="width: 100%;">
                <el-option label="正常" :value="1"></el-option>
                <el-option label="即将到期" :value="2"></el-option>
                <el-option label="已逾期" :value="3"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="诊断结果">
          <el-input type="textarea" :rows="3" v-model="form.diagnosis" placeholder="填写诊断结果"></el-input>
        </el-form-item>
        <el-form-item label="处方建议">
          <el-input type="textarea" :rows="3" v-model="form.prescription" placeholder="填写处方或护理建议"></el-input>
        </el-form-item>
        <el-form-item label="记录描述">
          <el-input type="textarea" :rows="3" v-model="form.description" placeholder="填写健康记录描述"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-check" @click="submitRecord">保存记录</el-button>
          <el-button @click="resetForm">清空</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover">
      <div slot="header" style="font-weight: 600;">该宠物健康记录</div>
      <el-empty v-if="!records.length" description="请选择预约或暂无健康记录"></el-empty>
      <el-table v-else :data="records" border stripe>
        <el-table-column prop="recordId" label="编号" width="80"></el-table-column>
        <el-table-column prop="recordType" label="类型" width="100"></el-table-column>
        <el-table-column prop="recordTag" label="标签" width="100"></el-table-column>
        <el-table-column prop="recordDate" label="记录日期" width="120">
          <template slot-scope="scope">{{ formatDate(scope.row.recordDate) }}</template>
        </el-table-column>
        <el-table-column prop="diagnosis" label="诊断结果"></el-table-column>
        <el-table-column prop="prescription" label="处方建议"></el-table-column>
        <el-table-column prop="nextDate" label="下次复查" width="120">
          <template slot-scope="scope">{{ formatDate(scope.row.nextDate) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template slot-scope="scope">
            <el-button type="primary" size="mini" @click="editRecord(scope.row)">编辑</el-button>
            <el-popconfirm title="确定删除此健康记录吗？" @confirm="removeRecord(scope.row.recordId)">
              <el-button type="danger" size="mini" slot="reference" style="margin-left:8px;">删除</el-button>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { getVetBooking } from '@/api/BookingAPI.js';
import { addHealthRecord, updateHealthRecord, deleteHealthRecord, getHealthRecordsForVet } from '@/api/HealthAPI.js';

export default {
  name: 'VetHealthRecord',
  data() {
    return {
      vet: {},
      bookings: [],
      bookingOptions: [],
      selectedBookingId: '',
      selectedBooking: {},
      keyword: '',
      records: [],
      form: this.emptyForm()
    };
  },
  async created() {
    this.vet = JSON.parse(localStorage.getItem('vet') || '{}');
    await this.loadBookings();
  },
  methods: {
    emptyForm() {
      return {
        recordId: '',
        petId: '',
        bookingId: '',
        recordType: '体检',
        recordTag: '',
        recordDate: this.todayDate(),
        nextDate: '',
        reminderStatus: 1,
        diagnosis: '',
        prescription: '',
        description: '',
        createdBy: ''
      };
    },
    todayDate() {
      const d = new Date();
      const y = d.getFullYear();
      const m = String(d.getMonth() + 1).padStart(2, '0');
      const day = String(d.getDate()).padStart(2, '0');
      return `${y}-${m}-${day}`;
    },
    formatDate(value) {
      if (!value) return '';
      const d = new Date(value);
      if (Number.isNaN(d.getTime())) return String(value).slice(0, 10);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    },
    buildBookingLabel(item) {
      return `${item.bookingId} - ${item.name || '未知用户'} - ${item.petName || ('宠物ID:' + item.petId)} - ${item.serveName || ''}`;
    },
    async loadBookings() {
      const vetId = this.vet.userId || this.vet.vetId;
      if (!vetId) {
        this.$message.warning('登录状态已失效，请重新登录');
        this.$router.push('/login');
        return;
      }
      const rows = await getVetBooking(vetId);
      this.bookings = Array.isArray(rows) ? rows : [];
      this.bookingOptions = this.bookings;
      if (!this.bookings.length) {
        this.$message.warning('当前没有分配给你的预约');
      }
    },
    filterBookings() {
      const key = this.keyword.trim();
      if (!key) {
        this.bookingOptions = this.bookings;
        return;
      }
      this.bookingOptions = this.bookings.filter(item => {
        const text = `${item.bookingId || ''} ${item.name || ''} ${item.phone || ''} ${item.petId || ''} ${item.petName || ''} ${item.serveName || ''}`;
        return text.includes(key);
      });
    },
    async handleBookingChange(bookingId) {
      const booking = this.bookings.find(item => Number(item.bookingId) === Number(bookingId));
      this.selectedBooking = booking || {};
      this.form = this.emptyForm();
      if (booking) {
        this.form.petId = booking.petId;
        this.form.bookingId = booking.bookingId;
        this.form.createdBy = this.vet.userId || this.vet.vetId;
        await this.loadRecords(booking.petId);
      }
    },
    async loadRecords(petId) {
      if (!petId) {
        this.records = [];
        return;
      }
      const rows = await getHealthRecordsForVet(petId);
      this.records = Array.isArray(rows) ? rows : [];
    },
    async submitRecord() {
      if (!this.form.petId || !this.form.bookingId) {
        this.$message.warning('请先选择预约');
        return;
      }
      this.form.createdBy = this.vet.userId || this.vet.vetId;
      const payload = { ...this.form };
      const ok = payload.recordId ? await updateHealthRecord(payload) : await addHealthRecord(payload);
      if (ok) {
        this.$message.success('健康记录保存成功');
        await this.loadRecords(this.form.petId);
        this.resetFormKeepBooking();
      } else {
        this.$message.error('健康记录保存失败');
      }
    },
    editRecord(row) {
      this.form = {
        recordId: row.recordId,
        petId: row.petId,
        bookingId: row.bookingId || this.selectedBooking.bookingId,
        recordType: row.recordType || '体检',
        recordTag: row.recordTag || '',
        recordDate: this.formatDate(row.recordDate) || this.todayDate(),
        nextDate: this.formatDate(row.nextDate),
        reminderStatus: row.reminderStatus || 1,
        diagnosis: row.diagnosis || '',
        prescription: row.prescription || '',
        description: row.description || '',
        createdBy: row.createdBy || this.vet.userId || this.vet.vetId
      };
    },
    async removeRecord(recordId) {
      const ok = await deleteHealthRecord(recordId);
      if (ok) {
        this.$message.success('删除成功');
        await this.loadRecords(this.form.petId || this.selectedBooking.petId);
      } else {
        this.$message.error('删除失败');
      }
    },
    resetForm() {
      this.form = this.emptyForm();
      this.selectedBookingId = '';
      this.selectedBooking = {};
      this.records = [];
    },
    resetFormKeepBooking() {
      const petId = this.selectedBooking.petId;
      const bookingId = this.selectedBooking.bookingId;
      this.form = this.emptyForm();
      this.form.petId = petId;
      this.form.bookingId = bookingId;
      this.form.createdBy = this.vet.userId || this.vet.vetId;
    }
  }
};
</script>
