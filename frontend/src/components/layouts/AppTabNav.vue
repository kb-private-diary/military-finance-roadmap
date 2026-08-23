<script setup>
import { nextTick, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const router = useRouter();
const route = useRoute();

// 탭 네비게이션 정의 (라우트 정의서 기준 route name 사용)
const tabItems = [
  { label: '홈', routeName: 'Home', match: ['/', '/home'] },
  { label: 'D-Day', routeName: 'Dashboard', match: ['/dashboard'] },
  // 로드맵 섹션: 로드맵 메인 + 목표 도메인 하위 페이지(자동차·자취·진로·여행)까지 포함
  { label: '로드맵', routeName: 'RoadmapMain', match: ['/roadmap', '/car', '/rent', '/job', '/travel'] },
  { label: '목돈작전', routeName: 'Simulator', match: ['/simulator'] },
  { label: '전우들', routeName: 'Social', match: ['/social'] },
  { label: '후회소비', routeName: 'RegretDashboard', match: ['/regret'] },
];

// 현재 경로가 탭 섹션에 속하는지 판정 — 하위 상세 페이지도 상위 탭이 활성으로 보이도록 prefix 매칭
const isTabActive = (tab) =>
  tab.match.some((m) =>
    m === '/' ? route.path === '/' : route.path === m || route.path.startsWith(`${m}/`),
  );

// 탭 버튼 DOM 참조 모음 (선택된 탭 전체 노출을 위한 스크롤 이동에 사용)
const tabRefs = ref({});

const setTabRef = (routeName) => (el) => {
  if (el) {
    tabRefs.value[routeName] = el;
  }
};

// 선택된 탭이 잘려 보이지 않도록 스크롤 위치 조정
const scrollActiveTabIntoView = () => {
  const activeName = tabItems.find((tab) => isTabActive(tab))?.routeName;
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
      :class="{ 'app-tabnav__item--active': isTabActive(tab) }"
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
  padding: 0 20px;
  border-bottom: 1px solid var(--line);
  flex-shrink: 0;
  scrollbar-width: none; /* Firefox */
  -webkit-overflow-scrolling: touch; /* iOS 관성 스크롤 */
}

.app-tabnav::-webkit-scrollbar {
  display: none; /* Chrome, Safari */
}

.app-tabnav__item {
  flex-shrink: 0;
  padding: 12px 14px;
  background: none;
  border: none;
  font-size: 14px;
  color: var(--text-hint);
  cursor: pointer;
  white-space: nowrap;
}

.app-tabnav__item--active {
  color: var(--text-strong); /* 찐한 검정 글자 */
  font-weight: 700;
  border-bottom: 3px solid var(--text-strong); /* 하단 검정 줄 */
}
</style>
