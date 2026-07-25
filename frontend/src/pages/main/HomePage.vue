<script setup>
// SCR-MAIN-01 · 홈 메인 (요약)  (담당: 지원 / 데모)
// 상단 탭(홈·대시보드…)은 전역 AppTabNav가 렌더. 여기선 요약 카드만.
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// 데모용 요약 데이터
const summary = {
  name: '수연',
  dday: 234,
  paid: 5800000,
  dischargeDate: '2027.09.29',
  maturity: 20000000,
  regretCount: 3,
  socialRank: 18,
};

// 목표 로드맵 (자취는 우리 플로우로 연결)
const roadmaps = ref([
  { key: 'travel', label: '여행', liked: true, to: null },
  { key: 'rent', label: '자취', liked: true, to: 'RentGoalCreate' },
  { key: 'car', label: '자동차', liked: false, to: null },
  { key: 'job', label: '진로', liked: false, to: null },
]);

const won = (n) => n.toLocaleString('ko-KR') + '원';

const goSimulator = () => router.push({ name: 'Simulator' });
const goRegret = () => router.push({ name: 'RegretDashboard' });
const goSocial = () => router.push({ name: 'Social' });
const openRoadmap = (r) => {
  if (r.to) router.push({ name: r.to });
};
const toggleLike = (r) => (r.liked = !r.liked);
</script>

<template>
  <div class="home">
    <!-- D-Day 카드 -->
    <section class="dday">
      <p class="dday__top">🪖 {{ summary.name }}님, 전역까지 <b class="dday__d">D-{{ summary.dday }}</b></p>
      <p class="dday__paid">현재 납입 금액 <b>{{ won(summary.paid) }}</b></p>
      <p class="dday__date">{{ summary.dischargeDate }} 전역 예정</p>
    </section>

    <!-- 만기 수령액 -->
    <section class="card maturity" @click="goSimulator">
      <p class="maturity__label">군적금 만기 예상 수령액</p>
      <p class="maturity__value">{{ won(summary.maturity) }}</p>
      <div class="maturity__foot">
        <span>시뮬레이터 보기 ›</span>
      </div>
    </section>

    <!-- 요약 행 -->
    <button type="button" class="row-card" @click="goRegret">
      <span>후회 소비 회고</span>
      <span class="row-card__val row-card__val--danger">{{ summary.regretCount }}건 ›</span>
    </button>

    <button type="button" class="row-card" @click="goSocial">
      <span>소셜·부대 통계</span>
      <span class="row-card__val">상위 {{ summary.socialRank }}% ›</span>
    </button>

    <!-- 목표 로드맵 -->
    <h3 class="section-title">목표 로드맵</h3>
    <div class="roadmap-box">
      <div v-for="r in roadmaps" :key="r.key" class="roadmap-item" @click="openRoadmap(r)">
        <span class="roadmap-item__pill">{{ r.label }}</span>
        <button
          type="button"
          class="roadmap-item__star"
          :class="{ on: r.liked }"
          @click.stop="toggleLike(r)"
          aria-label="관심"
        >
          ★
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home {
  padding: 16px 0 24px;
}

/* D-Day */
.dday {
  padding: 20px;
  border-radius: 16px;
  background-color: #ededed;
  margin-bottom: 14px;
}
.dday__top {
  margin: 0 0 14px;
  font-size: 14px;
  color: var(--kb-dark-gray);
}
.dday__d {
  font-size: 20px;
  color: var(--text-strong);
  margin-left: 4px;
}
.dday__paid {
  margin: 0 0 6px;
  font-size: 15px;
  color: var(--kb-dark-gray);
}
.dday__paid b {
  color: var(--text-strong);
}
.dday__date {
  margin: 0;
  font-size: 12px;
  color: var(--text-muted);
}

/* 카드 공통 */
.card {
  border: 1px solid var(--line);
  border-radius: 16px;
  background-color: #ffffff;
}

/* 만기 수령액 */
.maturity {
  padding: 20px;
  margin-bottom: 14px;
  cursor: pointer;
}
.maturity__label {
  margin: 0 0 8px;
  font-size: 15px;
  color: var(--text-body);
}
.maturity__value {
  margin: 0 0 14px;
  font-size: 24px;
  font-weight: 800;
  color: var(--text-strong);
}
.maturity__foot {
  padding-top: 12px;
  border-top: 1px solid var(--line);
  text-align: right;
  font-size: 13px;
  color: var(--text-muted);
}

/* 요약 행 */
.row-card {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px;
  margin-bottom: 12px;
  border: 1px solid var(--line);
  border-radius: 14px;
  background-color: #ffffff;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-strong);
  cursor: pointer;
}
.row-card__val {
  font-size: 14px;
  color: var(--text-muted);
}
.row-card__val--danger {
  color: var(--danger);
  font-weight: 700;
}

/* 목표 로드맵 */
.section-title {
  margin: 22px 0 12px;
  font-size: 17px;
  font-weight: 700;
  color: var(--text-strong);
}
.roadmap-box {
  padding: 16px;
  border: 1px solid var(--line);
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.roadmap-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border: 1px solid var(--line);
  border-radius: 999px;
  cursor: pointer;
}
.roadmap-item__pill {
  padding: 4px 14px;
  border-radius: 999px;
  background-color: var(--kb-gray-pale);
  font-size: 13px;
  font-weight: 600;
  color: var(--kb-dark-gray);
}
.roadmap-item__star {
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 50%;
  background-color: var(--kb-gray-pale);
  color: #ffffff;
  font-size: 15px;
  cursor: pointer;
}
.roadmap-item__star.on {
  background-color: var(--kb-yellow);
  color: var(--text-strong);
}
</style>
