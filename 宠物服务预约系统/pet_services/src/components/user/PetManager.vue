<template>
  <div class="pet-manager">
    <div class="page-hero">
      <div>
        <h2>宠物档案管理</h2>
        <p>记录宠物基础信息、疫苗记录与病史，并查看近期健康提醒</p>
      </div>
      <el-button type="primary" round icon="el-icon-plus" @click="showAddDialog = true">添加宠物</el-button>
    </div>

    <el-card class="panel-card" shadow="hover">
      <div class="hint-title">健康管理提醒</div>
      <div class="hint-sub">当前为页面内提醒展示模式（不做站外推送）</div>
      <div v-if="healthReminders.length" class="reminder-list">
        <div v-for="item in healthReminders" :key="`${item.petId}-${item.reminderType}-${item.dueDate}`" class="reminder-item">
          <el-tag size="mini" type="warning" style="margin-right:8px;">{{ item.reminderType }}</el-tag>
          <el-tag size="mini" :type="item.level === 'overdue' ? 'danger' : 'success'" style="margin-right:8px;">
            {{ item.level === 'overdue' ? '已逾期' : '即将到期' }}
          </el-tag>
          <el-tag size="mini" type="info" style="margin-right:8px;">
            {{ item.source === 'healthRecord' ? '健康档案' : '系统估算' }}
          </el-tag>
          {{ item.petName }}：{{ item.reminderText }}（提醒日期 {{ item.dueDate }}）
        </div>
      </div>
      <el-empty v-else description="暂无近期健康提醒" :image-size="72"></el-empty>
    </el-card>

    <el-card class="panel-card" shadow="hover" style="margin-top:12px;">
      <el-table :data="petList" style="width: 100%" border>
        <el-table-column prop="petName" label="昵称"></el-table-column>
        <el-table-column prop="species" label="物种"></el-table-column>
        <el-table-column prop="breed" label="品种"></el-table-column>
        <el-table-column prop="age" label="年龄" width="80"></el-table-column>
        <el-table-column prop="vaccineRecord" label="疫苗记录" show-overflow-tooltip></el-table-column>
        <el-table-column prop="medicalHistory" label="病史" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" width="240">
          <template slot-scope="scope">
            <el-button type="text" @click="editPet(scope.row)">编辑</el-button>
            <el-button type="text" @click="deletePet(scope.row.petId)">删除</el-button>
            <el-button type="text" @click="viewHealth(scope.row.petId, scope.row.petName)">健康档案</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="dialogTitle" :visible.sync="showAddDialog" @close="resetForm" width="620px">
      <el-form :model="petForm" label-width="90px">
        <el-form-item label="昵称">
          <el-input v-model="petForm.petName"></el-input>
        </el-form-item>
        <el-form-item label="物种">
          <el-select v-model="petForm.species">
            <el-option label="狗" value="狗"></el-option>
            <el-option label="猫" value="猫"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="品种">
          <el-input v-model="petForm.breed"></el-input>
        </el-form-item>
        <el-form-item label="年龄">
          <el-input-number v-model="petForm.age" :min="0" :max="60"></el-input-number>
        </el-form-item>
        <el-form-item label="疫苗记录">
          <el-input type="textarea" :rows="3" v-model="petForm.vaccineRecord"></el-input>
        </el-form-item>
        <el-form-item label="病史">
          <el-input type="textarea" :rows="3" v-model="petForm.medicalHistory"></el-input>
        </el-form-item>
        <el-form-item label="出生日期">
          <el-date-picker v-model="petForm.birthDate" type="date" placeholder="选择日期" value-format="yyyy-MM-dd"></el-date-picker>
        </el-form-item>
        <el-form-item label="体重(kg)">
          <el-input-number v-model="petForm.weight" :precision="2" :min="0"></el-input-number>
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="petForm.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="头像">
          <el-upload
            action="http://localhost:9999/upload"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload">
            <el-button size="small">上传图片</el-button>
          </el-upload>
          <img v-if="petForm.avatar" :src="petForm.avatar" width="100" style="margin-top:10px; border-radius:8px;">
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="submitPet">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getPetList, addPet, updatePet, deletePet, getHealthReminders } from '@/api/PetAPI';

export default {
  data() {
    return {
      petList: [],
      healthReminders: [],
      showAddDialog: false,
      dialogTitle: '添加宠物',
      petForm: {
        petId: null,
        petName: '',
        species: '',
        breed: '',
        age: null,
        vaccineRecord: '',
        medicalHistory: '',
        birthDate: '',
        weight: null,
        gender: 1,
        avatar: ''
      }
    };
  },
  created() {
    this.fetchPets();
  },
  methods: {
    async fetchPets() {
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      const userId = user.userId;
      if (!userId) {
        this.$message.warning('登录状态已失效，请重新登录');
        this.$router.push('/login');
        return;
      }
      getPetList(userId).then(res => {
        if (Array.isArray(res)) {
          this.petList = res;
          this.loadHealthReminders();
        } else {
          this.petList = [];
          this.healthReminders = [];
        }
      }).catch(() => {
        this.$message.error('获取宠物列表失败');
      });
    },
    async loadHealthReminders() {
      try {
        const user = JSON.parse(localStorage.getItem('user') || '{}');
        const userId = user.userId;
        if (!userId) {
          this.healthReminders = [];
          return;
        }
        const reminders = await getHealthReminders(userId, 'display');
        this.healthReminders = Array.isArray(reminders) ? reminders.slice(0, 8) : [];
      } catch (e) {
        this.healthReminders = [];
      }
    },
    editPet(row) {
      this.dialogTitle = '编辑宠物';
      this.petForm = { ...row };
      this.showAddDialog = true;
    },
    deletePet(petId) {
      this.$confirm('确定删除该宠物吗？', '提示', { type: 'warning' }).then(() => {
        const user = JSON.parse(localStorage.getItem('user') || '{}');
        const userId = user.userId;
        if (!userId) {
          this.$message.warning('登录状态已失效，请重新登录');
          this.$router.push('/login');
          return;
        }
        deletePet(petId, userId).then(res => {
          if (res === true) {
            this.$message.success('删除成功');
            this.fetchPets();
          } else {
            this.$message.error('删除失败');
          }
        });
      }).catch(() => {});
    },
    submitPet() {
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      const userId = user.userId;
      if (!userId) {
        this.$message.warning('登录状态已失效，请重新登录');
        this.$router.push('/login');
        return;
      }
      const api = this.petForm.petId ? updatePet : addPet;
      api(this.petForm, userId).then(res => {
        if ((this.petForm.petId && res === true) || (!this.petForm.petId && res && res.petId)) {
          this.$message.success(this.petForm.petId ? '修改成功' : '添加成功');
          this.showAddDialog = false;
          this.fetchPets();
        } else {
          this.$message.error('操作失败');
        }
      }).catch(() => {
        this.$message.error('操作失败');
      });
    },
    handleAvatarSuccess(res) {
      this.petForm.avatar = res.url;
    },
    beforeAvatarUpload(file) {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png';
      if (!isJPG) {
        this.$message.error('只能上传 JPG/PNG 图片');
        return false;
      }
      const isLt2M = file.size / 1024 / 1024 < 2;
      if (!isLt2M) {
        this.$message.error('图片大小不能超过 2MB');
        return false;
      }
      return true;
    },
    resetForm() {
      this.petForm = {
        petId: null,
        petName: '',
        species: '',
        breed: '',
        age: null,
        vaccineRecord: '',
        medicalHistory: '',
        birthDate: '',
        weight: null,
        gender: 1,
        avatar: ''
      };
      this.dialogTitle = '添加宠物';
    },
    viewHealth(petId, petName) {
      this.$router.push(`/userHome/health/${petId}?name=${petName}`);
    }
  }
};
</script>

<style scoped>
.pet-manager { padding: 8px; }
.page-hero {
  margin-bottom: 14px;
  padding: 16px 18px;
  border-radius: 14px;
  background: linear-gradient(135deg, #eef4ff, #e4eeff);
  border: 1px solid #dbe8ff;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.page-hero h2 { color: #2f3f63; margin-bottom: 4px; }
.page-hero p { color: #7c8eaf; font-size: 13px; }
.panel-card { border-radius: 14px; }
.hint-title { font-weight: 600; color: #2f3f63; margin-bottom: 4px; }
.hint-sub { color: #8a9bb9; font-size: 12px; margin-bottom: 10px; }
.reminder-list { display: grid; gap: 8px; }
.reminder-item {
  background: #fff8e9;
  border: 1px solid #ffe5b5;
  border-radius: 10px;
  padding: 8px 10px;
  color: #6f5834;
}
</style>
