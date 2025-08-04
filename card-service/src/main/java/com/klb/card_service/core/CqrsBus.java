package com.klb.card_service.core;

public interface CqrsBus {
    <T extends RequestData, R extends ResponseData> R execute(T requestData);
}
