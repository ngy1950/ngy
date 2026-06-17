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

        List<Order> orders = orderRepository
                .findByDeliveryDateBetweenOrderByDeliveryDateAsc(
                        ym.atDay(1), ym.atEndOfMonth());

        Map<LocalDate, List<Order>> byDate = orders.stream()
                .collect(Collectors.groupingBy(Order::getDeliveryDate,
                        LinkedHashMap::new, Collectors.toList()));

        Set<Integer> orderDays = byDate.keySet().stream()
                .map(LocalDate::getDayOfMonth)
                .collect(Collectors.toSet());

        model.addAttribute("ym", ym);
        model.addAttribute("byDate", byDate);
        model.addAttribute("orderDays", orderDays);
        model.addAttribute("token", token);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("prevYm", ym.minusMonths(1));
        model.addAttribute("nextYm", ym.plusMonths(1));
        return "admin/orders-calendar";
    }
}
