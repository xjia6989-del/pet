<template>
  <div class="health-records-page">
    <div class="page-hero">
      <div>
        <h3>健康档案 - {{ petName }}</h3>
        <p>系统会根据记录类型和下次提醒时间，自动帮你整理和提示重点档案</p>
      </div>
      <div class="summary-box">
        <div class="summary-item">
          <div class="summary-value">{{ records.length }}</div>
          <div class="summary-label">记录总数</div>
        </div>
        <div class="summary-item">
          <div class="summary-value">{{ upcomingCount }}</div>
          <div class="summary-label">待提醒</div>
        </div>
        <div class="summary-item">
          <div class="summary-value">{{ typeCount }}</div>
          <div class="summary-label">分类数</div>
        </div>
      </div>
    </div>

    <el-card shadow="hover" class="smart-card" v-if="smartTips.length">
      <div slot="header" class="card-title">智能档案概览</div>
      <div class="overview-grid">
        <div class="overview-item">
          <div class="overview-title">风险等级</div>
          <div class="overview-value" :class="riskLevelClass">{{ riskLevelText }}</div>
        </div>
        <div class="overview-item">
          <div class="overview-title">重点关注</div>
          <div class="overview-value">{{ focusCount }} 条</div>
        </div>
        <div class="overview-item">
          <div class="overview-title">建议动作</div>
          <div class="overview-value">{{ nextAction }}</div>
        </div>
      </div>
      <el-divider></el-divider>
      <div class="advice-box">
        <div class="overview-title">智能建议摘要</div>
        <div class="advice-text">{{ adviceSummary }}</div>
      </div>
      <div class="tip-list" style="margin-top:12px;">
        <div v-for="(item, idx) in smartTips" :key="idx" class="tip-item">
          <el-tag size="mini" :type="item.type">{{ item.label }}</el-tag>
          <span>{{ item.text }}</span>
        </div>
      </div>
    </el-card>

    <el-card shadow="hover" class="table-card">
      <div class="filter-row">
        <el-select v-model="filterType" clearable placeholder="按类型筛选" style="width: 160px;">
          <el-option label="疫苗" value="疫苗"></el-option>
          <el-option label="体检" value="体检"></el-option>
          <el-option label="驱虫" value="驱虫"></el-option>
          <el-option label="治疗" value="治疗"></el-option>
        </el-select>
        <el-select v-model="filterReminder" clearable placeholder="按提醒状态筛选" style="width: 160px; margin-left: 10px;">
          <el-option label="即将到期" value="upcoming"></el-option>
          <el-option label="已过期" value="overdue"></el-option>
          <el-option label="正常" value="normal"></el-option>
        </el-select>
      </div>
      <el-table :data="filteredRecords" style="width: 100%" :row-class-name="rowClassName">
        <el-table-column prop="recordType" label="类型" width="100"></el-table-column>
        <el-table-column prop="recordTag" label="标签" width="100">
          <template slot-scope="scope">
            <el-tag size="mini" type="info">{{ scope.row.recordTag || scope.row.recordType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="recordDate" label="日期" width="120">
          <template slot-scope="scope">{{ formatDate(scope.row.recordDate) }}</template>
        </el-table-column>
        <el-table-column prop="description" label="描述"></el-table-column>
        <el-table-column prop="diagnosis" label="诊断"></el-table-column>
        <el-table-column prop="prescription" label="处方/建议"></el-table-column>
        <el-table-column prop="nextDate" label="下次提醒" width="120">
          <template slot-scope="scope">{{ formatDate(scope.row.nextDate) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getReminderTagType(scope.row)">{{ getReminderText(scope.row) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-button class="back-btn" @click="$router.back()">返回</el-button>
  </div>
</template>

<script>
import { getHealthRecords } from '@/api/HealthAPI';
import { getPetList } from '@/api/PetAPI';

export default {
  data() {
    return {
      petId: null,
      petName: '',
      records: [],
      filterType: '',
      filterReminder: '',
      adviceSummary: '正在生成智能建议...'
    };
  },
  computed: {
    filteredRecords() {
      return this.records.filter(item => {
        const typeMatch = !this.filterType || item.recordType === this.filterType;
        const reminder = this.getReminderLevel(item);
        const reminderMatch = !this.filterReminder || reminder === this.filterReminder;
        return typeMatch && reminderMatch;
      });
    },
    upcomingCount() {
      return this.records.filter(item => this.getReminderLevel(item) === 'upcoming').length;
    },
    overdueCount() {
      return this.records.filter(item => this.getReminderLevel(item) === 'overdue').length;
    },
    typeCount() {
      return new Set(this.records.map(item => item.recordType)).size;
    },
    focusCount() {
      return this.records.filter(item => this.getReminderLevel(item) !== 'normal').length;
    },
    riskLevelText() {
      if (this.overdueCount > 0) return '较高';
      if (this.upcomingCount > 0) return '中等';
      return '正常';
    },
    riskLevelClass() {
      if (this.overdueCount > 0) return 'risk-high';
      if (this.upcomingCount > 0) return 'risk-mid';
      return 'risk-low';
    },
    nextAction() {
      if (this.overdueCount > 0) return '优先处理过期档案';
      if (this.upcomingCount > 0) return '关注即将到期档案';
      if (this.records.length) return '继续保持常规管理';
      return '等待新增档案';
    },
    smartTips() {
      const tips = [];
      const typeSet = new Set(this.records.map(item => item.recordType));
      if (typeSet.has('疫苗')) {
        tips.push({ label: '疫苗', type: 'success', text: '系统已识别到疫苗档案，后续会根据下次提醒时间自动提示接种。' });
      }
      if (typeSet.has('体检')) {
        tips.push({ label: '体检', type: 'warning', text: '系统已识别到体检档案，建议按复查时间持续关注。' });
      }
      if (typeSet.has('驱虫')) {
        tips.push({ label: '驱虫', type: 'info', text: '系统已识别到驱虫档案，后续可按周期继续提醒。' });
      }
      const overdueCount = this.overdueCount;
      if (overdueCount > 0) {
        tips.push({ label: '过期', type: 'danger', text: `当前有 ${overdueCount} 条档案已过期，建议尽快处理。` });
      }
      const upcomingCount = this.upcomingCount;
      if (upcomingCount > 0) {
        tips.push({ label: '提醒', type: 'warning', text: `当前有 ${upcomingCount} 条档案即将到期，请留意。` });
      }
      if (!tips.length && this.records.length) {
        tips.push({ label: '提示', type: 'info', text: '当前宠物已有健康档案记录，系统会自动按下次提醒时间进行管理。' });
      }
      return tips;
    }
  },
  created() {
    this.petId = this.$route.params.petId;
    this.petName = this.$route.query.name || '宠物';
    this.fetchRecords();
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
   getReminderLevel(item) {
     if (!item.nextDate) {
       return 'normal';
     }
     if (item.reminderStatus === 3) return 'overdue';
     if (item.reminderStatus === 2) return 'upcoming';
     if (item.reminderStatus === 1) return 'normal';
     const diff = new Date(item.nextDate).getTime() - new Date().getTime();
     const day = 24 * 60 * 60 * 1000;
     if (diff < 0) return 'overdue';
     if (diff <= 7 * day) return 'upcoming';
     return 'normal';
   },
   getReminderText(item) {
     const level = this.getReminderLevel(item);
     if (level === 'overdue') return '已过期';
     if (level === 'upcoming') return '待提醒';
     return '正常';
   },
   getReminderTagType(item) {
     const level = this.getReminderLevel(item);
     if (level === 'overdue') return 'danger';
     if (level === 'upcoming') return 'warning';
     return 'success';
   },
   rowClassName({ row }) {
     const level = this.getReminderLevel(row);
     if (level === 'overdue') return 'row-overdue';
     if (level === 'upcoming') return 'row-upcoming';
     return '';
   },
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
     }).then(async () => {
       try {
         const pets = await getPetList(userId);
         const pet = Array.isArray(pets) ? pets.find(item => Number(item.petId) === Number(this.petId)) : null;
         if (pet) {
           const age = pet.age || 0;
           const weight = pet.weight || 0;
           if (age > 0 && age < 1) {
             this.adviceSummary = '建议优先关注疫苗接种和基础体检';
           } else if (age >= 8) {
             this.adviceSummary = '建议适当提高体检频率，关注老年宠物健康变化';
           } else if (weight >= 10) {
             this.adviceSummary = '建议注意饮食管理与运动安排，保持健康体重';
           } else {
             this.adviceSummary = '建议按常规节奏进行体检、驱虫和疫苗管理';
           }
         }
       } catch (e) {
         this.adviceSummary = '建议按常规节奏进行体检、驱虫和疫苗管理';
       }
     }).catch(() => {
       this.$message.error('获取健康记录失败');
     });
   }
  }
};
</script>

<style scoped>
.health-records-page { padding: 8px; }
.page-hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  padding: 16px 18px;
  border-radius: 14px;
  background: linear-gradient(135deg, #eef4ff, #f7fbff);
  border: 1px solid #e2ecff;
  margin-bottom: 14px;
}
.page-hero h3 { margin-bottom: 6px; color: #2d3f62; }
.page-hero p { color: #7a8ba8; font-size: 13px; }
.summary-box { display: flex; gap: 12px; }
.summary-item {
  min-width: 88px;
  text-align: center;
  padding: 10px 12px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #edf2ff;
}
.summary-value { font-size: 22px; font-weight: 700; color: #2f64f5; }
.summary-label { font-size: 12px; color: #7a8ba8; margin-top: 4px; }
.smart-card, .table-card { margin-bottom: 14px; }
.card-title { font-weight: 600; color: #2d3f62; }
.tip-list { display: grid; gap: 10px; }
.tip-item { display: flex; align-items: center; gap: 8px; color: #51607a; }
.back-btn { margin-top: 6px; }
.filter-row { margin-bottom: 12px; }
.row-overdue { background: #fff5f5; }
.row-upcoming { background: #fffaf0; }
.overview-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.overview-item {
  padding: 12px;
  border: 1px solid #eef2fa;
  border-radius: 10px;
  background: #fff;
}
.overview-title { font-size: 12px; color: #7a8ba8; margin-bottom: 6px; }
.overview-value { font-size: 18px; font-weight: 700; color: #2d3f62; }
.risk-high { color: #f56c6c; }
.risk-mid { color: #e6a23c; }
.risk-low { color: #67c23a; }
.advice-box {
  padding: 12px;
  background: #f8fbff;
  border: 1px solid #e8eef8;
  border-radius: 10px;
  margin-top: 8px;
}
.advice-text { color: #4d5f7a; margin-top: 6px; }
</style>