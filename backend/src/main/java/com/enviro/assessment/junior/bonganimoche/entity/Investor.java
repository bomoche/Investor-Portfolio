package com.enviro.assessment.junior.bonganimoche.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * An investor in the Enviro365 platform.
 *
 * This entity doubles as the authentication principal — email and passwordHash
 * live here rather than in a separate User entity. For a system of this scope
 * every authenticated user is an investor, so splitting them would add a join
 * and a mapping layer without buying anything.
 */
@Entity
@Table(name = "investors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Investor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    /** Natural login identifier — unique and indexed. */
    @Column(nullable = false, unique = true, length = 120)
    private String email;

    /** BCrypt hash. Never exposed through any DTO. */
    @Column(nullable = false)
    private String passwordHash;

    /**
     * Stored rather than storing age directly, so the retirement eligibility
     * rule stays correct as time passes instead of going stale.
     */
    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Products are the owned side of the relationship from the investor's view.
     * LAZY because most queries touching an investor don't need the products;
     * the portfolio endpoint fetches them explicitly.
     */
    @OneToMany(mappedBy = "investor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /** Derived, never stored. Used by the retirement withdrawal rule. */
    @Transient
    public int getAge() {
        return Period.between(this.dateOfBirth, LocalDate.now()).getYears();
    }

    @Transient
    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }
}