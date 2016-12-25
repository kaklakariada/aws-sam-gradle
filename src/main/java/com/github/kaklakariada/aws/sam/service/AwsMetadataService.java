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
