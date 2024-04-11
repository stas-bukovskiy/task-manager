import {reactive} from "vue";

export default {
  namespaced: true,
  state: reactive({
    users: new Map()
  }),
  mutations: {
    setUsers(state, users) {
      state.users = new Map(users);
    },
    addUser(state, user) {
      state.users.set(user.id, user);
    },
    deleteUser(state, userId) {
      state.users.delete(userId);
    }
  },
  actions: {
    async fetchUsers({commit}, userIds) {
      return new Promise((resolve) => {
        setTimeout(() => {
          const users = [
            ['1', {id: '1', username: 'user1', email: 'user1@mail.com', firstName: 'User', lastName: 'One'}],
            ['2', {id: '2', username: 'user2', email: 'user2@mail.com', firstName: 'User', lastName: 'Two'}],
            ['3', {id: '3', username: 'Jonh_Doe', email: 'jnh@main.com', firstName: 'Jonh', lastName: 'Doe'}]
          ];
          commit('setUsers', users);
          resolve();
        }, 1000);
      })
    },
    async searchUsers({commit}, query) {
      return new Promise((resolve) => {
        setTimeout(() => {
          const users = [
            {id: '1', username: 'user1', email: 'user1@mail.com', firstName: 'User', lastName: 'One'},
            {id: '2', username: 'user2', email: 'user2@mail.com', firstName: 'User', lastName: 'Two'},
            {id: '3', username: 'Jonh_Doe', email: 'jnh@main.com', firstName: 'Jonh', lastName: 'Doe'}
          ];
          resolve(users.filter(user => user.username.includes(query) || user.email.includes(query) ||
            user.firstName.includes(query) || user.lastName.includes(query)));
        }, 1000)
      })
    },
    addUser({commit}, user) {
      commit('addUser', user);
    },
    deleteUser({commit}, userId) {
      commit('deleteUser', userId);
    }
  },
  getters: {
    getUsers: state => userIds => {
      const users = [];
      for (const userId of userIds) {
        if (!state.users.has(userId)) {
          continue;
        }
        users.push(state.users.get(userId));
      }
      console.log('getUsers', state.users, userIds, users)
      return users;
    },
    getUser: state => userId => state.users.get(userId)
  }
}
