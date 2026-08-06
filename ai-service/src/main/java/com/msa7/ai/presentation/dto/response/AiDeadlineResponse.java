package com.msa7.ai.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record AiDeadlineResponse(
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        LocalDateTime calculatedDeadline,
        String generatedMessage
) {}