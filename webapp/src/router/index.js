import {route} from 'quasar/wrappers'
import {createRouter, createWebHistory} from 'vue-router'
import routes from './routes'

export default route(function (/* { store, ssrContext } */) {
  const router = createRouter({
    history: createWebHistory('/'),
    routes,
  })

  // router.beforeEach((to, from, next) => {
  //   const store = useStore()
  //   const isNotAuthenticated = !store.state.auth.token;
  //
  //   if (isNotAuthenticated && !to.path.startsWith("/auth")) {
  //     next('/auth/login');
  //   } else {
  //     next();
  //   }
  // });


  return router
})
