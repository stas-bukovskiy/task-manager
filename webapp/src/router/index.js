import {route} from 'quasar/wrappers'
import {createRouter, createWebHistory} from 'vue-router'
import routes from './routes'


export default route(function (/* { store, ssrContext } */) {
  return createRouter({
    history: createWebHistory('/'),
    routes,
  })
})
