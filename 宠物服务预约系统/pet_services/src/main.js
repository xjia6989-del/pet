import Vue from 'vue'
import App from './App.vue'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import router from './router'
import store from './store'

Vue.config.productionTip = false

Vue.use(ElementUI);

Vue.prototype.$infoRule = function hasEmptyProperties(obj) {
  for (let key in obj) {
    if (obj.hasOwnProperty(key) && obj[key] === '') {
      return true; // 如果发现有空属性，返回 true
    }
  }
  return false; // 如果没有空属性，返回 false
}

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
