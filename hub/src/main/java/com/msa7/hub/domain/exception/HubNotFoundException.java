package com.msa7.hub.domain.exception;

public class HubNotFoundException extends BusinessException {

    public HubNotFoundException() {
        super(ErrorCode.HUB_NOT_FOUND);
    }
}
