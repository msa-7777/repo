package com.msa7.v1.order.presentation.dto.mq;

import java.util.UUID;

public class OrderCreateMsg {
	UUID orderId;
	UUID productId;
	String address;
}
