<template>
  <div id="box">
    <div class="card">
      <div class="title">宠物服务预约系统</div>

      <div class="wave-group" id="input1">
        <input required type="text" class="input" v-model="loginForm.username">
        <span class="bar"></span>
        <label class="label">
          <span class="label-char" style="--index: 0">用</span>
          <span class="label-char" style="--index: 1">户</span>
          <span class="label-char" style="--index: 2">名</span>
        </label>
      </div>

      <div class="wave-group" id="input2">
        <input required type="password" class="input" v-model="loginForm.password">
        <span class="bar"></span>
        <label class="label">
          <span class="label-char" style="--index: 0">密</span>
          <span class="label-char" style="--index: 1">码</span>
        </label>
      </div>

      <div class="radio-button-container">
        <el-radio-group v-model="loginForm.role" class="role-group">
          <el-radio-button label="user">用户</el-radio-button>
          <el-radio-button label="admin">管理员</el-radio-button>
          <el-radio-button label="vet">兽医</el-radio-button>
        </el-radio-group>
      </div>

      <button @click="login">登录</button>
      <div class="toRegister" @click="toRegister">没有账号？去注册</div>
    </div>
  </div>
</template>

<script>
import { userLogin } from '@/api/UserAPI';
import { adminLogin } from '@/api/AdminAPI';

export default {
  name: 'CampusRecruitmentLogin',
  data() {
    return {
      loginForm: {
        username: '',
        password: '',
        role: 'user'
      }
    };
  },
  methods: {
    toRegister() {
      this.$router.push('/register');
    },
    normalizeResp(resp) {
      return resp && resp.data ? resp.data : resp;
    },
    async login() {
      const { username, password, role } = this.loginForm;
      if (!username) return this.$message.error('请输入用户名');
      if (!password) return this.$message.error('请输入密码');
      if (!role) return this.$message.error('请选择角色');

      try {
        if (role === 'admin') {
          const raw = await adminLogin(username, password);
          const res = this.normalizeResp(raw);
          if (res && res.success) {
            localStorage.setItem('admin', JSON.stringify(res.result));
            localStorage.removeItem('user');
            localStorage.removeItem('vet');
            localStorage.setItem('usertype', role);
            this.$router.push('/adminHome');
            return;
          }
          this.$message.error((res && res.msg) || '管理员登录失败');
          return;
        }

        const raw = await userLogin(username, password);
        const res = this.normalizeResp(raw);
        if (res && res.success) {
          if (role === 'vet') {
            if (!res.result || res.result.role !== 'vet') {
              this.$message.error('当前账号不是兽医角色');
              return;
            }
            localStorage.setItem('vet', JSON.stringify(res.result));
            localStorage.removeItem('user');
            localStorage.removeItem('admin');
            localStorage.setItem('usertype', role);
            this.$router.push('/vetHome');
            return;
          }

          localStorage.setItem('user', JSON.stringify(res.result));
          localStorage.removeItem('admin');
          localStorage.removeItem('vet');
          localStorage.setItem('usertype', role);
          this.$router.push('/userHome');
          return;
        }
        this.$message.error((res && res.msg) || '登录失败，请重试');
      } catch (error) {
        this.$message.error('网络错误或服务器异常');
      }
    }
  }
};
</script>
<style scoped>
#box {
  background: url("@/assets/bg.jpeg") no-repeat center center fixed;
  width: 100%;
  height: 100%;
  position: fixed;
  background-size: 100% 100%;
}

.card {
  width: 650px;
  height: 550px;
  border-radius: 30px;
  background: rgba(224, 224, 224, 0.4);
  box-shadow: 5px 5px 15px #bebebe,
    -15px -15px 30px #ffffff;
  margin: 180px auto;
  position: relative;
}

.title {
  position: absolute;
  top: 80px;
  font-size: 35px;
  font-weight: 900;
  left: 160px;
  letter-spacing: 10px;
}

#input1,
#input2 {
  left: 175px;
}

#input1 {
  top: 180px;
}

#input2 {
  top: 250px;
}

.wave-group {
  position: absolute;
}

.wave-group .input {
  font-size: 16px;
  padding: 10px 10px 10px 5px;
  display: block;
  width: 300px;
  border: none;
  border-bottom: 1px solid #515151;
  background: transparent;
}

.wave-group .input:focus {
  outline: none;
}

.wave-group .label {
  color: #424040;
  font-size: 18px;
  font-weight: normal;
  position: absolute;
  pointer-events: none;
  left: 5px;
  top: 10px;
  display: flex;
}

.wave-group .label-char {
  transition: 0.2s ease all;
  transition-delay: calc(var(--index) * .05s);
}

.wave-group .input:focus~label .label-char,
.wave-group .input:valid~label .label-char {
  transform: translateY(-20px);
  font-size: 14px;
  color: #F37C57;
}

.wave-group .bar {
  position: relative;
  display: block;
  width: 300px;
}

.wave-group .bar:before,
.wave-group .bar:after {
  content: '';
  height: 2px;
  width: 0;
  bottom: 1px;
  position: absolute;
  background: #F37C57;
  transition: 0.2s ease all;
  -moz-transition: 0.2s ease all;
  -webkit-transition: 0.2s ease all;
}

.wave-group .bar:before {
  left: 50%;
}

.wave-group .bar:after {
  right: 50%;
}

.wave-group .input:focus~.bar:before,
.wave-group .input:focus~.bar:after {
  width: 50%;
}
.radio-button-container {
  width: 420px;
  display: flex;
  justify-content: center;
  position: absolute;
  top: 350px;
  left: 175px;
}

.role-group {
  display: flex;
  gap: 12px;
}

.role-group /deep/ .el-radio-button__inner {
  border: 1px solid #F37C57;
  color: #F37C57;
  background: transparent;
  box-shadow: none;
}

.role-group /deep/ .el-radio-button__orig-radio:checked + .el-radio-button__inner {
  background: #F37C57;
  border-color: #F37C57;
  color: #fff;
  box-shadow: none;
}

.toRegister {
  position: absolute;
  top: 460px;
  left: 175px;
  cursor: pointer;
  color: #f37c57;
}

button {
  position: absolute;
  top: 400px;
  left: 175px;
  width: 300px;
  height: 40px;
  border: none;
  border-radius: 20px;
  background: #f37c57;
  color: #fff;
  font-size: 16px;
  cursor: pointer;
}
</style>