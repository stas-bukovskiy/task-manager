import {GET_TOKEN_GETTER} from "src/store/constants";

export default {
  // Getter to access the token
  [GET_TOKEN_GETTER]: (state) => {
    return state.auth.token;
  },
}
