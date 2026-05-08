<template>
  <div class="pet-manager">
    <div class="page-hero">
      <div>
        <h2>宠物档案管理</h2>
        <p>记录宠物基础信息、疫苗记录与病史，并查看近期健康提醒</p>
      </div>
      <div style="display:flex;gap:10px;flex-wrap:wrap;">
        <el-button type="primary" round icon="el-icon-plus" @click="showAddDialog = true">添加宠物</el-button>
        <el-button round icon="el-icon-camera" @click="showAddDialog = true">拍照识别建档</el-button>
      </div>
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
        <el-table-column type="index" label="序号" width="70"></el-table-column>
        <el-table-column prop="petId" label="档案ID" width="90"></el-table-column>
        <el-table-column label="图片" width="110">
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
        <el-table-column label="昵称" min-width="180">
          <template slot-scope="scope">
            <div style="display:flex;align-items:center;gap:6px;">
              <span>{{ scope.row.petName }}</span>
              <el-tag size="mini" type="info">#{{ scope.row.petId }}</el-tag>
            </div>
          </template>
        </el-table-column>
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
          <el-select v-model="petForm.species" filterable placeholder="请选择物种">
            <el-option v-for="item in speciesOptions" :key="item" :label="item" :value="item"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="品种">
          <el-input v-model="petForm.breed"></el-input>
        </el-form-item>
        <el-form-item label="年龄">
          <el-input-number v-model="petForm.age" :min="0" :max="60"></el-input-number>
          <span v-if="petForm.aiAgeEstimate" style="margin-left:10px;color:#909399;font-size:12px;">AI预估：{{ petForm.aiAgeEstimate }}</span>
        </el-form-item>
        <el-form-item label="AI毛色">
          <el-input v-model="petForm.aiColor" placeholder="智能识别后自动填写"></el-input>
        </el-form-item>
        <el-form-item label="AI体型">
          <el-input v-model="petForm.aiSize" placeholder="智能识别后自动填写"></el-input>
        </el-form-item>
        <el-form-item label="AI性格">
          <el-input type="textarea" :rows="3" v-model="petForm.aiPersonality" placeholder="智能生成性格侧写"></el-input>
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
        <el-form-item label="头像/拍照">
          <el-upload
            action="http://localhost:9999/upload"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
            :on-change="handleImageChange">
            <el-button size="small" icon="el-icon-upload">上传/拍照选择</el-button>
          </el-upload>
          <div style="display:flex;align-items:center;gap:10px;flex-wrap:wrap;margin-top:10px;">
            <img v-if="petForm.avatar" :src="petForm.avatar" width="100" style="border-radius:8px;">
            <el-button size="small" type="success" icon="el-icon-camera" :loading="visionLoading" @click="recognizeVision">AI识别并回填</el-button>
          </div>
          <el-alert v-if="visionResultText" :title="visionResultText" type="success" show-icon :closable="false" style="margin-top:8px;"></el-alert>
          <div v-if="visionTip" style="margin-top:8px;color:#67c23a;font-size:12px;">{{ visionTip }}</div>
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
import { getPetList, addPet, updatePet, deletePet, getHealthReminders, recognizePetVision } from '@/api/PetAPI';

export default {
  data() {
    return {
      petList: [],
      healthReminders: [],
      speciesOptions: [
        '狗', '猫', '兔', '仓鼠', '龙猫', '豚鼠', '雪貂', '鹦鹉', '金丝雀', '乌龟', '蜥蜴', '蛇', '其他'
      ],
      showAddDialog: false,
      dialogTitle: '添加宠物',
      visionLoading: false,
      visionResultText: '',
      visionTip: '',
      visionFile: null,
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
        avatar: '',
        aiBreed: '',
        aiColor: '',
        aiSize: '',
        aiAgeEstimate: '',
        aiPersonality: '',
        aiBreed: '',
        aiColor: '',
        aiSize: '',
        aiAgeEstimate: '',
        aiPersonality: ''
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
      if (!this.petForm.petName || !this.petForm.petName.trim()) {
        this.$message.warning('请填写宠物昵称');
        return;
      }
      if (!this.petForm.species) {
        this.$message.warning('请选择宠物物种');
        return;
      }
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
      // 后端 /upload 接口返回的是图片URL字符串（不是 { url: ... } 对象）
      this.petForm.avatar = typeof res === 'string' ? res : (res && res.url ? res.url : '');
      if (!this.petForm.avatar) {
        this.$message.error('图片上传返回异常，请重试');
      }
    },
    handleImageChange(file) {
      this.visionFile = file && file.raw ? file.raw : null;
      this.visionTip = this.visionFile ? '已选择图片，可点击智能识别' : '';
    },
    async recognizeVision() {
      if (!this.visionFile) {
        this.$message.warning('请先上传宠物图片');
        return;
      }
      this.visionLoading = true;
      this.visionResultText = '正在识别中...';
      try {
        const formData = new FormData();
        formData.append('file', this.visionFile);
        const result = await recognizePetVision(formData);
        if (result && typeof result === 'object') {
          this.petForm.species = result.species || this.petForm.species;
          this.petForm.breed = result.breed || this.petForm.breed;
          this.petForm.aiBreed = result.breed || this.petForm.aiBreed;
          this.petForm.aiColor = result.color || this.petForm.aiColor;
          this.petForm.aiSize = result.size || this.petForm.aiSize;
          this.petForm.aiAgeEstimate = result.ageEstimate || this.petForm.aiAgeEstimate;
          this.petForm.aiPersonality = result.personalitySummary || this.petForm.aiPersonality;
          if (result.age) {
            this.petForm.age = result.age;
          } else if (result.ageEstimate) {
            const ageMap = { '幼年': 1, '成年': 3, '老年': 8 };
            this.petForm.age = ageMap[result.ageEstimate] || this.petForm.age;
          }
          if (result.color && !this.petForm.medicalHistory) {
            this.petForm.medicalHistory = `毛色：${result.color}；体型：${result.size || '需人工确认'}`;
          }
          this.visionResultText = `识别完成：${result.breed || '未知品种'}，${result.color || '未知毛色'}，${result.ageEstimate || '未知年龄阶段'}`;
          this.visionTip = `来源：${result.source || 'AI识别'}；置信度：${result.confidence != null ? result.confidence : '未知'}`;
          this.$message.success('识别成功，已回填表单');
        } else {
          this.visionResultText = '未获取到识别结果';
          this.$message.warning('识别结果为空');
        }
      } catch (e) {
        this.visionResultText = '识别失败，请稍后重试';
        this.$message.error('识别失败，请稍后重试');
      } finally {
        this.visionLoading = false;
      }
    },

    beforeAvatarUpload(file) {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png';
      if (!isJPG) {
        this.$message.error('只能上传 JPG/PNG 图片');
        return false;
      }
      const isLt5M = file.size / 1024 / 1024 < 5;
      if (!isLt5M) {
        this.$message.error('图片大小不能超过 5MB');
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
      this.visionFile = null;
      this.visionResultText = '';
      this.visionTip = '';
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
