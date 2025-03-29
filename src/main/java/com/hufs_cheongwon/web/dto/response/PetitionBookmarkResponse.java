package com.hufs_cheongwon.web.dto.response;

import com.hufs_cheongwon.domain.PetitionBookmark;
import com.hufs_cheongwon.repository.PetitionBookmarkRepository;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PetitionBookmarkResponse {

    private Long id;
    private Long petitionId;
    private boolean isBookmarked;

    public static PetitionBookmarkResponse onBookmark (PetitionBookmark petitionBookmark, Long petitionId) {
        return PetitionBookmarkResponse.builder()
                .id(petitionBookmark.getId())
                .petitionId(petitionBookmark.getPetition().getId())
                .isBookmarked(true)
                .build();
    }

    public static PetitionBookmarkResponse offBookmark (Long petitionId) {
        return PetitionBookmarkResponse.builder()
                .id(null)
                .petitionId(petitionId)
                .isBookmarked(false)
                .build();
    }
}
