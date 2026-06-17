package com.study.ngy.domain.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

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
                       String recipientPhone, String recipientAddress, String memo) {
        Order order = new Order();
        order.setDeliveryDate(deliveryDate);
        order.setMenuDescription(menuDescription);
        order.setPaid(paid);
        order.setTrackingNumber(trackingNumber);
        order.setRecipientName(recipientName);
        order.setRecipientPhone(recipientPhone);
        order.setRecipientAddress(recipientAddress);
        order.setMemo(memo);
        orderRepository.save(order);
    }

    @Transactional
    public void update(Long id, LocalDate deliveryDate, String menuDescription, boolean paid,
                       String trackingNumber, String recipientName,
                       String recipientPhone, String recipientAddress, String memo) {
        Order order = findById(id);
        order.setDeliveryDate(deliveryDate);
        order.setMenuDescription(menuDescription);
        order.setPaid(paid);
        order.setTrackingNumber(trackingNumber);
        order.setRecipientName(recipientName);
        order.setRecipientPhone(recipientPhone);
        order.setRecipientAddress(recipientAddress);
        order.setMemo(memo);
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
}
