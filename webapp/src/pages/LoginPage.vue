<template>
  <q-page class="flex flex-center" style="width: 50%">
    <q-card class="" style="width: 100%">
      <q-card-section horizontal>
        <q-img
          :ratio="3/4" alt="tasker-auth-bg"
          basic
          src="../assets/auth-bg.png"
          style="position: relative;"
        >
          <div class="absolute-top flex text-caption q-ma-md" style="width: 100%;">
            <q-img alt="tasker-icon" src="../assets/text-logo.png" style="width: 50%;"/>
          </div>
          <div class="absolute-bottom text-caption q-ma-md" style="width: 100%;">
            <h1>Welcome back!</h1>
            <h4>Login to your account</h4>
          </div>
        </q-img>
        <q-card-section class="flex flex-center q-ma-lg" style="width: 70%">
          <q-form class="column" style=" width: 100%; gap: 20px" @submit="onSubmit">
            <h3 class="text-center">Login</h3>
            <q-input v-model="username" :rules="[
                        val => !!val || 'Username is required',
                        val => val.length > 3 || 'Username must be more than 3 characters',
                      ]" label="Username" outlined
                     type="text"/>
            <q-input
              v-model="password"
              :rules="[
                val => !!val || 'Password is required',
                val => val.length > 6 || 'Password must be more than 6 characters',
              ]"
              :type="showPassword ? 'text' : 'password'"
              label="Password"
              outlined
            >
              <template v-slot:append>
                <q-icon
                  :name="showPassword ? 'visibility_off' : 'visibility'"
                  class="cursor-pointer"
                  @click="togglePasswordVisibility"
                />
              </template>
            </q-input>
            <q-btn color="primary" label="Login" size="md" type="submit"/>
            <div class="flex flex-center">
              Does not have account?<a class="q-pl-sm" href="/auth/register">Sing up</a>
            </div>
          </q-form>
        </q-card-section>
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script setup>
import {ref} from 'vue';
import {Notify} from "quasar";
import {useRouter} from "vue-router";
import {useStore} from "vuex";

const router = useRouter();

const store = useStore();

const username = ref(null)

const password = ref(null);
const showPassword = ref(false);

const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value;
};

const onSubmit = async () => {
    const userData = {
      login: username.value,
      password: password.value,
    };

    try {
      await store.dispatch('auth/login', userData)
      await router.push('/')
    } catch (error) {
      Notify.create({
        message: error.message,
        position: "top",
        type: "negative"
      })
    }
  }
;
</script>

<style scoped>
.text-caption {
  background: transparent !important;
}

h1, h4, h3 {
  margin: 2px;
  padding: 0;
}
</style>
