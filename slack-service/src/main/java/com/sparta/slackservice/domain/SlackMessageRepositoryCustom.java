package com.sparta.slackservice.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SlackMessageRepositoryCustom {

    Page<SlackMessage> search(
            SlackMessageSearchCondition condition,
            Pageable pageable
    );
}
