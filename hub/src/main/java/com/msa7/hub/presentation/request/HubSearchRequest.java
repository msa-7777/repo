package com.msa7.hub.presentation.request;

public record HubSearchRequest(

    String name,

    String address,

    Boolean isCentral

) {
}
