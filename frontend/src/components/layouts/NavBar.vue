<script setup>
// Vue의 Composition API에서 반응형 객체와 계산 속성을 불러옴
import { reactive, computed } from 'vue';
// 전역 설정 값을 담고 있는 config 파일을 불러옴 (현재 코드에서는 사용되지 않음)
import config from '@/config';
import MenuGroup from './menu/MenuGroup.vue';
import AccountMenuGroup from './menu/AccountMenuGroup.vue';

// 반응형 객체 생성. isNavShow는 네비게이션 메뉴의 표시 여부를 나타냄
// 객체 형태의 반응형 상태 선언
let state = reactive({ isNavShow: false });
// state.isNavShow 값에 따라 적용할 CSS 클래스명을 동적으로 계산
// 상태에 따라 자동 계산되는 속성 정의
let navClass = computed(
  () =>
    state.isNavShow
      ? 'collapse navbar-collapse show' // 메뉴가 열릴 때 클래스
      : 'collapse navbar-collapse', // 메뉴가 닫힐 때 클래스
);
// 버튼 클릭 등으로 isNavShow 값을 true/false로 토글하는 함수
const toggleNavShow = () => (state.isNavShow = !state.isNavShow);
</script>
<template>
  <!-- 부트스트랩 스타일이 적용된 최상위 네비게이션 바 -->
  <nav class="navbar navbar-expand-sm bg-primary navbar-dark">
    <!-- 반응형 레이아웃을 위한 컨테이너 -->
    <div class="container-fluid">
      <!-- 홈으로 이동하는 라우터 링크, 브랜드 영역 -->
      <router-link class="navbar-brand" to="/">
        <i class="fa-solid fa-house"></i>
        {{ config.title }}
      </router-link>
      <!-- 모바일 화면에서 메뉴 버튼 역할을 하는 햄버거 버튼 -->
      <button
        class="navbar-toggler"
        type="button"
        data-bs-toggle="collapse"
        data-bs-target="#collapsibleNavbar"
        @click="toggleNavShow"
      >
        <!-- 햄버거 아이콘 -->
        <span class="navbar-toggler-icon"></span>
      </button>
      <!-- 메뉴 영역: isNavShow 값에 따라 동적으로 class가 변경됨 -->
      <div :class="navClass" id="collapsibleNavbar">
        <!-- 추후 네비게이션 메뉴 항목들이 들어갈 자리 -->
        <MenuGroup :menus="config.menus" />
        <AccountMenuGroup />
      </div>
    </div>
  </nav>
</template>
