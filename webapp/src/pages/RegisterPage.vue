<template>
  <q-page class="flex flex-center" style="width: 60%">
    <q-card class="" style="width: 100%">
      <q-card-section horizontal>
        <q-card-section class="flex flex-center q-ma-lg" style="width: 90%">
          <q-form class="column" style=" width: 100%; gap: 40px" @submit="onSubmit">
            <h3 class="text-center">Sign Up</h3>
            <div class="flex row justify-between">
              <q-banner v-if="errorMessage" class="bg-negative text-white q-mb-md">
                {{ errorMessage }}
              </q-banner>

              <q-input v-model="firstName" :rules="[
                        val => !!val || 'First name is required',
                        val => val.length > 3 || 'First name must be more than 3 characters',
                      ]" label="First Name" outlined
                       type="text"/>
              <q-input v-model="lastName" label="Last Name" outlined type="text"/>
            </div>
            <q-input v-model="username" :rules="[
                        val => !!val || 'Username is required',
                        val => val.length > 3 || 'Username must be more than 3 characters',
                      ]" label="Username" outlined
                     type="text"/>
            <q-input v-model="email" :rules="[
                        val => !!val || 'Email is required',
                        val => val.length > 3 || 'Email must be more than 3 characters',
                      ]" label="Email" outlined
                     type="email"/>
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
            <q-input
              v-model="confirmPassword"
              :rules="[
                val => !!val || 'Password confirmation is required',
                val => val === password || 'Passwords do not match',
              ]"
              :type="showConfirmPassword ? 'text' : 'password'"
              label="Confirm Password"
              outlined
            >
              <template v-slot:append>
                <q-icon
                  :name="showConfirmPassword ? 'visibility_off' : 'visibility'"
                  class="cursor-pointer"
                  @click="toggleConfirmPasswordVisibility"
                />
              </template>
            </q-input>
            <q-btn color="primary" label="Signup" size="md" type="submit"/>
            <div class="flex flex-center">
              Already have account?<a class="q-pl-sm" href="/auth/login">Log in</a>
            </div>
          </q-form>
        </q-card-section>
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
            <h1>Are you new?</h1>
            <h4>Register your first account</h4>
          </div>
        </q-img>
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script setup>
import {ref} from 'vue';
import {useStore} from 'vuex';
import {useRouter} from "vue-router";
import {Notify} from "quasar";

const router = useRouter();

const store = useStore();

const firstName = ref('test')
const lastName = ref('')

const username = ref('test')
const email = ref('test@mail.com')

const password = ref('password');
const confirmPassword = ref('password')
const showPassword = ref(false);
const showConfirmPassword = ref(false)

const errorMessage = ref(''); // Error message state

const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value;
};

const toggleConfirmPasswordVisibility = () => {
  showConfirmPassword.value = !showConfirmPassword.value;
};

const onSubmit = async () => {
  const userData = {
    first_name: firstName.value,
    last_name: lastName.value,
    username: username.value,
    email: email.value,
    password: password.value,
  };

  try {
    await store.dispatch('auth/signup', userData)
    Notify.create({
      message: "Your account successfully created",
      position: "top",
      type: "positive"
    })
    await router.push('/auth/login')

  } catch (error) {
    Notify.create({
      message: error.message,
      position: "top",
      type: "negative"
    })
  }
};
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
