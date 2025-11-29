import { createRouter, createWebHistory } from 'vue-router';
import List from './views/List.vue';
import AdminDashboard from './views/AdminDashboard.vue';
import Dashboard from './views/Dashboard.vue';
import Cart from './views/Cart.vue';
import Chat from './views/Chat.vue';
import Notifications from './views/Notifications.vue';
import Detail from './views/Detail.vue';
import Publish from './views/Publish.vue';
import Login from './views/Login.vue';
const routes = [
  { path: '/admin', component: AdminDashboard, meta: { requiresAdmin: true } },
  { path: '/analytics', component: Dashboard },
  { path: '/cart', component: Cart },
  { path: '/chat', component: Chat },
  { path: '/notifications', component: Notifications },
  { path: '/', component: List },
  { path: '/p/:id', component: Detail },
  { path: '/publish', component: Publish },
  { path: '/login', component: Login }
];
export default createRouter({ history: createWebHistory(), routes });


// simple role guard
const router = createRouter({ history: createWebHistory(), routes });
router.beforeEach((to, from, next) => {
  const requiresAdmin = to.meta && to.meta.requiresAdmin;
  if (requiresAdmin) {
    const u = JSON.parse(localStorage.getItem('trader_user') || 'null');
    if (!u || u.role !== 'ADMIN') return next('/login');
  }
  next();
});
export default router;
