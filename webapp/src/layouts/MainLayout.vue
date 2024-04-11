<template>
  <q-layout class="rounded-borders q-ma-md" view="lHh lpR lFf">
    <q-drawer
      v-model="drawerLeft"
      :breakpoint="700"
      :width="300"
    >

      <q-list bordered>
        <q-item clickable>
          <q-item-section avatar>
            <img alt="tasker" src="../assets/tasker-icon.svg" width="40">
          </q-item-section>
          <q-item-section>Tasker</q-item-section>
        </q-item>

        <q-item v-ripple clickable>
          <q-item-section avatar class="justify-center items-center">
            <q-icon name="account_circle"/>
          </q-item-section>
          <q-item-section>jonh.sm</q-item-section>
        </q-item>

        <q-item v-ripple clickable>
          <q-item-section avatar class="justify-center items-center">
            <q-icon name="home"/>
          </q-item-section>
          <q-item-section>Home</q-item-section>
        </q-item>

        <q-item v-ripple clickable>
          <q-item-section avatar class="justify-center items-center">
            <q-icon name="notifications"/>
          </q-item-section>
          <q-item-section>Notifications</q-item-section>
        </q-item>

        <q-separator/>

        <q-expansion-item :content-inset-level="0.5" expand-separator icon="" label="">
          <template v-slot:header>
            <q-item-section avatar class="justify-center items-center">
              <q-icon name="dashboard"/>
            </q-item-section>

            <q-item-section>
              Boards
            </q-item-section>
          </template>

          <q-item v-for="board in boards" :key="board.id" v-ripple clickable>
            <q-item-section>
              <router-link v-slot="{ navigate }" :to="{ name: 'board', params: { id: board.id } }" custom>
                <div @click="navigate">
                  {{ board.title }}
                </div>
              </router-link>
            </q-item-section>
          </q-item>

          <q-item-section>
            <q-item v-ripple clickable @click="openCreateBoard">
              <q-item-section avatar class="justify-center items-center">
                <q-icon name="add"/>
              </q-item-section>
              <q-item-section>New board</q-item-section>
            </q-item>
          </q-item-section>
        </q-expansion-item>

        <q-separator/>

        <q-item v-ripple clickable>
          <q-item-section avatar class="justify-center items-center">
            <q-icon name="logout"/>
          </q-item-section>
          <q-item-section @click.prevent="onLogout">Logout</q-item-section>
        </q-item>
      </q-list>
    </q-drawer>

    <q-dialog v-model="createBoardModal" persistent>
      <NewBoardModal/>
    </q-dialog>

    <q-page-container>
      <router-view/>
    </q-page-container>
  </q-layout>
</template>

<script>
import {useQuasar} from 'quasar'
import {ref} from 'vue'
import {mapActions, mapGetters} from "vuex";
import NewBoardModal from "components/NewBoardModal.vue";

export default {
  components: {
    NewBoardModal
  },
  setup() {
    const $q = useQuasar();
    const drawerLeft = ref($q.screen.width > 700);
    const drawerRight = ref($q.screen.width > 500);

    return {
      drawerLeft,
      drawerRight
    };
  },
  data() {
    return {
      createBoardModal: false,
      boards: [],
    }
  },
  methods: {
    ...mapActions('auth', ["logout"]),
    ...mapActions('boards', ["fetchBoards"]),
    ...mapGetters('boards', ['getBoards']),

    onLogout() {
      this.logout()
    },
    openCreateBoard() {
      this.createBoardModal = true
    }
  },
  mounted() {
    this.fetchBoards().then(() => {
      this.boards = this.getBoards()
    })
  },
}
</script>

<style>

</style>
