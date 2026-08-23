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
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import BaseBottomSheet from '@/components/common/BaseBottomSheet.vue';
import SearchSelect from '@/components/common/SearchSelect.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import TabBar from '@/components/common/TabBar.vue';
import RangeSlider from '@/components/common/RangeSlider.vue';
import rentSchoolIcon from '@/assets/images/rent-school.png';
import rentMapIcon from '@/assets/images/rent-map.png';

const router = useRouter();
const rentStore = useRentStore();
const { show } = useToast();
const draft = rentStore.draft;

// 학교 검색 — 전체 학교 목록을 한 번 불러와 공통 SearchSelect가 클라이언트 필터링
// (백엔드 /rent/schools 는 keyword 없으면 전체 목록 반환)
const schoolList = ref([]);
const selectedSchool = ref(null);
const loadSchools = async () => {
  try {
    const data = await rentApi.searchSchools('');
    schoolList.value = Array.isArray(data) ? data : [];
  } catch {
    schoolList.value = [];
  }
};
loadSchools();
const selectSchool = (s) => {
  rentStore.setConditions({ schoolId: s.schoolId, schoolName: s.schoolName, schoolAddress: s.address });
  // 선택된 학교는 아래 카드에 표시됨 (SearchSelect 는 선택 후 내부 검색어를 스스로 비움)
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

const loadSido = async () => {
  try {
    sidoOpts.value = toOpts(await rentApi.findRegions({}));
  } catch {
    sidoOpts.value = []; // 시연: 가짜 폴백 제거, 실패 시 빈 목록
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

// 반경 (텍스트 링크로 펼침, 목업 기준 기본 펼침)
const radiusOpen = ref(true);

// 월 예산 슬라이더 채움(노랑) 표현 — 30~150 구간을 %로 환산해 트랙 배경에 그린다.
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

    <PageHeader title="어디에 집을 구하고 싶습니까?" />

    <!-- 1. 어디에서 -->
    <section class="field">
      <TabBar
        :model-value="draft.locationType"
        :tabs="[
          { label: '학교 근처', value: 'SCHOOL' },
          { label: '지역으로', value: 'REGION' },
        ]"
        @update:model-value="rentStore.setConditions({ locationType: $event })"
      />

      <!-- 학교 모드 -->
      <template v-if="draft.locationType === 'SCHOOL'">
        <p class="label">학교 선택</p>
        <SearchSelect
          v-model="selectedSchool"
          :items="schoolList"
          label-key="schoolName"
          sub-key="address"
          placeholder="학교 이름 검색"
          @select="selectSchool"
        />
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
          <div class="chips">
            <span v-for="r in draft.regions" :key="r.code" class="chip">
              {{ r.name }}
              <button class="chip__x" @click="rentStore.removeRegion(r.code)">×</button>
            </span>
            <button v-if="draft.regions.length < 3" class="add-btn" @click="openRegionSheet">
              지역 추가 +
            </button>
          </div>
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
      <TabBar
        v-if="radiusOpen"
        variant="fill"
        :model-value="draft.radiusKm"
        :tabs="[
          { label: '1km', value: 1 },
          { label: '3km', value: 3 },
          { label: '5km', value: 5 },
        ]"
        @update:model-value="rentStore.setConditions({ radiusKm: $event })"
      />
    </section>

    <!-- 2. 월 예산 (월세 + 관리비) — 매물 추천은 실질 월부담(월세+관리비+보증금환산) 기준 -->
    <section class="field">
      <RangeSlider
        :model-value="draft.monthlyBudget"
        :min="30"
        :max="150"
        :step="5"
        label="월 예산 (월세 + 관리비)"
        unit="만원"
        :ticks="['30만원', '90만원', '150만원']"
        hint="월세와 관리비를 합친 실질 월부담을 기준으로 매물을 추천해드려요"
        @update:model-value="rentStore.setConditions({ monthlyBudget: $event })"
      />
    </section>

    <BottomButtonBar
      secondary-label="이전"
      :primary-label="submitting ? '불러오는 중...' : '자취 추천받기'"
      :primary-disabled="!canProceed || submitting"
      @secondary-click="router.push({ name: 'RoadmapMain' })"
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
  gap: var(--space-8);
}
.field {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
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
.row-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.search {
  position: relative;
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
  align-items: center;
  gap: 8px;
}
.chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: 1px solid var(--kb-yellow);
  border-radius: 999px;
  background: #fff;
  color: var(--text-body);
  font-size: 12px;
  font-weight: 600;
}
.chip__x {
  border: 0;
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
  font-size: 14px;
  line-height: 1;
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
/* 지역 추가 - 칩과 같은 줄, 연회색 pill */
.add-btn {
  padding: 6px 14px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: #f5f6f8;
  color: var(--text-hint);
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
}
/* 월 예산 슬라이더 — 채움(노랑)은 인라인 배경, 핸들은 흰 원+노랑 테두리 */
</style>
