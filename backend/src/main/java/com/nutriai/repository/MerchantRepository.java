package com.nutriai.repository;

import com.nutriai.entity.Merchant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    Page<Merchant> findAllByOrderBySortOrderAsc(Pageable pageable);

    Page<Merchant> findByStatusOrderBySortOrderAsc(String status, Pageable pageable);

    @Query("SELECT m FROM Merchant m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(m.address) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Merchant> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    List<Merchant> findByStatusOrderBySortOrderAsc(String status);

    Optional<Merchant> findByOwnerId(Long ownerId);

    long countByStatus(String status);
}
