<template>
  <div class="store-page">
    <div class="page-hero">
      <div>
        <h2>健康护理预约</h2>
        <p>选择适合毛孩子的健康护理项目，并快速完成预约</p>
      </div>
    </div>

    <el-card class="panel-card" shadow="hover">
      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索服务名称" prefix-icon="el-icon-search" clearable />
        <el-select v-model="filterStatus" placeholder="全部" class="filter-select">
          <el-option label="全部" value="all" />
          <el-option v-for="item in categoryList" :key="item.categoryId" :label="item.categoryName" :value="item.categoryId" />
        </el-select>
      </div>

      <div class="store-list">
        <el-row :gutter="20">
          <el-col v-for="serve in filteredServes" :key="serve.serveId" :xs="24" :sm="12" :md="8" :lg="6">
            <div class="store-card">
              <div class="store-image">
                <img :src="serve.serveImage" alt="服务图片" />
                <div class="status-tag available">{{ getCategoryName(serve.category) }}</div>
              </div>
              <div class="store-info">
                <h3 class="store-name">{{ serve.serveName }}</h3>
                <div class="store-address">描述：{{ serve.serveDesc }}</div>
                <div class="store-foot">
                  <div class="store-price">{{ serve.price + '￥' }}</div>
                  <div class="adoptBtn" @click="booking(serve.serveId)">立即预约</div>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <el-dialog title="预约信息" :visible.sync="dialogFormVisible" width="560px">
      <el-form :model="bookingForm">
        <el-form-item label="选择宠物" :label-width="formLabelWidth">
          <el-select v-model="bookingForm.petId" placeholder="请选择宠物">
            <el-option v-for="pet in petList" :key="pet.petId" :label="pet.petName" :value="pet.petId"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="预约日期" :label-width="formLabelWidth">
          <el-date-picker
            v-model="bookingForm.bookingDay"
            type="date"
            :picker-options="pickerOptions"
            format="yyyy-MM-dd"
            value-format="yyyy-MM-dd"
            @change="loadAvailableSlots">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="时间段" :label-width="formLabelWidth">
          <el-select v-model="bookingForm.timeSlot" placeholder="请选择时间段" style="width: 220px;">
            <el-option
              v-for="slot in allSlots"
              :key="slot"
              :label="slot"
              :value="slot">
            </el-option>
          </el-select>
          <div style="margin-top:6px;color:#909399;font-size:12px;">系统将自动校验容量，默认每个时间段至少可预约 1 个名额。</div>
        </el-form-item>

      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="bookingSubmit()">确定预约</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getAllServe } from '@/api/ServeAPI.js';
import { getAllCategory } from '@/api/CategoryAPI.js'
import { addBooking, getAvailableSlots } from '@/api/BookingAPI.js'
import { getPetList } from '@/api/PetAPI';

export default {
  name: 'SmartCommunityStores',
  data() {
    return {
      searchKeyword: '',
      filterStatus: 'all',
      serves: [],
      categoryList: [],
      petList: [],
      bookingForm: {
        bookingDate: '',
        bookingDay: '',
        timeSlot: '',
        userId: '',
        serve: '',
        petId: null
      },
      availableSlots: [],
      allSlots: ['09:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00'],
      dialogFormVisible: false,
      formLabelWidth: '120px',
      pickerOptions: {
        disabledDate(time) {
          const now = new Date();
          now.setHours(0, 0, 0, 0);
          return time.getTime() < now.getTime();
        }
      }
    };
  },
  computed: {
    filteredServes() {
      return this.serves.filter(serve => {
        const keyword = (this.searchKeyword || '').trim();
        const keywordMatch = !keyword || (serve.serveName || '').includes(keyword);
        const categoryMatch = this.filterStatus === 'all' || String(serve.category) === String(this.filterStatus);
        return keywordMatch && categoryMatch;
      });
    }
  },
  methods: {
    async bookingSubmit() {
      if (!this.bookingForm.petId) {
        this.$message.error('请选择宠物');
        return;
      }
      if (!this.bookingForm.bookingDay) {
        this.$message.error('请选择预约日期');
        return;
      }
      if (!this.bookingForm.timeSlot) {
        this.$message.error('请选择时间段');
        return;
      }

      const bookingDate = `${this.bookingForm.bookingDay} ${this.bookingForm.timeSlot}:00`;
      const payload = { ...this.bookingForm, bookingDate };

      const res = await addBooking(payload);
      if (res && res.bookingId) {
        this.$message.success('预约成功!');
        this.bookingForm.timeSlot = '';
        this.availableSlots = [...this.allSlots];
        this.dialogFormVisible = false;
        this.$router.push('/userHome/myBooking');
      } else {
        this.$message.error('该时间段已被预约满，请重新选择');
        await this.loadAvailableSlots();
      }
    },
    booking(serveId) {
      this.bookingForm.serve = serveId;
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      this.bookingForm.userId = user.userId || 1;
      this.bookingForm.bookingDate = '';
      this.bookingForm.bookingDay = '';
      this.bookingForm.timeSlot = '';
      this.bookingForm.petId = null;
      this.availableSlots = [];
      this.dialogFormVisible = true;
    },
    getCategoryName(categoryId) {
      const cat = this.categoryList.find(item => item.categoryId == categoryId);
      return cat ? cat.categoryName : '未知';
    },
    async loadAvailableSlots() {
      if (!this.bookingForm.serve || !this.bookingForm.bookingDay) {
        this.availableSlots = [];
        this.bookingForm.timeSlot = '';
        return;
      }
      try {
        const slots = await getAvailableSlots(this.bookingForm.serve, this.bookingForm.bookingDay);
        this.availableSlots = Array.isArray(slots) ? slots : [];
        if (!this.availableSlots.includes(this.bookingForm.timeSlot)) {
          this.bookingForm.timeSlot = '';
        }
      } catch (e) {
        this.availableSlots = [];
        this.bookingForm.timeSlot = '';
        this.$message.error('获取可预约时间段失败');
      }
    }
  },
  async created() {
    try {
      const serves = await getAllServe();
      this.serves = Array.isArray(serves) ? serves : [];
    } catch (e) {
      this.serves = [];
    }

    try {
      const { data: categoryRes } = await getAllCategory();
      this.categoryList = Array.isArray(categoryRes?.result) ? categoryRes.result : [];
    } catch (e) {
      this.categoryList = [];
    }

    try {
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      const userId = user.userId || 1;
      const pets = await getPetList(userId);
      this.petList = Array.isArray(pets) ? pets : [];
    } catch (e) {
      this.petList = [];
    }
  }
};
</script>

<style lang="less" scoped>
.store-page { padding: 8px; }
.page-hero {
  margin-bottom: 14px;
  padding: 16px 18px;
  border-radius: 14px;
  background: linear-gradient(135deg, #ebfbf7, #dff7ef);
  border: 1px solid #d1f1e5;
}
.page-hero h2 { color: #2f3f63; margin-bottom: 4px; }
.page-hero p { color: #7c8eaf; font-size: 13px; }
.panel-card { padding: 10px; }
.search-bar { display: flex; gap: 12px; margin-bottom: 18px; }
.search-bar .el-input { flex: 1; }
.filter-select { width: 160px; }
.adoptBtn {
  height: 30px;
  width: 92px;
  background: linear-gradient(135deg, #ffa46b, #ff8f5c);
  border-radius: 20px;
  color: #fff;
  font-size: 13px;
  text-align: center;
  line-height: 30px;
  cursor: pointer;
}
.store-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 6px 16px rgba(70, 120, 200, 0.1);
  margin-bottom: 16px;
  overflow: hidden;
  transition: transform 0.25s;
  border: 1px solid #ebf1ff;
}
.store-card:hover { transform: translateY(-4px); }
.store-image { position: relative; height: 180px; }
.store-image img { width: 100%; height: 100%; object-fit: cover; }
.status-tag {
  position: absolute; top: 10px; right: 10px; padding: 6px 12px;
  border-radius: 20px; font-size: 12px; color: white;
}
.status-tag.available { background: #67c23a; }
.store-info { padding: 14px; }
.store-name { font-size: 17px; margin: 0 0 8px; color: #303133; }
.store-price { color: #ff8a52; font-weight: bold; margin: 8px 0; }
.store-address {
  min-height: 52px; color: #606266; font-size: 13px;
  display: -webkit-box; -webkit-box-orient: vertical; -webkit-line-clamp: 3;
  overflow: hidden;
}
.store-foot { display: flex; justify-content: space-between; align-items: center; }
</style>
