package com.nutriai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 商家/门店实体
 */
@Entity
@Table(name = "merchants", indexes = {
    @Index(name = "idx_merchant_status", columnList = "status"),
    @Index(name = "idx_merchant_owner", columnList = "owner_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 商家名称 */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /** 商家Logo */
    @Column(name = "logo", length = 500)
    private String logo;

    /** 联系电话 */
    @Column(name = "phone", length = 20)
    private String phone;

    /** 详细地址 */
    @Column(name = "address", length = 500)
    private String address;

    /** 纬度 */
    @Column(name = "latitude")
    private Double latitude;

    /** 经度 */
    @Column(name = "longitude")
    private Double longitude;

    /** 商家简介 */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** 营业时间 */
    @Column(name = "business_hours", length = 200)
    private String businessHours;

    /** 状态: ACTIVE/INACTIVE/SUSPENDED */
    @Builder.Default
    @Column(name = "status", length = 20)
    private String status = "ACTIVE";

    /** 商家类型: RESTAURANT/SHOP/BOTH */
    @Builder.Default
    @Column(name = "type", length = 20)
    private String type = "BOTH";

    /** 关联的用户ID(商家主) */
    @Column(name = "owner_id")
    private Long ownerId;

    /** 排序值 */
    @Builder.Default
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
