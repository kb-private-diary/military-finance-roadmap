<script setup>
// SCR-RENT-03 · Step 3) 매물 상세 + 내 재정 체크  담당: 수연
// 디자인: UI/rent_ui_school_mode.html — 담백 버전(색·이모지 제거)
// 거주기간 슬라이더(6~24) 조작 → 총비용/재정체크 실시간 갱신됨, months 는 Step4~5로 이어짐
import { ref, computed, onMounted, watch, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import rentApi from '@/api/rentApi';
import { useRentStore } from '@/stores/rent';
import { formatManwon } from '@/util/format';
import { useToast } from '@/composables/useToast';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseBottomSheet from '@/components/common/BaseBottomSheet.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';

const { show: showToast } = useToast();

const route = useRoute();
const router = useRouter();
const rentStore = useRentStore();
const listingId = Number(route.params.listingId);
const goalId = Number(route.query.goalId) || rentStore.currentGoalId;
const maturity = computed(() => rentStore.maturityAmount || 7200000);

// TODO: 백엔드 매물 상세 API(WIP) 준비되면 샘플 폴백 제거
const SAMPLE = {
  listing: { buildingName: '부산대 앞 오피스텔', dongName: '부산 금정구 장전동', areaSqm: 23, floor: 5, buildYear: 2018, dealDate: '2026-06-15', deposit: 5000000, monthlyRent: 450000, maintenanceFee: 50000 },
  // propertyBadges 는 신선도(실거래 시점)만 — 시세는 step2 priceLevel 로 별도 표시
  propertyBadges: ['1개월 전 실거래'],
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

// 시세 뱃지: step2에서 넘어온 priceLevel(CHEAP/AVERAGE/EXPENSIVE)로 표시.
// step3 단건 응답은 지역평균을 못 줘서 시세가 없으므로 store 값에 의존.
// step2를 안 거치고 직접 진입하면 priceLevel이 없어 시세 뱃지는 생략(신선도만 표시).
const PRICE_BADGE = {
  CHEAP: { label: '💰 지역 평균보다 저렴', tone: 'good' }, // 초록
  AVERAGE: { label: '💰 지역 평균 수준', tone: 'info' }, // 파랑
  EXPENSIVE: { label: '⚠️ 지역 평균보다 비쌈', tone: 'warn' }, // 노랑
};
const priceBadge = computed(() => PRICE_BADGE[rentStore.selectedPriceLevel] || null);

// 신선도 뱃지: 백엔드 propertyBadges 텍스트("1개월 전 실거래" 등)에서 개월 수를 읽어 이모지+색 매핑.
// 1/3개월 = 최근 실거래(✨ 보라), 6개월+ = 오래된 실거래(📅 회색). 그 외는 텍스트 그대로(중립).
const freshBadge = (raw) => {
  const t = String(raw ?? '');
  if (t.includes('6개월')) return { label: '📅 6개월 이상 전', tone: 'old' };
  if (t.includes('1개월') || t.includes('3개월')) return { label: `✨ ${t}`, tone: 'fresh' };
  return { label: t, tone: 'neutral' };
};
const freshBadges = computed(() => (data.value?.propertyBadges || []).map(freshBadge));
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

// 좌표로 지도 생성 + 대략 위치 원(Circle)
// 정확 위치 노출 방지: 마커 대신 반경 150m 원만 표시해 "대략 범위"만 보여준다.
const initMap = () => {
  if (!mapEl.value || !hasCoords.value) return;
  const { kakao } = window;
  const pos = new kakao.maps.LatLng(
    Number(listing.value.latitude),
    Number(listing.value.longitude),
  );
  // level 4: 원(150m)이 화면에 적당히 차도록
  const map = new kakao.maps.Map(mapEl.value, { center: pos, level: 4 });
  const circle = new kakao.maps.Circle({
    center: pos,
    radius: 150, // m
    strokeWeight: 2,
    strokeColor: '#F0A500', // KB 노랑 계열(목업 지정색)
    strokeOpacity: 1,
    strokeStyle: 'dashed',
    fillColor: '#FFBC00',
    fillOpacity: 0.22,
  });
  circle.setMap(map);
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

// ── 동네 시세 상세보기 바텀시트 ─────────────────────────────
const marketSheetOpen = ref(false);
const market = ref(null);
const marketLoading = ref(false);
const marketError = ref(false);
let marketLoaded = false; // 재오픈 시 재호출 방지(성공한 경우만)

const openMarketSheet = async () => {
  marketSheetOpen.value = true;
  if (marketLoaded) return; // 이미 받아온 데이터 재사용
  marketLoading.value = true;
  marketError.value = false;
  try {
    market.value = await rentApi.findMarketComparison(listingId);
    marketLoaded = true;
  } catch {
    marketError.value = true;
    showToast('시세 정보를 불러오지 못했어요', 'error');
  } finally {
    marketLoading.value = false;
  }
};

// 만원 단위 표기(접미사 "만"). formatManwon 은 "원"까지 붙어 표에 부적합해 별도 유틸.
const toMan = (v) => `${Math.round((v ?? 0) / 10000).toLocaleString('ko-KR')}만`;

// 비교표 셀 포맷: key/label 로 단위 판별(면적 ㎡ / 건축년도 년 / 층 / 그 외 금액 만).
const fmtCompare = (row, value) => {
  const t = `${row?.key ?? ''} ${row?.label ?? ''}`;
  if (t.includes('면적')) return `${value}㎡`;
  if (t.includes('건축') || t.includes('년도')) return `${value}년`;
  if (t.includes('층')) return `${value}층`;
  return toMan(value);
};

// 이 매물 월세(㎡당월세 행은 제외). 없으면 listing 폴백.
const mineRent = computed(() => {
  const rows = market.value?.rows ?? [];
  const row = rows.find((r) => {
    const t = `${r.key ?? ''} ${r.label ?? ''}`;
    return t.includes('월세') && !t.includes('㎡');
  });
  return row?.mine ?? listing.value?.monthlyRent ?? 0;
});

// 슬라이더 위치(%) — (값-최저)/(최고-최저). 범위가 0이면 중앙.
const pctOf = (v) => {
  const min = market.value?.rentMin ?? 0;
  const max = market.value?.rentMax ?? 0;
  if (max <= min) return 50;
  return Math.min(100, Math.max(0, ((v - min) / (max - min)) * 100));
};
const minePct = computed(() => pctOf(mineRent.value));
const avgPct = computed(() => pctOf(market.value?.rentAvg ?? 0));

// 지역 평균 대비 차액(원). 음수=저렴, 양수=비쌈.
const rentDiff = computed(() => mineRent.value - (market.value?.rentAvg ?? 0));
const rentDiffLabel = computed(() => {
  const man = rentDiff.value / 10000;
  const sign = man > 0 ? '+' : ''; // 음수는 toFixed 가 이미 '-' 포함
  return `${sign}${man.toFixed(1)}만`;
});
// 저렴(음수) 초록 / 비쌈(양수) 빨강 / 동일 중립
const rentDiffTone = computed(() => {
  if (rentDiff.value < 0) return 'good';
  if (rentDiff.value > 0) return 'bad';
  return 'neutral';
});

// 가성비 판정 박스 톤/아이콘
const VERDICT = {
  GOOD: { tone: 'good', icon: '👍' },
  NORMAL: { tone: 'normal', icon: '🙂' },
  BAD: { tone: 'bad', icon: '😥' },
};
const verdictMeta = computed(() => VERDICT[market.value?.verdict] || VERDICT.NORMAL);

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
    <RoadmapCharacterSlider :step="3" label="자취 로드맵" />
    <header>
      <p class="step">STEP 3</p>
      <h2 class="name">{{ listing.buildingName }}</h2>
      <p class="meta">{{ listing.dongName }} · {{ listing.floor }}층 · {{ listing.areaSqm }}㎡</p>
    </header>

    <!-- 카카오맵: 앱키(.env VITE_KAKAO_MAP_KEY)+좌표 있으면 대략 위치 원(Circle), 아니면 폴백 -->
    <div v-if="showMap" class="map-wrap">
      <div ref="mapEl" class="map map--live"></div>
      <p class="lockbar">🔒 정확한 위치는 대략 범위로만 표시돼요</p>
    </div>
    <div v-else class="map">
      <template v-if="hasCoords">
        <span class="map__label">매물 위치</span>
        <span class="map__coord">{{ coordText }}</span>
      </template>
      <span v-else class="map__label">위치 정보 없음</span>
    </div>

    <!-- 시세 뱃지(step2 priceLevel, 있을 때만) + 신선도 뱃지(응답 propertyBadges) -->
    <div class="pbadges">
      <span v-if="priceBadge" class="tag" :class="`tag--${priceBadge.tone}`">{{ priceBadge.label }}</span>
      <span v-for="(b, i) in freshBadges" :key="i" class="tag" :class="`tag--${b.tone}`">{{ b.label }}</span>
    </div>

    <BaseCard padding="12px 14px">
      <p class="cap">세부 정보</p>
      <div class="row"><span>계약일</span><span>{{ listing.dealDate }}</span></div>
      <div class="row"><span>건축년도</span><span>{{ listing.buildYear }}년</span></div>
      <div class="row"><span>층수</span><span>{{ listing.floor }}층</span></div>
      <button type="button" class="market-btn" @click="openMarketSheet">
        동네 시세 상세보기
      </button>
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

    <!-- 동네 시세 상세보기 바텀시트 -->
    <BaseBottomSheet
      v-model="marketSheetOpen"
      title="동네 시세 상세보기"
      confirm-text="닫기"
    >
      <p v-if="marketLoading" class="mkt-state">불러오는 중...</p>
      <p v-else-if="marketError" class="mkt-state">시세 정보를 불러오지 못했어요</p>

      <!-- 표본 부족: (A)(B)(C) 대신 안내만 -->
      <p v-else-if="market && !market.enough" class="mkt-empty">
        이 동네는 비교할 매물 데이터가 부족해요 🥲
      </p>

      <template v-else-if="market">
        <!-- (A) 시세 슬라이더 -->
        <section class="mkt-slider">
          <p class="mkt-slider__title">{{ market.umdName }} 시세에서 이 매물은?</p>
          <p class="mkt-diff">
            <span class="mkt-diff__num" :class="`mkt-diff__num--${rentDiffTone}`">{{ rentDiffLabel }}</span>
            <span class="mkt-diff__unit">월세 기준</span>
          </p>
          <div class="bar">
            <div class="bar__track"></div>
            <div class="bar__avg" :style="{ left: avgPct + '%' }" title="동네 평균"></div>
            <div class="bar__mine" :style="{ left: minePct + '%' }">
              <span class="bar__mine-dot"></span>
              <span class="bar__mine-tag">이 매물</span>
            </div>
          </div>
          <div class="bar__scale">
            <span>{{ toMan(market.rentMin) }}</span>
            <span>평균 {{ toMan(market.rentAvg) }}</span>
            <span>{{ toMan(market.rentMax) }}</span>
          </div>
          <p class="mkt-cap">
            같은 조건 최근 {{ market.sampleCount }}건 중 하위 {{ market.rentPercentile }}% 가격이에요
          </p>
        </section>

        <!-- (B) 비교 테이블 -->
        <section class="mkt-table">
          <div class="mkt-table__head">
            <span class="mkt-table__title">{{ market.umdName }} 평균과 비교</span>
            <span class="mkt-table__badge">동네 실거래 {{ market.sampleCount }}건</span>
          </div>
          <div class="cmp cmp--head">
            <span>항목</span>
            <span>이 매물</span>
            <span>동네 평균</span>
            <span aria-hidden="true"></span>
          </div>
          <div v-for="row in market.rows" :key="row.key" class="cmp">
            <span class="cmp__label">{{ row.label }}</span>
            <span class="cmp__mine">{{ fmtCompare(row, row.mine) }}</span>
            <span class="cmp__avg">{{ fmtCompare(row, row.avg) }}</span>
            <span class="cmp__flag">{{ row.better ? '✅' : '⚠️' }}</span>
          </div>
          <p class="cmp-sum">🏆 {{ market.totalItems }}개 중 {{ market.betterCount }}개 우위</p>
        </section>

        <!-- (C) 가성비 판정 박스 -->
        <section class="verdict" :class="`verdict--${verdictMeta.tone}`">
          <span class="verdict__icon">{{ verdictMeta.icon }}</span>
          <div class="verdict__body">
            <p class="verdict__title">{{ market.verdictTitle }}</p>
            <p class="verdict__text">{{ market.verdictText }}</p>
          </div>
        </section>
      </template>
    </BaseBottomSheet>
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
/* 뱃지 톤: 테두리+글자색으로 (기존 .tag 결 유지, 색만 명세대로) */
/* 시세 CHEAP - 초록 */
.tag--good {
  border-color: var(--military-green-light);
  color: var(--success);
}
/* 시세 AVERAGE - 파랑 */
.tag--info {
  border-color: var(--pastel-blue);
  color: var(--info-blue);
}
/* 시세 EXPENSIVE - 노랑(amber) */
.tag--warn {
  border-color: var(--roadmap-active);
  color: var(--roadmap-active);
}
/* 신선도 1/3개월 - 보라 */
.tag--fresh {
  border-color: var(--pastel-purple);
  color: var(--purple);
}
/* 신선도 6개월+ - 회색 */
.tag--old {
  border-color: var(--line);
  color: var(--text-hint);
}
.tag--neutral {
  border-color: var(--line);
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

/* ── 지도: 대략 위치 원 + 잠금바 ───────────────────────── */
.map-wrap {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.lockbar {
  font-size: 11px;
  color: var(--text-hint);
  text-align: center;
}

/* ── 동네 시세 상세보기 버튼 ───────────────────────────── */
.market-btn {
  width: 100%;
  margin-top: 10px;
  padding: 9px 0;
  border: 1px solid var(--kb-yellow-deep);
  border-radius: 8px;
  background: var(--kb-yellow-pale);
  color: var(--text-strong);
  font-size: 12px;
  font-weight: 700;
  font-family: inherit;
  cursor: pointer;
}

/* ── 바텀시트 공통 상태 ────────────────────────────────── */
.mkt-state {
  padding: 40px 0;
  text-align: center;
  font-size: 13px;
  color: var(--text-hint);
}
.mkt-empty {
  padding: 40px 12px;
  text-align: center;
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-muted);
}

/* ── (A) 시세 슬라이더 ─────────────────────────────────── */
.mkt-slider {
  margin-bottom: 20px;
}
.mkt-slider__title {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-strong);
  margin-bottom: 8px;
}
.mkt-diff {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-bottom: 14px;
}
.mkt-diff__num {
  font-size: 22px;
  font-weight: 800;
}
.mkt-diff__num--good {
  color: var(--success);
}
.mkt-diff__num--bad {
  color: var(--danger);
}
.mkt-diff__num--neutral {
  color: var(--info-blue);
}
.mkt-diff__unit {
  font-size: 12px;
  color: var(--text-muted);
}
.bar {
  position: relative;
  height: 28px;
  margin: 0 6px;
}
.bar__track {
  position: absolute;
  top: 13px;
  left: 0;
  right: 0;
  height: 4px;
  border-radius: 999px;
  background: var(--kb-gray-pale);
}
.bar__avg {
  position: absolute;
  top: 7px;
  width: 2px;
  height: 16px;
  background: var(--info-blue);
  transform: translateX(-50%);
}
.bar__mine {
  position: absolute;
  top: 0;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
}
.bar__mine-dot {
  width: 14px;
  height: 14px;
  border-radius: 999px;
  background: var(--kb-yellow-deep);
  border: 2px solid var(--text-strong);
  box-sizing: border-box;
}
.bar__mine-tag {
  margin-top: 2px;
  font-size: 10px;
  font-weight: 700;
  color: var(--text-strong);
  white-space: nowrap;
}
.bar__scale {
  display: flex;
  justify-content: space-between;
  margin: 8px 0 0;
  font-size: 10px;
  color: var(--text-hint);
}
.mkt-cap {
  margin-top: 10px;
  font-size: 11px;
  color: var(--text-muted);
  line-height: 1.5;
}

/* ── (B) 비교 테이블 ───────────────────────────────────── */
.mkt-table {
  margin-bottom: 20px;
}
.mkt-table__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.mkt-table__title {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-strong);
}
.mkt-table__badge {
  font-size: 10px;
  color: var(--text-muted);
  background: var(--kb-gray-pale);
  border-radius: 999px;
  padding: 2px 8px;
}
.cmp {
  display: grid;
  grid-template-columns: 1.2fr 1fr 1fr 24px;
  align-items: center;
  padding: 8px 0;
  border-top: 1px solid var(--line);
  font-size: 12px;
}
.cmp--head {
  border-top: 0;
  color: var(--text-hint);
  font-size: 11px;
}
.cmp__label {
  color: var(--text-muted);
}
.cmp__mine {
  font-weight: 700;
  color: var(--kb-yellow-deep);
}
.cmp__avg {
  color: var(--text-muted);
}
.cmp__flag {
  text-align: right;
  font-size: 12px;
}
.cmp-sum {
  margin-top: 10px;
  padding: 8px 12px;
  border-radius: 999px;
  background: var(--kb-gray-pale);
  color: var(--text-strong);
  font-size: 12px;
  font-weight: 700;
  text-align: center;
}

/* ── (C) 가성비 판정 박스 ──────────────────────────────── */
.verdict {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  padding: 14px;
  border-radius: 12px;
}
.verdict--good {
  background: var(--military-green-light);
}
.verdict--normal {
  background: #eaf3fb; /* 파랑 계열 연한 배경(목업 톤) */
}
.verdict--bad {
  background: #ffecec;
}
.verdict__icon {
  font-size: 22px;
  line-height: 1.2;
}
.verdict__title {
  font-size: 14px;
  font-weight: 800;
  color: var(--text-strong);
  margin-bottom: 3px;
}
.verdict__text {
  font-size: 12px;
  line-height: 1.6;
  color: var(--text-body);
}
</style>
