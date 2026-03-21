<template>
  <div>
    <el-breadcrumb separator="/" style="height: 35px;" separator-class="el-icon-arrow-right">
      <el-breadcrumb-item :to="'/adminHome'">首页</el-breadcrumb-item>
      <el-breadcrumb-item>{{ this.$route.meta.title }}</el-breadcrumb-item>
    </el-breadcrumb>

    <el-alert title="机构端-健康记录录入（基础版）" type="success" :closable="false" show-icon style="margin-bottom: 16px;" />

    <el-card>
      <el-form :inline="true" :model="form">
        <el-form-item label="宠物">
          <el-select v-model="form.petId" placeholder="请选择宠物" @change="fetchRecords" style="width: 220px;">
            <el-option v-for="item in petList" :key="item.petId" :label="item.petName" :value="item.petId" />
          </el-select>
        </el-form-item>
        <el-form-item label="记录类型">
          <el-select v-model="form.recordType" placeholder="请选择类型" style="width: 180px;">
            <el-option label="体检" value="体检" />
            <el-option label="疫苗" value="疫苗" />
            <el-option label="驱虫" value="驱虫" />
            <el-option label="治疗" value="治疗" />
          </el-select>
        </el-form-item>
        <el-form-item label="记录日期">
          <el-date-picker v-model="form.recordDate" type="date" value-format="yyyy-MM-dd" />
        </el-form-item>
      </el-form>

      <el-form :model="form" label-width="100px">
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="诊断结果">
          <el-input v-model="form.diagnosis" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="处方建议">
          <el-input v-model="form.prescription" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="下次提醒">
          <el-date-picker v-model="form.nextDate" type="date" value-format="yyyy-MM-dd" />
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="submitRecord">保存健康记录</el-button>
    </el-card>

    <el-table :data="recordList" border style="width: 100%; margin-top: 16px;">
      <el-table-column prop="recordId" label="记录ID" width="90" />
      <el-table-column prop="recordType" label="类型" width="100" />
      <el-table-column prop="recordDate" label="日期" width="130" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="diagnosis" label="诊断" />
      <el-table-column prop="prescription" label="处方建议" />
      <el-table-column prop="nextDate" label="下次提醒" width="130" />
      <el-table-column label="操作" width="100">
        <template slot-scope="scope">
          <el-button type="text" @click="deleteRow(scope.row.recordId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import { getAllPetsForAdmin } from '@/api/PetAPI';
import { addHealthRecord, getHealthRecords, deleteHealthRecord } from '@/api/HealthAPI';

export default {
  name: 'HealthRecordEntry',
  data() {
    return {
      petList: [],
      recordList: [],
      form: {
        petId: null,
        recordType: '',
        recordDate: '',
        description: '',
        diagnosis: '',
        prescription: '',
        nextDate: ''
      }
    };
  },
  methods: {
    async fetchPets() {
      const list = await getAllPetsForAdmin();
      this.petList = Array.isArray(list) ? list : [];
      if (!this.form.petId && this.petList.length > 0) {
        this.form.petId = this.petList[0].petId;
        await this.fetchRecords();
      }
    },
    async fetchRecords() {
      if (!this.form.petId) return;
      const res = await getHealthRecords(this.form.petId);
      this.recordList = res.success && Array.isArray(res.result) ? res.result : [];
    },
    async submitRecord() {
      if (!this.form.petId || !this.form.recordType || !this.form.recordDate) {
        this.$message.error('请至少选择宠物、记录类型和记录日期');
        return;
      }
      const res = await addHealthRecord(this.form);
      if (res.success && res.result) {
        this.$message.success('保存成功');
        this.form.recordType = '';
        this.form.recordDate = '';
        this.form.description = '';
        this.form.diagnosis = '';
        this.form.prescription = '';
        this.form.nextDate = '';
        await this.fetchRecords();
      } else {
        this.$message.error('保存失败');
      }
    },
    async deleteRow(recordId) {
      const res = await deleteHealthRecord(recordId);
      if (res.success && res.result) {
        this.$message.success('删除成功');
        await this.fetchRecords();
      } else {
        this.$message.error('删除失败');
      }
    }
  },
  async created() {
    await this.fetchPets();
  }
};
</script>

<style scoped></style>