package com.msa7.v1.order.contorller;

import static org.mockito.BDDMockito.*;
// 올바른 MockMvc용 import (MockHttpServletRequestBuilder 반환)
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.msa7.v1.order.app.OrderService;
import com.msa7.v1.order.domain.aggregate.Order;
import com.msa7.v1.order.presentation.controller.OrderController;

@WebMvcTest(OrderController.class)
class OrderControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@MockitoBean
	private OrderService orderService; // Service 로직 격리(가짜 객체)

	@Test
	void 주문_생성_API_작동확인() throws Exception {
		// given: 요청 데이터 세팅 및 가짜 응답 설정
		String requestJson = "{ \"receiverCompanyId\": \"uuid\", ... }";
		given(orderService.createOrder(any(UUID.class), any(UUID.class), any(Integer.class), any()))
			.willReturn(Order.create(UUID.randomUUID(), UUID.randomUUID(), 1, "요청사항"));

		// when & then: POST 요청 전송 및 201 CREATED 나와야 하지만, 유저가 없어서 현재는 403
		mockMvc.perform(post("/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
			.andExpect(status().isCreated());
	}
}
