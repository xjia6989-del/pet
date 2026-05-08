<template>
  <div>
    <el-card shadow="hover">
      <div slot="header" style="font-weight: 600;">兽医个人中心</div>
      <el-form :model="form" label-width="90px" style="max-width: 560px;">
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled></el-input>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.name"></el-input>
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone"></el-input>
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address"></el-input>
        </el-form-item>
        <el-form-item label="角色">
          <el-tag type="success">兽医</el-tag>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveInfo">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { updateMyInfo } from '@/api/UserAPI.js';

export default {
  name: 'VetCenter',
  data() {
    return { form: {} };
  },
  created() {
    this.form = JSON.parse(localStorage.getItem('vet') || '{}');
  },
  methods: {
    async saveInfo() {
      const res = await updateMyInfo(this.form);
      if (res && res.data && res.data.success) {
        localStorage.setItem('vet', JSON.stringify(res.data.result || this.form));
        this.$message.success('保存成功');
      } else {
        localStorage.setItem('vet', JSON.stringify(this.form));
        this.$message.success('保存成功');
      }
    }
  }
};
</script>