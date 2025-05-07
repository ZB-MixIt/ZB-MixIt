package com.team1.mixIt.term.service;

import com.team1.mixIt.common.code.ResponseCode;
import com.team1.mixIt.common.exception.ClientException;
import com.team1.mixIt.term.entity.Terms;
import com.team1.mixIt.term.repository.TermsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TermsService {

    private final TermsRepository termsRepository;

    public List<Terms> getAllTerms() {
        return termsRepository.findAll();
    }

    public void checkIsAllRequiredTermsAccepted(List<Integer> termsIds) {
        List<Terms> requiredTerms = termsRepository.findByRequiredTrue();

        Set<Integer> termsSet = new HashSet<>(termsIds);

        if (termsSet.containsAll(requiredTerms.stream().map(Terms::getId).toList())) throw new ClientException(ResponseCode.REQUIRED_TERMS_NOT_PROVIDED);
    }
}
