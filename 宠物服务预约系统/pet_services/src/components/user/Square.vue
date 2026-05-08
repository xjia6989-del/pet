<template>
  <div class="square-page">
    <section class="hero">
      <div class="hero-overlay"></div>
      <div class="hero-content">
        <div class="hero-badge">🐾 萌宠乐园</div>
        <h1 class="hero-title">欢迎来到宠物健康护理中心</h1>
        <p class="hero-description">在这里，您可以预约体检、疫苗与康复等护理服务，记录宠物成长并持续关注健康状态。</p>
        <div class="hero-actions">
          <el-button type="primary" round @click="$router.push('/userHome/petServices')">立即预约</el-button>
          <el-button round @click="$router.push('/userHome/myInfo')">进入个人中心</el-button>
        </div>
      </div>
    </section>

    <section class="quick panel">
      <div class="section-head">
        <h2 class="section-title">今日快捷入口</h2>
        <span class="section-sub">常用功能一键到达，不再和顶部导航重复堆叠</span>
      </div>
      <div class="quick-grid">
        <div class="quick-card quick-a" @click="$router.push('/userHome/myBooking')">
          <div class="quick-icon">📅</div>
          <div class="quick-name">我的预约记录</div>
          <div class="quick-desc">查看待处理与历史预约</div>
        </div>
        <div class="quick-card quick-b" @click="$router.push('/userHome/myPets')">
          <div class="quick-icon">🐶</div>
          <div class="quick-name">宠物档案</div>
          <div class="quick-desc">维护宠物资料与健康信息</div>
        </div>
        <div class="quick-card quick-c" @click="$router.push('/userHome/myInfo')">
          <div class="quick-icon">🙋</div>
          <div class="quick-name">个人中心</div>
          <div class="quick-desc">修改资料、查看通知、联系客服</div>
        </div>
      </div>
    </section>

    <section class="services panel">
      <div class="section-head">
        <h2 class="section-title">热门健康护理</h2>
        <span class="section-sub">精选护理项目，守护毛孩子健康</span>
      </div>
      <div class="service-cards">
        <div
          v-for="(service, index) in services"
          :key="index"
          class="service-card"
          :style="{ animationDelay: `${index * 0.08}s` }">
          <img :src="service.serveImage" alt="服务图片" class="service-image" />
          <h3 class="service-name">{{ service.serveName }}</h3>
          <p class="service-description">{{ service.serveDesc }}</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import { getAllServe } from '@/api/ServeAPI.js';

export default {
  name: 'PetSquare',
  data() {
    return {
      services: []
    };
  },
  async created() {
    const serves = await getAllServe();
    this.services = Array.isArray(serves) ? serves : [];
  }
};
</script>

<style scoped>
.square-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hero {
  position: relative;
  min-height: 300px;
  border-radius: 18px;
  padding: 36px;
  display: flex;
  align-items: center;
  overflow: hidden;
  background-image: url('https://images.unsplash.com/photo-1548199973-03cce0bbc87b?auto=format&fit=crop&w=1400&q=80');
  background-size: cover;
  background-position: center;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.88) 0%, rgba(255, 255, 255, 0.62) 48%, rgba(255, 255, 255, 0.25) 100%);
}

.hero-content {
  position: relative;
  max-width: 760px;
}

.hero-badge {
  display: inline-block;
  background: #fff4de;
  color: #d6872c;
  border: 1px solid #ffe7bb;
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 13px;
  margin-bottom: 10px;
}

.hero-title {
  font-size: 34px;
  font-weight: 800;
  color: #243251;
  margin-bottom: 8px;
}

.hero-description {
  color: #4f6288;
  font-size: 15px;
  line-height: 1.9;
}

.hero-actions {
  margin-top: 16px;
  display: flex;
  gap: 10px;
}

.panel {
  background: #ffffffd6;
  border: 1px solid #e7efff;
  border-radius: 14px;
  padding: 20px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 14px;
}

.section-title {
  font-size: 20px;
  color: #2c3f63;
}

.section-sub {
  color: #8193b5;
  font-size: 13px;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 12px;
}

.quick-card {
  border-radius: 12px;
  padding: 14px;
  cursor: pointer;
  transition: all 0.25s ease;
  border: 1px solid transparent;
}

.quick-card:hover {
  transform: translateY(-3px);
}

.quick-a { background: linear-gradient(135deg, #eef4ff, #e4eeff); border-color: #dbe8ff; }
.quick-b { background: linear-gradient(135deg, #ebfbf7, #dff7ef); border-color: #d1f1e5; }
.quick-c { background: linear-gradient(135deg, #fff4ea, #ffeede); border-color: #ffe2c9; }

.quick-icon { font-size: 22px; margin-bottom: 6px; }
.quick-name { font-weight: 700; color: #2e4068; margin-bottom: 4px; }
.quick-desc { color: #6c7fa3; font-size: 13px; }

.service-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 14px;
}

.service-card {
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  border: 1px solid #e7efff;
  border-radius: 12px;
  padding: 14px;
  opacity: 0;
  transform: translateY(14px);
  animation: fadeInUp 0.4s forwards;
  transition: all 0.25s ease;
}

.service-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 20px rgba(60, 110, 190, 0.12);
}

.service-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 10px;
  margin-bottom: 10px;
}

.service-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3f63;
  margin-bottom: 6px;
}

.service-description {
  font-size: 13px;
  color: #6b7d9d;
  line-height: 1.6;
}

@keyframes fadeInUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
