package com.study.ngy.domain.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAllByOrderByDeliveryDateAscCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + id));
    }

    @Transactional
    public void create(LocalDate deliveryDate, String menuDescription, boolean paid,
                       String trackingNumber, String recipientName,
                       String recipientPhone, String recipientAddress, String memo,
                       Integer price, String deliveryType) {
        Order order = new Order();
        order.setDeliveryDate(deliveryDate);
        order.setMenuDescription(menuDescription);
        order.setPaid(paid);
        order.setTrackingNumber(trackingNumber);
        order.setRecipientName(recipientName);
        order.setRecipientPhone(recipientPhone);
        order.setRecipientAddress(recipientAddress);
        order.setMemo(memo);
        order.setPrice(price);
        order.setDeliveryType(deliveryType != null ? deliveryType : "택배");
        orderRepository.save(order);
    }

    @Transactional
    public void update(Long id, LocalDate deliveryDate, String menuDescription, boolean paid,
                       String trackingNumber, String recipientName,
                       String recipientPhone, String recipientAddress, String memo,
                       Integer price, String deliveryType) {
        Order order = findById(id);
        order.setDeliveryDate(deliveryDate);
        order.setMenuDescription(menuDescription);
        order.setPaid(paid);
        order.setTrackingNumber(trackingNumber);
        order.setRecipientName(recipientName);
        order.setRecipientPhone(recipientPhone);
        order.setRecipientAddress(recipientAddress);
        order.setMemo(memo);
        order.setPrice(price);
        order.setDeliveryType(deliveryType != null ? deliveryType : "택배");
    }

    @Transactional
    public void togglePaid(Long id) {
        Order order = findById(id);
        order.setPaid(!order.isPaid());
    }

    @Transactional
    public void delete(Long id) {
        orderRepository.deleteById(id);
    }

    /** 이번 주·이번 달 매출 + 최근 6개월 월별 매출 (입금완료 주문만) */
    @Transactional(readOnly = true)
    public Map<String, Object> getSalesStats() {
        LocalDate today = LocalDate.now();

        LocalDate weekStart  = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd    = today.with(DayOfWeek.SUNDAY);
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd   = today.withDayOfMonth(today.lengthOfMonth());

        Map<String, Long> monthly = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            long sum = sumPaid(ym.atDay(1), ym.atEndOfMonth());
            monthly.put(ym.getYear() + "." + String.format("%02d", ym.getMonthValue()), sum);
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("thisWeek",  sumPaid(weekStart, weekEnd));
        stats.put("thisMonth", sumPaid(monthStart, monthEnd));
        stats.put("monthly",   monthly);
        return stats;
    }

    private long sumPaid(LocalDate start, LocalDate end) {
        return orderRepository
                .findByDeliveryDateBetweenAndPaidTrueOrderByDeliveryDateAsc(start, end)
                .stream()
                .filter(o -> o.getPrice() != null)
                .mapToLong(Order::getPrice)
                .sum();
    }
}
