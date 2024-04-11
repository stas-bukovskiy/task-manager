import mutations from "src/store/auth/mutations";
import actions from "src/store/auth/actions";

export default {
  namespaced: true,
  state() {
    return {
      token: null,
      user: {
        id: '1',
        username: null,
        email: null,
        firstName: null,
        lastName: null
      }
    }
  },
  mutations: mutations,
  actions: actions,
  getters: {
    getToken: state => {
      return state.token;
    },
    getUser: state => {
      return state.user;
    }
  }
}
