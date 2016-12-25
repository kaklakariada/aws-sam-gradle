package com.github.kaklakariada.aws.sam.config;

public class Stage {
	final String name;
	String awsRegion;
	String awsProfile;
	String deploymentBucket;

	public Stage(String name) {
		this.name = name;
	}
}
