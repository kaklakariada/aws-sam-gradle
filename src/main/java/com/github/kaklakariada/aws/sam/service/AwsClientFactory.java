package com.github.kaklakariada.aws.sam.service;

import java.util.function.Function;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

public class AwsClientFactory {
	private final Regions region;
	private final AWSCredentialsProvider credentialsProvider;

	private AwsClientFactory(Regions region, AWSCredentialsProvider credentialsProvider) {
		this.region = region;
		this.credentialsProvider = credentialsProvider;
	}

	public static AwsClientFactory create(Regions region, String profileName) {
		final AWSCredentialsProvider credentialsProvider = new ProfileCredentialsProvider(profileName);
		return new AwsClientFactory(region, credentialsProvider);
	}

	public <T extends AmazonWebServiceClient> T create(Function<AWSCredentialsProvider, T> factory) {
		final T client = factory.apply(credentialsProvider);
		client.setRegion(Region.getRegion(region));
		return client;
	}
}
