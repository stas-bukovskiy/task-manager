<template>
  <q-card>
    <q-card-section class="row items-center">
      <h5 class="q-ma-md">Do you really want to delete {{ title }}?</h5>
    </q-card-section>

    <q-card-actions align="right">
      <q-btn v-close-popup color="primary" flat label="Cancel"/>
      <q-btn v-close-popup color="primary" flat label="Confirm Deletion" @click="confirmDeleting"/>
    </q-card-actions>
  </q-card>
</template>

<script>
import {mapActions} from "vuex";

export default {
  props: {
    id: {
      type: String,
      required: true
    },
    title: {
      type: String,
      required: true
    }
  },
  methods: {
    ...mapActions('boards', ['deleteBoard']),
    confirmDeleting() {
      this.deleteBoard(this.id);
      this.$router.push({name: 'home'});
    }
  }
}
</script>

<style scoped>
</style>
