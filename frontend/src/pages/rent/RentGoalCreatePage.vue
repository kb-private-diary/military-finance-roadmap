<script setup>
// SCR-RENT-01 · Step 1) 자취방 찾기  담당: 수연
// 디자인: UI/rent_ui_school_mode.html·region_mode.html — 담백(공통 컴포넌트 우선)
// 위치(학교/지역) + 반경 + 월예산 → [매물 보기]
import { ref, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import rentApi from '@/api/rentApi';
import { useRentStore } from '@/stores/rent';
import { useToast } from '@/composables/useToast';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseInput from '@/components/common/BaseInput.vue';
import CategoryButton from '@/components/common/CategoryButton.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import BaseBottomSheet from '@/components/common/BaseBottomSheet.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';

const router = useRouter();
const rentStore = useRentStore();
const { show } = useToast();
const draft = rentStore.draft;
const maturityManwon = 720; // TODO: 오픈뱅킹/적금 데이터 연동

// 학교 검색 — 정상 응답(배열)만 반영, 결과 없거나 실패 시 빈 목록(부산대 폴백 제거)
const keyword = ref('');
const schoolResults = ref([]);
const searching = ref(false);
const searchSchools = async () => {
  if (keyword.value.trim().length < 2) return (schoolResults.value = []);
  searching.value = true;
  try {
    const data = await rentApi.searchSchools(keyword.value.trim());
    schoolResults.value = Array.isArray(data) ? data : [];
  } catch {
    schoolResults.value = [];
  } finally {
    searching.value = false;
  }
};
watch(keyword, searchSchools);
const selectSchool = (s) => {
  rentStore.setConditions({ schoolId: s.schoolId, schoolName: s.schoolName, schoolAddress: s.address });
  schoolResults.value = [];
  keyword.value = ''; // 검색창 초기화 (선택된 학교는 아래 카드에 표시됨)
};

// 지역 (최대 3개) — 바텀시트에서 시도 → 시군구 → 동 계층 선택 (rentApi.findRegions 실 API)
// 백엔드 RegionResponseDTO { code, name } → BaseInput 옵션 { value, label } 로 매핑
// 흐름: findRegions({}) 시도 → findRegions({ sido }) 시군구 → findRegions({ sigunguCode }) 동
const toOpts = (list) =>
  (list || []).map((r) => ({ value: r.code, label: r.name }));

const regionSheetOpen = ref(false);
const selSido = ref('');
const selSigungu = ref('');
const selDong = ref('');
const sidoOpts = ref([]);
const sigunguOpts = ref([]);
const dongOpts = ref([]);

// API 실패 시에만 쓰는 최소 폴백 (에러 안전)
const SAMPLE_SIDO = [{ value: '부산광역시', label: '부산광역시' }];

const loadSido = async () => {
  try {
    sidoOpts.value = toOpts(await rentApi.findRegions({}));
  } catch {
    sidoOpts.value = SAMPLE_SIDO;
  }
};
// 시도 선택 → 시군구 로드 (하위 선택 초기화)
watch(selSido, async (sido) => {
  selSigungu.value = '';
  selDong.value = '';
  sigunguOpts.value = [];
  dongOpts.value = [];
  if (!sido) return;
  try {
    sigunguOpts.value = toOpts(await rentApi.findRegions({ sido }));
  } catch {
    sigunguOpts.value = [];
  }
});
// 시군구 선택 → 동 로드 (하위 선택 초기화)
watch(selSigungu, async (sigunguCode) => {
  selDong.value = '';
  dongOpts.value = [];
  if (!sigunguCode) return;
  try {
    dongOpts.value = toOpts(await rentApi.findRegions({ sigunguCode }));
  } catch {
    dongOpts.value = [];
  }
});
const openRegionSheet = () => {
  selSido.value = '';
  selSigungu.value = '';
  selDong.value = '';
  sigunguOpts.value = [];
  dongOpts.value = [];
  if (!sidoOpts.value.length) loadSido();
  regionSheetOpen.value = true;
};
// 동까지 고르면 칩으로 추가 + 시트 닫기 (region_code = 법정동코드, 기존 addRegion 유지)
watch(selDong, (v) => {
  if (!v) return;
  const sido = sidoOpts.value.find((o) => o.value === selSido.value)?.label || '';
  const gu = sigunguOpts.value.find((o) => o.value === selSigungu.value)?.label || '';
  const dong = dongOpts.value.find((o) => o.value === v)?.label || '';
  rentStore.addRegion({ code: v, name: `${sido} ${gu} ${dong}`.trim() });
  regionSheetOpen.value = false;
});

// 바텀시트 안에서 시/도 → 시/군/구 → 읍/면/동을 '스크롤 리스트' 단계별로 보여준다.
// (커스텀 드롭다운 메뉴는 position:absolute라 시트 overflow에 잘려서 모바일에서 안 보임)
const regionStep = computed(() => {
  if (!selSido.value) return 'sido';
  if (!selSigungu.value) return 'sigungu';
  return 'dong';
});
const currentRegionOpts = computed(() => {
  if (regionStep.value === 'sido') return sidoOpts.value;
  if (regionStep.value === 'sigungu') return sigunguOpts.value;
  return dongOpts.value;
});
const selSidoLabel = computed(
  () => sidoOpts.value.find((o) => o.value === selSido.value)?.label || '',
);
const selSigunguLabel = computed(
  () => sigunguOpts.value.find((o) => o.value === selSigungu.value)?.label || '',
);
// 현재 단계의 항목을 탭하면 해당 ref만 채우고, 나머지는 watch가 처리
const selectRegionOption = (opt) => {
  if (regionStep.value === 'sido') selSido.value = opt.value;
  else if (regionStep.value === 'sigungu') selSigungu.value = opt.value;
  else selDong.value = opt.value;
};
// 선택 경로(breadcrumb)를 탭해 상위 단계로 되돌리기 (watch가 하위 선택/옵션 초기화)
const resetToSido = () => {
  selSido.value = '';
};
const resetToSigungu = () => {
  selSigungu.value = '';
};

// 반경 (텍스트 링크로 펼침)
const radiusOpen = ref(false);

const canProceed = computed(() =>
  draft.locationType === 'SCHOOL' ? !!draft.schoolId : draft.regions.length > 0,
);
const submitting = ref(false);
const goNext = async () => {
  if (!canProceed.value || submitting.value) return;
  submitting.value = true;
  try {
    const goalId = await rentStore.createGoal();
    await router.push({ name: 'RentListingList', params: { goalId } });
  } catch {
    show('조건 저장에 실패했어요. 다시 시도해주세요.', 'error');
  } finally {
    submitting.value = false;
  }
};
</script>

<template>
  <div class="rent-goal">
    <RoadmapCharacterSlider :step="1" label="자취 로드맵" />

    <header>
      <p class="step">STEP 1</p>
      <h2 class="title">자취방 찾기</h2>
      <p class="desc">위치와 예산만 입력하면 딱 맞는 매물을 찾아드려요</p>
    </header>

    <!-- 1. 어디에서 -->
    <section class="field">
      <p class="label">어디에 집을 구하고 싶습니까?</p>
      <div class="btn-row">
        <CategoryButton
          variant="square-yellow"
          icon="🎓"
          label="학교 근처"
          :active="draft.locationType === 'SCHOOL'"
          @click="rentStore.setConditions({ locationType: 'SCHOOL' })"
        />
        <CategoryButton
          variant="square-yellow"
          icon="📍"
          label="지역으로"
          :active="draft.locationType === 'REGION'"
          @click="rentStore.setConditions({ locationType: 'REGION' })"
        />
      </div>

      <!-- 학교 모드 -->
      <template v-if="draft.locationType === 'SCHOOL'">
        <div class="search">
          <BaseInput v-model="keyword" placeholder="학교 이름 검색 (예: 부산대)" />
          <ul v-if="schoolResults.length" class="dropdown">
            <li v-for="s in schoolResults" :key="s.schoolId" @click="selectSchool(s)">
              <strong>{{ s.schoolName }}</strong><span>{{ s.address }}</span>
            </li>
          </ul>
          <p v-else-if="searching" class="hint">검색 중...</p>
          <p v-else-if="keyword.trim().length >= 2" class="hint">검색 결과가 없어요</p>
        </div>
        <BaseCard v-if="draft.schoolId" padding="12px 14px">
          <div class="picked-name">{{ draft.schoolName }}</div>
          <div class="picked-addr">{{ draft.schoolAddress }}</div>
        </BaseCard>
      </template>

      <!-- 지역 모드 -->
      <template v-else>
        <div class="row-between">
          <span class="label">지역 선택</span>
          <span class="muted">최대 3개</span>
        </div>
        <BaseCard padding="12px 14px">
          <div v-if="draft.regions.length" class="chips">
            <span v-for="r in draft.regions" :key="r.code" class="chip">
              {{ r.name }}
              <button class="chip__x" @click="rentStore.removeRegion(r.code)">×</button>
            </span>
          </div>
          <button v-if="draft.regions.length < 3" class="add-btn" @click="openRegionSheet">
            + 지역 추가
          </button>
        </BaseCard>
      </template>
    </section>

    <!-- 반경 (조정 링크로 펼침 → 1km/3km/5km 세그먼트 버튼) -->
    <section class="field">
      <div class="row-between">
        <button class="link-add" @click="radiusOpen = !radiusOpen">
          반경 조정하기 {{ radiusOpen ? '▲' : '▼' }}
        </button>
        <span class="muted">{{ draft.radiusKm }}km</span>
      </div>
      <div v-if="radiusOpen" class="btn-row">
        <CategoryButton
          v-for="km in [1, 3, 5]"
          :key="km"
          variant="square-yellow"
          :label="`${km}km`"
          :active="draft.radiusKm === km"
          @click="rentStore.setConditions({ radiusKm: km })"
        />
      </div>
    </section>

    <!-- 2. 월 예산 (월세 + 관리비) — 매물 추천은 실질 월부담(월세+관리비+보증금환산) 기준 -->
    <section class="field">
      <div class="row-between">
        <p class="label">월 예산 (월세 + 관리비)</p>
        <span class="budget-val">{{ draft.monthlyBudget }}만원</span>
      </div>
      <BaseCard padding="14px 16px">
        <input type="range" min="30" max="150" step="5" :value="draft.monthlyBudget" class="slider"
          @input="rentStore.setConditions({ monthlyBudget: Number($event.target.value) })" />
        <div class="scale"><span>30만원</span><span>90만원</span><span>150만원</span></div>
      </BaseCard>
      <p class="hint">월세와 관리비를 합친 실질 월부담을 기준으로 매물을 추천해드려요</p>
    </section>

    <!-- 만기금 안내 -->
    <div class="maturity">
      <div class="maturity__t">만기금 {{ maturityManwon }}만원 자동 반영</div>
      <div class="maturity__s">오픈뱅킹 데이터 기반</div>
    </div>

    <BottomButtonBar
      secondary-label="이전"
      :primary-label="submitting ? '불러오는 중...' : '추천 받기'"
      :primary-disabled="!canProceed || submitting"
      @secondary-click="router.push({ name: 'Home' })"
      @primary-click="goNext"
    />

    <!-- 지역 선택 바텀시트 (시/도 → 시/군/구 → 읍/면/동, 단계별 리스트) -->
    <BaseBottomSheet v-model="regionSheetOpen" title="지역 선택" confirm-text="닫기">
      <div class="cascade">
        <!-- 선택 경로: 탭하면 해당 단계로 되돌아감 -->
        <div class="cascade-crumbs">
          <button
            type="button"
            class="crumb"
            :class="{ 'crumb--active': regionStep === 'sido' }"
            @click="resetToSido"
          >
            {{ selSidoLabel || '시/도' }}
          </button>
          <span class="crumb-sep">›</span>
          <button
            type="button"
            class="crumb"
            :class="{ 'crumb--active': regionStep === 'sigungu' }"
            :disabled="!selSido"
            @click="resetToSigungu"
          >
            {{ selSigunguLabel || '시/군/구' }}
          </button>
          <span class="crumb-sep">›</span>
          <span class="crumb" :class="{ 'crumb--active': regionStep === 'dong' }">읍/면/동</span>
        </div>

        <!-- 현재 단계 옵션 리스트 (시트 안에서 스크롤) -->
        <ul class="region-list">
          <li
            v-for="opt in currentRegionOpts"
            :key="opt.value"
            class="region-item"
            @click="selectRegionOption(opt)"
          >
            {{ opt.label }}
          </li>
          <li v-if="!currentRegionOpts.length" class="region-empty">불러오는 중...</li>
        </ul>
      </div>
    </BaseBottomSheet>
  </div>
</template>

<style scoped>
.rent-goal {
  padding: 20px 0 88px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.step {
  font-size: 12px;
  color: var(--text-muted);
}
.title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-strong);
}
.desc {
  margin-top: 4px;
  font-size: 13px;
  color: var(--text-muted);
}
.field {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-body);
}
.muted {
  font-size: 11px;
  color: var(--text-hint);
}
.strong {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-body);
}
.row-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.btn-row {
  display: flex;
  gap: 8px;
}
.search {
  position: relative;
}
.dropdown {
  margin: 6px 0 0;
  padding: 6px 0;
  list-style: none;
  border: 1px solid var(--line);
  border-radius: 8px;
  max-height: 190px;
  overflow-y: auto;
}
.dropdown li {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 9px 14px;
  cursor: pointer;
}
.dropdown li:hover {
  background: #f7f7f8;
}
.dropdown strong {
  font-size: 13px;
  color: var(--text-body);
}
.dropdown span {
  font-size: 11px;
  color: var(--text-hint);
}
.hint {
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-hint);
}
.picked-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-body);
}
.picked-addr {
  margin-top: 3px;
  font-size: 11px;
  color: var(--text-muted);
}
.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
}
.chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 10px;
  border: 1px solid var(--line-strong);
  border-radius: 999px;
  background: #fff;
  color: var(--text-body);
  font-size: 12px;
}
.chip__x {
  border: 0;
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
  font-size: 13px;
  padding: 0;
}
.link-add {
  align-self: flex-start;
  padding: 2px 0;
  border: 0;
  background: transparent;
  color: var(--text-muted);
  font-size: 12px;
  text-decoration: underline;
  cursor: pointer;
  font-family: inherit;
}
.cascade {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.cascade-crumbs {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}
.crumb {
  padding: 4px 2px;
  border: 0;
  background: transparent;
  font-family: inherit;
  font-size: 13px;
  color: var(--text-hint);
  cursor: pointer;
}
.crumb:disabled {
  cursor: default;
}
.crumb--active {
  color: var(--text-body);
  font-weight: 600;
}
.crumb-sep {
  color: var(--text-hint);
  font-size: 13px;
}
.region-list {
  list-style: none;
  margin: 0;
  padding: 4px 0;
  border: 1px solid var(--line);
  border-radius: 10px;
  max-height: 44vh;
  overflow-y: auto;
}
.region-item {
  padding: 13px 14px;
  font-size: 15px;
  color: var(--text-body);
  border-bottom: 1px solid var(--line);
  cursor: pointer;
}
.region-item:last-child {
  border-bottom: 0;
}
.region-item:active {
  background: var(--kb-yellow-pale);
}
.region-empty {
  padding: 20px 14px;
  text-align: center;
  font-size: 13px;
  color: var(--text-hint);
}
.add-btn {
  align-self: flex-start;
  padding: 9px 16px;
  border: 1px solid var(--line-strong);
  border-radius: 8px;
  background: #fff;
  color: var(--text-body);
  font-size: 13px;
  cursor: pointer;
  font-family: inherit;
}
.slider {
  width: 100%;
  margin: 10px 0 4px;
  accent-color: var(--kb-yellow-deep);
}
.scale {
  display: flex;
  justify-content: space-between;
  font-size: 10px;
  color: var(--text-hint);
}
.budget-val {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-strong);
}
.maturity {
  margin-top: auto;
  padding: 12px 14px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fafafa;
}
.maturity__t {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-body);
}
.maturity__s {
  margin-top: 3px;
  font-size: 11px;
  color: var(--text-muted);
}
</style>
