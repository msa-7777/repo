package com.sparta.slackservice.presentation.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record SlackMessagePageResponse(

        List<SlackMessageSummaryResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    public static SlackMessagePageResponse from(
            Page<SlackMessageSummaryResponse> page
    ) {
        return new SlackMessagePageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
