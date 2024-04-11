import {LOGIN_ACTION, SET_TOKEN_MUTATION, SIGNUP_ACTION} from "src/store/constants";
import axios from "axios";

export default {
  async [SIGNUP_ACTION](_, payload) {
    const serverHost = process.env.SERVER_HOST;
    try {
      let response = await axios.post(`${serverHost}/api/v1/auth/sign-up`, payload);

      if (response.status === 201) {
        return 'Successfully registered your account';
      } else {
        throw new Error(response.data.message || 'An error occurred during registration');
      }
    } catch (error) {
      // If the error is from the server response, use that message, otherwise use the error message
      throw new Error(error.response?.data?.message || error.message);
    }
  },

  async [LOGIN_ACTION](context, payload) {
    let serverHost = process.env.SERVER_HOST;

    try {
      let response = await axios.post(`${serverHost}/api/v1/auth/sign-in`, payload);

      console.log(response.data);
      if (response.status === 200) {
        context.commit(SET_TOKEN_MUTATION, {
          token: response.data.data.token,
          user: {
            id: response.data.data.user.id,
            email: response.data.data.user.email,
            username: response.data.data.user.username,
            firstName: response.data.data.user.first_name,
            lastName: response.data.data.user.last_name,
          }
        });
      } else {
        throw new Error(response.data.message || 'An error occurred during authentication');
      }
    } catch (error) {
      // If the error is from the server response, use that message, otherwise use the error message
      throw new Error(error.response?.data?.message || error.message);
    }
  },
  logout(context) {
    context.commit(SET_TOKEN_MUTATION, {
      token: '',
      user: {
        id: '',
        email: '',
        username: '',
        firstName: '',
        lastName: '',
      }
    });
  }
}

