<template>
  <div>
    <el-breadcrumb separator="/" style="height: 35px;" separator-class="el-icon-arrow-right">
      <el-breadcrumb-item :to="'/adminHome'">首页</el-breadcrumb-item>
      <el-breadcrumb-item>{{ this.$route.meta.title }}</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card shadow="hover" style="margin-bottom: 16px;">
      <div slot="header" style="font-weight: 600;">预约时段配置</div>
      <div style="display:flex;align-items:center;gap:12px;flex-wrap:wrap;">
        <el-select v-model="slotServeId" placeholder="选择服务" style="width: 220px;" filterable @change="loadSlotConfig">
          <el-option v-for="item in serveOptions" :key="item.serveId" :label="item.serveName" :value="item.serveId"></el-option>
        </el-select>
        <el-date-picker
          v-model="slotDate"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="选择日期"
          style="width: 180px;"
          @change="loadSlotConfig">
        </el-date-picker>
        <el-button type="primary" icon="el-icon-refresh" @click="loadSlotConfig">加载时段</el-button>
      </div>
      <div style="margin-top:8px;color:#909399;font-size:12px;">
        可在这里设置某个服务某一天每个时段的开放状态和预约人数上限，保存后用户端会同步生效。
      </div>

      <el-table :data="slotConfigs" style="width:100%;margin-top:12px;" border>
        <el-table-column prop="timeSlot" label="时间段" width="120"></el-table-column>
        <el-table-column label="是否开放" width="120">
          <template slot-scope="scope">
            <el-switch v-model="scope.row.enabled" :active-value="1" :inactive-value="0"></el-switch>
          </template>
        </el-table-column>
        <el-table-column label="容量上限" width="160">
          <template slot-scope="scope">
            <el-input-number v-model="scope.row.capacity" :min="1" :max="50"></el-input-number>
          </template>
        </el-table-column>
        <el-table-column label="已预约人数" width="120">
          <template slot-scope="scope">
            <span>{{ scope.row.bookedCount != null ? scope.row.bookedCount : 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="剩余名额" width="120">
          <template slot-scope="scope">
            <span>{{ calcRemaining(scope.row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template slot-scope="scope">
            <el-button type="primary" size="mini" @click="saveSlot(scope.row)">保存</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="hover" style="margin-bottom: 16px;">
      <div slot="header" style="font-weight: 600;">预约管理</div>
      <el-select v-model="flag" clearable placeholder="服务状态" style="width: 180px;">
        <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value"></el-option>
      </el-select>
      <el-select v-model="assignedVetId" clearable filterable placeholder="分配兽医" style="width: 220px; margin-left: 10px;">
        <el-option v-for="item in vetOptions" :key="item.userId" :label="item.name || item.username" :value="item.userId"></el-option>
      </el-select>
      <el-button type="primary" icon="el-icon-search" @click="search" style="margin-left: 10px;">搜索</el-button>
      <el-button type="success" icon="el-icon-user" @click="saveVetAssign" style="margin-left: 10px;">分配兽医</el-button>
      <el-button type="primary" style="margin-left: 20px;" @click="exportExcelSelect" icon="el-icon-folder-add">导出Excel文件</el-button>
    </el-card>

    <el-dialog title="表格保存预览" width="70%" :visible.sync="selectWindow">
      <el-table :data="selectData" id="selectTable" height="380px">
        <el-table-column prop="bookingId" label="预约编号"></el-table-column>
        <el-table-column prop="bookingDate" label="预约时间"></el-table-column>
        <el-table-column prop="name" label="预约人"></el-table-column>
        <el-table-column prop="phone" label="电话"></el-table-column>
        <el-table-column prop="serveName" label="服务项目"></el-table-column>
        <el-table-column prop="flag" label="服务状态">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.flag == 1">预约中</el-tag>
            <el-tag v-if="scope.row.flag == 2">已服务</el-tag>
            <el-tag v-if="scope.row.flag == 3">已评价</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="star" label="评分"></el-table-column>
        <el-table-column prop="appraise" label="服务评价"></el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="exportExcel">确定保存</el-button>
      </div>
    </el-dialog>

    <el-table :data="tableData" style="width: 100%;margin-top: 10px" border :row-class-name="tableRowClassName" stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection"></el-table-column>
      <el-table-column prop="bookingId" label="预约编号"></el-table-column>
      <el-table-column prop="bookingDate" label="预约时间" width="200"></el-table-column>
      <el-table-column prop="name" label="姓名"></el-table-column>
      <el-table-column prop="phone" label="电话"></el-table-column>
      <el-table-column prop="serveName" label="服务名称"></el-table-column>
      <el-table-column prop="vetId" label="分配兽医" width="110">
        <template slot-scope="scope">
          <span>{{ getVetName(scope.row.vetId) || '未分配' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="flag" label="服务状态">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.flag == 1">预约中</el-tag>
          <el-tag v-if="scope.row.flag == 2">已服务</el-tag>
          <el-tag type="success" v-if="scope.row.flag == 3">已评价</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="star" label="评分" width="200">
        <template slot-scope="scope">
          <span v-if="scope.row.flag != 3">未评分</span>
          <el-rate v-model="scope.row.star" show-text disabled v-if="scope.row.flag == 3"></el-rate>
        </template>
      </el-table-column>
      <el-table-column prop="appraise" label="服务评价">
        <template slot-scope="scope">
          <span v-if="scope.row.flag != 3">未评价</span>
          <span v-if="scope.row.flag == 3">{{ scope.row.appraise }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template slot-scope="scope">
          <el-button type="primary" size="mini" @click="updateSubmit(scope.row.bookingId, 2)" icon="el-icon-check" v-if="scope.row.flag == 1">点击完成服务</el-button>
          <el-tag v-if="scope.row.flag == 2">已服务,待评价</el-tag>
          <el-tag type="success" v-if="scope.row.flag == 3">用户已评价</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <el-row>
      <el-col style="text-align:left;margin-top: 10px;">
        <el-pagination @current-change="handleCurrentChange" :current-page="currentpage" :page-size="pagesize" layout="total, prev, pager, next" :total="total"></el-pagination>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getAllBooking, deleteBooking, updateBookingFlag, searchBooking, assignVet, getSlotConfig, saveSlotConfig } from '@/api/BookingAPI.js'
import { getAllUser } from '@/api/UserAPI.js'
import { getAllServe } from '@/api/ServeAPI.js'
import htmlToExcel from '@/utils/htmlToExcel.js'
export default {
  name: 'InnovationSystemBooking',
  data() {
    return {
      selectData: [],
      selectWindow: false,
      tableData: [],
      vetOptions: [],
      assignedVetId: '',
      serveOptions: [],
      slotServeId: null,
      slotDate: '',
      slotConfigs: [],
      options: [{ value: 1, label: '预约中' }, { value: 2, label: '已服务' }, { value: 3, label: '已评价' }],
      flag: '',
      total: 0,
      currentpage: 1,
      pagesize: 8,
      visible: false
    };
  },
  methods: {
    calcRemaining(row) {
      return Math.max(Number(row.capacity || 0) - Number(row.bookedCount || 0), 0);
    },
    getVetName(vetId) {
      const item = this.vetOptions.find(v => Number(v.userId) === Number(vetId));
      return item ? (item.name || item.username) : '';
    },
    async loadVets() {
      const res = await getAllUser(1, 9999);
      const rows = res && res.data && res.data.result && Array.isArray(res.data.result.records) ? res.data.result.records : [];
      this.vetOptions = rows.filter(item => item.role === 'vet');
    },
    todayDate() {
      const d = new Date();
      const y = d.getFullYear();
      const m = String(d.getMonth() + 1).padStart(2, '0');
      const day = String(d.getDate()).padStart(2, '0');
      return `${y}-${m}-${day}`;
    },
    async loadServeOptions() {
      const list = await getAllServe();
      this.serveOptions = Array.isArray(list) ? list : [];
      if (!this.slotServeId && this.serveOptions.length) {
        this.slotServeId = this.serveOptions[0].serveId;
      }
      if (!this.slotDate) {
        this.slotDate = this.todayDate();
      }
      if (this.slotServeId && this.slotDate) {
        await this.loadSlotConfig();
      }
    },
    async loadSlotConfig() {
      if (!this.slotServeId || !this.slotDate) return;
      const rows = await getSlotConfig(this.slotServeId, this.slotDate);
      this.slotConfigs = Array.isArray(rows) ? rows : [];
      if (!this.slotConfigs.length) {
        this.slotConfigs = ['09:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00'].map(slot => ({
          serveId: this.slotServeId,
          date: this.slotDate,
          timeSlot: slot,
          enabled: 1,
          capacity: 1,
          bookedCount: 0,
          remainingCount: 1
        }));
      }
    },
    async saveSlot(row) {
      const ok = await saveSlotConfig({ serveId: this.slotServeId, date: this.slotDate, timeSlot: row.timeSlot, enabled: row.enabled, capacity: row.capacity });
      if (ok) {
        this.$message.success('时段配置已保存');
        await this.loadSlotConfig();
      } else {
        this.$message.error('保存失败');
      }
    },
    async saveVetAssign() {
      if (!this.assignedVetId) {
        this.$message.warning('请选择兽医');
        return;
      }
      const selected = this.selectData.length ? this.selectData : this.tableData.filter(item => item.flag === 1);
      if (!selected.length) {
        this.$message.warning('请选择要分配的预约');
        return;
      }
      for (const row of selected) {
        const ok = await assignVet(row.bookingId, this.assignedVetId);
        if (!ok) {
          this.$message.error(`预约 ${row.bookingId} 分配失败`);
          return;
        }
      }
      this.$message.success('兽医分配成功');
      await this.handleCurrentChange(this.currentpage);
    },
    exportExcel() { htmlToExcel.getExcel('#selectTable', '服务预约信息') },
    exportExcelSelect() {
      if (this.selectData.length < 1) return this.$message.error('请选择要导出的内容！');
      this.selectWindow = true;
    },
    handleSelectionChange(val) { this.selectData = val; },
    tableRowClassName({ rowIndex }) { return rowIndex === 1 ? 'warning-row' : rowIndex === 3 ? 'success-row' : ''; },
    async handleCurrentChange(currentPage) {
      const result = await getAllBooking(currentPage, 8);
      this.tableData = Array.isArray(result?.records) ? result.records : [];
      this.total = Number(result?.total || 0);
      this.currentpage = currentPage;
    },
    async updateSubmit(bookingId, flag) {
      const result = await updateBookingFlag(bookingId, flag);
      this.tableData = Array.isArray(result?.records) ? result.records : [];
      this.total = Number(result?.total || 0);
    },
    async handleConfirm(row) {
      const result = await deleteBooking(row.bookingId);
      this.$message.success('删除预约成功');
      this.tableData = Array.isArray(result?.records) ? result.records : [];
      this.total = Number(result?.total || 0);
      this.visible = false;
    },
    async search() {
      if (this.flag === '' || this.flag === null || this.flag === undefined) {
        await this.handleCurrentChange(1);
      } else {
        const result = await searchBooking(this.flag);
        this.tableData = Array.isArray(result) ? result : [];
        this.total = this.tableData.length;
        this.currentpage = 1;
        this.$message.success('搜索成功!');
      }
    }
  },
  async created() {
    this.flag = '';
    await this.handleCurrentChange(1);
    await this.loadVets();
    await this.loadServeOptions();
  }
};
</script>

<style lang="less" scoped></style>