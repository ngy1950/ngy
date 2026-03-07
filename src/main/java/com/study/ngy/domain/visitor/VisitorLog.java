package com.study.ngy.domain.visitor;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "visitor_log", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"date", "page"})
})
@Getter
@NoArgsConstructor
public class VisitorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, length = 100)
    private String page;

    @Column(nullable = false)
    private int count;

    public VisitorLog(LocalDate date, String page) {
        this.date = date;
        this.page = page;
        this.count = 1;
    }

    public void increment() {
        this.count++;
    }
}
