package com.study.ngy.domain.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByOrderByDeliveryDateAscCreatedAtDesc();
    List<Order> findByDeliveryDateBetweenOrderByDeliveryDateAsc(LocalDate start, LocalDate end);
    List<Order> findByDeliveryDateBetweenAndPaidTrueOrderByDeliveryDateAsc(LocalDate start, LocalDate end);
}
