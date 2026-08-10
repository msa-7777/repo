package com.sparta.productservice.domain.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository
        extends JpaRepository<Product, UUID>, ProductRepositoryCustom {

    // 상품 ID로 삭제되지 않은 상품을 단건 조회
    Optional<Product> findByIdAndDeletedAtIsNull(UUID id);

    // 삭제되지 않은 상품 목록을 페이징하여 조회
    // Page<Product> findAllByDeletedAtIsNull(Pageable pageable);

    // 동일 업체에 같은 이름의 삭제되지 않은 상품이 존재하는지 확인
    boolean existsByCompanyIdAndNameAndDeletedAtIsNull(
            UUID companyId,
            String name
    );

    // 현재 수정 중인 상품을 제외하고, 동일 업체에 같은 이름의 삭제되지 않은 상품이 존재하는지 확인
    // 상품 수정 시 자기 자신을 중복 상품으로 판단하지 않기 위해 사용 -> AndIdNot
    boolean existsByCompanyIdAndNameAndIdNotAndDeletedAtIsNull(
            UUID companyId,
            String name,
            UUID productId
    );
}