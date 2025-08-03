package com.cqrs.handler;

import com.cqrs.query.Query;

public interface QueryHandler<Q extends Query<R>, R> {
    R handle(Q query);
}
