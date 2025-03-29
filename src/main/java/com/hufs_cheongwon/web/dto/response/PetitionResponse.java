package com.hufs_cheongwon.web.dto.response;

import com.hufs_cheongwon.common.Constant;
import com.hufs_cheongwon.common.Util;
import com.hufs_cheongwon.domain.Link;
import com.hufs_cheongwon.domain.Petition;
import com.hufs_cheongwon.domain.enums.Category;
import com.hufs_cheongwon.domain.enums.PetitionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.*;

@Schema(description = "청원 응답 DTO")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PetitionResponse {

    private Long id;
    private String title;
    private Category category;
    private String content;
    private PetitionStatus petitionStatus;
    private Integer agreeCount;
    private Integer viewCount;
    private Integer reportCount;
    private String writerEmail;
    private List<String> links;
    private LocalDate createDate;
    private LocalDate endDate;
    private AnswerResponse answerResponse;

    public static PetitionResponse from(Petition petition) {
        List<String> linkList = new ArrayList<>();
        if (petition.getLinks() != null) {
            for (Link link : petition.getLinks()) {
                linkList.add(link.getLink());
            }
        }

        return PetitionResponse.builder()
                .id(petition.getId())
                .title(petition.getTitle())
                .category(petition.getCategory())
                .content(petition.getContent())
                .petitionStatus(petition.getPetitionStatus())
                .agreeCount(petition.getAgreeCount())
                .viewCount(petition.getViewCount())
                .reportCount(petition.getReportCount())
                .writerEmail(Util.maskEmail(petition.getUsers().getEmail()))
                .links(linkList)
                .createDate(petition.getCreatedAt().toLocalDate())
                .endDate(petition.getCreatedAt().toLocalDate().plus(Constant.PETITION_ACTIVE_PERIOD))
                .answerResponse(null)
                .build();
    }

    public static PetitionResponse from(Petition petition, AnswerResponse answerResponse) {
        List<String> linkList = new ArrayList<>();
        if (petition.getLinks() != null) {
            for (Link link : petition.getLinks()) {
                linkList.add(link.getLink());
            }
        }

        return PetitionResponse.builder()
                .id(petition.getId())
                .title(petition.getTitle())
                .category(petition.getCategory())
                .content(petition.getContent())
                .petitionStatus(petition.getPetitionStatus())
                .agreeCount(petition.getAgreeCount())
                .viewCount(petition.getViewCount())
                .reportCount(petition.getReportCount())
                .writerEmail(Util.maskEmail(petition.getUsers().getEmail()))
                .links(linkList)
                .createDate(petition.getCreatedAt().toLocalDate())
                .endDate(petition.getCreatedAt().toLocalDate().plus(Constant.PETITION_ACTIVE_PERIOD))
                .answerResponse(answerResponse)
                .build();
    }
}
