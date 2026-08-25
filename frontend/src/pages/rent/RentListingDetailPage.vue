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
import PageHeader from '@/components/common/PageHeader.vue';
import RangeSlider from '@/components/common/RangeSlider.vue';
import Badge from '@/components/common/Badge.vue';
import checkGreenIcon from '@/assets/images/check-green.png';
import checkBlueIcon from '@/assets/images/check-blue.png';
import exclamationIcon from '@/assets/images/exclamation.png';
import realestateIcon from '@/assets/images/realestate.png';

const { show: showToast } = useToast();

const route = useRoute();
const router = useRouter();
const rentStore = useRentStore();
const listingId = Number(route.params.listingId);
const goalId = Number(route.query.goalId) || rentStore.currentGoalId;
// 만기금: 감당도 응답(백엔드 실제 계산값) 우선 → store → 0.
// (하드코딩 폴백을 쓰면 "만기금 720만원인데 남는 돈 776만원" 같은 앞뒤 안 맞는 문구가 나옴)
const maturity = computed(
  () => affordability.value?.maturityAmount ?? rentStore.maturityAmount ?? 0,
);

const data = ref(null);
const loading = ref(true);
const months = ref(rentStore.months || 6);

const load = async () => {
  loading.value = true;
  try {
    const res = await rentApi.findListingDetail(listingId, goalId, months.value);
    data.value = res?.listing ? res : null;
  } catch {
    data.value = null;
    showToast('매물 정보를 불러오지 못했어요', 'error');
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

// 매물종류 한글 매핑(응답 estateType). 값이 없으면 목업 기본(오피스텔).
const ESTATE_TYPE = { OFFICETEL: '오피스텔', APARTMENT: '아파트', VILLA: '빌라', ROOM: '원룸' };
const estateTypeLabel = computed(() => ESTATE_TYPE[listing.value?.estateType] || '오피스텔');
// 평수 = ㎡ / 3.3058 (소수 1자리)
const pyeong = computed(() => ((listing.value?.areaSqm ?? 0) / 3.3058).toFixed(1));

// 시세 뱃지: step2에서 넘어온 priceLevel(CHEAP/AVERAGE/EXPENSIVE)로 표시.
// step3 단건 응답은 지역평균을 못 줘서 시세가 없으므로 store 값에 의존.
// step2를 안 거치고 직접 진입하면 priceLevel이 없어 시세 뱃지는 생략(신선도만 표시).
// 목업: 배경 채운 pill(초록/파랑/빨강) + 검정 글자.
const PRICE_BADGE = {
  CHEAP: { icon: checkGreenIcon, tone: 'green', text: '지역 평균보다 저렴' },
  AVERAGE: { icon: checkBlueIcon, tone: 'blue', text: '지역 평균 수준' },
  EXPENSIVE: { icon: exclamationIcon, tone: 'red', text: '지역 평균보다 비쌈' },
};
// 시세뱃지: 백엔드 상세 응답 priceLevel 우선(항상 계산됨), 없으면 step2에서 넘긴 store 값
const priceBadge = computed(
  () => PRICE_BADGE[listing.value?.priceLevel || rentStore.selectedPriceLevel] || null,
);

// 신선도 뱃지: 백엔드 propertyBadges 원문("1개월 전 실거래" 등)을 그대로 노랑 pill(⭐)로 노출.
const freshBadges = computed(() => data.value?.propertyBadges || []);
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
// 지도 표시 조건: 앱키 있고 (좌표 있거나 동 주소 있으면) 시도. 둘 다 없으면 "위치 정보 없음" 폴백
const showMap = computed(() => !!KAKAO_KEY && (hasCoords.value || !!geocodeQuery.value));
let mapReady = false; // 중복 초기화 방지

// SDK 동적 로드(한 번만). window.kakao.maps 있으면 재사용, 없으면 script 주입
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

// 좌표 위치에 대략 위치 원(Circle) 그리기 (공통)
// 정확 위치 노출 방지: 마커 대신 반경 150m 원만 표시해 "대략 범위"만 보여준다.
const drawCircle = (pos) => {
  const { kakao } = window;
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

// ── 비율 막대그래프(N개월 거주 한다면?) ─────────────────────
// 분모(합계)는 항상 보증금+월세×개월+관리비×개월. 각 %는 항목/합계.
// 토글 OFF(보증금 제외)면 보증금 구간은 막대·리스트에서 숨기고 빈 트랙으로 남긴다.
const depositAmt = computed(() => listing.value?.deposit ?? 0);
const rentTotal = computed(() => (listing.value?.monthlyRent ?? 0) * months.value);
const maintTotal = computed(() => (listing.value?.maintenanceFee ?? 0) * months.value);
const ratioSum = computed(() => depositAmt.value + rentTotal.value + maintTotal.value);
const ratioPct = (v) => (ratioSum.value > 0 ? Math.round((v / ratioSum.value) * 100) : 0);
const depositPct = computed(() => ratioPct(depositAmt.value));
const rentPct = computed(() => ratioPct(rentTotal.value));
const maintPct = computed(() => ratioPct(maintTotal.value));

// 감당도 "보증금 포함/제외" 토글 — INCLUDE: 목돈으로 보증금까지 / EXCLUDE: 보증금은 전세대출로
// 목업의 우상단 스위치와 연동(ON=INCLUDE, OFF=EXCLUDE). watch 로 재정체크 자동 재계산.
// step3 보증금 포함/제외 선택을 store에 저장 → step4·step5 감당도 계산과 통일
const depositMode = ref(rentStore.depositMode || 'INCLUDE');
watch(depositMode, (v) => {
  rentStore.depositMode = v;
});
const includeDeposit = computed({
  get: () => depositMode.value === 'INCLUDE',
  set: (on) => (depositMode.value = on ? 'INCLUDE' : 'EXCLUDE'),
});
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
// 백엔드 affordability 응답(affordabilityLabel: "딱 맞아요" 등)을 우선, 없으면 로컬 판정으로 폴백
const affordText = computed(
  () => affordability.value?.affordabilityLabel ?? judge(requiredForMode.value, maturity.value),
);
// 재정 체크 박스: 배경색 + 아이콘 + 헤드라인(헤드라인만 볼드, "내 재정 체크"는 별도 라벨)
const AFFORD = {
  '딱 맞아요': { cls: 'afford--ok', icon: '👍', headline: '딱 맞아요' },
  '빠듯해요': { cls: 'afford--tight', icon: '⚠️', headline: '빠듯해요' },
  '예산 초과': { cls: 'afford--over', icon: '❌', headline: '예산 초과' },
};
const affordMeta = computed(() => AFFORD[affordText.value] || AFFORD['빠듯해요']);

// 재정 분석 문구: 만기금으로 몇 개월 가능 + 남는 돈/부족분
// (백엔드 surplus/shortfall 응답을 우선, 없으면 로컬 계산으로 폴백)
const analysisText = computed(() => {
  const a = affordability.value;
  const mat = formatManwon(maturity.value);
  if (affordText.value === '딱 맞아요') {
    const surplus = a?.surplus ?? Math.max(0, maturity.value - requiredForMode.value);
    return `만기금 ${mat}으로 ${months.value}개월 거주가 가능해요. 남는 돈 ${formatManwon(surplus)}은 비상금으로 두는 걸 추천합니다.`;
  }
  if (affordText.value === '빠듯해요') {
    return `만기금 ${mat}으로 ${months.value}개월 거주는 가능하지만 여유가 빠듯해요. 예비비를 넉넉히 준비하는 걸 추천해요.`;
  }
  const shortfall = a?.shortfall ?? Math.max(0, requiredForMode.value - maturity.value);
  return `${months.value}개월 거주하려면 ${formatManwon(shortfall)}이 부족해요. 거주기간을 줄이거나 전월세보증금대출을 확인해보세요.`;
});

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
    <PageHeader title="선택한 로드맵 비용은?" />

    <!-- 매물 카드: 상단 KB 노랑 가로 바 -->
    <BaseCard class="listing-card" padding="16px 16px 18px">
      <!-- (a) 매물종류 칩 + 매물명 -->
      <div class="lc-title">
        <span class="type-chip">{{ estateTypeLabel }}</span>
        <span class="lc-name">{{ listing.buildingName }}</span>
      </div>
      <!-- (b) 메타: 동 · 평수 · 층 -->
      <p class="lc-meta">{{ listing.dongName }} · {{ pyeong }}평 · {{ listing.floor }}층</p>

      <!-- (c) 시세 뱃지(step2 priceLevel, 있을 때만) + 신선도 뱃지 -->
      <div class="pbadges">
        <Badge
          v-if="priceBadge"
          :tone="priceBadge.tone"
          :icon="priceBadge.icon"
          :text="priceBadge.text"
        />
        <Badge
          v-for="(b, i) in freshBadges"
          :key="i"
          tone="yellow"
          :icon="realestateIcon"
          :text="b"
        />
      </div>

      <!-- (d) 카카오맵: 앱키(.env)+좌표 있으면 대략 위치 원(Circle), 아니면 폴백 -->
      <div v-if="showMap" class="map-wrap">
        <div ref="mapEl" class="map map--live"></div>
        <p class="lockbar">🔒 정확한 주소는 저장 후 보여드려요</p>
      </div>
      <div v-else class="map">
        <template v-if="hasCoords">
          <span class="map__label">매물 위치</span>
          <span class="map__coord">{{ coordText }}</span>
        </template>
        <span v-else class="map__label">위치 정보 없음</span>
      </div>

      <!-- (e) 스펙 2열 grid -->
      <div class="spec-grid">
        <div class="spec"><span class="spec__l">계약일</span><span class="spec__v">{{ listing.dealDate }}</span></div>
        <div class="spec"><span class="spec__l">면적</span><span class="spec__v">{{ listing.areaSqm }}㎡ ({{ pyeong }}평)</span></div>
        <div class="spec"><span class="spec__l">건축년도</span><span class="spec__v">{{ listing.buildYear }}년</span></div>
        <div class="spec"><span class="spec__l">층수</span><span class="spec__v">{{ listing.floor }}층</span></div>
      </div>

      <!-- (f) 동네 시세 상세보기 -->
      <button type="button" class="market-btn" @click="openMarketSheet">
        동네 시세 상세보기
      </button>
    </BaseCard>

    <!-- 기간별 예상 비용 -->
    <p class="sec-title">기간별 예상 비용</p>

    <section class="period">
      <RangeSlider
        v-model="months"
        :min="6"
        :max="24"
        :step="1"
        label="거주기간"
        unit="개월"
        :ticks="['6개월', '12개월', '24개월']"
      />
    </section>

    <!-- 내 재정 체크 -->
    <div class="afford" :class="affordMeta.cls">
      <p class="afford__label">내 재정 체크</p>
      <p class="afford__t">{{ affordMeta.icon }} {{ affordMeta.headline }}</p>
      <p class="afford__s">{{ analysisText }}</p>
    </div>

    <!-- N개월 거주 한다면? (비율 막대그래프) -->
    <BaseCard padding="14px 16px 16px">
      <div class="ratio__head">
        <p class="ratio__title">{{ months }}개월 거주 한다면?</p>
        <label class="switch">
          <input type="checkbox" v-model="includeDeposit" />
          <span class="switch__track"><span class="switch__thumb"></span></span>
          <span class="switch__label">보증금 포함</span>
        </label>
      </div>

      <div class="ratiobar">
        <div v-if="includeDeposit" class="ratiobar__seg seg--deposit" :style="{ width: depositPct + '%' }"></div>
        <div class="ratiobar__seg seg--rent" :style="{ width: rentPct + '%' }"></div>
        <div class="ratiobar__seg seg--maint" :style="{ width: maintPct + '%' }"></div>
      </div>

      <ul class="ratio-list">
        <li v-if="includeDeposit">
          <span class="dot dot--deposit"></span>
          <span class="ratio-name">보증금</span>
          <span class="ratio-amt">{{ formatManwon(depositAmt) }}</span>
          <span class="ratio-pct">{{ depositPct }}%</span>
        </li>
        <li>
          <span class="dot dot--rent"></span>
          <span class="ratio-name">월세</span>
          <span class="ratio-amt">{{ formatManwon(rentTotal) }}</span>
          <span class="ratio-pct">{{ rentPct }}%</span>
        </li>
        <li>
          <span class="dot dot--maint"></span>
          <span class="ratio-name">관리비</span>
          <span class="ratio-amt">{{ formatManwon(maintTotal) }}</span>
          <span class="ratio-pct">{{ maintPct }}%</span>
        </li>
      </ul>
    </BaseCard>

    <p class="promo">💡 해당 매물의 예상 공과금까지 계산해 더 정확한 자취 예산을 알려드려요</p>

    <BottomButtonBar
      secondary-label="이전"
      primary-label="다음"
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
  <p v-else-if="loading" class="loading">불러오는 중...</p>
  <p v-else class="loading">매물 정보를 불러오지 못했어요</p>
</template>

<style scoped>
.detail {
  padding: 20px 0 88px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
/* ── 매물 카드 ─────────────────────────────────────────── */
.listing-card {
  border-top: 4px solid var(--kb-yellow);
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
/* ── 뱃지 pill(배경 채움) ──────────────────────────────── */
.pbadges {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin: 10px 0;
}

/* ── 스펙 2열 grid ─────────────────────────────────────── */
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

/* ── 섹션 제목 ─────────────────────────────────────────── */
.sec-title {
  margin-top: 18px;
  font-size: 17px;
  font-weight: 700;
  color: var(--text-strong);
}

/* ── 거주기간 슬라이더 ─────────────────────────────────── */
.period {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
/* ── 내 재정 체크 박스 ─────────────────────────────────── */
.afford {
  padding: 12px 14px;
  border-radius: 12px;
}
.afford--ok {
  background: #e1f3e0;
}
.afford--tight {
  background: #ffffc3;
}
.afford--over {
  background: #f4d1d1;
}
/* "내 재정 체크" 라벨 - 볼드 아님, 작은 회색 */
.afford__label {
  font-size: 11px;
  font-weight: 500;
  color: var(--text-muted);
  margin-bottom: 3px;
}
/* 헤드라인(👍 딱 맞아요)만 볼드 */
.afford__t {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-strong);
}
.afford__s {
  margin-top: 5px;
  font-size: 12px;
  line-height: 1.6;
  color: var(--text-body);
}

/* ── 비율 막대그래프 ───────────────────────────────────── */
.ratio__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.ratio__title {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-strong);
}
/* 보증금 포함 토글 스위치 */
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
  background: var(--kb-yellow-deep);
}
.switch input:checked + .switch__track .switch__thumb {
  transform: translateX(16px);
}
.switch__label {
  font-size: 11px;
  color: var(--text-muted);
}
.ratiobar {
  display: flex;
  width: 100%;
  height: 14px;
  border-radius: 999px;
  overflow: hidden;
  background: var(--kb-gray-pale);
}
.ratiobar__seg {
  height: 100%;
}
.seg--deposit {
  background: #6b5b4d;
}
.seg--rent {
  background: #a68b73;
}
.seg--maint {
  background: #e5c558;
}
.ratio-list {
  list-style: none;
  margin: 14px 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.ratio-list li {
  display: flex;
  align-items: center;
  font-size: 13px;
}
.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 8px;
  flex: none;
}
.dot--deposit {
  background: #6b5b4d;
}
.dot--rent {
  background: #a68b73;
}
.dot--maint {
  background: #e5c558;
}
.ratio-name {
  color: var(--text-body);
}
.ratio-amt {
  margin-left: auto;
  font-weight: 700;
  color: var(--text-strong);
}
.ratio-pct {
  margin-left: 10px;
  font-weight: 700;
  color: #f0a500;
  min-width: 38px;
  text-align: right;
}
.promo {
  text-align: center;
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
