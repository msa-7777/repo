package com.msa7.v1.order.domain.vo;

public record RequestNotes(
	String contents
	// LocalDateTime deliveryDeadLine
) {
	public RequestNotes {
		if (contents != null && contents.length() > 50){
			throw new IllegalArgumentException("contents must be less than 50");
		}
		contents = (contents == null) ? "" : contents;
	}

}
