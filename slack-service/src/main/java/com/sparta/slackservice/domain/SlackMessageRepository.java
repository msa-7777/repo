package com.sparta.slackservice.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SlackMessageRepository
        extends JpaRepository<SlackMessage, UUID>, SlackMessageRepositoryCustom {

    Optional<SlackMessage> findByIdAndDeletedAtIsNull(UUID id);
}