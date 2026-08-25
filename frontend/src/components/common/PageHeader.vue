<script setup>
// 공통 컴포넌트: 페이지 제목 헤더 (담당: 수연)
// 모든 기능 화면 상단 제목을 통일 - eyebrow(작은 안내) + title(주제목) + description(보조설명)
// 사용: <PageHeader eyebrow="전역하면 뭐하지?" title="군적금 로드맵" size="lg" />
//   size="lg" (20px) = 네비게이션 메뉴로 바로 보이는 메인 화면 (홈·로드맵·목돈 작전·후회소비 등)
//   size="md" (18px, 기본) = 더 눌러 들어가는 하위 페이지 (상세·생성·목록 등)
defineProps({
  breadcrumb: {
    type: String, // 제일 상단 작은 위치표시 (어떤 페이지·섹션인지, 예: "자취" / "로드맵 · 자취")
    default: '',
  },
  eyebrow: {
    type: String, // 제목 위 작은 안내문 (없으면 생략)
    default: '',
  },
  title: {
    type: String, // 주제목 (필수)
    default: '',
  },
  description: {
    type: String, // 제목 아래 보조 설명 (없으면 생략)
    default: '',
  },
  size: {
    type: String, // 'lg'(메인 20px) | 'md'(하위 18px, 기본)
    default: 'md',
    validator: (v) => ['lg', 'md'].includes(v),
  },
});
</script>

<template>
  <header class="page-header" :class="`page-header--${size}`">
    <p v-if="breadcrumb" class="page-header__crumb">{{ breadcrumb }}</p>
    <p v-if="eyebrow" class="page-header__eyebrow">{{ eyebrow }}</p>
    <h2 v-if="title" class="page-header__title">{{ title }}</h2>
    <p v-if="description" class="page-header__desc">{{ description }}</p>
  </header>
</template>

<style scoped>
.page-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
  /* 회원 하위페이지(md): 넉넉한 아래 여백 */
  margin-bottom: 28px;
}

/* nav 메인화면(lg): 컨테이너 자체 gap이 있어 아래 여백은 작게 (제목↔내용 과다여백 방지) */
.page-header--lg {
  margin-bottom: 8px;
}

/* 제일 상단 위치표시 (어떤 페이지·섹션인지) - 아주 작게, 연한 회색 */
.page-header__crumb {
  margin: 0 0 8px;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-gray-light); /* 연한 회색 838B93 */
  letter-spacing: 0.2px;
}

/* 제목 위 작은 안내문 - 회색 */
.page-header__eyebrow {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-muted); /* 글자 회색 696E76 */
}

/* 주제목 - 진한 검정 + 굵게 (기본 md = 18px) */
.page-header__title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-strong);
  line-height: 1.3;
}

/* lg = 네비게이션 메인 화면 (19px, 살짝 줄임) */
.page-header--lg .page-header__title {
  font-size: 19px;
}

/* 보조 설명 - 연한 회색 */
.page-header__desc {
  margin: 2px 0 0;
  font-size: 13px;
  font-weight: 400;
  color: var(--text-muted);
  line-height: 1.5;
}
</style>
