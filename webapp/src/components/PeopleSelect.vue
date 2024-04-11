<template>
  <div>
    <q-select
      v-model="selectedPeople"
      :options="users"
      fill-input
      hint="Search people"
      input-debounce="0"
      multiple
      outlined
      stack-label
      use-chips
      use-input
      @filter="searchPeople"
      @update:model-value="addPerson"
    >
      <template v-slot:selected>
        <div class="row">
          <q-chip
            v-for="(person) in selectedPeople"
            :key="person.id"
            dense
            icon="account_circle"
            removable
            @remove="removePerson(person.id)"
          >
            {{ person.username }}
          </q-chip>
        </div>
      </template>
      <template v-slot:option="scope">
        <q-item v-bind="scope.itemProps">
          <q-item-section avatar>
            <q-icon name="account_circle"/>
          </q-item-section>
          <q-item-section>
            <q-item-label>{{ scope.opt.username }}</q-item-label>
          </q-item-section>
        </q-item>
      </template>
    </q-select>
  </div>
</template>

<script>
import {mapActions} from "vuex";

export default {
  props: {
    selected: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      users: [],
      selectedPeople: []
    };
  },
  methods: {
    ...mapActions('users', ['searchUsers', 'addUser', 'deleteUser']),
    searchPeople(val, update) {
      if (val.length <= 3) {
        update(() => {
          this.users = [];
        })
        return
      }

      update(async () => {
        const searchQuery = val.toLowerCase()
        const res = await this.searchUsers(searchQuery)

        this.users = res || [];
      })
    },
    removePerson(userId) {
      this.selectedPeople = this.selectedPeople.filter(person => person.id !== userId)
      this.$emit('update:selected', this.selectedPeople); // Emit the event to update the selected prop in the parent
      this.deleteUser(userId);
    },
    addPerson(opt) {
      this.$emit('update:selected', this.selectedPeople); // Emit the event to update the selected prop in the parent
      this.addUser(opt);
    },
  }
}
;
</script>
