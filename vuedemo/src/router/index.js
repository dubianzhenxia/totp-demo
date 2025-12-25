import Vue from 'vue'
import Router from 'vue-router'
import Home from '../views/Home.vue'
import ProgressTag from '../views/ProgressTag.vue'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'Home',
      component: Home
    },
    {
      path: '/progress-tag',
      name: 'ProgressTag',
      component: ProgressTag
    }
  ]
})