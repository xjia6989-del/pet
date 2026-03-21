<template>
  <div>
    <h3>健康档案 - {{ petName }}</h3>
    <el-table :data="records" style="width: 100%">
      <el-table-column prop="recordType" label="类型"></el-table-column>
      <el-table-column prop="recordDate" label="日期"></el-table-column>
      <el-table-column prop="description" label="描述"></el-table-column>
      <el-table-column prop="diagnosis" label="诊断"></el-table-column>
      <el-table-column prop="prescription" label="处方/建议"></el-table-column>
      <el-table-column prop="nextDate" label="下次提醒"></el-table-column>
    </el-table>
    <el-button @click="$router.back()">返回</el-button>
  </div>
</template>

<script>
import { getHealthRecords } from '@/api/HealthAPI';

export default {
  data() {
    return {
      petId: null,
      petName: '',
      records: []
    };
  },
  created() {
    this.petId = this.$route.params.petId;
    this.petName = this.$route.query.name || '宠物';
    this.fetchRecords();
  },
  methods: {
   fetchRecords() {
     const user = JSON.parse(localStorage.getItem('user') || '{}');
     const userId = user.userId;
     if (!userId) {
       this.$message.warning('登录状态已失效，请重新登录');
       this.$router.push('/login');
       return;
     }
     getHealthRecords(this.petId, userId).then(res => {
       if (res.success && Array.isArray(res.result)) {
         this.records = res.result;
       } else {
         this.records = [];
         this.$message.warning('暂无健康记录');
       }
     }).catch(() => {
       this.$message.error('获取健康记录失败');
     });
   }
  }
};
</script>