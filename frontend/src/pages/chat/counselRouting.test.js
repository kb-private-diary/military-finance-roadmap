// counselRouting.js에 있는 자유입력 라우팅 함수들의 대표 문구 회귀 테스트.
// 이 파일의 목적은 완전한 커버리지가 아니라, 실제로 한 번 문제가 됐던(혹은 될 뻔한) 문구들을
// 고정해두는 것 - 새 키워드를 추가할 땐 이 테스트를 먼저 돌려보고, 깨지는 케이스가 있으면
// 의도한 변경인지 확인한 뒤 여기도 같이 업데이트할 것.
import { describe, expect, it } from 'vitest';
import { detectCounselGoal, detectDirectListCategory, detectTentativeListCategory } from './counselRouting';

describe('detectCounselGoal', () => {
  it('"군적금으로 뭐하지?"는 목적을 savings로 자동 인식한다(상품 목록 직행 금지)', () => {
    expect(detectCounselGoal('군적금으로 뭐하지?')?.value).toBe('savings');
  });

  it('"적금 추천해줘"도 savings로 인식한다(2026-08-11)', () => {
    expect(detectCounselGoal('적금 추천해줘')?.value).toBe('savings');
  });

  it('"투자해보고싶어"는 investment로 인식한다', () => {
    expect(detectCounselGoal('투자해보고싶어')?.value).toBe('investment');
  });

  it('"청약 넣고 싶어요"는 housing으로 인식한다', () => {
    expect(detectCounselGoal('청약 넣고 싶어요')?.value).toBe('housing');
  });

  it('"용돈 관리하고 싶어"는 spending으로 인식한다', () => {
    expect(detectCounselGoal('용돈 관리하고 싶어')?.value).toBe('spending');
  });

  it('목적을 짐작할 수 없는 문장은 null(=목적부터 되묻기)', () => {
    expect(detectCounselGoal('오늘 날씨 어때')).toBeNull();
  });
});

describe('detectDirectListCategory', () => {
  it('"적금 추천해줘"는 상품 목록으로 직행한다(savings)', () => {
    expect(detectDirectListCategory('적금 추천해줘')?.category).toBe('savings');
  });

  it('"예금 상품 보여줘"는 상품 목록으로 직행한다(deposit)', () => {
    expect(detectDirectListCategory('예금 상품 보여줘')?.category).toBe('deposit');
  });

  it('"군적금으로 뭐하지?"는 직행하지 않는다(상담 되묻기로 가야 함) - 2026-08-12 회귀 케이스', () => {
    expect(detectDirectListCategory('군적금으로 뭐하지?')).toBeNull();
  });

  it('추천/목록 의도 없는 "적금이 뭐예요?" 같은 순수 질문도 직행하지 않는다', () => {
    expect(detectDirectListCategory('적금이 뭐예요?')).toBeNull();
  });

  it('"투자 추천해줘"는 적금/예금 카테고리가 아니라서 직행 대상이 아니다', () => {
    // 추천 의도는 있지만(추천), 적금/예금 키워드가 없어 COUNSEL_DIRECT_LIST_KEYWORDS.find가
    // undefined를 반환한다 - null이 아니라 undefined인 게 정상 동작(falsy면 충분).
    expect(detectDirectListCategory('투자 추천해줘')).toBeFalsy();
  });
});

describe('detectTentativeListCategory', () => {
  it('"아 그냥 적금 들까"는 확인 후 목록으로 갈 후보(savings)로 잡힌다(2026-08-13)', () => {
    expect(detectTentativeListCategory('아 그냥 적금 들까')?.category).toBe('savings');
  });

  it('"예금이나 할까"는 확인 후 목록으로 갈 후보(deposit)로 잡힌다', () => {
    expect(detectTentativeListCategory('예금이나 할까')?.category).toBe('deposit');
  });

  it('"군적금으로 뭐하지?"는 잡히지 않는다(단어 경계 밖 - 되묻기로 가야 함, 2026-08-12와 동일한 함정)', () => {
    expect(detectTentativeListCategory('군적금으로 뭐하지?')).toBeFalsy();
  });

  it('문장 맨 앞에 붙어도 잡힌다("적금 할까")', () => {
    expect(detectTentativeListCategory('적금 할까')?.category).toBe('savings');
  });
});
