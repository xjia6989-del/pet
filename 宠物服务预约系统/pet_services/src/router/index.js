import Vue from 'vue'
import VueRouter from 'vue-router'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import UserHome from '@/views/UserHome.vue'
import AdminHome from '@/views/AdminHome.vue'
import User from '@/components/admin/User.vue'
import Welcome from '@/components/admin/Welcome.vue'
import Admin from '@/components/admin/Admin.vue'
import Category from '@/components/admin/Category.vue'
import Serve from '@/components/admin/Serve.vue'
import Goods from '@/components/admin/Goods.vue'
import Booking from '@/components/admin/Booking.vue'
import ContactMessage from '@/components/admin/ContactMessage.vue'
import VetHome from '@/views/VetHome.vue'
import VetBooking from '@/components/vet/BookingList.vue'
import VetHealthRecord from '@/components/vet/HealthRecord.vue'
import VetCenter from '@/views/VetCenter.vue'
import Square from '@/components/user/Square.vue'
import PetServices from '@/components/user/PetServices.vue'
import PetGoods from '@/components/user/PetGoods.vue'
import MyBooking from '@/components/user/MyBooking.vue'
import MyOrder from '@/components/user/MyOrder.vue'
import ChangePassword from '@/components/ChangePassword.vue'
import UserCenter from '@/components/user/UserCenter.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: '宠物健康护理预约与智能档案管理系统',
    component: Login
  },
  {
    path: '/login',
    name: '宠物健康护理预约与智能档案管理系统',
    component: Login
  },
  {
    path: '/register',
    name: '注册',
    component: Register
  },
  {
    path: '/userHome',
    name: '用户页面',
    component: UserHome,
    redirect: '/userHome/square',
   children: [
     {
       path: 'square',
       component: Square,
       meta: { title: '宠物健康护理预约与智能档案管理系统' }
     },
     {
       path: 'petServices',
       component: PetServices,
       meta: { title: '预约管理-预约服务' }
     },
     {
       path: 'myBooking',
       component: MyBooking,
       meta: { title: '预约管理-预约记录' }
     },
     {
       path: 'changePassword',
       component: ChangePassword,
       meta: { title: '用户更改密码' }
     },
     {
       path: 'myInfo',
       component: UserCenter,
       meta: { title: '用户管理-个人中心' }
     },
     {
       path: 'health/:petId',
       component: () => import('@/views/user/HealthRecords.vue'),
       meta: { title: '健康提醒' }
     },
     {
       path: 'myPets',
       component: () => import('@/views/user/MyPets.vue'),
       meta: { title: '宠物档案管理' }
     }
   ]
  },
  {
    path: '/adminHome',
    name: '管理员页面',
    component: AdminHome,
    redirect: '/adminHome/welcome',
    children: [
      {
        path: 'welcome',
        component: Welcome,
        meta: {
          title: '运营总览'
        }
      },
      {
        path: 'user',
        component: User,
        meta: {
          title: '登录认证-机构用户'
        }
      },
      {
        path: 'admin',
        component: Admin,
        meta: {
          title: '登录认证-机构管理员'
        }
      },
      {
        path: 'category',
        component: Category,
        meta: {
          title: '服务项目管理-分类'
        }
      },
      {
        path: 'serve',
        component: Serve,
        meta: {
          title: '服务项目管理'
        }
      },
      {
        path: 'goods',
        component: Goods,
        meta: {
          title: '宠物档案查看'
        }
      },
      {
        path: 'booking',
        component: Booking,
        meta: {
          title: '预约管理'
        }
      },
      {
        path: 'contactMessage',
        component: ContactMessage,
        meta: {
          title: '客服留言管理'
        }
      },
      {
        path: 'changePassword',
        component: ChangePassword,
        meta: {
          title: '更改密码'
        }
      },
    ]
  },
  {
    path: '/vetHome',
    name: '兽医页面',
    component: VetHome,
    redirect: '/vetHome/bookingList',
    children: [
      {
        path: 'bookingList',
        component: VetBooking,
        meta: { title: '预约列表' }
      },
      {
        path: 'healthRecord',
        component: VetHealthRecord,
        meta: { title: '健康记录' }
      },
      {
        path: 'center',
        component: VetCenter,
        meta: { title: '个人中心' }
      },
      {
        path: 'changePassword',
        component: ChangePassword,
        meta: { title: '更改密码' }
      }
    ]
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

const originalPush = VueRouter.prototype.push
// 解决ElementUI导航栏中的vue-router在3.0版本以上重复点菜单报错问题
VueRouter.prototype.push = function push(location) {
  return originalPush.call(this, location).catch(err => err)
}

//路由守卫
router.beforeEach((to, from, next) => {
  const protectedPaths = ['/adminHome', '/userHome', '/vetHome']
  const token = localStorage.user
  const token2 = localStorage.admin
  const token3 = localStorage.vet
  if (protectedPaths.some(path => to.path.indexOf(path) === 0)) {
    if (token || token2 || token3) {
      next()
      return
    }
    next({ path: '/login' })
    return
  }
  next()
})

export default router
