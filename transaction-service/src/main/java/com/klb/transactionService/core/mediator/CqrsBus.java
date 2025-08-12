package com.klb.transactionService.core.mediator;

import com.klb.transactionService.core.abstractions.RequestData;
import com.klb.transactionService.core.abstractions.ResponseData;

public interface CqrsBus {
    <T extends RequestData, R extends ResponseData> R execute(T requestData);
}
