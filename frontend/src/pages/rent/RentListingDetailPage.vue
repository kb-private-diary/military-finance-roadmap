<script setup>
// SCR-RENT-03 · Step 3) 매물 상세 + 내 재정 체크  담당: 수연
// 디자인: UI/rent_ui_school_mode.html — 담백 버전(색·이모지 제거)
// 거주기간 슬라이더(6~24) 조작 → 총비용/재정체크 실시간 갱신됨, months 는 Step4~5로 이어짐
import { ref, computed, onMounted, watch, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import rentApi from '@/api/rentApi';
import { useRentStore } from '@/stores/rent';
import { formatManwon } from '@/util/format';
import BaseCard from '@/components/common/BaseCard.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';

const route = useRoute();
const router = useRouter();
const rentStore = useRentStore();
const listingId = Number(route.params.listingId);
const goalId = Number(route.query.goalId) || rentStore.currentGoalId;
const maturity = computed(() => rentStore.maturityAmount || 7200000);

// TODO: 백엔드 매물 상세 API(WIP) 준비되면 샘플 폴백 제거
const SAMPLE = {
  listing: { buildingName: '부산대 앞 오피스텔', dongName: '부산 금정구 장전동', areaSqm: 23, floor: 5, buildYear: 2018, dealDate: '2026-06-15', deposit: 5000000, monthlyRent: 450000, maintenanceFee: 50000 },
  propertyBadges: ['지역 평균보다 저렴', '1개월 전 실거래'],
};

const data = ref(null);
const loading = ref(true);
const months = ref(rentStore.months || 6);

const load = async () => {
  loading.value = true;
  try {
    const res = await rentApi.findListingDetail(listingId, goalId, months.value);
    data.value = res?.listing ? res : SAMPLE;
  } catch {
    data.value = SAMPLE;
  } finally {
    loading.value = false;
  }
};
onMounted(() => {
  load();
  loadAffordability();
});
watch(months, (m) => (rentStore.months = m));

const listing = computed(() => data.value?.listing);
// 위치 좌표(국토부 실거래 API에 매물 이미지가 없어 지도로 대체). 좌표 있을 때만 안내 노출.
const hasCoords = computed(
  () => listing.value?.latitude != null && listing.value?.longitude != null,
);
const coordText = computed(() =>
  hasCoords.value
    ? `위도 ${Number(listing.value.latitude).toFixed(4)}, 경도 ${Number(listing.value.longitude).toFixed(4)}`
    : '',
);

// 카카오맵: 앱키는 .env(VITE_KAKAO_MAP_KEY)에서만, 하드코딩 금지
const KAKAO_KEY = import.meta.env.VITE_KAKAO_MAP_KEY;
const mapEl = ref(null);
// 지도 표시 조건: 앱키 있고 좌표 있을 때만. 아니면 좌표 안내 폴백
const showMap = computed(() => !!KAKAO_KEY && hasCoords.value);
let mapReady = false; // 중복 초기화 방지

// SDK 동적 로드(한 번만). window.kakao.maps 있으면 재사용, 없으면 script 주입
const loadKakaoSdk = () =>
  new Promise((resolve, reject) => {
    if (window.kakao?.maps) return resolve();
    const existing = document.getElementById('kakao-map-sdk');
    if (existing) {
      existing.addEventListener('load', () => resolve());
      existing.addEventListener('error', reject);
      return;
    }
    const script = document.createElement('script');
    script.id = 'kakao-map-sdk';
    script.src = `//dapi.kakao.com/v2/maps/sdk.js?appkey=${KAKAO_KEY}&autoload=false`;
    script.onload = () => resolve();
    script.onerror = reject;
    document.head.appendChild(script);
  });

// 좌표로 지도 생성 + 마커
const initMap = () => {
  if (!mapEl.value || !hasCoords.value) return;
  const { kakao } = window;
  const pos = new kakao.maps.LatLng(
    Number(listing.value.latitude),
    Number(listing.value.longitude),
  );
  const map = new kakao.maps.Map(mapEl.value, { center: pos, level: 3 });
  new kakao.maps.Marker({ position: pos, map });
};

const setupMap = async () => {
  if (mapReady || !showMap.value) return;
  mapReady = true;
  try {
    await loadKakaoSdk();
    window.kakao.maps.load(initMap);
  } catch {
    mapReady = false; // 로드 실패 시 폴백 유지(showMap은 여전히 true라 컨테이너만 비어보임)
  }
};

// listing 이 비동기로 채워지므로 좌표 준비되면 DOM 렌더 후 초기화
watch(
  showMap,
  async (ok) => {
    if (!ok) return;
    await nextTick();
    setupMap();
  },
  { immediate: true },
);
const monthlyCost = computed(() => (listing.value?.monthlyRent ?? 0) + (listing.value?.maintenanceFee ?? 0));
const totalCost = computed(() => (listing.value?.deposit ?? 0) + monthlyCost.value * months.value);
// 실제 소멸 비용: 돌려받지 못하고 나가는 돈 (월세+관리비)×개월. totalCost 에서 보증금을 뺀 값과 같다.
const spentCost = computed(() => monthlyCost.value * months.value);
// 보증금환산(원/월): 응답에 depositConverted 있으면 사용, 없으면 보증금×5.5%÷12 (전월세전환율 기준)
const depositConverted = computed(() => {
  const d = listing.value?.depositConverted;
  if (d != null) return d;
  return Math.round(((listing.value?.deposit ?? 0) * 0.055) / 12);
});

// 감당도 "보증금 포함/제외" 토글 — INCLUDE: 목돈으로 보증금까지 / EXCLUDE: 보증금은 전세대출로
const depositMode = ref('INCLUDE');
const affordability = ref(null);
// 모드별 필요 자금: 포함=총 필요 자금, 제외=보증금 뺀 소멸 비용
const requiredForMode = computed(() =>
  depositMode.value === 'EXCLUDE' ? spentCost.value : totalCost.value,
);
const judge = (need, m) => {
  if (m >= need * 1.2) return '딱 맞아요';
  if (m >= need * 0.8) return '빠듯해요';
  return '예산 초과';
};
// 백엔드 affordability 응답(affordText)이 오면 그 값을, 없으면 로컬 판정으로 폴백
const affordText = computed(
  () => affordability.value?.affordText ?? judge(requiredForMode.value, maturity.value),
);

const loadAffordability = async () => {
  try {
    const res = await rentApi.findAffordability(listingId, months.value, depositMode.value);
    affordability.value = res || null;
  } catch {
    affordability.value = null;
  }
};
watch([months, depositMode], loadAffordability);

const goProducts = () => {
  router.push({ name: 'RentProducts', params: { goalId }, query: { listingId, months: months.value } });
};
// 이전: step2 매물 리스트로 (draft·선택값은 store에 유지됨)
const goPrev = () => {
  router.push({ name: 'RentListingList', params: { goalId } });
};
</script>

<template>
  <div v-if="listing" class="detail">
    <RoadmapCharacterSlider :progress="60" label="자취 로드맵" />
    <header>
      <p class="step">STEP 3</p>
      <h2 class="name">{{ listing.buildingName }}</h2>
      <p class="meta">{{ listing.dongName }} · {{ listing.floor }}층 · {{ listing.areaSqm }}㎡</p>
    </header>

    <!-- 카카오맵: 앱키(.env VITE_KAKAO_MAP_KEY)+좌표 있으면 지도, 아니면 좌표 안내 폴백 -->
    <div v-if="showMap" ref="mapEl" class="map map--live"></div>
    <div v-else class="map">
      <template v-if="hasCoords">
        <span class="map__label">매물 위치</span>
        <span class="map__coord">{{ coordText }}</span>
      </template>
      <span v-else class="map__label">위치 정보 준비 중</span>
    </div>

    <div class="pbadges">
      <span v-for="(b, i) in (data.propertyBadges || SAMPLE.propertyBadges)" :key="i" class="tag">{{ b }}</span>
    </div>

    <BaseCard padding="12px 14px">
      <p class="cap">세부 정보</p>
      <div class="row"><span>계약일</span><span>{{ listing.dealDate }}</span></div>
      <div class="row"><span>건축년도</span><span>{{ listing.buildYear }}년</span></div>
      <div class="row"><span>층수</span><span>{{ listing.floor }}층</span></div>
    </BaseCard>

    <section class="period">
      <div class="row"><span class="label">거주기간</span><span class="val">{{ months }}개월</span></div>
      <input v-model.number="months" type="range" min="6" max="24" step="1" class="slider" />
      <div class="scale"><span>6개월</span><span>24개월</span></div>
    </section>

    <BaseCard padding="12px 14px">
      <p class="cap">{{ months }}개월간 실제로 나가는 돈</p>
      <p class="total">{{ formatManwon(spentCost) }}</p>
      <div class="row"><span>월세 × {{ months }}</span><span>{{ formatManwon(listing.monthlyRent * months) }}</span></div>
      <div class="row"><span>관리비 × {{ months }}</span><span>{{ formatManwon(listing.maintenanceFee * months) }}</span></div>
      <div class="divider" />
      <div class="row"><span>보증금 (계약 끝나면 반환)</span><span>{{ formatManwon(listing.deposit) }}</span></div>
      <p class="deposit-conv">보증금 {{ formatManwon(listing.deposit) }} = 월 {{ formatManwon(depositConverted) }} 상당 (전월세전환 5.5% 기준)</p>
      <p class="deposit-note">보증금은 계약 종료 시 돌려받는 돈이라 실제 소멸 비용은 아니에요</p>
      <div class="divider" />
      <div class="row total-row"><span>총 필요 자금 (지금 마련)</span><span>{{ formatManwon(totalCost) }}</span></div>
      <p class="link-note">보증금이 부담되면 다음 단계에서 전월세보증금대출을 확인하세요</p>
    </BaseCard>

    <div class="check">
      <div class="seg" role="group" aria-label="보증금 감당 방식">
        <button
          type="button"
          class="seg__btn"
          :class="{ on: depositMode === 'INCLUDE' }"
          :aria-pressed="depositMode === 'INCLUDE'"
          @click="depositMode = 'INCLUDE'"
        >보증금 포함</button>
        <button
          type="button"
          class="seg__btn"
          :class="{ on: depositMode === 'EXCLUDE' }"
          :aria-pressed="depositMode === 'EXCLUDE'"
          @click="depositMode = 'EXCLUDE'"
        >보증금 제외</button>
      </div>
      <p class="seg__desc">{{ depositMode === 'INCLUDE' ? '목돈으로 보증금까지' : '보증금은 전세대출로' }}</p>
      <div class="check__t">내 재정 체크: {{ affordText }}</div>
      <div class="check__s">만기금 {{ formatManwon(maturity) }} 기준</div>
    </div>

    <p class="promo">저장하면 지역별 실제 공과금까지 계산해서 진짜 정확한 자취 예산을 알려드려요</p>

    <BottomButtonBar
      secondary-label="이전"
      primary-label="금융상품 보기"
      @secondary-click="goPrev"
      @primary-click="goProducts"
    />
  </div>
  <p v-else class="loading">불러오는 중...</p>
</template>

<style scoped>
.detail {
  padding: 20px 0 88px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.step {
  font-size: 12px;
  color: var(--text-muted);
}
.name {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-strong);
}
.meta {
  margin-top: 3px;
  font-size: 12px;
  color: var(--text-muted);
}
.map {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  height: 100px;
  border-radius: 8px;
  background: var(--kb-gray-pale);
  color: var(--text-muted);
  font-size: 13px;
}
/* 실제 카카오맵 컨테이너: 타일이 보이도록 높이 확보 */
.map--live {
  display: block;
  height: 200px;
  background: var(--kb-gray-pale);
  overflow: hidden;
}
.map__label {
  font-size: 12px;
  color: var(--text-muted);
}
.map__coord {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-body);
}
.pbadges {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.tag {
  font-size: 11px;
  padding: 3px 10px;
  border: 1px solid var(--line);
  border-radius: 999px;
  color: var(--text-muted);
}
.cap {
  font-size: 11px;
  color: var(--text-muted);
  margin-bottom: 6px;
}
.row {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-body);
  margin-bottom: 3px;
}
.row span:first-child {
  color: var(--text-muted);
}
.label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-body);
}
.val {
  font-size: 13px;
  font-weight: 700;
}
.period {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.slider {
  width: 100%;
  accent-color: var(--text-strong);
}
.scale {
  display: flex;
  justify-content: space-between;
  font-size: 10px;
  color: var(--text-hint);
}
.total {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-strong);
  margin-bottom: 8px;
}
.divider {
  height: 1px;
  background: var(--line);
  margin: 6px 0;
}
.deposit-conv {
  font-size: 11px;
  color: var(--text-body);
  line-height: 1.5;
  margin: 3px 0 1px;
}
.deposit-note {
  font-size: 10px;
  color: var(--text-hint);
  line-height: 1.5;
  margin: 2px 0 1px;
}
.row.total-row span {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-strong);
}
.link-note {
  margin-top: 6px;
  font-size: 11px;
  color: var(--text-muted);
  line-height: 1.5;
}
.check {
  padding: 12px 14px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fafafa;
}
.seg {
  display: flex;
  gap: 0;
  border: 1px solid var(--line);
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 6px;
}
.seg__btn {
  flex: 1;
  padding: 7px 0;
  border: 0;
  background: #fff;
  color: var(--text-muted);
  font-size: 12px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
}
.seg__btn + .seg__btn {
  border-left: 1px solid var(--line);
}
.seg__btn.on {
  background: var(--kb-yellow);
  color: var(--text-strong);
}
.seg__desc {
  font-size: 11px;
  color: var(--text-muted);
  margin-bottom: 8px;
}
.check__t {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-strong);
}
.check__s {
  margin-top: 3px;
  font-size: 11px;
  color: var(--text-muted);
}
.promo {
  padding: 12px 14px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fafafa;
  font-size: 11px;
  color: var(--text-muted);
  line-height: 1.6;
}
.loading {
  padding: 60px 0;
  text-align: center;
  font-size: 13px;
  color: var(--text-hint);
}
/* 버튼 색은 BottomButtonBar 컴포넌트 기본값 사용 */
</style>
