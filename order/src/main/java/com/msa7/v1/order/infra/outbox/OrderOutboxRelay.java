package com.msa7.v1.order.infra.outbox;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.msa7.v1.order.infra.entity.OrderOutboxEntity;
import com.msa7.v1.order.infra.repo.OrderOutboxRepository;

import lombok.RequiredArgsConstructor;

// outbox->> RabbitMq
@Component
@RequiredArgsConstructor
public class OrderOutboxRelay {
	private final OrderOutboxRepository orderOutboxRepo;
	private final RabbitTemplate rabbitTemplate;

	@Scheduled(fixedDelay = 5000)
	@Transactional
	// 만약 다중 서버라면? 스케줄러가 동시에 실행될수도 있음
	// 분산락을 적용항여 스케줄러 중복 실행 막는 방법도 있다는데 이건 해볼지 모르겠음
	public void publishOutboxMessages() {
		// 만약 미발행 메시지가 엄청 쌓인다면?->> 이때 pagable로 가져오는거
		// 다음 플젝때 해보자
		List<OrderOutboxEntity> messages = orderOutboxRepo.findByPublishedFalse();

		for (OrderOutboxEntity message : messages) {
			// 응답이 지연되는 경우에 대한 대비 부족
			// 메시지 전송 작업과 DB 업데이트 작업을 분리해서 rabbitMQ 트랜잭션 의존성 줄여야함
			// try catch로 해봐야하는데 일단 넘어가고 이 기록을 토대로 다음 플젝때 시도
			rabbitTemplate.convertAndSend("order-exchange", "order.created", message.getPayload());
			message.markAsPublished();
		}
	}
}
