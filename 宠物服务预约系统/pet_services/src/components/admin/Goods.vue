<template>
  <div>
    <el-breadcrumb separator="/" style="height: 35px;" separator-class="el-icon-arrow-right">
      <el-breadcrumb-item :to="'/adminHome'">首页</el-breadcrumb-item>
      <el-breadcrumb-item>{{ this.$route.meta.title }}</el-breadcrumb-item>
    </el-breadcrumb>

    <el-alert title="机构端-宠物档案查看（支持停用）" type="info" :closable="false" show-icon style="margin-bottom: 12px;" />

    <el-input
      placeholder="按宠物昵称搜索"
      prefix-icon="el-icon-search"
      v-model.trim="keyword"
      clearable
      style="width: 220px;"
    />
    <el-button type="primary" icon="el-icon-search" @click="doFilter" style="margin-left: 8px;">搜索</el-button>
    <el-button @click="resetFilter" style="margin-left: 8px;">重置</el-button>

    <el-table :data="tableData" style="width: 100%; margin-top: 12px;" border>
      <el-table-column prop="petId" label="宠物ID" width="90" />
      <el-table-column prop="petName" label="昵称" min-width="180" />
      <el-table-column label="宠物图片" width="120">
        <template slot-scope="scope">
          <el-image
            v-if="scope.row.avatar"
            :src="scope.row.avatar"
            fit="cover"
            style="width:48px;height:48px;border-radius:8px;border:1px solid #ebeef5;"
            :preview-src-list="[scope.row.avatar]">
          </el-image>
          <el-tag v-else size="mini" type="info">无图片</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="species" label="物种" width="100" />
      <el-table-column label="状态" width="100">
        <template slot-scope="scope">
          <el-tag :type="Number(scope.row.status) === 1 ? 'success' : 'info'">
            {{ Number(scope.row.status) === 1 ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="关注" width="100">
        <template slot-scope="scope">
          <el-tag :type="getFocusType(scope.row.focusLevel)">{{ getFocusText(scope.row.focusLevel) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="关注原因" width="160">
        <template slot-scope="scope">
          <span>{{ scope.row.focusReason || '暂无' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="breed" label="品种" />
      <el-table-column prop="birthDate" label="出生日期" width="130">
        <template slot-scope="scope">{{ formatDate(scope.row.birthDate) }}</template>
      </el-table-column>
      <el-table-column prop="weight" label="体重(kg)" width="110" />
      <el-table-column prop="gender" label="性别" width="90">
        <template slot-scope="scope">
          <span>{{ Number(scope.row.gender) === 1 ? '公' : '母' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template slot-scope="scope">
          <el-button type="text" @click="viewHealth(scope.row)">查看健康记录</el-button>
          <el-button
            v-if="Number(scope.row.status) === 1"
            type="text"
            style="color:#e6a23c;"
            @click="removePet(scope.row)">停用档案</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog title="健康记录" :visible.sync="healthVisible" width="60%">
      <el-table :data="healthRecords" border>
        <el-table-column prop="recordType" label="类型" width="100" />
        <el-table-column prop="recordDate" label="日期" width="130">
          <template slot-scope="scope">{{ formatDate(scope.row.recordDate) }}</template>
        </el-table-column>
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="diagnosis" label="诊断" />
        <el-table-column prop="prescription" label="建议" />
        <el-table-column prop="nextDate" label="下次提醒" width="130">
          <template slot-scope="scope">{{ formatDate(scope.row.nextDate) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import { getAllPetsForAdmin, deletePetByAdmin } from '@/api/PetAPI';
import { getHealthRecordsForAdmin } from '@/api/HealthAPI';

export default {
  name: 'AdminPetArchiveView',
  data() {
    return {
      keyword: '',
      allPets: [],
      tableData: [],
      healthVisible: false,
      healthRecords: []
    };
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
    async loadPets() {
      const list = await getAllPetsForAdmin();
      const rows = Array.isArray(list) ? list : [];
      rows.sort((a, b) => {
        const focusDiff = Number(b.focusLevel || 0) - Number(a.focusLevel || 0);
        if (focusDiff !== 0) return focusDiff;
        return Number(a.petId || 0) - Number(b.petId || 0);
      });
      this.allPets = rows;
      this.tableData = [...rows];
    },
    getFocusText(level) {
      if (Number(level) === 2) return '重点关注';
      if (Number(level) === 1) return '需关注';
      return '正常';
    },
    getFocusType(level) {
      if (Number(level) === 2) return 'danger';
      if (Number(level) === 1) return 'warning';
      return 'success';
    },
    doFilter() {
      const key = (this.keyword || '').trim();
      this.tableData = key
        ? this.allPets.filter(item => (item.petName || '').includes(key))
        : [...this.allPets];
    },
    resetFilter() {
      this.keyword = '';
      this.tableData = [...this.allPets];
    },
    async viewHealth(row) {
      const res = await getHealthRecordsForAdmin(row.petId);
      this.healthRecords = Array.isArray(res) ? res : [];
      this.healthVisible = true;
    },
    async removePet(row) {
      this.$confirm(`确定停用宠物档案【${row.petName}】吗？停用后将不可用于新预约。`, '提示', {
        type: 'warning'
      }).then(async () => {
        const admin = JSON.parse(localStorage.getItem('admin') || '{}');
        const ok = await deletePetByAdmin(row.petId, admin.adminId || null);
        if (ok) {
          this.$message.success('已停用');
          await this.loadPets();
        } else {
          this.$message.error('停用失败');
        }
      }).catch(() => {});
    }
  },
  async created() {
    await this.loadPets();
  }
};
</script>

<style lang="less" scoped></style>