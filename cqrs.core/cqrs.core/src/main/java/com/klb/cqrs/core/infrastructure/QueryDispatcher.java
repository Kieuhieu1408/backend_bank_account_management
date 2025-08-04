package com.klb.cqrs.core.infrastructure;

import com.klb.cqrs.core.domain.BaseEntity;
import com.klb.cqrs.core.queries.BaseQuery;
import com.klb.cqrs.core.queries.QueryHandlerMethod;

import java.util.List;

public interface QueryDispatcher {
    <T extends BaseQuery> void registerHandler(Class<T> type, QueryHandlerMethod<T> handler);
    <U extends BaseEntity> List<U> send(BaseQuery query);
}
