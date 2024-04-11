import {store} from 'quasar/wrappers'
import {createStore} from 'vuex'
import auth from "src/store/auth";
import boards from "src/store/boards";
import users from "src/store/users";
import tasks from "src/store/tasks";

// import example from './module-example'

/*
 * If not building with SSR mode, you can
 * directly export the Store instantiation;
 *
 * The function below can be async too; either use
 * async/await or return a Promise which resolves
 * with the Store instance.
 */

export default store(function () {
  return createStore({
    modules: {
      auth,
      boards,
      users,
      tasks
    },

    // enable strict mode (adds overhead!)
    // for dev mode and --debug builds only
    strict: process.env.DEBUGGING
  })
})
