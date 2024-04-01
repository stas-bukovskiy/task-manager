<template>
  <q-layout class="rounded-borders q-ma-md" view="lHh lpR lFf">
    <q-header class="bg-primary text-white q-ma-md q-ml-lg">
      <q-toolbar>
        <q-toolbar-title>
          <q-avatar font-size="52px" icon="assessment" size="60px">
          </q-avatar>
          Title
        </q-toolbar-title>
      </q-toolbar>
    </q-header>

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

          <q-item v-ripple clickable>
            <q-item-section>Board 1</q-item-section>
          </q-item>

          <q-item v-ripple clickable>
            <q-item-section>Board 2</q-item-section>
          </q-item>

          <q-item v-ripple clickable>
            <q-item-section>Board 3</q-item-section>
          </q-item>
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
  </q-layout>
</template>

<script>
import {useQuasar} from 'quasar'
import {ref} from 'vue'
import {mapActions} from "vuex";
import {LOGOUT_ACTION} from "src/store/constants";
// const store = useStore()
// const token = store.getters["auth/token"]
export default {
  setup() {
    const $q = useQuasar()

    return {
      // token: token,
      drawerLeft: ref($q.screen.width > 700),
      drawerRight: ref($q.screen.width > 500)
    }
  },
  methods: {
    ...mapActions('auth', {
      logout: LOGOUT_ACTION
    }),
    onLogout() {
      this.logout()
    }
  }
}
</script>

<style>

</style>
