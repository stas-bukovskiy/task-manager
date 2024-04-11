<template>
  <div class="task-card q-pa-md q-mb-md shadow-2 rounded-borders">
    <div v-if="title" class="text-h6">{{ title }}</div>

    <q-separator v-if="title"></q-separator>

    <div class="row justify-between q-mt-sm q-mb-sm" style="gap: 10px">
      <span v-if="formattedDueDate">
        <q-icon class="q-mr-xs" name="watch_later"/>
        <span>{{ formattedDueDate }}</span>
      </span>
      <span v-if="estimatedTime">
        <q-icon class="q-mr-xs" name="hourglass_empty"/>
        <span>{{ estimatedTime }} h</span>
      </span>
      <span v-if="priority">
        <q-icon class="q-mr-xs" name="flag"/>
        <span>{{ priority }}</span>
      </span>
    </div>

    <div v-if="assignees && assignees.length" class="q-mt-sm q-mb-sm">
      <q-icon class="q-mr-xs" name="person"/>
      <span v-for="(assignee, index) in assignees" :key="index">{{ assignee }}<span v-if="index < assignees.length - 1">, </span></span>
    </div>
  </div>
</template>

<script>
import {computed} from "vue";

export default {
  props: {
    title: {
      type: String,
      default: null
    },
    dueDate: {
      type: String,
      default: null
    },
    estimatedTime: {
      type: Number,
      default: null
    },
    priority: {
      type: String,
      default: null
    },
    assignees: {
      type: Array,
      default: () => []
    },
  },
  setup(props) {
    const formattedDueDate = computed(() => {
      return props.dueDate ? formatDate(props.dueDate) : null;
    });

    function formatDate(timestamp) {
      const date = new Date(timestamp);
      return date.toLocaleDateString('default', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
      });
    }

    return {formattedDueDate};
  }
};
</script>
