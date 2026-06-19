<template>
  <footer class="footer">
    <div class="footer-inner">
      <div class="footer-brand">
        <span class="logo-icon">🎬</span>
        <span class="logo-text">MovieHub</span>
      </div>
      <div class="footer-links">
        <span class="footer-copyright">
          &copy; {{ year }} MovieHub. All rights reserved.
        </span>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
const year = new Date().getFullYear()
</script>

<style scoped>
.footer {
  background: var(--bg-darker);
  border-top: 1px solid var(--border-color);
  padding: 30px 0;
  margin-top: auto;
}

.footer-inner {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--primary-color);
}

.footer-desc {
  color: var(--text-muted);
  font-size: 13px;
  margin-left: 12px;
}

.footer-copyright {
  color: var(--text-muted);
  font-size: 13px;
}
</style>
