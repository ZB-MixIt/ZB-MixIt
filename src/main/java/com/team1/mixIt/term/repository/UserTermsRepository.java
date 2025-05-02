package com.team1.mixIt.term.repository;

import com.team1.mixIt.term.entity.Terms;
import com.team1.mixIt.term.entity.UserTerms;
import com.team1.mixIt.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserTermsRepository extends JpaRepository<UserTerms, Long> {

    List<UserTerms> findByUserAndTermsIdIn(User user, List<Integer> termIds);

    @Query("SELECT ut.terms FROM UserTerms ut WHERE ut.user.id =: userId")
    List<Terms> findTermsByUserId(@Param("userId") Long userId);
}
