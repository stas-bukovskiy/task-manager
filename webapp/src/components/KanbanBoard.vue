<template>
  <div class="q-pa-md kanban-board">
    <div v-for="statusObj in tasksArray" :key="statusObj.statusKey" class="q-mr-md">
      <div class="text-h6 q-pa-md bg-grey-3">{{ statusObj.statusValue }}</div>
      <draggable v-model="statusObj.tasks" class="drag-list" group="tasks" item-key="id"
                 @change="event => handleTaskChange(statusObj.statusKey, event)">
        <template #item="{ element }">
          <TaskCard
            :assignees="element.assignee_ids"
            :dueDate="element.due_date"
            :estimatedTime="element.estimated_time"
            :priority="element.priority"
            :title="element.title"
          />
        </template>
      </draggable>
    </div>
  </div>
</template>

<script>
import draggable from 'vuedraggable'
import TaskCard from "components/TaskCard.vue";
import {mapActions, mapGetters} from "vuex";

export default {
  components: {
    draggable,
    TaskCard
  },
  props: {
    id: {
      type: String,
      required: true
    }
  },
  data() {
    const statuses = new Map([['TODO', 'To Do'], ['IN_PROGRESS', 'In Progress'], ['IN_REVISION', 'In Review'], ['DONE', 'Done'], ['ARCHIVED', 'Archived']]);
    return {
      statuses,
    };
  },
  methods: {
    ...mapActions('tasks', ['fetchTasks', 'updateTaskStatus']),
    handleTaskChange(newStatus, event) {
      if (!event.added) return;
      this.updateTaskStatus({task: event.added.element, newStatus});
    },
  },
  computed: {
    ...mapGetters('tasks', ['getTasks']),
    tasksArray() {
      return Array.from(this.statuses.entries()).map(([statusKey, statusValue]) => ({
        statusKey,
        statusValue,
        tasks: this.getTasks(this.id).filter(task => task.status === statusKey)
      }));
    }
  },
  mounted() {
    this.fetchTasks(this.id);
  }
};
</script>


<style>
.kanban-board {
  display: flex;
  overflow-x: auto;
  width: 100%;
  height: 100%;
}

.drag-list {
  min-height: 200px;
  background: #f0f0f0;
  border-radius: 5px;
  padding: 10px;
}

/* Add the following styles */
.kanban-board > div {
  flex: 1; /* Each column will take up an equal amount of space */
  display: flex;
  flex-direction: column;
  min-width: 200px; /* Minimum width for each column */
}

.text-h6 {
  margin-bottom: 10px; /* Add some spacing between the header and the tasks */
}
</style>

