<template>
  <div>
    <el-breadcrumb separator="/" style="height: 35px;" separator-class="el-icon-arrow-right">
      <el-breadcrumb-item :to="'/adminHome'">首页</el-breadcrumb-item>
      <el-breadcrumb-item>{{ this.$route.meta.title }}</el-breadcrumb-item>
    </el-breadcrumb>

    <el-alert title="机构端-宠物档案查看（基础版）" type="info" :closable="false" show-icon style="margin-bottom: 12px;" />

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
      <el-table-column prop="petName" label="昵称" width="120" />
      <el-table-column prop="species" label="物种" width="100" />
      <el-table-column prop="breed" label="品种" />
      <el-table-column prop="birthDate" label="出生日期" width="130" />
      <el-table-column prop="weight" label="体重(kg)" width="110" />
      <el-table-column prop="gender" label="性别" width="90">
        <template slot-scope="scope">
          <span>{{ Number(scope.row.gender) === 1 ? '公' : '母' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template slot-scope="scope">
          <el-button type="text" @click="viewHealth(scope.row)">查看健康记录</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog title="健康记录" :visible.sync="healthVisible" width="60%">
      <el-table :data="healthRecords" border>
        <el-table-column prop="recordType" label="类型" width="100" />
        <el-table-column prop="recordDate" label="日期" width="130" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="diagnosis" label="诊断" />
        <el-table-column prop="prescription" label="建议" />
        <el-table-column prop="nextDate" label="下次提醒" width="130" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import { getPetList } from '@/api/PetAPI';
import { getHealthRecords } from '@/api/HealthAPI';

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
    async loadPets() {
      const list = await getPetList();
      this.allPets = Array.isArray(list) ? list : [];
      this.tableData = [...this.allPets];
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
      const res = await getHealthRecords(row.petId);
      this.healthRecords = res.success && Array.isArray(res.result) ? res.result : [];
      this.healthVisible = true;
    }
  },
  async created() {
    await this.loadPets();
  }
};
</script>

<style lang="less" scoped></style>