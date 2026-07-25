<script setup>
// SCR-COM-01 · 미니앱 진입 (KB 슈퍼앱 허브 — 데모용)
// 급여클럽·밀리터리클럽 아래에 KB텅장일기(우리 앱)를 노출. 탭하면 미니앱 진입(로그인).
import { useRouter } from 'vue-router';

const router = useRouter();

const miniApps = [
  { key: 'salary', name: '급여클럽', desc: '월급 관리 서비스', enabled: false },
  { key: 'military', name: '밀리터리클럽', desc: '군 장병 금융 혜택', enabled: false },
  { key: 'kbdiary', name: 'KB텅장일기', desc: '전역 후 목돈 로드맵', enabled: true, ours: true },
];

const enter = (app) => {
  if (!app.enabled) return;
  // KB텅장일기 진입 → 텅장일병일기 미니앱 시작
  router.push({ name: 'Login' });
};
</script>

<template>
  <div class="hub">
    <header class="hub__top">
      <p class="hub__brand">KB Star</p>
      <h1 class="hub__title">내 미니앱</h1>
    </header>

    <ul class="hub__list">
      <li
        v-for="app in miniApps"
        :key="app.key"
        class="hub-item"
        :class="{ 'hub-item--ours': app.ours, 'hub-item--disabled': !app.enabled }"
        @click="enter(app)"
      >
        <div class="hub-item__thumb">{{ app.name.charAt(0) }}</div>
        <div class="hub-item__body">
          <p class="hub-item__name">
            {{ app.name }}
            <span v-if="app.ours" class="hub-item__badge">NEW</span>
          </p>
          <p class="hub-item__desc">{{ app.desc }}</p>
        </div>
        <span v-if="app.enabled" class="hub-item__arrow">›</span>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.hub {
  width: 393px;
  min-height: 852px;
  margin: 0 auto;
  padding: 32px 20px;
  background-color: #f5f5f7;
  box-sizing: border-box;
}
.hub__brand {
  margin: 0;
  font-size: 13px;
  font-weight: 700;
  color: var(--kb-yellow-deep);
}
.hub__title {
  margin: 4px 0 24px;
  font-size: 22px;
  font-weight: 700;
  color: var(--text-strong);
}
.hub__list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.hub-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border: 1px solid var(--line);
  border-radius: 16px;
  background-color: #ffffff;
  cursor: pointer;
}
.hub-item--ours {
  border-color: var(--kb-yellow);
  background-color: #fffdf3;
}
.hub-item--disabled {
  cursor: default;
  opacity: 0.55;
}
.hub-item__thumb {
  width: 46px;
  height: 46px;
  flex-shrink: 0;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--kb-gray-pale);
  font-size: 18px;
  font-weight: 700;
  color: var(--kb-dark-gray);
}
.hub-item--ours .hub-item__thumb {
  background-color: var(--kb-yellow);
  color: var(--text-strong);
}
.hub-item__body {
  flex: 1;
  min-width: 0;
}
.hub-item__name {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-strong);
}
.hub-item__badge {
  padding: 2px 6px;
  border-radius: 8px;
  background-color: var(--danger);
  color: #ffffff;
  font-size: 10px;
  font-weight: 700;
}
.hub-item__desc {
  margin: 2px 0 0;
  font-size: 13px;
  color: var(--text-muted);
}
.hub-item__arrow {
  font-size: 22px;
  color: var(--text-hint);
}
</style>
