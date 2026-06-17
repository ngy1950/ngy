package com.study.ngy.web;

import com.study.ngy.domain.order.Order;
import com.study.ngy.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class OrderViewController {

    @Value("${order.view.token}")
    private String viewToken;

    private final OrderRepository orderRepository;

    @GetMapping("/schedule/{token}")
    public String calendarView(@PathVariable String token,
                               @RequestParam(required = false) Integer year,
                               @RequestParam(required = false) Integer month,
                               Model model) {
        if (!token.equals(viewToken)) {
            return "redirect:/";
        }

        YearMonth ym = (year != null && month != null)
                ? YearMonth.of(year, month) : YearMonth.now();

        LocalDate todayDate = LocalDate.now();

        // 이번 달이면 오늘 이전 날짜 주문 제외, 과거/미래 달이면 전부 표시
        List<Order> orders = orderRepository
                .findByDeliveryDateBetweenOrderByDeliveryDateAsc(ym.atDay(1), ym.atEndOfMonth())
                .stream()
                .filter(o -> !ym.equals(YearMonth.now()) || !o.getDeliveryDate().isBefore(todayDate))
                .collect(Collectors.toList());

        Map<LocalDate, List<Order>> byDate = orders.stream()
                .collect(Collectors.groupingBy(Order::getDeliveryDate,
                        LinkedHashMap::new, Collectors.toList()));

        Set<Integer> orderDays = byDate.keySet().stream()
                .map(LocalDate::getDayOfMonth)
                .collect(Collectors.toSet());

        Set<Integer> directDeliveryDays = byDate.entrySet().stream()
                .filter(e -> e.getValue().stream()
                        .anyMatch(o -> "직접배송".equals(o.getDeliveryType())))
                .map(e -> e.getKey().getDayOfMonth())
                .collect(Collectors.toSet());

        List<Order> todayActionOrders = orders.stream()
                .filter(o -> getEffectiveActionDate(o).equals(todayDate))
                .collect(Collectors.toList());

        // 오늘 처리 주문을 byDate에서 제거해 중복 표시 방지
        // orderDays는 달력 셀 강조용이므로 원본 유지
        if (!todayActionOrders.isEmpty()) {
            Set<Long> todayIds = todayActionOrders.stream()
                    .map(Order::getId)
                    .collect(Collectors.toSet());
            Map<LocalDate, List<Order>> filteredByDate = new LinkedHashMap<>();
            for (Map.Entry<LocalDate, List<Order>> entry : byDate.entrySet()) {
                List<Order> remaining = entry.getValue().stream()
                        .filter(o -> !todayIds.contains(o.getId()))
                        .collect(Collectors.toList());
                if (!remaining.isEmpty()) {
                    filteredByDate.put(entry.getKey(), remaining);
                }
            }
            byDate = filteredByDate;
        }

        model.addAttribute("ym", ym);
        model.addAttribute("byDate", byDate);
        model.addAttribute("orderDays", orderDays);
        model.addAttribute("directDeliveryDays", directDeliveryDays);
        model.addAttribute("todayActionOrders", todayActionOrders);
        model.addAttribute("token", token);
        model.addAttribute("today", todayDate);
        model.addAttribute("prevYm", ym.minusMonths(1));
        model.addAttribute("nextYm", ym.plusMonths(1));
        return "admin/orders-calendar";
    }

    private LocalDate getEffectiveActionDate(Order o) {
        if ("직접배송".equals(o.getDeliveryType())) return o.getDeliveryDate();
        DayOfWeek dow = o.getDeliveryDate().getDayOfWeek();
        if (dow == DayOfWeek.SUNDAY)  return o.getDeliveryDate().minusDays(2);
        if (dow == DayOfWeek.MONDAY)  return o.getDeliveryDate().minusDays(3);
        return o.getDeliveryDate().minusDays(1);
    }
}
