<template>
  <ul class="navbar-nav">
    <MenuItem v-for="menu in menus" :menu="menu" :key="menu.title" />
  </ul>
</template>

<script setup>
import MenuItem from './MenuItem.vue';

const props = defineProps({
  menus: { Type: Array, required: true },
});
</script>

<style lang="scss" scoped></style>
