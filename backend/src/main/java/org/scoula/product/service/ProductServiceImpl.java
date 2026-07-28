package org.scoula.product.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.scoula.common.exception.BusinessException;
import org.scoula.product.client.FssApiClient;
import org.scoula.product.domain.SavingProductVO;
import org.scoula.product.dto.PolicyProductListResponseDTO;
import org.scoula.product.dto.ProductSyncItemDTO;
import org.scoula.product.dto.ProductSyncResponseDTO;
import org.scoula.product.dto.SavingProductDetailResponseDTO;
import org.scoula.product.dto.SavingProductListResponseDTO;
import org.scoula.product.mapper.ProductMapper;

@Log4j2
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final String CATEGORY_DEPOSITS = "deposits";
    private static final String CATEGORY_SAVINGS = "savings";
    private static final String PRODUCT_TYPE_DEPOSIT = "DEPOSIT";
    private static final String PRODUCT_TYPE_SAVING = "SAVING";

    private final ProductMapper mapper;
    private final FssApiClient fssApiClient;

    @Override
    @Transactional(readOnly = true)
    public List<SavingProductListResponseDTO> findSavingProductListByCategory(String category) {
        String productType = this.toProductType(category);
        return this.mapper.findSavingProductListByType(productType).stream()
                .map(SavingProductListResponseDTO::of)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SavingProductDetailResponseDTO findSavingProductDetail(String productId) {
        List<SavingProductVO> rows = this.mapper.findSavingProductDetailByFinPrdtCd(productId);
        if (rows.isEmpty()) {
            throw BusinessException.notFound("상품을 찾을 수 없습니다.", "PRODU_003");
        }
        return SavingProductDetailResponseDTO.of(rows);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PolicyProductListResponseDTO> findPolicyProductList() {
        return this.mapper.findPolicyProductList().stream()
                .map(PolicyProductListResponseDTO::of)
                .toList();
    }

    @Override
    @Transactional
    public ProductSyncResponseDTO syncSavingProducts() {
        Set<String> existingCodes = new HashSet<>(this.mapper.findAllFinPrdtCds());

        List<SavingProductVO> savings = this.fssApiClient.fetchKbProducts(PRODUCT_TYPE_SAVING);
        List<SavingProductVO> deposits = this.fssApiClient.fetchKbProducts(PRODUCT_TYPE_DEPOSIT);

        // API에 없는 필드(product_link/is_tax_exempt/gov_match_rate 등)를 사람이 채우기 전까지는
        // 목록 조회 API에 노출되면 안 되므로, DB에 한 번도 없던 상품코드는 is_active 를 강제로 FALSE 처리한다.
        // (save_trm 별로 여러 행이 오므로, 상품코드 단위로 먼저 신규 여부를 판단해야 일부 행만 빠지지 않는다.)
        Set<String> newCodes = Stream.concat(savings.stream(), deposits.stream())
                .map(SavingProductVO::getFinPrdtCd)
                .filter(code -> !existingCodes.contains(code))
                .collect(Collectors.toSet());

        for (String code : newCodes) {
            log.warn("신규 상품 발견 (수동 검토 후 is_active=TRUE 필요): fin_prdt_cd={}", code);
        }

        for (SavingProductVO vo : savings) {
            this.upsertOne(vo, newCodes);
        }
        for (SavingProductVO vo : deposits) {
            this.upsertOne(vo, newCodes);
        }

        log.info("KB 예·적금 상품 동기화 완료: savings={}, deposits={}, 신규상품={}",
                savings.size(), deposits.size(), newCodes.size());

        // 응답엔 신규 상품만 담는다 — 기존 상품 금리 갱신은 일상적인 동작이라 사람이 볼 필요 없고,
        // 신규 상품만 is_active=FALSE로 숨겨져 있어 실제로 검토/조치가 필요하기 때문이다.
        List<ProductSyncItemDTO> newProducts = Stream.concat(savings.stream(), deposits.stream())
                .filter(vo -> newCodes.contains(vo.getFinPrdtCd()))
                .map(ProductSyncItemDTO::of)
                .toList();

        return ProductSyncResponseDTO.builder()
                .savingsCount(savings.size())
                .depositsCount(deposits.size())
                .newProductCount(newCodes.size())
                .newProducts(newProducts)
                .build();
    }

    private void upsertOne(SavingProductVO vo, Set<String> newCodes) {
        SavingProductVO toSave = newCodes.contains(vo.getFinPrdtCd())
                ? vo.toBuilder().isActive(false).build()
                : vo;
        this.mapper.upsertSavingProduct(toSave);
    }

    private String toProductType(String category) {
        if (CATEGORY_DEPOSITS.equals(category)) {
            return PRODUCT_TYPE_DEPOSIT;
        }
        if (CATEGORY_SAVINGS.equals(category)) {
            return PRODUCT_TYPE_SAVING;
        }
        throw BusinessException.badRequest("유효하지 않은 상품 카테고리입니다.", "PRODU_001");
    }
}
