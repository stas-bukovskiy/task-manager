import mutations from "src/store/auth/mutations";
import actions from "src/store/auth/actions";
import getters from "src/store/auth/getters";

export default {
  namespaced: true,
  state() {
    return {}
  },
  mutations: mutations,
  actions: actions,
  getters: getters
}
