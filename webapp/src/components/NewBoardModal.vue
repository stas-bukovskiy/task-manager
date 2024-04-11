<template>
  <q-card class="column q-pa-lg" style="width: 600px; max-width: 80vw;">
    <q-card-section class="text-center">
      <div class="text-h5">Lets start with new board!</div>
    </q-card-section>

    <q-card-section class="q-pt-none column">
      <q-input v-model="title" :rules="[
                val => !!val || 'Title is required',
                val => val.length > 3 || 'Title must be more than 3 characters',
              ]" label="Title" outlined/>
    </q-card-section>

    <q-card-section class="q-pt-none column">
      <q-input v-model="description" label="Description" outlined/>
    </q-card-section>

    <q-card-section class="q-pt-none column">
      <h6 style="margin: 0;">Invite people to your board</h6>
      <PeopleSelect v-model:selected="selected"/>
    </q-card-section>


    <q-card-actions align="center" class="bg-white text-teal">
      <q-btn v-close-popup :disable="title.length <= 3" color="primary" label="Create" @click="confirmCreating"/>
      <q-btn v-close-popup color="negative" label="Cancel"/>
    </q-card-actions>
  </q-card>
</template>

<script>

import PeopleSelect from "components/PeopleSelect.vue";
import {mapActions} from "vuex";

export default {
  components: {
    PeopleSelect
  },
  data() {
    return {
      title: "",
      description: '',
      selected: []
    };
  },
  methods: {
    ...mapActions('boards', ['createBoard']),
    confirmCreating() {
      console.log(this.title, this.selected);
      this.createBoard({title: this.title, invitedIds: this.selected});
    }
  }
};
</script>


<style scoped>

</style>
