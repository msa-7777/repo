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

}
