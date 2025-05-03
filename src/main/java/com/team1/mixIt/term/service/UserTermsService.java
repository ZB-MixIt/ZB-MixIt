package com.team1.mixIt.term.service;

import com.team1.mixIt.common.code.ResponseCode;
import com.team1.mixIt.common.exception.ClientException;
import com.team1.mixIt.term.entity.Terms;
import com.team1.mixIt.term.entity.UserTerms;
import com.team1.mixIt.term.repository.TermsRepository;
import com.team1.mixIt.term.repository.UserTermsRepository;
import com.team1.mixIt.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserTermsService {

    private final UserTermsRepository userTermsRepository;
    private final TermsRepository termsRepository;

    public void checkRequiredTerms(List<Integer> termsIds) {
        List<Terms> requiredTerms = termsRepository.findByRequiredTrue();

        Set<Integer> termsSet = new HashSet<>(termsIds);

        if (!termsSet.containsAll(requiredTerms.stream().map(Terms::getId).toList())) throw new ClientException(ResponseCode.REQUIRED_TERMS_NOT_PROVIDED);
    }

    public List<Terms> getAgreedTerms(User user) {
        return userTermsRepository.findTermsByUserId(user.getId());
    }

    public void agreeTerms(List<Integer> termsIds, User user) {
        List<Terms> foundTerms = termsRepository.findByIdIn(termsIds);
        if (termsIds.size() != foundTerms.size()) throw new ClientException(ResponseCode.TERMS_NOT_FOUND);;

        List<UserTerms> existing = userTermsRepository.findByUserAndTermsIdIn(user, termsIds);

        Set<Integer> alreadyAgreedIds = existing.stream()
                .map(ut -> ut.getTerms().getId())
                .collect(Collectors.toSet());

        List<UserTerms> newUserTerms = foundTerms.stream()
                .filter(term -> !alreadyAgreedIds.contains(term.getId()))
                .map(term -> UserTerms.builder()
                        .user(user)
                        .terms(term)
                        .build())
                .toList();

        userTermsRepository.saveAll(newUserTerms);
    }

    public void disagreeTerms(List<Integer> termsIds, User user) {
        List<Terms> foundTerms = termsRepository.findByIdIn(termsIds);
        if (termsIds.size() != foundTerms.size()) throw new ClientException(ResponseCode.TERMS_NOT_FOUND);

        List<UserTerms> userTerms = userTermsRepository.findByUserAndTermsIdIn(user, termsIds);
        userTermsRepository.deleteAll(userTerms);
    }
}
