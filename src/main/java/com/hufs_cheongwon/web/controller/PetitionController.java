package com.hufs_cheongwon.web.controller;

import com.hufs_cheongwon.common.security.CustomUserDetails;
import com.hufs_cheongwon.domain.Agreement;
import com.hufs_cheongwon.domain.Petition;
import com.hufs_cheongwon.domain.Report;
import com.hufs_cheongwon.domain.Response;
import com.hufs_cheongwon.domain.enums.PetitionStatus;
import com.hufs_cheongwon.repository.AgreementRepository;
import com.hufs_cheongwon.repository.PetitionRepository;
import com.hufs_cheongwon.repository.ResponseRepository;
import com.hufs_cheongwon.service.PetitionService;
import com.hufs_cheongwon.service.PetitionStatService;
import com.hufs_cheongwon.web.apiResponse.ApiResponse;
import com.hufs_cheongwon.web.apiResponse.success.SuccessStatus;
import com.hufs_cheongwon.web.dto.PetitionSortType;
import com.hufs_cheongwon.web.dto.request.PetitionCreateRequest;
import com.hufs_cheongwon.web.dto.response.*;
import jakarta.validation.Valid;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/petitions")
@RequiredArgsConstructor
public class PetitionController {

    private final PetitionRepository petitionRepository;
    private final PetitionService petitionService;
    private final PetitionStatService petitionStatService;
    private final AgreementRepository agreementRepository;
    private final ResponseRepository responseRepository;

    /**
     * 청원 목록 조회
     */
    @GetMapping
    public ApiResponse<Page<PetitionResponse>> getPetitions(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Petition> petitions = petitionRepository.findAll(pageable);
        Page<PetitionResponse> petitionPage = petitions.map(PetitionResponse::from);
        return ApiResponse.onSuccess(SuccessStatus.PETITION_RETRIEVED, petitionPage);
    }

    /**
     * 최근 청원 목록 조회
     */
    @GetMapping("/recent")
    public ApiResponse<Page<PetitionResponse>> getRecentPetitions(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size",defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Petition> petitions = petitionRepository.findAllByOrderByCreatedAtDesc(pageable);
        Page<PetitionResponse> petitionPage = petitions.map(PetitionResponse::from);
        return ApiResponse.onSuccess(SuccessStatus.RECENT_PETITIONS_RETRIEVED, petitionPage);
    }

    /**
     * 인기 청원 목록 조회 - 동의 수 기준
     */
    @GetMapping("/popular")
    public ApiResponse<Page<PetitionResponse>> getPopularPetitions(
            @RequestParam(name = "page",defaultValue = "0") int page,
            @RequestParam(name = "size",defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Petition> petitions = petitionRepository.findAllByOrderByAgreeCountDesc(pageable);
        Page<PetitionResponse> petitionPage = petitions.map(PetitionResponse::from);
        return ApiResponse.onSuccess(SuccessStatus.POPULAR_PETITIONS_RETRIEVED, petitionPage);
    }

    /**
     * 상태별 청원 목록 조회
     */
    @GetMapping("/status/{status}")
    public ApiResponse<Page<PetitionResponse>> getPetitionsByStatus(
            @PathVariable(name = "status") PetitionStatus status,
            @RequestParam(name = "page",defaultValue = "0") int page,
            @RequestParam(name = "size",defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Petition> petitions = petitionRepository.findByPetitionStatusOrderByCreatedAtDesc(status, pageable);
        Page<PetitionResponse> petitionPage = petitions.map(PetitionResponse::from);
        return ApiResponse.onSuccess(SuccessStatus.PETITION_RETRIEVED, petitionPage);
    }

    /**
     * 특정 청원 상세 조회
     */
    @GetMapping("/{petition_id}/view")
    public ApiResponse<PetitionResponse> getPetitionById(@PathVariable (name = "petition_id")Long id) {

        Petition petition = petitionService.getPetitionById(id);

        Optional<Response> responseOpt = responseRepository.findOptionalByPetitionId(id);

        if (responseOpt.isPresent()) {
            Response response = responseOpt.get();
            return ApiResponse.onSuccess(SuccessStatus.PETITION_RETRIEVED,
                    PetitionResponse.from(petition, AnswerResponse.from(response, response.getAdmin())));
        } else {
            return ApiResponse.onSuccess(SuccessStatus.PETITION_RETRIEVED,
                    PetitionResponse.from(petition));
        }
    }

    /**
     * 키워드로 청원 검색
     */
    @GetMapping("/search")
    public ApiResponse<List<PetitionResponse>> searchPetitions(
            @RequestParam (name = "keyword")String keyword,
            @RequestParam(required = false, name = "status") PetitionStatus status) {

        PetitionStatus searchStatus = (status != null) ? status : PetitionStatus.ONGOING;
        List<Petition> petitions = petitionRepository.searchPetitionsByKeywordAndStatus(keyword, searchStatus);
        List<PetitionResponse> petitionResponses = new ArrayList<>();
        for (Petition petition : petitions) {
            petitionResponses.add(PetitionResponse.from(petition));
        }
        return ApiResponse.onSuccess(SuccessStatus.PETITION_SEARCH_SUCCESS, petitionResponses);
    }

    /**
     * 진행 중인 청원 목록 조회
     */
    @GetMapping("/ongoing")
    public ApiResponse<Page<PetitionResponse>> getOngoingPetitions(
            @RequestParam(name = "page",defaultValue = "0") int page,
            @RequestParam(name = "size",defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt") @Valid PetitionSortType sortBy) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Petition> petitions;

        if (sortBy == PetitionSortType.AGREE_COUNT) {
            petitions = petitionRepository.findAllOngoingPetitionsByAgreeCount(pageable);
        } else {
            petitions = petitionRepository.findAllOngoingPetitions(pageable);
        }

        Page<PetitionResponse> petitionPage = petitions.map(PetitionResponse::from);
        return ApiResponse.onSuccess(SuccessStatus.ONGOING_PETITIONS_RETRIEVED, petitionPage);
    }

    /**
     * 만료된 청원 목록 조회
     */
    @GetMapping("/expired")
    public ApiResponse<Page<PetitionResponse>> getExpiredPetitions(
            @RequestParam(name = "page",defaultValue = "0") int page,
            @RequestParam(name = "size",defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt") @Valid PetitionSortType sortBy) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Petition> petitions;

        if (sortBy == PetitionSortType.AGREE_COUNT) {
            petitions = petitionRepository.findAllExpiredPetitionsByAgreeCount(pageable);
        } else {
            petitions = petitionRepository.findAllExpiredPetitions(pageable);
        }

        Page<PetitionResponse> petitionPage = petitions.map(PetitionResponse::from);
        return ApiResponse.onSuccess(SuccessStatus.EXPIRED_PETITIONS_RETRIEVED, petitionPage);
    }

    /**
     * 대기 중인 청원 목록 조회
     */
    @GetMapping("/waiting")
    public ApiResponse<Page<PetitionResponse>> getWaitingPetitions(
            @RequestParam(name = "page",defaultValue = "0") int page,
            @RequestParam(name = "size",defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt") @Valid PetitionSortType sortBy) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Petition> petitions;

        if (sortBy == PetitionSortType.AGREE_COUNT) {
            petitions = petitionRepository.findAllWaitingPetitionsByAgreeCount(pageable);
        } else {
            petitions = petitionRepository.findAllWaitingPetitions(pageable);
        }

        Page<PetitionResponse> petitionPage = petitions.map(PetitionResponse::from);
        return ApiResponse.onSuccess(SuccessStatus.WAITING_PETITIONS_RETRIEVED, petitionPage);
    }

    /**
     * 답변 완료된 청원 목록 조회
     */
    @GetMapping("/answered")
    public ApiResponse<Page<PetitionResponse>> getAnsweredPetitions(
            @RequestParam(name = "page",defaultValue = "0") int page,
            @RequestParam(name = "size",defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt") @Valid PetitionSortType sortBy) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Petition> petitions;

        if (sortBy == PetitionSortType.AGREE_COUNT) {
            petitions = petitionRepository.findAllAnsweredPetitionsByAgreeCount(pageable);
        } else {
            petitions = petitionRepository.findAllAnsweredPetitions(pageable);
        }

        Page<PetitionResponse> petitionPage = petitions.map(PetitionResponse::from);
        return ApiResponse.onSuccess(SuccessStatus.ANSWERED_PETITIONS_RETRIEVED, petitionPage);
    }

    /**
     * 청원 등록
     */
    @PostMapping("/new")
    public ApiResponse<PetitionResponse> createPetition(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody PetitionCreateRequest petitionCreateRequest
    ) {
        return ApiResponse.onSuccess(
                SuccessStatus.PETITION_CREATED, PetitionResponse.from(petitionService.createPetition(
                        petitionCreateRequest, customUserDetails.getUser().getId()
                )));
    }

    /**
     * 청원 동의
     */
    @PostMapping("/{petition_id}/agree")
    public ApiResponse<AgreementResponse> agreePetition(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable("petition_id") Long petitionId
    ){
        Agreement agreement = petitionService.agreePetition(petitionId, customUserDetails.getUser().getId());
        return ApiResponse.onSuccess(SuccessStatus.PETITION_AGREE_SUCCESS, AgreementResponse.from(agreement));
    }

    /**
     * 동의 내용 조회
     */
    @GetMapping("/{petition_id}/agreements")
    public ApiResponse<Page<AgreementResponse>> getAgreements(
            @PathVariable("petition_id") Long petitionId,
            @RequestParam(name = "page",defaultValue = "0") int page,
            @RequestParam(name = "size",defaultValue = "10") int size

    ){
        Pageable pageable = PageRequest.of(page, size);
        Page<Agreement> agreements = agreementRepository.findByPetitionId(petitionId, pageable);
        Page<AgreementResponse> agreementResponses = agreements.map(AgreementResponse::from);
        return ApiResponse.onSuccess(SuccessStatus.PETITION_AGREEMENTS_RETRIEVED, agreementResponses);
    }


    /**
     * 청원 신고
     */
    @PostMapping("/{petition_id}/report")
    public ApiResponse<ReportResponse> reportPetition(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable("petition_id") Long petitionId
    ){
        Report report = petitionService.reportPetition(petitionId, customUserDetails.getUser().getId());
        return ApiResponse.onSuccess(SuccessStatus.PETITION_REPORTED, ReportResponse.from(report));
    }

    /**
     * 청원 통계 조회
     */
    @GetMapping("/stat")
    public ApiResponse<PetitionStatsDto> getPetitionStats(){
        return ApiResponse.onSuccess(SuccessStatus.PETITION_STATS_RETRIEVED ,petitionStatService.getPetitionStats());
    }

    @PostMapping("/{petition_id}/bookmark")
    public ApiResponse<PetitionBookmarkResponse> togglePetitionBookmark(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable("petition_id") Long petitionId
    ) {
        return ApiResponse.onSuccess(SuccessStatus.PETITION_BOOKMARKED,
                petitionService.togglePetitionBookmark(customUserDetails.getUser().getId(), petitionId));
    }

    @GetMapping("/bookmark")
    public ApiResponse<Page<PetitionResponse>> getBookmarkedPetitions(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PetitionResponse> responses = petitionService.getBookmarkedPetitions(customUserDetails.getUser(), pageable);
        return ApiResponse.onSuccess(SuccessStatus.PETITION_BOOKMARKS_RETRIEVED, responses);
    }
}
