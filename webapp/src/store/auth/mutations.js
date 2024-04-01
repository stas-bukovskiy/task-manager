import {SET_TOKEN_MUTATION} from "src/store/constants";

export default {
  [SET_TOKEN_MUTATION](state, payload) {
    state.token = payload.token;
    state.user = payload.user;
  }
}
