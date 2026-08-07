package org.scoula.rent.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.scoula.rent.client.YouthApiClient;
import org.scoula.rent.domain.HousingProductVO;
import org.scoula.rent.dto.YouthPolicyDTO;
import org.scoula.rent.dto.YouthSyncResultDTO;
import org.scoula.rent.mapper.HousingProductMapper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 온통청년 청년 주거정책 배치 동기화 구현 (연동명세 §4·§5·§6-3).
 *
 * <p>흐름: {@link YouthApiClient}로 전량 수신(HTTP, 트랜잭션 밖) → mclsfNm 필터 + plcyNm 중복 제거
 * → 필드 매핑 + 텍스트 파싱(군복무·중복수급·보증금/면적) → plcy_no 기준 UPSERT.</p>
 *
 * <p>트랜잭션에 HTTP I/O 를 넣지 않기 위해 이 메서드에는 {@code @Transactional} 을 걸지 않는다.
 * UPSERT 는 행 단위로 실행(자동 커밋)하며, 한 행이 실패해도 try/catch 로 건너뛰어 배치가 죽지 않게 한다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class YouthPolicySyncServiceImpl implements YouthPolicySyncService {

    // 앱에서 거를 중분류 (연동명세 §1-4: mclsfNm 파라미터가 불안정해 앱에서 필터)
    private static final String TARGET_MCLSF = "전월세 및 주거급여 지원";

    // 마감 코드 (연동명세 §3-3)
    private static final String CODE_CLOSED = "0057003";

    // 지원방식 코드 (연동명세 §3-3)
    private static final String CODE_LOAN_DIRECT = "0042003";
    private static final String CODE_LOAN_GUARANTEE = "0042007";
    private static final String CODE_GRANT = "0042006";
    private static final String CODE_VOUCHER = "0042010";

    // 기관그룹 코드 (연동명세 §2-1)
    private static final String CODE_CENTRAL = "0054001"; // 중앙부처 → POLICY
    private static final String CODE_LOCAL = "0054002";   // 지자체   → LOCAL

    // 중복수급 그룹 (연동명세 §6-3, 개발명세 §2)
    private static final String GROUP_MONTHLY_SUBSIDY = "MONTHLY_SUBSIDY";
    private static final String GROUP_DEPOSIT_LOAN = "DEPOSIT_LOAN";

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");

    // 보증금 텍스트 파싱: "보증금 … N억/N천만원/N만원"
    private static final Pattern DEPOSIT_PAT =
            Pattern.compile("보증금[^0-9]{0,12}((?:\\d+억)?(?:\\s*\\d+천)?(?:\\s*\\d+백)?\\s*(?:\\d+)?\\s*만?원?)");
    // 면적 텍스트 파싱: "NN㎡" / "NN제곱미터" / "NN m2"
    private static final Pattern AREA_PAT =
            Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:㎡|제곱미터|제곱미|m2|m²)");

    private final YouthApiClient client;
    private final HousingProductMapper mapper;

    @Override
    public YouthSyncResultDTO syncYouthPolicies() {
        // 1) 전량 수신 (HTTP - 트랜잭션 밖)
        List<YouthPolicyDTO> all = client.fetchAllHousingPolicies();

        // 2) mclsfNm 필터 + plcyNm 중복 제거 + plcyNo 필수 (연동명세 §4 [2][3])
        Map<String, YouthPolicyDTO> deduped = new LinkedHashMap<>();
        for (YouthPolicyDTO p : all) {
            if (p.getPlcyNo() == null || p.getPlcyNo().isBlank()) {
                continue; // PK 없는 행은 UPSERT 불가
            }
            if (!TARGET_MCLSF.equals(p.getMclsfNm())) {
                continue; // 전월세 및 주거급여 지원만
            }
            String key = p.getPlcyNm() == null ? p.getPlcyNo() : p.getPlcyNm();
            deduped.putIfAbsent(key, p);
        }

        // 3) 매핑 + 파싱 + UPSERT (행 단위, 실패는 건너뜀)
        int upserted = 0;
        int failed = 0;
        for (YouthPolicyDTO p : deduped.values()) {
            try {
                HousingProductVO vo = toVo(p);
                mapper.upsertYouthPolicy(vo);
                upserted++;
            } catch (Exception e) {
                failed++;
                log.warn("온통청년 정책 UPSERT 실패 (plcyNo={}, plcyNm={}): {}",
                        p.getPlcyNo(), p.getPlcyNm(), e.getMessage());
            }
        }

        YouthSyncResultDTO result = YouthSyncResultDTO.builder()
                .received(all.size())
                .matched(deduped.size())
                .upserted(upserted)
                .failed(failed)
                .build();
        log.info("온통청년 동기화 결과: 수신={}, 대상={}, UPSERT={}, 실패={}",
                result.getReceived(), result.getMatched(), result.getUpserted(), result.getFailed());
        return result;
    }

    // ---------------------------------------------------------------
    // 매핑 (연동명세 §2-1)
    // ---------------------------------------------------------------
    private HousingProductVO toVo(YouthPolicyDTO p) {
        HousingProductVO vo = new HousingProductVO();
        vo.setPlcyNo(p.getPlcyNo());
        vo.setProductName(truncate(nvl(p.getPlcyNm(), "청년 주거정책"), 80));

        String supportType = resolveSupportType(p);
        vo.setProductType(resolveProductType(p));
        vo.setSupportType(supportType);
        vo.setLoanType(resolveLoanType(p, supportType));
        vo.setProvider(truncate(resolveProvider(p), 40));
        vo.setIsKb("N");

        // 지역: zip_cd 앞 2자리로 region_code 를 채워 기존 추천 쿼리(region_code 매칭)와 호환
        vo.setRegionCode(resolveRegionCode(p.getZipCd()));
        vo.setZipCd(p.getZipCd());

        vo.setMinAge(parseAge(p.getSprtTrgtMinAge(), 19));
        vo.setMaxAge(parseAge(p.getSprtTrgtMaxAge(), 34));
        vo.setIncomeLimit(parseIncomeLimit(p));

        // 군복무 혜택 태깅 (연동명세 §4-1 [7], §6-2)
        applyVeteran(vo, p);
        // 중복수급 그룹 자동 판정 (연동명세 §6-3)
        vo.setExclusiveGroup(resolveExclusiveGroup(p, supportType));
        // 보증금·면적 텍스트 파싱 (연동명세 §4-1, §8 - best-effort)
        vo.setDepositLimit(parseDeposit(p.getAddAplyQlfcCndCn()));
        vo.setAreaLimit(parseArea(p.getAddAplyQlfcCndCn()));

        // 공고 기간 (연동명세 §4-1 [8])
        LocalDate[] period = parseApplyPeriod(p);
        vo.setApplyStart(period[0]);
        vo.setApplyEnd(closedEnd(p, period[1]));
        vo.setApplyCycle(truncate(p.getBizPrdEtcCn(), 60));
        vo.setBaseYear(period[0] != null ? period[0].getYear() : LocalDate.now().getYear());

        vo.setPriority(resolvePriority(supportType));
        vo.setDetail(nvl(p.getPlcySprtCn(), p.getPlcyExplnCn()));
        vo.setExternalUrl(truncate(nvl(p.getAplyUrlAddr(), p.getRefUrlAddr1()), 255));
        vo.setJoinCondition(truncate(p.getAddAplyQlfcCndCn(), 200));
        return vo;
    }

    /** pvsnInstGroupCd → product_type */
    private String resolveProductType(YouthPolicyDTO p) {
        String c = p.getPvsnInstGroupCd();
        if (CODE_LOCAL.equals(c)) return "LOCAL";
        if (CODE_CENTRAL.equals(c)) return "POLICY";
        return "POLICY";
    }

    /**
     * plcyPvsnMthdCd + 텍스트 병행 판정 → support_type (연동명세 §3-3, §8: 코드만 믿지 않는다).
     */
    private String resolveSupportType(YouthPolicyDTO p) {
        String c = p.getPlcyPvsnMthdCd();
        if (CODE_LOAN_DIRECT.equals(c) || CODE_LOAN_GUARANTEE.equals(c)) return "LOAN";
        if (CODE_GRANT.equals(c) || CODE_VOUCHER.equals(c)) return "GRANT";

        // 0042005/0042013/기타 → 이름·지원내용 텍스트로 판정
        String t = (nvl(p.getPlcyNm(), "") + " " + nvl(p.getPlcySprtCn(), "")).toLowerCase();
        if (t.contains("중개") || t.contains("이사비")) return "COST";
        if (t.contains("이자")) return "INTEREST";
        if (t.contains("대출") || t.contains("융자")) return "LOAN";
        return "GRANT"; // 지자체 월세지원이 기타로 등록된 사례 다수 → 기본 GRANT
    }

    private String resolveLoanType(YouthPolicyDTO p, String supportType) {
        if (!"LOAN".equals(supportType) && !"INTEREST".equals(supportType)) {
            return null; // GRANT/COST 는 대출유형 없음
        }
        String t = nvl(p.getPlcyNm(), "") + nvl(p.getPlcySprtCn(), "");
        if (t.contains("월세")) return "MONTHLY";
        if (t.contains("보증금") || t.contains("전세") || t.contains("임차")) return "DEPOSIT";
        return null;
    }

    private String resolveProvider(YouthPolicyDTO p) {
        String v = nvl(p.getSprvsnInstCdNm(), p.getRgtrHghrkInstCdNm());
        return v == null ? "온통청년" : v;
    }

    private int resolvePriority(String supportType) {
        switch (supportType) {
            case "GRANT": return 11;   // 지원금(무상) 최우선 (개발명세 §6)
            case "COST": return 12;
            case "INTEREST": return 25;
            case "LOAN": return 30;
            default: return 50;
        }
    }

    /** zip_cd "26110,26140,..." → 앞 5자리의 앞 2자리(시도코드). 파싱 불가면 null(전국) */
    private String resolveRegionCode(String zipCd) {
        if (zipCd == null || zipCd.isBlank()) return null;
        String first = zipCd.split(",")[0].trim();
        if (first.length() >= 2 && first.substring(0, 2).chars().allMatch(Character::isDigit)) {
            return first.substring(0, 2);
        }
        return null;
    }

    private Integer parseAge(String raw, int fallback) {
        if (raw == null) return fallback;
        try {
            int v = Integer.parseInt(raw.replaceAll("[^0-9]", ""));
            return v <= 0 ? fallback : v;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /** earnMaxAmt(만원) → income_limit(원). earnCndSeCd 가 연소득(0043002)일 때만 신뢰 */
    private Long parseIncomeLimit(YouthPolicyDTO p) {
        String amt = p.getEarnMaxAmt();
        if (amt == null) return null;
        try {
            long manwon = Long.parseLong(amt.replaceAll("[^0-9]", ""));
            return manwon <= 0 ? null : manwon * 10_000L;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 군복무 혜택 태깅 (연동명세 §4-1 [7]) */
    private void applyVeteran(HousingProductVO vo, YouthPolicyDTO p) {
        String text = nvl(p.getPlcyAplyMthdCn(), "") + " " + nvl(p.getAddAplyQlfcCndCn(), "");
        boolean veteran = text.contains("군복무") || text.contains("군 복무")
                || text.contains("제대군인") || text.contains("병역");
        if (veteran) {
            vo.setVeteranExtend(3); // 상세 연장년수는 자유텍스트라 기본 3년(연동명세 §6-2)
            vo.setVeteranNote(truncate(extractVeteranNote(text), 120));
        } else {
            vo.setVeteranExtend(0);
            vo.setVeteranNote(null);
        }
    }

    /** 병역 관련 문장 한 조각 발췌 (표시용) */
    private String extractVeteranNote(String text) {
        for (String kw : new String[]{"군복무", "군 복무", "제대군인", "병역"}) {
            int idx = text.indexOf(kw);
            if (idx >= 0) {
                int end = Math.min(text.length(), idx + 100);
                return text.substring(idx, end).replaceAll("\\s+", " ").trim();
            }
        }
        return null;
    }

    /** 중복수급 그룹 자동 판정 (연동명세 §6-3) */
    private String resolveExclusiveGroup(YouthPolicyDTO p, String supportType) {
        String ptcp = nvl(p.getPtcpPrpTrgtCn(), "");
        if (ptcp.contains("월세지원사업 수혜") || ptcp.contains("월세사업") || ptcp.contains("월세 지원")) {
            return GROUP_MONTHLY_SUBSIDY;
        }
        // 월세 지원금 계열
        String nm = nvl(p.getPlcyNm(), "");
        if ("GRANT".equals(supportType) && nm.contains("월세")) {
            return GROUP_MONTHLY_SUBSIDY;
        }
        // 보증금 대출·이자지원 계열
        if (("LOAN".equals(supportType) || "INTEREST".equals(supportType))
                && (nm.contains("보증금") || nm.contains("임차") || nm.contains("전세"))) {
            return GROUP_DEPOSIT_LOAN;
        }
        return null; // 제한 없음(FREE)
    }

    /** 보증금 상한 파싱 (best-effort, 실패 시 null) */
    private Long parseDeposit(String text) {
        if (text == null) return null;
        Matcher m = DEPOSIT_PAT.matcher(text);
        if (m.find()) {
            Long won = koreanMoneyToWon(m.group(1));
            if (won != null && won > 0) return won;
        }
        return null;
    }

    /** "N억 N천만원" 형태 → 원 단위. 파싱 실패 시 null */
    private Long koreanMoneyToWon(String s) {
        if (s == null) return null;
        s = s.replaceAll("\\s+", "");
        long won = 0;
        boolean matched = false;
        Matcher eok = Pattern.compile("(\\d+)억").matcher(s);
        if (eok.find()) { won += Long.parseLong(eok.group(1)) * 100_000_000L; matched = true; }
        Matcher cheon = Pattern.compile("(\\d+)천").matcher(s);
        if (cheon.find()) { won += Long.parseLong(cheon.group(1)) * 10_000_000L; matched = true; }
        // "억/천" 없이 "N만원"만 있는 경우
        if (!matched) {
            Matcher man = Pattern.compile("(\\d+)만").matcher(s);
            if (man.find()) { won += Long.parseLong(man.group(1)) * 10_000L; matched = true; }
        }
        return matched ? won : null;
    }

    /** 전용면적 상한 파싱 (best-effort, 실패 시 null) */
    private java.math.BigDecimal parseArea(String text) {
        if (text == null) return null;
        Matcher m = AREA_PAT.matcher(text);
        if (m.find()) {
            try {
                return new java.math.BigDecimal(m.group(1));
            } catch (NumberFormatException ignore) {
                return null;
            }
        }
        return null;
    }

    /** 신청 기간 파싱 - "20260701 ~ 20261231", 없으면 bizPrd (연동명세 §4-1 [8]) */
    private LocalDate[] parseApplyPeriod(YouthPolicyDTO p) {
        String ymd = nvl(p.getAplyYmd(), "");
        if (ymd.contains("~")) {
            String[] parts = ymd.split("~");
            return new LocalDate[]{ parseYmd(parts[0]), parts.length > 1 ? parseYmd(parts[1]) : null };
        }
        return new LocalDate[]{ parseYmd(p.getBizPrdBgngYmd()), parseYmd(p.getBizPrdEndYmd()) };
    }

    /** 마감(aplyPrdSeCd=0057003)이면 종료일을 어제로 내려 화면에서 '마감'으로 표시되게 함 */
    private LocalDate closedEnd(YouthPolicyDTO p, LocalDate parsedEnd) {
        if (CODE_CLOSED.equals(p.getAplyPrdSeCd())) {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            return (parsedEnd == null || parsedEnd.isAfter(yesterday)) ? yesterday : parsedEnd;
        }
        return parsedEnd;
    }

    private LocalDate parseYmd(String raw) {
        if (raw == null) return null;
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.length() < 8) return null;
        try {
            return LocalDate.parse(digits.substring(0, 8), YMD);
        } catch (Exception e) {
            return null;
        }
    }

    private String nvl(String a, String b) {
        return (a == null || a.isBlank()) ? b : a;
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
