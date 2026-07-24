<!--
  EmptyState - 데이터 없을 때(빈 화면) 공통 컴포넌트

  기본 사용법:
    <EmptyState
      title="아직 저장한 목표가 없어요"
      description="위의 관심있는 카테고리를 눌러 나만의 로드맵을 시작해보세요"
    />

  카테고리별 전용 아이콘이 필요하면 #icon 슬롯으로 교체:
    <EmptyState title="자동차 목표가 아직 없어요" description="자동차 카드에서 목표를 등록하면 여기에 표시돼요">
      <template #icon>
        <svg ...>도메인 전용 아이콘</svg>
      </template>
    </EmptyState>

  title, description은 필수 prop이며 도메인마다 문구가 다르므로 고정 텍스트를 두지 않았습니다.
  #icon 슬롯을 안 채우면 기본 핀+아이콘이 나옵니다.
-->
<script setup>
// Props: 빈 상태 문구 (카테고리/도메인마다 문구·아이콘이 다르므로 고정 텍스트 대신 prop으로 받음)
// (컨벤션: camelCase, 타입/필수/기본값 명시)
const props = defineProps({
  title: { type: String, required: true },
  description: { type: String, required: true },
});
</script>

<template>
  <div class="empty-state">
    <div class="empty-state__icon">
      <!-- 도메인/카테고리별 아이콘은 사용하는 쪽에서 slot으로 주입 -->
      <slot name="icon">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="24"
          height="24"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
          class="icon icon-tabler icons-tabler-outline icon-tabler-map-pin-plus"
        >
          <path stroke="none" d="M0 0h24v24H0z" fill="none" />
          <path d="M9 11a3 3 0 1 0 6 0a3 3 0 0 0 -6 0" />
          <path
            d="M12.794 21.322a2 2 0 0 1 -2.207 -.422l-4.244 -4.243a8 8 0 1 1 13.59 -4.616"
          />
          <path d="M16 19h6" />
          <path d="M19 16v6" />
        </svg>
      </slot>
    </div>
    <p class="empty-state__title">{{ title }}</p>
    <p class="empty-state__description">{{ description }}</p>
  </div>
</template>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
  text-align: center;
  border: 1px dashed #d1d5db;
  border-radius: 12px;
}

.empty-state__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  margin-bottom: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 50%;
}

.empty-state__icon :deep(svg) {
  width: 24px;
  height: 24px;
}

.empty-state__title {
  font-size: 16px;
  font-weight: 700;
  color: #1b1b1b;
  margin: 0 0 8px;
}

.empty-state__description {
  font-size: 13px;
  color: #9ca3af;
  margin: 0;
}
</style>
