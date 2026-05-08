<template>
  <div class="user-layout">
    <el-header class="header-container">
      <div class="header-left">
        <span class="system-title">宠物健康护理预约与智能档案管理系统</span>
        <span class="system-subtitle">Pet Health Care</span>
      </div>

      <el-menu
        mode="horizontal"
        router
        :default-active="$route.path"
        class="nav-menu"
        background-color="transparent"
        text-color="#2f3a4a"
        active-text-color="#3a78ff">
        <el-menu-item index="/userHome/square"><i class="el-icon-house"></i><span>首页广场</span></el-menu-item>
        <el-menu-item index="/userHome/petServices"><i class="el-icon-date"></i><span>预约服务</span></el-menu-item>
        <el-menu-item index="/userHome/myBooking"><i class="el-icon-tickets"></i><span>预约记录</span></el-menu-item>
        <el-menu-item index="/userHome/myPets"><i class="el-icon-notebook-2"></i><span>宠物档案</span></el-menu-item>
      </el-menu>

      <div class="header-right">
        <el-dropdown trigger="click" @command="handleCommand">
          <div class="user-panel">
            <el-avatar :size="34" :src="user.avatar"></el-avatar>
            <span class="username">{{ user.name || user.username }}</span>
            <i class="el-icon-arrow-down"></i>
          </div>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="myInfo">个人中心</el-dropdown-item>
            <el-dropdown-item command="myBooking">我的预约</el-dropdown-item>
            <el-dropdown-item command="myPets">我的宠物</el-dropdown-item>
            <el-dropdown-item command="changePassword">修改密码</el-dropdown-item>
            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </el-header>

    <main class="page-body">
      <section class="page-shell">
        <router-view></router-view>
      </section>
    </main>
  </div>
</template>

<script>
export default {
  data() {
    return {
      user: {}
    }
  },
  methods: {
    handleCommand(command) {
      if (command === 'logout') {
        localStorage.removeItem('user');
        this.$router.push('/login');
      }
      if (command === 'myInfo') this.$router.push('/userHome/myInfo');
      if (command === 'myBooking') this.$router.push('/userHome/myBooking');
      if (command === 'changePassword') this.$router.push('/userHome/changePassword');
      if (command === 'myPets') this.$router.push('/userHome/myPets');
    }
  },
  created() {
    this.user = JSON.parse(localStorage.getItem('user') || '{}');
  }
}
</script>

<style lang="less" scoped>
.user-layout {
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f9ff 0%, #eef4ff 100%);
}

.header-container {
  position: sticky;
  top: 0;
  z-index: 20;
  height: 68px !important;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid #e8efff;
  box-shadow: 0 6px 18px rgba(80, 120, 200, 0.08);
}

.header-left {
  display: flex;
  flex-direction: column;

  .system-title {
    font-size: 18px;
    font-weight: 700;
    color: #22324d;
  }

  .system-subtitle {
    font-size: 12px;
    color: #7f8ca6;
    margin-top: 2px;
  }
}

.nav-menu {
  border: none;
  min-width: 520px;

  /deep/ .el-menu-item {
    height: 68px;
    line-height: 68px;
    font-weight: 600;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    border-radius: 10px 10px 0 0;
    margin: 0 2px;
    transition: all 0.2s ease;
  }

  /deep/ .el-menu-item:hover {
    background: rgba(255, 156, 99, 0.08) !important;
    color: #ff8e5a !important;
  }

  /deep/ .el-menu-item i {
    color: #8ba0c2;
    font-size: 15px;
  }

  /deep/ .el-menu-item.is-active {
    background: rgba(255, 156, 99, 0.15) !important;
    color: #ff8e5a !important;
    border-bottom: 2px solid #ff8e5a !important;
  }

  /deep/ .el-menu-item.is-active i {
    color: #ff8e5a;
  }
}

.user-panel {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 999px;
  border: 1px solid #e7eeff;
  background: #fff;
  cursor: pointer;
  transition: all 0.25s ease;

  &:hover {
    box-shadow: 0 6px 14px rgba(58, 120, 255, 0.18);
  }

  .username {
    margin: 0 8px;
    color: #2f3a4a;
    font-weight: 500;
  }
}

.page-body {
  padding: 18px;
}

.page-shell {
  max-width: 1280px;
  margin: 0 auto;
  min-height: calc(100vh - 110px);
}
</style>
