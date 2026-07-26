package org.scoula.product.service;

import org.scoula.product.dto.SavingProductListResponseDTO;

import java.util.List;

public interface ProductService {
    // category: deposits(예금) | savings(적금)
    List<SavingProductListResponseDTO> findSavingProductListByCategory(String category);
}
