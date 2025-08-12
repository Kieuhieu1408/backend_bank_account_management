package com.klb.transactionService.core.abstractions;

public interface Handler<T extends RequestData, R extends ResponseData> {
    R handle(T requestData);
}
