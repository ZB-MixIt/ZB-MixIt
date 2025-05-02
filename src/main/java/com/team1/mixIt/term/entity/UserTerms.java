package com.team1.mixIt.term.entity;

import com.team1.mixIt.common.entity.BaseEntity;
import com.team1.mixIt.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_terms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTerms extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id", nullable = false)
    private Terms terms;

    @Builder
    private UserTerms(User user, Terms terms) {
        this.user = user;
        this.terms = terms;
    }
}
