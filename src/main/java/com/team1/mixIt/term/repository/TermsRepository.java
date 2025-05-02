package com.team1.mixIt.term.repository;

import com.team1.mixIt.term.entity.Terms;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TermsRepository extends JpaRepository<Terms, Integer> {

    List<Terms> findByIdIn(List<Integer> ids);

    List<Terms> findByRequiredTrue();
}
