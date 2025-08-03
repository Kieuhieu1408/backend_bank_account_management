package com.cqrs.business.user.handler;

import com.cqrs.business.user.query.GetUserQuery;
import com.cqrs.handler.QueryHandler;

public class GetUserQueryHandler implements QueryHandler<GetUserQuery, String> {
    @Override
    public String handle(GetUserQuery query) {
        return "User details for ID: " + query.getUserId();
    }

}
