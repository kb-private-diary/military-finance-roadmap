// product 도메인 axios 호출 — 예적금/정책 상품 목록 조회
import instance from '@/api'; // api/index.js

const BASE_URL = '/api/products';

export default {
  // 예적금 상품 목록 조회 (category: deposits(예금) | savings(적금))
  async findSavingProductList(category) {
    const { data } = await instance.get(`${BASE_URL}/savings/${category}`);
    return data.data; // ApiResponse<List<SavingProductListResponseDTO>> 래핑 해제
  },

  // 예적금 상품 상세 조회 (productId = fin_prdt_cd)
  async findSavingProductDetail(productId) {
    const { data } = await instance.get(
      `${BASE_URL}/saving-details/${productId}`,
    );
    return data.data; // ApiResponse<SavingProductDetailResponseDTO> 래핑 해제
  },

  // 정책 상품 목록 조회
  async findPolicyProductList() {
    const { data } = await instance.get(`${BASE_URL}/policies`);
    return data.data; // ApiResponse<List<PolicyProductListResponseDTO>> 래핑 해제
  },
};
