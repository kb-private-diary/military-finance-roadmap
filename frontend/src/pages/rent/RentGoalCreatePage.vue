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
    <RoadmapCharacterSlider :progress="20" label="자취 로드맵" />

    <header>
      <p class="step">STEP 1</p>
      <h2 class="title">자취방 찾기</h2>
      <p class="desc">위치와 예산만 입력하면 딱 맞는 매물을 찾아드려요</p>
    </header>

    <!-- 1. 어디에서 -->
    <section class="field">
      <p class="label">1. 어디에서 찾을까요?</p>
      <div class="btn-row">
        <CategoryButton
          variant="square-yellow"
          label="학교 근처"
          :active="draft.locationType === 'SCHOOL'"
          @click="rentStore.setConditions({ locationType: 'SCHOOL' })"
        />
        <CategoryButton
          variant="square-yellow"
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

    <!-- 반경 (제목 없이, 조정 링크 + 현재값 한 줄) -->
    <section class="field">
      <div class="row-between">
        <button class="link-add" @click="radiusOpen = !radiusOpen">
          반경 조정하기 {{ radiusOpen ? '▲' : '▼' }}
        </button>
        <span class="muted">{{ draft.radiusKm }}km</span>
      </div>
      <BaseCard v-if="radiusOpen" padding="12px 14px">
        <input type="range" min="1" max="5" step="1" :value="draft.radiusKm" class="slider"
          @input="rentStore.setConditions({ radiusKm: Number($event.target.value) })" />
        <div class="scale"><span>1km</span><span>5km</span></div>
      </BaseCard>
    </section>

    <!-- 2. 월예산 -->
    <section class="field">
      <p class="label">2. 월 예산 (월세 + 관리비)</p>
      <BaseCard padding="14px 16px">
        <div class="budget-val">{{ draft.monthlyBudget }}만원</div>
        <input type="range" min="30" max="150" step="5" :value="draft.monthlyBudget" class="slider"
          @input="rentStore.setConditions({ monthlyBudget: Number($event.target.value) })" />
        <div class="scale"><span>30만</span><span>150만</span></div>
      </BaseCard>
    </section>

    <!-- 만기금 안내 -->
    <div class="maturity">
      <div class="maturity__t">만기금 {{ maturityManwon }}만원 자동 반영</div>
      <div class="maturity__s">오픈뱅킹 데이터 기반</div>
    </div>

    <BottomButtonBar
      secondary-label="이전"
      :primary-label="submitting ? '불러오는 중...' : '매물 보기'"
      :primary-disabled="!canProceed || submitting"
      @secondary-click="router.push({ name: 'Home' })"
      @primary-click="goNext"
    />

    <!-- 지역 선택 바텀시트 (시 → 군/구 → 동) -->
    <BaseBottomSheet v-model="regionSheetOpen" title="지역 선택" confirm-text="닫기">
      <div class="cascade">
        <div class="cascade-row">
          <BaseInput type="select" v-model="selSido" :options="sidoOpts" placeholder="시/도" />
          <BaseInput type="select" v-model="selSigungu" :options="sigunguOpts" placeholder="시/군/구" />
          <BaseInput type="select" v-model="selDong" :options="dongOpts" placeholder="읍/면/동" />
        </div>
        <p class="cascade-hint">시 · 군/구 · 동 순으로 선택하세요</p>
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
  gap: 10px;
}
.cascade-row {
  display: flex;
  gap: 8px;
}
.cascade-row > * {
  flex: 1;
  min-width: 0;
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
.cascade-hint {
  font-size: 11px;
  color: var(--text-hint);
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
