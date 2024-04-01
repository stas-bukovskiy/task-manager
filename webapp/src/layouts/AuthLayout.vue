<template>
  <q-layout view="hHh lpR fFf">

    <q-page-container class="flex flex-center">
      <router-view/>
    </q-page-container>

  </q-layout>
</template>

<script>
import {useQuasar} from 'quasar'
import {ref} from 'vue'

export default {
  setup() {
    const $q = useQuasar()

    return {
      drawerLeft: ref($q.screen.width > 700),
      drawerRight: ref($q.screen.width > 500)
    }
  }
}
</script>

<style>

</style>
