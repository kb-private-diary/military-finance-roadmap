<script setup>
// SCR-RENT-05 · Step 5) 저장된 로드맵 조회 (풍부 버전)  담당: 수연
// 목업 레이아웃: 헤더 + 매물 카드(step3 이식) + 3탭(나의 매물/비용 계산/금융상품)
// - 매물 카드: RentListingDetailPage 의 매물종류칩·시세/신선도 뱃지·카카오맵 Circle·스펙·동네시세 바텀시트를 이식
// - 나의 매물: 주변 교통/편의시설 그리드 + 후회소비 인사이트 카드(킬러)
// - 비용 계산: 공과금 정밀 결과 + 월 고정비 breakdown + 보증금 포함/제외 토글
// - 금융상품: 저장한 목표에 맞는 주거 금융상품
import { ref, computed, onMounted, watch, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import rentApi from '@/api/rentApi';
import { useRentStore } from '@/stores/rent';
import { formatWon, formatManwon, formatDate } from '@/util/format';
import { useToast } from '@/composables/useToast';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseTag from '@/components/common/BaseTag.vue';
import BaseBottomSheet from '@/components/common/BaseBottomSheet.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const route = useRoute();
const router = useRouter();
const rentStore = useRentStore();
const { show: showToast } = useToast();
const goalId = Number(route.params.goalId);

// 매물종류 한글 매핑(응답 estateType)
const ESTATE_TYPE = { OFFICETEL: '오피스텔', APARTMENT: '아파트', VILLA: '빌라', ROOM: '원룸' };

// 시설 타입 → 라벨/이모지 (SUBWAY/BUS=교통, CONVENIENCE/MART/HOSPITAL=편의)
const FACILITY_META = {
  SUBWAY: { label: '지하철', emoji: '🚇' },
  BUS: { label: '버스', emoji: '🚌' },
  CONVENIENCE: { label: '편의점', emoji: '🏪' },
  MART: { label: '마트', emoji: '🛒' },
  HOSPITAL: { label: '병원', emoji: '🏥' },
};

// 생활 인프라 충족도: 1인 가구 기준 4대 시설 종류(있음/없음만 판정)
const INFRA_TYPES = [
  { type: 'SUBWAY', emoji: '🚇', label: '역' },
  { type: 'CONVENIENCE', emoji: '🏪', label: '편의점' },
  { type: 'MART', emoji: '🛒', label: '마트' },
  { type: 'HOSPITAL', emoji: '🏥', label: '병원' },
];

// 시세 뱃지: priceLevel(CHEAP/AVERAGE/EXPENSIVE) → 배경 채운 pill
const PRICE_BADGE = {
  CHEAP: { label: '✅ 지역 평균보다 저렴', cls: 'pill--cheap' },
  AVERAGE: { label: '✔ 지역 평균 수준', cls: 'pill--average' },
  EXPENSIVE: { label: '⛔ 지역 평균보다 비쌈', cls: 'pill--expensive' },
};

// 통신비: findGoal 응답에 없어 상수로 둠(하드코딩 "데이터"가 아니라 명시 상수).
// 1인 가구 월 평균 통신비 추정치(과기정통부 가계통신비 통계 참고). 실데이터 연동 시 교체.
const MONTHLY_TELECOM_FEE = 55000;

const goal = ref(null);
const listing = ref(null);
const facilities = ref([]);
const sim = ref(null);
const productSections = ref([]);
const loading = ref(true);
const activeTab = ref('listing');

const TABS = [
  { key: 'listing', label: '나의 매물' },
  { key: 'cost', label: '비용 계산' },
  { key: 'products', label: '금융상품' },
];

// findProducts 응답 → 섹션 레이아웃 정규화. 실패/빈값이면 빈 배열.
//   백엔드는 monthlySubsidy/depositLoan/free 3그룹으로 응답 (RentProductsPage와 동일 구조)
const normalizeProducts = (data) => {
  const all = [
    ...(data?.monthlySubsidy || []),
    ...(data?.depositLoan || []),
    ...(data?.free || []),
  ];
  if (!all.length) return [];
  return [
    {
      caption: '관련 금융상품',
      items: all.map((p) => ({
        name: p.productName,
        org:
          p.productType === 'POLICY'
            ? '정책상품'
            : p.productType === 'LOCAL'
              ? '지자체'
              : 'KB국민',
        link: p.externalUrl || '#',
      })),
    },
  ];
};

// 두 응답 형태 모두 방어:
//  (A) 태스크 계약: { goal, listing, nearbyFacilities, precisionSimulation }
//  (B) 실제 DTO(flat): { goalId, residenceMonths, listing, nearbyFacilities, precisionSimulation }
const normalize = (d) => {
  if (!d) return null;
  const goalObj = d.goal || (d.goalId != null ? d : null);
  if (!goalObj) return null;
  return {
    goal: goalObj,
    listing: d.listing ?? goalObj.listing ?? null,
    nearbyFacilities: d.nearbyFacilities ?? [],
    precisionSimulation: d.precisionSimulation ?? null,
  };
};

const load = async () => {
  loading.value = true;
  try {
    const d = await rentApi.findGoal(goalId);
    const src = normalize(d);
    if (src) {
      goal.value = src.goal;
      listing.value = src.listing;
      facilities.value = src.nearbyFacilities || [];
      sim.value = src.precisionSimulation;
    } else {
      showToast('로드맵을 불러오지 못했어요', 'error');
    }
  } catch {
    showToast('로드맵을 불러오지 못했어요', 'error');
  }

  // 금융상품은 별도 시도 → 실패/없으면 빈 배열(빈 상태 문구)
  try {
    const lid = listing.value?.listingId || goal.value?.confirmedListingId;
    const months = goal.value?.residenceMonths || rentStore.months || 6;
    const p = await rentApi.findProducts(lid, months);
    productSections.value = normalizeProducts(p);
  } catch {
    productSections.value = [];
  } finally {
    loading.value = false;
  }
};
onMounted(load);

// ── 파생값 ──────────────────────────────────────────────
const residenceMonths = computed(() => goal.value?.residenceMonths || rentStore.months || 6);
const listingId = computed(() => listing.value?.listingId || goal.value?.confirmedListingId || null);
const estateTypeLabel = computed(() => ESTATE_TYPE[listing.value?.estateType] || '오피스텔');
// 평수 = ㎡ / 3.3058 (소수 1자리). 면적 없으면 null.
const pyeong = computed(() =>
  listing.value?.areaSqm ? (listing.value.areaSqm / 3.3058).toFixed(1) : null,
);
const listingMeta = computed(() => {
  const l = listing.value;
  if (!l) return '';
  const parts = [];
  if (l.dongName) parts.push(l.dongName);
  if (pyeong.value) parts.push(`${pyeong.value}평`);
  if (l.floor != null) parts.push(`${l.floor}층`);
  return parts.join(' · ');
});
const priceBadge = computed(() => PRICE_BADGE[listing.value?.priceLevel] || null);

// 신선도: 확정 매물의 실거래일(dealDate)로 "N개월 전 실거래" 파생 (하드코딩 아님)
const freshLabel = computed(() => {
  const raw = listing.value?.dealDate;
  if (!raw) return '';
  const deal = new Date(raw);
  if (Number.isNaN(deal.getTime())) return '';
  const diffMonths = Math.round((Date.now() - deal.getTime()) / (1000 * 60 * 60 * 24 * 30));
  if (diffMonths <= 0) return '이번 달 실거래';
  return `${diffMonths}개월 전 실거래`;
});

// ── 좌표/카카오맵 (step3 이식) ─────────────────────────────
const hasCoords = computed(
  () => listing.value?.latitude != null && listing.value?.longitude != null,
);
const coordText = computed(() =>
  hasCoords.value
    ? `위도 ${Number(listing.value.latitude).toFixed(4)}, 경도 ${Number(listing.value.longitude).toFixed(4)}`
    : '',
);
const KAKAO_KEY = import.meta.env.VITE_KAKAO_MAP_KEY; // 하드코딩 금지 (.env)
const mapEl = ref(null);
// 지오코딩용 동 주소: load-nationwide 매물은 좌표가 NULL이라 dongName(예 "부산 남구 대연동")으로 대략 위치를 찾는다.
// "부산 남구 대연동~~~"처럼 뒤에 물결·부가문자가 붙는 경우가 있어 앞 3토큰(시 구 동)만 쓰고 비주소 문자는 제거해 검색 정확도를 높인다.
const geocodeQuery = computed(() => {
  const raw = listing.value?.dongName;
  if (!raw) return '';
  return raw
    .trim()
    .split(/\s+/)
    .slice(0, 3)
    .join(' ')
    .replace(/[^가-힣0-9\s]/g, '') // 물결·특수문자 제거
    .trim();
});
const showMap = computed(() => !!KAKAO_KEY && (hasCoords.value || !!geocodeQuery.value));
let mapReady = false;

// libraries=services: Geocoder(동 주소 → 좌표 변환)를 쓰려면 필수
const loadKakaoSdk = () =>
  new Promise((resolve, reject) => {
    if (window.kakao?.maps) return resolve();
    const existing = document.getElementById('kakao-map-sdk');
    if (existing) {
      // 이미 삽입된 스크립트: load 이벤트가 이미 지났을 수 있어(재방문) 폴링으로 로드 완료를 감지
      const t0 = Date.now();
      const timer = setInterval(() => {
        if (window.kakao?.maps) {
          clearInterval(timer);
          resolve();
        } else if (Date.now() - t0 > 5000) {
          clearInterval(timer);
          reject(new Error('kakao sdk load timeout'));
        }
      }, 50);
      return;
    }
    const script = document.createElement('script');
    script.id = 'kakao-map-sdk';
    script.src = `//dapi.kakao.com/v2/maps/sdk.js?appkey=${KAKAO_KEY}&autoload=false&libraries=services`;
    script.onload = () => resolve();
    script.onerror = reject;
    document.head.appendChild(script);
  });

// 대략 위치만: 마커 대신 반경 150m 원(Circle). 자취 테마라 핑크(목업 지정색).
const drawCircle = (pos) => {
  const { kakao } = window;
  const map = new kakao.maps.Map(mapEl.value, { center: pos, level: 4 });
  const circle = new kakao.maps.Circle({
    center: pos,
    radius: 150,
    strokeWeight: 2,
    strokeColor: '#e85d84', // rent-pink (목업 지정색)
    strokeOpacity: 1,
    strokeStyle: 'dashed',
    fillColor: '#e85d84',
    fillOpacity: 0.18,
  });
  circle.setMap(map);
  // 컨테이너 크기가 늦게 확정되는 경우(탭/렌더 타이밍) 대비해 재배치 - 회색 빈 지도 방지
  setTimeout(() => {
    map.relayout();
    map.setCenter(pos);
  }, 150);
};

// 지도 초기화: 좌표 있으면 그 좌표로, 없으면 동 주소를 지오코딩해 대략 위치로 Circle 표시.
const initMap = (retry = 0) => {
  if (!mapEl.value) {
    // 지도 컨테이너가 아직 렌더 전이면 다음 프레임에 재시도 (최대 10회, 렌더 타이밍 경쟁 방어)
    if (retry < 10) requestAnimationFrame(() => initMap(retry + 1));
    return;
  }
  const { kakao } = window;
  // (1) 좌표 있으면 기존 경로 그대로
  if (hasCoords.value) {
    drawCircle(
      new kakao.maps.LatLng(
        Number(listing.value.latitude),
        Number(listing.value.longitude),
      ),
    );
    return;
  }
  // (2) 좌표 없고 동 주소 있으면 지오코딩 → 대략 위치 Circle. 실패 시 조용히 폴백(컨테이너만 빈 상태)
  if (geocodeQuery.value && kakao.maps.services) {
    const geocoder = new kakao.maps.services.Geocoder();
    geocoder.addressSearch(geocodeQuery.value, (result, status) => {
      if (status === kakao.maps.services.Status.OK && result[0]) {
        drawCircle(new kakao.maps.LatLng(Number(result[0].y), Number(result[0].x)));
      }
    });
  }
};

const setupMap = async () => {
  if (mapReady || !showMap.value) return;
  mapReady = true;
  try {
    await loadKakaoSdk();
    window.kakao.maps.load(initMap);
  } catch {
    mapReady = false;
  }
};
watch(
  showMap,
  async (ok) => {
    if (!ok) return;
    await nextTick();
    setupMap();
  },
  { immediate: true },
);

// ── 나의 매물 탭: 시설 분리 ─────────────────────────────
const transitFacilities = computed(() =>
  facilities.value.filter((f) => f.type === 'SUBWAY' || f.type === 'BUS'),
);
const convenienceFacilities = computed(() =>
  facilities.value.filter((f) => ['CONVENIENCE', 'MART', 'HOSPITAL'].includes(f.type)),
);
const facilityMeta = (type) => FACILITY_META[type] || { label: '기타', emoji: '📍' };
// 거리 = 도보 분 × 80m (평균 보행속도 환산)
const facilityDistance = (f) =>
  `도보 ${f.walkMinutes}분 · ${(f.walkMinutes * 80).toLocaleString('ko-KR')}m`;

// ── 생활 인프라 충족도 (계산만, 하드코딩 없음) ─────────────
// 4대 시설 종류별 존재 여부 → 충족 개수(0~4). facilities 비면 카드 자체를 숨김.
const infraStatus = computed(() =>
  INFRA_TYPES.map((t) => ({
    ...t,
    has: facilities.value.some((f) => f?.type === t.type),
  })),
);
const infraCount = computed(() => infraStatus.value.filter((i) => i.has).length);

// ── 이 집의 강점 요약 (해당하는 태그만 노출) ──────────────
const strengthTags = computed(() => {
  const l = listing.value;
  if (!l) return [];
  const tags = [];
  // 시세 저렴
  if (l.priceLevel === 'CHEAP') tags.push('✅ 시세 저렴');
  // 역세권: SUBWAY 도보 10분 이내
  const subway = facilities.value.find((f) => f?.type === 'SUBWAY');
  if (subway && subway.walkMinutes != null && subway.walkMinutes <= 10) {
    tags.push('✅ 역세권');
  }
  // 편의시설 인접: 편의점 또는 마트 존재
  if (facilities.value.some((f) => f?.type === 'CONVENIENCE' || f?.type === 'MART')) {
    tags.push('✅ 편의시설 인접');
  }
  // 최근 실거래: dealDate 6개월 이내
  if (l.dealDate) {
    const deal = new Date(l.dealDate);
    if (!Number.isNaN(deal.getTime())) {
      const monthsAgo = (Date.now() - deal.getTime()) / (1000 * 60 * 60 * 24 * 30);
      if (monthsAgo >= 0 && monthsAgo <= 6) tags.push('✅ 최근 실거래');
    }
  }
  // 신축급: buildYear 10년 이내
  if (l.buildYear) {
    const age = new Date().getFullYear() - Number(l.buildYear);
    if (age >= 0 && age <= 10) tags.push('✅ 신축급');
  }
  return tags;
});

// ── 후회소비 인사이트 (킬러) ─────────────────────────────
const showRegretInsight = computed(
  () => !!sim.value && (sim.value.userSpending?.avgRegretSpending ?? 0) > 0,
);
const round1 = (n) => (Number(n) || 0).toFixed(1); // 개월수 소수1자리(반올림)

// ── 비용 계산 탭 ─────────────────────────────────────────
const utilityFee = computed(() => sim.value?.monthlyHousingCost?.utilityFee ?? 0);

// 월 고정비 breakdown 항목
const fixedCostItems = computed(() => {
  const l = listing.value;
  if (!l) return [];
  return [
    { label: '월세', amount: l.monthlyRent ?? 0 },
    { label: '관리비', amount: l.maintenanceFee ?? 0, sub: '평당 추정' },
    { label: '전기·가스·수도', amount: utilityFee.value, sub: '월별 계수 반영' },
    { label: '통신비', amount: MONTHLY_TELECOM_FEE, sub: '1인 가구 평균(상수)' },
  ];
});
const fixedCostTotal = computed(() =>
  fixedCostItems.value.reduce((sum, i) => sum + i.amount, 0),
);

// 보증금 포함/제외 토글 — DB 저장 안 함, 세션 store(depositMode) 있으면 그 값, 없으면 OFF
const includeDeposit = ref(rentStore.depositMode ? rentStore.depositMode === 'INCLUDE' : false);

// ── 부동산 중개비(중개보수) 계산 ──────────────────────────
// 주택 임대차 법정 중개보수 "상한". 하드코딩 금액이 아니라 요율표 상수 → 계산으로 도출.
// 출처: 공인중개사법 시행규칙 별표(주택 임대차 상한 요율·한도). 실데이터 연동 아님.
// 거래금액을 구간별 요율에 곱하고, 한도 있는 구간은 min 처리.
const BROKERAGE_BRACKETS = [
  { max: 50_000_000, rate: 0.005, cap: 200_000 }, // 5천만원 미만: 0.5% (한도 20만원)
  { max: 100_000_000, rate: 0.004, cap: 300_000 }, // 5천만~1억 미만: 0.4% (한도 30만원)
  { max: 300_000_000, rate: 0.003, cap: null }, // 1억~3억 미만: 0.3% (한도 없음)
  { max: 600_000_000, rate: 0.004, cap: null }, // 3억~6억 미만: 0.4%
  { max: Infinity, rate: 0.006, cap: null }, // 6억 이상: 0.6%
];
// 월세 환산 배수: 기본 ×100, 거래금액이 5천만원 미만이면 ×70으로 재계산(법 규정)
const RENT_MULTIPLIER_DEFAULT = 100;
const RENT_MULTIPLIER_LOW = 70;
const LOW_DEAL_THRESHOLD = 50_000_000;

// 거래금액(보증금 + 월세환산) → 구간 요율/한도로 중개보수 산출. deposit/monthlyRent null → 0.
const brokerageFee = computed(() => {
  const deposit = listing.value?.deposit ?? 0;
  const monthlyRent = listing.value?.monthlyRent ?? 0;
  // 1) 거래금액 = 보증금 + 월세 × 100
  let dealAmount = deposit + monthlyRent * RENT_MULTIPLIER_DEFAULT;
  // 2) 거래금액이 5천만원 미만이면 월세 환산을 ×70 으로 다시 계산(법 규정)
  if (dealAmount < LOW_DEAL_THRESHOLD) {
    dealAmount = deposit + monthlyRent * RENT_MULTIPLIER_LOW;
  }
  // 3) 거래금액이 속한 구간의 요율/한도 선택 (max 는 상한 미만 기준)
  const bracket =
    BROKERAGE_BRACKETS.find((b) => dealAmount < b.max) ??
    BROKERAGE_BRACKETS[BROKERAGE_BRACKETS.length - 1];
  // 4) 중개비 = 거래금액 × 요율, 한도 있는 구간만 min 적용, 원 단위 반올림
  const raw = dealAmount * bracket.rate;
  const fee = bracket.cap != null ? Math.min(raw, bracket.cap) : raw;
  return Math.round(fee);
});

// ── 동네 시세 상세보기 바텀시트 (step3 이식) ───────────────
const marketSheetOpen = ref(false);
const market = ref(null);
const marketLoading = ref(false);
const marketError = ref(false);
let marketLoaded = false;

const openMarketSheet = async () => {
  marketSheetOpen.value = true;
  if (marketLoaded || !listingId.value) return;
  marketLoading.value = true;
  marketError.value = false;
  try {
    market.value = await rentApi.findMarketComparison(listingId.value);
    marketLoaded = true;
  } catch {
    marketError.value = true;
    showToast('시세 정보를 불러오지 못했어요', 'error');
  } finally {
    marketLoading.value = false;
  }
};

const toMan = (v) => `${Math.round((v ?? 0) / 10000).toLocaleString('ko-KR')}만`;
const fmtCompare = (row, value) => {
  const t = `${row?.key ?? ''} ${row?.label ?? ''}`;
  if (t.includes('면적')) return `${value}㎡`;
  if (t.includes('건축') || t.includes('년도')) return `${value}년`;
  if (t.includes('층')) return `${value}층`;
  return toMan(value);
};
const mineRent = computed(() => {
  const rows = market.value?.rows ?? [];
  const row = rows.find((r) => {
    const t = `${r.key ?? ''} ${r.label ?? ''}`;
    return t.includes('월세') && !t.includes('㎡');
  });
  return row?.mine ?? listing.value?.monthlyRent ?? 0;
});
const pctOf = (v) => {
  const min = market.value?.rentMin ?? 0;
  const max = market.value?.rentMax ?? 0;
  if (max <= min) return 50;
  return Math.min(100, Math.max(0, ((v - min) / (max - min)) * 100));
};
const minePct = computed(() => pctOf(mineRent.value));
const avgPct = computed(() => pctOf(market.value?.rentAvg ?? 0));
const rentDiff = computed(() => mineRent.value - (market.value?.rentAvg ?? 0));
const rentDiffLabel = computed(() => {
  const man = rentDiff.value / 10000;
  const sign = man > 0 ? '+' : '';
  return `${sign}${man.toFixed(1)}만`;
});
const rentDiffTone = computed(() => {
  if (rentDiff.value < 0) return 'good';
  if (rentDiff.value > 0) return 'bad';
  return 'neutral';
});
const VERDICT = {
  GOOD: { tone: 'good', icon: '👍' },
  NORMAL: { tone: 'normal', icon: '🙂' },
  BAD: { tone: 'bad', icon: '😥' },
};
const verdictMeta = computed(() => VERDICT[market.value?.verdict] || VERDICT.NORMAL);

// ── 액션 ────────────────────────────────────────────────
const handleDelete = async () => {
  if (!window.confirm('저장한 자취 목표를 삭제할까요?')) return;
  try {
    if (typeof rentApi.deleteGoal === 'function') await rentApi.deleteGoal(goalId);
  } catch {
    // 백엔드 미구현 시 삭제 실패 무시하고 목록으로 이동
  } finally {
    router.push({ name: 'RoadmapMain' });
  }
};
const goConfirm = () => router.push({ name: 'RoadmapMain' });
</script>

<template>
  <div v-if="!loading && goal" class="detail">
    <!-- 1) 헤더 -->
    <header class="head">
      <h2 class="head-title">내가 저장한 로드맵</h2>
      <BaseTag label="자취" variant="pink" />
    </header>
    <p class="head-sub">목표 거주 {{ residenceMonths }}개월</p>

    <!-- 2) 매물 카드 (상단 핑크 4px 바) -->
    <BaseCard v-if="listing" class="listing-card" padding="16px 16px 18px">
      <div class="lc-title">
        <span class="type-chip">{{ estateTypeLabel }}</span>
        <span class="lc-name">{{ listing.buildingName }}</span>
      </div>
      <p v-if="listingMeta" class="lc-meta">{{ listingMeta }}</p>

      <!-- 시세 뱃지 + 신선도 뱃지 -->
      <div v-if="priceBadge || freshLabel" class="pbadges">
        <span v-if="priceBadge" class="pill" :class="priceBadge.cls">{{ priceBadge.label }}</span>
        <span v-if="freshLabel" class="pill pill--fresh">⭐ {{ freshLabel }}</span>
      </div>

      <!-- 카카오맵 대략 위치 원(Circle) + 잠금 캡션 -->
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

      <!-- 스펙 2열 grid -->
      <div class="spec-grid">
        <div class="spec"><span class="spec__l">계약일</span><span class="spec__v">{{ listing.dealDate ? formatDate(listing.dealDate) : '-' }}</span></div>
        <div class="spec"><span class="spec__l">면적</span><span class="spec__v">{{ listing.areaSqm ? `${listing.areaSqm}㎡ (${pyeong}평)` : '-' }}</span></div>
        <div class="spec"><span class="spec__l">건축년도</span><span class="spec__v">{{ listing.buildYear ? `${listing.buildYear}년` : '-' }}</span></div>
        <div class="spec"><span class="spec__l">층수</span><span class="spec__v">{{ listing.floor != null ? `${listing.floor}층` : '-' }}</span></div>
      </div>

      <button v-if="listingId" type="button" class="market-btn" @click="openMarketSheet">
        동네 시세 상세보기
      </button>
    </BaseCard>
    <BaseCard v-else class="listing-card" padding="20px">
      <p class="empty">확정된 매물 정보가 없어요.</p>
    </BaseCard>

    <!-- 3) 탭 -->
    <div class="tabs" role="tablist">
      <button
        v-for="t in TABS"
        :key="t.key"
        type="button"
        class="tab"
        :class="{ active: activeTab === t.key }"
        role="tab"
        :aria-selected="activeTab === t.key"
        @click="activeTab = t.key"
      >
        {{ t.label }}
      </button>
    </div>

    <!-- 탭 1: 나의 매물 -->
    <section v-show="activeTab === 'listing'" class="pane">
      <!-- 생활 인프라 충족도 (데이터 없으면 숨김) -->
      <BaseCard v-if="facilities.length" padding="14px">
        <div class="infra-head">
          <p class="cap cap--m0">1인 가구 생활 인프라</p>
          <span class="infra-count">{{ infraCount }}/4 충족</span>
        </div>
        <div class="infra-gauge">
          <span class="infra-gauge__fill" :style="{ width: (infraCount / 4) * 100 + '%' }"></span>
        </div>
        <div class="infra-chips">
          <span
            v-for="it in infraStatus"
            :key="it.type"
            class="infra-chip"
            :class="{ 'infra-chip--on': it.has }"
          >
            <span v-if="it.has" class="infra-chip__check">✓</span>
            <span class="infra-chip__icon">{{ it.emoji }}</span>
            <span class="infra-chip__label">{{ it.label }}</span>
          </span>
        </div>
      </BaseCard>

      <!-- 이 집의 강점 (해당 태그 없으면 숨김) -->
      <BaseCard v-if="strengthTags.length" padding="14px">
        <p class="cap">이 집의 강점</p>
        <div class="strength-tags">
          <span v-for="(t, i) in strengthTags" :key="i" class="pill strength-pill">{{ t }}</span>
        </div>
      </BaseCard>

      <!-- 주변 교통시설 -->
      <BaseCard padding="14px">
        <p class="cap">주변 교통시설</p>
        <div v-if="transitFacilities.length" class="fac-grid">
          <div v-for="(f, i) in transitFacilities" :key="`t${i}`" class="fac">
            <span class="fac__icon">{{ facilityMeta(f.type).emoji }}</span>
            <span class="fac__body">
              <span class="fac__name">{{ f.name }}</span>
              <span class="fac__dist">{{ facilityDistance(f) }}</span>
            </span>
          </div>
        </div>
        <p v-else class="empty">주변 교통시설 정보가 없어요.</p>
      </BaseCard>

      <!-- 주변 편의시설 -->
      <BaseCard padding="14px">
        <p class="cap">주변 편의시설</p>
        <div v-if="convenienceFacilities.length" class="fac-grid">
          <div v-for="(f, i) in convenienceFacilities" :key="`c${i}`" class="fac">
            <span class="fac__icon">{{ facilityMeta(f.type).emoji }}</span>
            <span class="fac__body">
              <span class="fac__name">{{ f.name }}</span>
              <span class="fac__dist">{{ facilityMeta(f.type).label }} · {{ facilityDistance(f) }}</span>
            </span>
          </div>
        </div>
        <p v-else class="empty">주변 편의시설 정보가 없어요.</p>
      </BaseCard>

      <!-- 후회소비 인사이트 (킬러) -->
      <div v-if="showRegretInsight" class="insight">
        <p class="insight__tag">⭐ 후회소비 인사이트</p>
        <p class="insight__text">
          지금 후회소비가 월 <strong>{{ formatManwon(sim.userSpending.avgRegretSpending) }}</strong>이에요.
          이걸 줄이면 자취 가능 기간이
          <strong class="insight__hl">{{ round1(sim.possibleMonths) }}개월 → {{ round1(sim.reducedPossibleMonths) }}개월</strong>로 늘어나요.
        </p>
      </div>
    </section>

    <!-- 탭 2: 비용 계산 -->
    <section v-show="activeTab === 'cost'" class="pane">
      <template v-if="sim">
        <!-- 공과금 정밀 계산 (세부 계수 API 미연동 → 결과만 크게) -->
        <BaseCard padding="18px 16px">
          <p class="cap">공과금 정밀 계산</p>
          <div class="util-result">
            <span class="util-result__label">월 예상 공과금</span>
            <span class="util-result__amt">{{ formatWon(utilityFee) }}</span>
          </div>
          <p class="util-formula">면적기준값 × 시도계수 × 월별계수로 산출</p>
          <p class="util-src">에너지경제연구원 HEPS + 한전 요금표 기반 · 1인 가구 보정값</p>
        </BaseCard>

        <!-- 월 고정비 breakdown -->
        <BaseCard padding="16px">
          <div class="cost-head">
            <p class="cap cap--m0">월 고정비</p>
            <label class="switch">
              <input type="checkbox" v-model="includeDeposit" />
              <span class="switch__track"><span class="switch__thumb"></span></span>
              <span class="switch__label">보증금 포함</span>
            </label>
          </div>

          <ul class="cost-list">
            <li v-for="item in fixedCostItems" :key="item.label">
              <span class="cost-name">
                {{ item.label }}
                <span v-if="item.sub" class="cost-sub">{{ item.sub }}</span>
              </span>
              <span class="cost-amt">{{ formatWon(item.amount) }}</span>
            </li>
          </ul>

          <div class="divider" />
          <div class="cost-total">
            <span>월 고정비</span>
            <strong>{{ formatWon(fixedCostTotal) }}</strong>
          </div>

          <!-- 보증금: 반환금이라 월 고정비와 분리해 "계약 시 목돈"으로 표기 -->
          <div v-if="includeDeposit && listing" class="deposit-box">
            <span class="deposit-box__l">보증금 <span class="cost-sub">계약 시 목돈 (반환금)</span></span>
            <strong class="deposit-box__v">{{ formatWon(listing.deposit ?? 0) }}</strong>
          </div>
        </BaseCard>

        <!-- 계약 시 초기 비용: 중개비는 1회성이라 월 고정비 "합계"와 분리해 별도 카드로 항상 표기 -->
        <BaseCard v-if="listing" padding="16px">
          <p class="cap cap--m0">계약 시 초기 비용</p>
          <div class="deposit-box brokerage-box">
            <span class="deposit-box__l">중개비 (예상) <span class="cost-sub">법정 상한 요율 기준</span></span>
            <strong class="deposit-box__v">{{ formatManwon(brokerageFee) }}</strong>
          </div>
          <p class="brokerage-note">월 고정비와 별개로 계약 시 1회 발생하는 비용이에요.</p>
        </BaseCard>

        <p class="note">계산 결과는 예상 금액이며 실제와 다를 수 있습니다.</p>
      </template>
      <BaseCard v-else padding="20px">
        <p class="empty">비용 계산 정보가 아직 없어요.</p>
      </BaseCard>
    </section>

    <!-- 탭 3: 금융상품 -->
    <section v-show="activeTab === 'products'" class="pane">
      <BaseCard v-for="sec in productSections" :key="sec.caption" padding="14px">
        <p class="cap">{{ sec.caption }}</p>
        <a
          v-for="(p, i) in sec.items"
          :key="i"
          class="product"
          :href="p.link"
          target="_blank"
          rel="noopener noreferrer"
        >
          <span class="p-info">
            <span class="p-name">{{ p.name }}</span>
            <span class="p-org">{{ p.org }}</span>
          </span>
          <span class="p-link" aria-hidden="true">↗</span>
        </a>
      </BaseCard>
      <BaseCard v-if="!productSections.length" padding="20px">
        <p class="empty">추천 가능한 금융상품이 없어요.</p>
      </BaseCard>
      <p class="veteran-note">군 복무 기간만큼 청년 지원 나이 요건이 연장돼요</p>
    </section>

    <BottomButtonBar
      primary-label="확인"
      @primary-click="goConfirm"
    />

    <!-- 동네 시세 상세보기 바텀시트 -->
    <BaseBottomSheet
      v-model="marketSheetOpen"
      title="동네 시세 상세보기"
      confirm-text="닫기"
    >
      <p v-if="marketLoading" class="mkt-state">불러오는 중...</p>
      <p v-else-if="marketError" class="mkt-state">시세 정보를 불러오지 못했어요</p>
      <p v-else-if="market && !market.enough" class="mkt-empty">
        이 동네는 비교할 매물 데이터가 부족해요 🥲
      </p>

      <template v-else-if="market">
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
  <p v-else-if="loading" class="loading">불러오는 중...</p>
  <p v-else class="loading">로드맵을 불러오지 못했어요</p>
</template>

<style scoped>
.detail {
  padding: 20px 0 88px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* 1) 헤더 */
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.head-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-strong);
}
.head-sub {
  margin-top: -8px;
  font-size: 12px;
  color: var(--text-muted);
}

/* 2) 매물 카드 (상단 핑크 바) */
.listing-card {
  border-top: 4px solid var(--rent-pink);
  overflow: hidden;
}
.lc-title {
  display: flex;
  align-items: center;
  gap: 8px;
}
.type-chip {
  flex: none;
  padding: 2px 8px;
  border-radius: 5px;
  background: #9d9d9d;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
}
.lc-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-strong);
}
.lc-meta {
  margin-top: 6px;
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

/* 뱃지 pill */
.pbadges {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin: 10px 0;
}
.pill {
  font-size: 11px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 999px;
  color: #000;
}
.pill--cheap {
  background: #e1f3e0;
}
.pill--average {
  background: #d3e6ff;
}
.pill--expensive {
  background: #f4d1d1;
}
.pill--fresh {
  background: #fff4cc;
}

/* 스펙 2열 grid */
.spec-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px 12px;
  margin-top: 12px;
}
.spec {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.spec__l {
  font-size: 11px;
  color: var(--text-muted);
}
.spec__v {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-strong);
}

/* 동네 시세 상세보기 버튼 */
.market-btn {
  width: 100%;
  margin-top: 14px;
  padding: 11px 0;
  border: 0;
  border-radius: 8px;
  background: #c9bca8;
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  font-family: inherit;
  cursor: pointer;
}

/* 3) pill 탭 */
.tabs {
  display: flex;
  gap: 6px;
  padding: 4px;
  border-radius: 999px;
  background: var(--gray-pale-bg);
}
.tab {
  flex: 1;
  height: 34px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: var(--text-muted);
  font-family: inherit;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}
.tab.active {
  background: #ffffff;
  color: var(--text-strong);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

/* 탭 콘텐츠 공통 */
.pane {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.cap {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-body);
  margin-bottom: 10px;
}
.cap--m0 {
  margin-bottom: 0;
}
.empty {
  padding: 8px 0;
  font-size: 12px;
  color: var(--text-hint);
  text-align: center;
}

/* 시설 2열 그리드 */
.fac-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}
.fac {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px;
  border: 1px solid var(--line);
  border-radius: 10px;
  min-width: 0;
}
.fac__icon {
  flex: none;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--surface-subtle);
  font-size: 15px;
}
.fac__body {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.fac__name {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-strong);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.fac__dist {
  font-size: 10px;
  color: var(--text-muted);
}

/* 생활 인프라 충족도 */
.infra-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 10px;
}
.infra-count {
  font-size: 13px;
  font-weight: 800;
  color: #f0a500; /* 강조 노랑(기존 cost-total 톤 재사용) */
}
.infra-gauge {
  height: 8px;
  border-radius: 999px;
  background: var(--kb-gray-pale);
  overflow: hidden;
  margin-bottom: 12px;
}
.infra-gauge__fill {
  display: block;
  height: 100%;
  border-radius: 999px;
  background: #f0a500;
  transition: width 0.3s;
}
.infra-chips {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}
.infra-chip {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  padding: 10px 4px;
  border-radius: 10px;
  background: var(--kb-gray-pale);
  color: var(--text-hint);
}
.infra-chip__icon {
  font-size: 18px;
  filter: grayscale(1);
  opacity: 0.45;
}
.infra-chip__label {
  font-size: 11px;
  font-weight: 600;
}
.infra-chip--on {
  background: #fff;
  border: 1px solid var(--kb-yellow);
  color: var(--text-body);
}
.infra-chip--on .infra-chip__icon {
  filter: none;
  opacity: 1;
}
.infra-chip__check {
  position: absolute;
  top: 4px;
  right: 6px;
  font-size: 10px;
  font-weight: 800;
  color: #2e9e5b;
}

/* 이 집의 강점 pill */
.strength-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.strength-pill {
  background: #f5f6f8;
  color: var(--text-body);
}

/* 후회소비 인사이트 카드 */
.insight {
  padding: 16px;
  border-radius: 14px;
  background: #fff9e0; /* 연노랑 강조(목업 지정색) */
  border: 1px solid #ffe9a8;
}
.insight__tag {
  font-size: 11px;
  font-weight: 700;
  color: #a9762a;
  margin-bottom: 6px;
}
.insight__text {
  font-size: 13px;
  line-height: 1.65;
  color: var(--text-body);
}
.insight__text strong {
  color: var(--text-strong);
}
.insight__hl {
  color: #2e9e5b; /* 연장 효과 강조(초록) */
}

/* 공과금 정밀 결과 */
.util-result {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
}
.util-result__label {
  font-size: 12px;
  color: var(--text-muted);
}
.util-result__amt {
  font-size: 24px;
  font-weight: 800;
  color: var(--text-strong);
}
.util-formula {
  margin-top: 8px;
  font-size: 11px;
  color: var(--text-body);
}
.util-src {
  margin-top: 6px;
  font-size: 10px;
  color: var(--text-hint);
  line-height: 1.5;
}

/* 월 고정비 breakdown */
.cost-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.cost-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.cost-list li {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-size: 13px;
}
.cost-name {
  color: var(--text-body);
}
.cost-sub {
  margin-left: 6px;
  font-size: 10px;
  color: var(--text-hint);
}
.cost-amt {
  font-weight: 600;
  color: var(--text-body);
}
.divider {
  height: 1px;
  background: var(--line);
  margin: 12px 0;
}
.cost-total {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  font-size: 14px;
  font-weight: 700;
  color: var(--text-strong);
}
.cost-total strong {
  font-size: 18px;
  color: #f0a500; /* 주황 강조(목업 톤) */
}
.deposit-box {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-top: 12px;
  padding: 12px;
  border-radius: 10px;
  background: var(--surface-subtle);
}
.deposit-box__l {
  font-size: 12px;
  color: var(--text-body);
}
.deposit-box__v {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-strong);
}
/* 계약 시 초기 비용(중개비) — 보증금 박스와 동일 톤, 핑크 강조로 1회성 구분 */
.brokerage-box {
  margin-top: 0;
}
.brokerage-box .deposit-box__v {
  color: var(--rent-pink);
}
.brokerage-note {
  margin-top: 8px;
  font-size: 11px;
  color: var(--text-hint);
  line-height: 1.5;
}
.note {
  font-size: 10px;
  color: var(--text-hint);
  text-align: center;
}

/* 보증금 포함 토글 스위치 (step3 이식) */
.switch {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}
.switch input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
}
.switch__track {
  position: relative;
  width: 36px;
  height: 20px;
  border-radius: 999px;
  background: var(--kb-gray-pale);
  transition: background 0.15s;
}
.switch__thumb {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  transition: transform 0.15s;
}
.switch input:checked + .switch__track {
  background: var(--rent-pink);
}
.switch input:checked + .switch__track .switch__thumb {
  transform: translateX(16px);
}
.switch__label {
  font-size: 11px;
  color: var(--text-muted);
}

/* 금융상품 */
.product {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  border-top: 1px solid var(--line);
  text-decoration: none;
}
.product:first-of-type {
  border-top: 0;
  padding-top: 0;
}
.p-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.p-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-body);
}
.p-org {
  font-size: 11px;
  color: var(--text-hint);
}
.p-link {
  flex-shrink: 0;
  font-size: 14px;
  color: var(--text-muted);
}
.veteran-note {
  padding: 10px 12px;
  border-radius: 10px;
  background: var(--military-green-light);
  color: var(--military-green);
  font-size: 11px;
  text-align: center;
}

.loading {
  padding: 60px 0;
  text-align: center;
  font-size: 13px;
  color: var(--text-hint);
}

/* ── 동네 시세 바텀시트 (step3 이식) ───────────────────────── */
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
  background: var(--rent-pink);
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
  color: var(--rent-pink);
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
  background: #eaf3fb;
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
