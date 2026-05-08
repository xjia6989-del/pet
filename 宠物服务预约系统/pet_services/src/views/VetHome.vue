<template>
  <el-container class="vet-layout">
    <el-header class="header">
      <div class="header-content">
        <div class="logo">
          <span class="title">宠物健康护理预约与智能档案管理系统 - 兽医端</span>
          <span class="sub">Veterinarian Console</span>
        </div>
        <el-dropdown trigger="click" @command="handleCommand">
          <div class="user-info">
            <el-avatar :src="user.avatar"></el-avatar>
            <span class="username">{{ user.username }}</span>
            <i class="el-icon-arrow-down"></i>
          </div>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="changePassword">
              <i class="el-icon-refresh-right"></i>更改密码
            </el-dropdown-item>
            <el-dropdown-item command="logout" divided>
              <i class="el-icon-switch-button"></i>退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </el-header>

    <el-container class="layout-body">
      <el-aside width="228px" class="sidebar">
        <el-menu router class="nav-menu" background-color="transparent" text-color="#314056" active-text-color="#3a78ff" :default-active="$route.path">
          <el-menu-item index="/vetHome/bookingList"><i class="el-icon-date"></i><span>预约列表</span></el-menu-item>
          <el-menu-item index="/vetHome/healthRecord"><i class="el-icon-edit-outline"></i><span>健康记录</span></el-menu-item>
          <el-menu-item index="/vetHome/center"><i class="el-icon-user"></i><span>个人中心</span></el-menu-item>
          <el-menu-item index="/vetHome/changePassword"><i class="el-icon-lock"></i><span>修改密码</span></el-menu-item>
        </el-menu>
      </el-aside>

      <el-main class="main-content">
        <div class="content-shell">
          <router-view></router-view>
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
export default {
  data() {
    return { user: {} };
  },
  methods: {
    handleCommand(command) {
      if (command === 'logout') {
        this.$confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          localStorage.removeItem('vet');
          localStorage.removeItem('usertype');
          this.$router.push('/login');
        });
      }
      if (command === 'changePassword') {
        this.$router.push('/vetHome/changePassword');
      }
    }
  },
  created() {
    this.user = JSON.parse(localStorage.getItem('vet') || '{}');
  }
}
</script>

<style lang="less" scoped>
.vet-layout { min-height: 100vh; background: #f4f7ff; }
.header { height: 64px !important; padding: 0 18px; background: linear-gradient(135deg, #3b6eff 0%, #5a8bff 50%, #6da8ff 100%); box-shadow: 0 8px 18px rgba(41, 84, 204, 0.22); }
.header-content { height: 100%; display: flex; justify-content: space-between; align-items: center; }
.logo { display: flex; flex-direction: column; }
.logo .title { color: #fff; font-size: 18px; font-weight: 700; }
.logo .sub { color: rgba(255,255,255,0.88); font-size: 12px; }
.user-info { display: flex; align-items: center; padding: 6px 10px; border-radius: 999px; background: rgba(255,255,255,0.18); color: #fff; cursor: pointer; }
.user-info .username { margin: 0 8px; font-weight: 500; }
.layout-body { height: calc(100vh - 64px); }
.sidebar { background: linear-gradient(180deg, #f8fbff, #f4f8ff); border-right: 1px solid #e7efff; padding-top: 10px; }
.nav-menu { border-right: none; }
.nav-menu /deep/ .el-menu-item, .nav-menu /deep/ .el-submenu__title { margin: 4px 10px; border-radius: 10px; height: 44px; line-height: 44px; }
.nav-menu /deep/ .el-menu-item.is-active { background: linear-gradient(135deg, #e8f0ff, #eef5ff) !important; color: #2f64f5 !important; font-weight: 600; }
.main-content { padding: 14px; }
.content-shell { min-height: 100%; background: #fff; border-radius: 14px; padding: 12px; border: 1px solid #eaf0ff; box-shadow: 0 10px 26px rgba(90, 123, 190, 0.08); }
</style>