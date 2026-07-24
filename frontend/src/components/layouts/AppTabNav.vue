<script setup>
import { computed, nextTick, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const router = useRouter();
const route = useRoute();

// 탭 네비게이션 정의 (라우트 정의서 기준 route name 사용)
const tabItems = [
  { label: '홈', routeName: 'Home' },
  { label: '대시보드', routeName: 'Dashboard' },
  { label: '로드맵', routeName: 'RoadmapMain' },
  { label: '시뮬레이터', routeName: 'Simulator' },
  { label: '소셜', routeName: 'Social' },
  { label: '후회소비', routeName: 'RegretDashboard' },
];

// 현재 라우트와 탭 매칭 (path 문자열 비교 대신 route name 기반 — 컨벤션 라우트 규칙 준수)
const isActiveTab = computed(() => (routeName) => route.name === routeName);

// 탭 버튼 DOM 참조 모음 (선택된 탭 전체 노출을 위한 스크롤 이동에 사용)
const tabRefs = ref({});

const setTabRef = (routeName) => (el) => {
  if (el) {
    tabRefs.value[routeName] = el;
  }
};

// 선택된 탭이 잘려 보이지 않도록 스크롤 위치 조정
const scrollActiveTabIntoView = () => {
  const activeName = tabItems.find(
    (tab) => tab.routeName === route.name,
  )?.routeName;
  const activeEl = activeName ? tabRefs.value[activeName] : null;
  if (activeEl) {
    activeEl.scrollIntoView({
      behavior: 'smooth',
      block: 'nearest',
      inline: 'nearest',
    });
  }
};

watch(
  () => route.name,
  () => {
    nextTick(scrollActiveTabIntoView);
  },
  { immediate: true },
);

const moveToTab = (routeName) => {
  // 컨벤션: router.push는 path 조합이 아닌 name 방식으로 통일
  router.push({ name: routeName });
};
</script>

<template>
  <nav class="app-tabnav">
    <button
      v-for="tab in tabItems"
      :key="tab.routeName"
      :ref="setTabRef(tab.routeName)"
      class="app-tabnav__item"
      :class="{ 'app-tabnav__item--active': isActiveTab(tab.routeName) }"
      type="button"
      @click="moveToTab(tab.routeName)"
    >
      {{ tab.label }}
    </button>
  </nav>
</template>

<style scoped>
.app-tabnav {
  display: flex;
  overflow-x: auto;
  min-width: 0;
  padding: 0 16px;
  border-bottom: 1px solid #e5e5e5;
  flex-shrink: 0;
  scrollbar-width: none; /* Firefox */
  -webkit-overflow-scrolling: touch; /* iOS 관성 스크롤 */
}

.app-tabnav::-webkit-scrollbar {
  display: none; /* Chrome, Safari */
}

.app-tabnav__item {
  flex-shrink: 0;
  padding: 10px 12px;
  background: none;
  border: none;
  font-size: 14px;
  color: #9ca3af;
  cursor: pointer;
  white-space: nowrap;
}

.app-tabnav__item--active {
  color: #1b1b1b;
  font-weight: 700;
  border-bottom: 2px solid #ffbc00;
}
</style>
