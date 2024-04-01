import {route} from 'quasar/wrappers'
import {createRouter, createWebHistory} from 'vue-router'
import routes from '../../../webapp/src/router/routes'


export default route(function (/* { index, ssrContext } */) {
  return createRouter({
    history: createWebHistory('/'),
    routes,
  })
})
