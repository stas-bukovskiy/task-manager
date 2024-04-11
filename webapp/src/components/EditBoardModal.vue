<template>
  <q-card class="column q-pa-lg" style="width: 600px; max-width: 80vw;">
    <q-card-section class="text-center">
      <div class="text-h5">Edit your board</div>
    </q-card-section>

    <q-card-section class="q-pt-none column">
      <q-input v-model="board.title" :rules="[
                val => !!val || 'Title is required',
                val => val.length > 3 || 'Title must be more than 3 characters',
              ]" label="Title" outlined/>
    </q-card-section>

    <q-card-section class="q-pt-none column">
      <q-input v-model="board.description" label="Description" outlined/>
    </q-card-section>

    <q-card-section class="q-pt-none column">
      <h6 style="margin: 0;">Invite people to your board</h6>
      <PeopleSelect v-model:selected="selected"/>
    </q-card-section>

    <q-card-section class="q-pt-none column">
      <h6 v-if="joined.length > 0" style="margin: 0;">Joined users</h6>
      <div class="row">
        <q-chip
          v-for="(person) in joined"
          :key="person.id"
          icon="account_circle"
          removable
          @remove="removeJoinedPerson(person.id)"
        >
          {{ person.username }}
        </q-chip>
      </div>
    </q-card-section>

    <q-card-actions align="center" class="bg-white text-teal">
      <q-btn v-close-popup :disable="board.title.length <= 3" color="primary" label="Update" @click="confirmCreating"/>
      <q-btn v-close-popup color="negative" label="Cancel"/>
    </q-card-actions>

  </q-card>
</template>

<script>
import PeopleSelect from "components/PeopleSelect.vue";
import {mapActions, mapGetters} from "vuex";

export default {
  props: {
    id: {
      type: String,
      required: true
    }
  },
  components: {
    PeopleSelect
  },
  data() {
    return {
      board: {
        id: this.id,
        title: '',
        description: ''
      },
      selected: [],
      joined: []
    };
  },
  computed: {
    ...mapGetters('boards', ['getBoard']),
    ...mapGetters('users', ['getUsers']),
  },
  methods: {
    ...mapActions('boards', ['updateBoard', 'fetchBoard']),
    ...mapActions('users', ['fetchUsers']),
    ...mapActions('users', ['fetchUsers']),
    confirmCreating() {
      console.log(this.title, this.selected);
      this.updateBoard({title: this.title, invitedIds: this.selected});
    },
    removeJoinedPerson(id) {
      this.joined = this.joined.filter(person => person.id !== id);
    }
  },
  mounted() {
    this.fetchBoard(this.board.id).then(() => {
      const item = this.getBoard(this.board.id);
      console.log("board", item)
      if (item) {
        this.board = {
          id: item.id,
          title: item.title,
          description: item.description
        };

        const usersToFetch = item.invitedIds.concat(item.joinedIds);
        this.fetchUsers(usersToFetch).then(() => {
          this.selected = [...this.getUsers(item.invitedIds)];
          this.joined = [...this.getUsers(item.joinedIds)];
        });
        console.log("selected", this.board)
      }
    });
  },
};
</script>


<style scoped>

</style>
