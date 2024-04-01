<template>
  <router-view/>
</template>

<script setup>

import index from "src/store";

defineOptions({
  name: 'App',
  store: index
});
</script>
