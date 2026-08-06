package com.msa7.ai.presentation.dto.response;

import java.time.LocalDateTime;

public record AiDeadlineResponse(
        LocalDateTime calculatedDeadline,
        String generatedMessage
) {}