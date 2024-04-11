export default {
  namespaced: true,
  state: {
    tasks: new Map()
  },
  getters: {
    getTasks: (state) => (id) => {
      console.log('GET', state);
      console.log('GET', state.tasks.get(id));
      if (!state.tasks.has(id)) {
        return [];
      }
      return Array.from(state.tasks.get(id).values());
    },
    getTask(state, boardId, taskId) {
      return state.tasks.get(boardId).get(taskId)
    },
  },
  mutations: {
    setTasks(state, tasks) {
      state.tasks = new Map(tasks);
    },
    addTask(state, task) {
      state.tasks.get(task.boardId).set(task.id, task);
    },
    updateTask(state, task) {
      state.tasks.get(task.boardId).set(task.id, task);
    },
    deleteTask(state, {boardId, taskId}) {
      state.tasks.get(boardId).delete(taskId);
    },
    updateTaskStatus(state, {task, newStatus}) {
      console.log('updateTaskStatus', task, newStatus);
      state.tasks.get(task.boardId).get(task.id).status = newStatus;
    }
  },
  actions: {
    fetchTasks({commit}, boardId) {
      return new Promise((resolve) => {
        setTimeout(() => {
          commit('setTasks', [
            ['1', new Map([
              ['1', {
                id: '1',
                title: 'Task 1',
                description: 'Task 1 description',
                start_date: '2021-01-01',
                due_date: '2021-01-31',
                estimated_time: 10,
                priority: 'HIGH',
                status: 'TODO',
                boardId: '1',
                assignee_ids: ['1', '2']
              }],
              ['2', {
                id: '2',
                title: 'Task 2',
                description: 'Task 2 description',
                start_date: '2021-01-01',
                due_date: '2021-01-31',
                estimated_time: 10,
                priority: 'HIGH',
                status: 'IN_PROGRESS',
                boardId: '1',
                assignee_ids: ['1', '2']
              }]
            ])],
            ['2', new Map([
              ['3', {
                id: '3',
                title: 'Task 3',
                description: 'Task 3 description',
                start_date: '2021-01-01',
                due_date: '2021-01-31',
                estimated_time: 10,
                priority: 'HIGH',
                status: 'IN_REVISION',
                boardId: '2',
                assignee_ids: ['1', '2']
              }],
              ['4', {
                id: '4',
                title: 'Task 4',
                description: 'Task 4 description',
                start_date: '2021-01-01',
                due_date: '2021-01-31',
                estimated_time: 10,
                priority: 'HIGH',
                status: 'DONE',
                boardId: '2',
                assignee_ids: ['1', '2']
              }]
            ])],
          ]);
          resolve();
        }, 1000);
      })
    },
    updateTaskStatus({commit}, {task, newStatus}) {
      console.log('updateTaskStatus', task, newStatus);
      commit('updateTaskStatus', {task, newStatus});
      setTimeout(() => {
        // Here you can send a request to the server to update the task status
      }, 1000);
    }
  }
}
