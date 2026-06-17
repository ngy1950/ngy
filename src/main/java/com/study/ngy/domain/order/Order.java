package com.study.ngy.domain.order;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate deliveryDate;

    @Column(columnDefinition = "TEXT")
    private String menuDescription;

    @Column(nullable = false)
    private boolean paid = false;

    @Column(length = 100)
    private String trackingNumber;

    @Column(nullable = false, length = 100)
    private String recipientName;

    @Column(nullable = false, length = 30)
    private String recipientPhone;

    @Column(columnDefinition = "TEXT")
    private String recipientAddress;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column
    private Integer price;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
