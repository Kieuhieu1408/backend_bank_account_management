package com.klb.card_service.core;

public interface Handler<T extends RequestData, R extends ResponseData> {
    R handle(T requestData);
}
