import {createStore} from "vuex";
import auth from "webapp/src/store/auth";

const store = createStore({
  modules: {
    auth,
  }
})

export default store
