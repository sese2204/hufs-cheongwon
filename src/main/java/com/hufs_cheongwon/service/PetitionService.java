package com.hufs_cheongwon.service;

import com.hufs_cheongwon.common.exception.BusinessException;
import com.hufs_cheongwon.common.exception.InvalidStateException;
import com.hufs_cheongwon.common.exception.ResourceNotFoundException;
import com.hufs_cheongwon.domain.*;
import com.hufs_cheongwon.domain.enums.PetitionStatus;
import com.hufs_cheongwon.repository.*;
import com.hufs_cheongwon.web.apiResponse.error.ErrorStatus;
import com.hufs_cheongwon.web.dto.request.PetitionCreateRequest;
import com.hufs_cheongwon.web.dto.response.PetitionResponse;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PetitionService {

    private final PetitionRepository petitionRepository;
    private final UsersRepository usersRepository;
    private final AgreementRepository agreementRepository;
    private final ReportRepository reportRepository;

    /**
     * 특정 청원 상세 조회 + 조회수 증가
     */
    @Transactional
    public Petition getPetitionById(Long id) {
        Petition petition = petitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorStatus.PETITION_NOT_FOUND));

        petition.addViewCount(1);

        return petition;
    }

    /**
     * 청원 작성
     */
    @Transactional
    public Petition createPetition(PetitionCreateRequest petitionRequest, Long userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorStatus.USER_NOT_FOUND));

        // 사용자의 마지막 청원 작성 시간 확인
        Optional<Petition> lastPetition = petitionRepository.findTopByUsersIdOrderByCreatedAtDesc(userId);

        if (lastPetition.isPresent()) {
            LocalDateTime lastPetitionTime = lastPetition.get().getCreatedAt();
            LocalDateTime currentTime = LocalDateTime.now();

            // 마지막 청원 후 7일이 지나지 않았다면 예외 발생
            if (ChronoUnit.DAYS.between(lastPetitionTime, currentTime) < 7) {
                throw new BusinessException(ErrorStatus.PETITION_TOO_FREQUENT);
            }
        }

        Petition petition = Petition.builder()
                .user(user)
                .title(petitionRequest.getTitle())
                .category(petitionRequest.getCategory())
                .content(petitionRequest.getContent())
                .petitionStatus(PetitionStatus.ONGOING) // 기본 상태는 ONGOING
                .build();

        if (petitionRequest.getLinks() != null && !petitionRequest.getLinks().isEmpty()) {
            for (String linkStr : petitionRequest.getLinks()) {
                Link link = Link.builder()
                        .link(linkStr)
                        .petition(petition)
                        .build();
                petition.addLink(link); // Petition에 추가
            }
        }
        return petitionRepository.save(petition);
    }

    /**
     * 청원 동의하기
     */
    @Transactional
    public Agreement agreePetition(Long petitionId, Long userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorStatus.USER_NOT_FOUND));

        Petition petition = petitionRepository.findById(petitionId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorStatus.PETITION_NOT_FOUND));

        // 진행 중인 청원만 동의 가능
        if (petition.getPetitionStatus() != PetitionStatus.ONGOING) {
            throw new InvalidStateException(ErrorStatus.PETITION_NOT_ONGOING);
        }

        // 이미 동의한 청원인지 확인
        if (hasUserAgreedPetition(userId, petitionId)) {
            throw new InvalidStateException(ErrorStatus.ALREADY_AGREED);
        }

        // 자신의 청원에는 동의할 수 없음
        if (petition.getUsers().getId().equals(userId)) {
            throw new InvalidStateException(ErrorStatus.SELF_AGREEMENT_NOT_ALLOWED);
        }

        Agreement agreement = Agreement.builder()
                .users(user)
                .petition(petition)
                .build();

        return agreementRepository.save(agreement);
    }

    /**
     * 청원 신고하기
     */
    @Transactional
    public Report reportPetition(Long petitionId, Long userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorStatus.USER_NOT_FOUND));

        Petition petition = petitionRepository.findById(petitionId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorStatus.PETITION_NOT_FOUND));

        // 이미 신고한 청원인지 확인
        if (hasUserReportedPetition(userId, petitionId)) {
            throw new InvalidStateException(ErrorStatus.ALREADY_REPORTED);
        }

        // 자신의 청원에는 신고할 수 없음
        if (petition.getUsers().getId().equals(userId)) {
            throw new InvalidStateException(ErrorStatus.SELF_REPORT_NOT_ALLOWED);
        }

        Report report = Report.builder()
                .users(user)
                .petition(petition)
                .build();

        return reportRepository.save(report);
    }

    /**
     * 사용자가 청원에 동의했는지 확인
     */
    public boolean hasUserAgreedPetition(Long userId, Long petitionId) {
        return agreementRepository.existsByUsersIdAndPetitionId(userId, petitionId);
    }

    /**
     * 사용자가 청원을 신고했는지 확인
     */
    public boolean hasUserReportedPetition(Long userId, Long petitionId) {
        return reportRepository.existsByUsersIdAndPetitionId(userId, petitionId);
    }

    /**
     * 관리자가 청원 삭제
     */
    @Transactional
    public PetitionResponse deletePetition(Long petitionId) {
        Petition petition = petitionRepository.findById(petitionId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorStatus.PETITION_NOT_FOUND));
        petitionRepository.deleteById(petitionId);
        return PetitionResponse.from(petition);
    }
}
