package com.cqrs.business.user.query;

import com.cqrs.query.Query;
import lombok.Getter;

@Getter
public class GetUserQuery implements Query<String> {

	private int userId;

	public GetUserQuery(int userId) {
		this.userId = userId;
	}

}
