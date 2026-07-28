package org.scoula.product.mapper;

import java.util.List;

import org.scoula.product.domain.PolicyProductVO;
import org.scoula.product.domain.SavingProductVO;

public interface ProductMapper {
    // productType(DEPOSIT/SAVING) 기준 판매중인 예적금 상품 목록 조회
    List<SavingProductVO> findSavingProductListByType(String productType);

    // fin_prdt_cd 기준 상세 조회 — save_trm(가입기간)별로 여러 행이 나온다 (상세페이지에서 기간 상관없이 최소~최대로 집계)
    List<SavingProductVO> findSavingProductDetailByFinPrdtCd(String finPrdtCd);

    // 판매중인 정책 상품 목록 조회
    List<PolicyProductVO> findPolicyProductList();

    // policy_id 기준 정책 상품 상세 조회
    PolicyProductVO findPolicyProductDetail(Long policyId);

    // FSS API 동기화 결과 UPSERT (fin_prdt_cd + save_trm 기준).
    // is_tax_exempt/gov_match_rate/product_link/min_limit 은 UPDATE 대상에서 제외해 기존 수동 입력값을 보존한다.
    int upsertSavingProduct(SavingProductVO vo);

    // 현재 DB에 존재하는 모든 fin_prdt_cd 목록 (신규 상품 판별용)
    List<String> findAllFinPrdtCds();
}
