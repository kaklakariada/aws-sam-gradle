/**
 * aws-sam-gradle - Gradle plugin for deploying AWS Serverless Application Models
 * Copyright (C) 2017 Christoph Pirkl <christoph at users.sourceforge.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.kaklakariada.aws.sam.service;

import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceAsyncClient;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityResult;
import com.github.kaklakariada.aws.sam.config.SamConfig;

public class AwsMetadataService {

	private final AWSSecurityTokenService tokenService;

	public AwsMetadataService(AWSSecurityTokenService tokenService) {
		this.tokenService = tokenService;
	}

	public AwsMetadataService(SamConfig config) {
		this(config.getAwsClientFactory().create(AWSSecurityTokenServiceAsyncClient::new));
	}

	public String getAccountId() {
		final GetCallerIdentityResult callerIdentity = tokenService.getCallerIdentity(new GetCallerIdentityRequest());
		return callerIdentity.getAccount();
	}
}
