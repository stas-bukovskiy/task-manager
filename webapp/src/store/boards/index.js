export default {
  namespaced: true,
  state: {
    boards: new Map()
  },
  getters: {
    getBoards: state => Array.from(state.boards.values()),
    getBoard: state => boardId => state.boards.get(boardId),
  },
  mutations: {
    setBoards(state, boards) {
      state.boards = new Map(boards);
    },
    addBoard(state, board) {
      state.boards.set(board.id, board);
    },
    updateBoard(state, board) {
      state.boards.set(board.id, board);
    },
    deleteBoard(state, boardId) {
      state.boards.delete(boardId);
    },
  },
  actions: {
    async fetchBoards({commit}) {
      return new Promise((resolve) => {
        setTimeout(() => {
          commit('setBoards', [
            ['1', {id: "1", title: 'Board 1', ownerId: '1', invitedIds: ['2'], joined_jds: ['1', '2']}],
            ['2', {id: "2", title: 'Board 2', ownerId: '2', invitedIds: ['1'], joinedIds: ['1', '2']}],
          ]);
          resolve();
        }, 1000);
      })
    },
    async fetchBoard({state, commit}, boardId) {
      return new Promise((resolve) => {
        setTimeout(() => {
          console.log('fetchBoard', state.boards);
          if (state.boards.has(boardId)) {
            console.log('fetchBoard', 'found')
            resolve();
            return
          }

          console.log('fetchBoard', 'not found')
          commit('addBoard', {id: boardId, title: `Board ${boardId}`});
          resolve();
        }, 1000);
      })
    },
    async createBoard({commit}, board) {
      commit('addBoard', {
        id: Math.floor(Math.random() * 1000).toString(),
        title: board.title,
        ownerId: '1',
        invitedIds: board.invitedIds,
      });
    },
    async updateBoard({commit}, board) {
      commit('updateBoard', board);
    },
    async deleteBoard({commit}, boardId) {
      commit('deleteBoard', boardId);
    },
  }
}
