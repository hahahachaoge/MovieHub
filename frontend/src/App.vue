<template>
  <div class="app-container">
    <NavBar v-if="showNavbar" />
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade-slide" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    <FooterBar v-if="showNavbar" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import NavBar from './components/NavBar.vue'
import FooterBar from './components/FooterBar.vue'

const route = useRoute()
const showNavbar = computed(() => {
  const name = route.name as string
  return !['Login', 'Register'].includes(route.name as string)
})
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-dark);
}

.main-content {
  flex: 1;
  padding-top: var(--header-height);
}
</style>