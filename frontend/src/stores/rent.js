import { ref } from 'vue';
import { defineStore } from 'pinia';
import rentApi from '@/api/rentApi';

// 자취 로드맵 위저드 '가방' (FINAL 스펙 v2.1)
// 화면(자취방찾기 → 매물 → 상세 → 금융상품 → 저장상세)을 옮겨도 값이 유지됨
// ⭐ Step3에서 정한 months(거주기간)가 Step4~5 계산 기준으로 이어짐
export const useRentStore = defineStore('rent', () => {
  // ── STEP1 입력 ────────────────────────────────────────────
  const draft = ref({
    locationType: 'SCHOOL', // 'SCHOOL'(학교 근처) | 'REGION'(지역으로)
    schoolId: null, //        학교 모드: 선택 학교 id
    schoolName: '', //        학교 모드: 학교명(표시용)
    schoolAddress: '', //     학교 모드: 학교 주소(표시용)
    regions: [], //           지역 모드: [{ code, name }] 최대 3개
    radiusKm: 3, //           반경(기본 3km, 접힘)
    monthlyBudget: 60, //     월예산 만원 (30~150, 5만 단위) — 전송 시 ×10000
  });

  const currentGoalId = ref(null);
  const maturityAmount = ref(0); // 오픈뱅킹 만기금(서버가 알려줌, 원)
  const selectedListingId = ref(null);
  const months = ref(6); // Step3 거주기간 → Step4~5 유지

  // ── setter ────────────────────────────────────────────────
  const setConditions = (payload) => Object.assign(draft.value, payload);

  const addRegion = (region) => {
    if (draft.value.regions.length >= 3) return;
    if (draft.value.regions.some((r) => r.code === region.code)) return;
    draft.value.regions.push(region);
  };
  const removeRegion = (code) => {
    draft.value.regions = draft.value.regions.filter((r) => r.code !== code);
  };

  const reset = () => {
    Object.assign(draft.value, {
      locationType: 'SCHOOL',
      schoolId: null,
      schoolName: '',
      schoolAddress: '',
      regions: [],
      radiusKm: 3,
      monthlyBudget: 60,
    });
    currentGoalId.value = null;
    maturityAmount.value = 0;
    selectedListingId.value = null;
    months.value = 6;
  };

  // ── 목표 생성 (매물 보기) → { goalId, maturityAmount } ─────
  const createGoal = async (userId) => {
    // 백엔드 실계약: { selectionMode, schoolId, commuteRadiusKm, regionCodes, monthlyBudget(원), residencePreset }
    const isSchool = draft.value.locationType === 'SCHOOL';
    const payload = {
      selectionMode: draft.value.locationType, // 'SCHOOL' | 'REGION'
      schoolId: isSchool ? draft.value.schoolId : null,
      commuteRadiusKm: isSchool ? draft.value.radiusKm : null,
      regionCodes: isSchool ? [] : draft.value.regions.map((r) => r.code),
      monthlyBudget: draft.value.monthlyBudget * 10000, // 만원 → 원
      residencePreset: 'YEAR', // 거주기간은 Step3에서 조정, 생성 시 기본값
    };
    const data = await rentApi.createGoal(payload, userId);
    const goalId = typeof data === 'object' && data !== null ? data.goalId : data;
    currentGoalId.value = goalId;
    if (data?.maturityAmount != null) maturityAmount.value = data.maturityAmount;
    return goalId;
  };

  return {
    draft,
    currentGoalId,
    maturityAmount,
    selectedListingId,
    months,
    setConditions,
    addRegion,
    removeRegion,
    reset,
    createGoal,
  };
});
