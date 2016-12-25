package com.github.kaklakariada.aws.sam.config;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;

import com.amazonaws.regions.Regions;
import com.github.kaklakariada.aws.sam.service.AwsClientFactory;

import groovy.lang.Closure;

public class SamConfig {
	private final NamedDomainObjectContainer<Stage> stages;
	private final long deploymentTimestamp;
	private final Project projct;

	public String currentStage;
	public ApiConfig api;

	public String defaultAwsRegion;
	public String defaultAwsProfile;
	public String defaultDeploymentBucket;

	public SamConfig(Project projct, NamedDomainObjectContainer<Stage> stages) {
		this.projct = projct;
		this.stages = stages;
		this.deploymentTimestamp = System.currentTimeMillis();
	}

	public void api(Closure<?> config) {
		api = (ApiConfig) projct.configure(new ApiConfig(), config);
	}

	public Project getProjct() {
		return projct;
	}

	private Stage getStage() {
		return stages.getByName(currentStage);
	}

	public String getStackName() {
		return api.stackBaseName + "-" + getStage().name;
	}

	public Regions getRegion() {
		final String stageRegion = getStage().awsRegion;
		return Regions.fromName(stageRegion != null ? stageRegion : defaultAwsRegion);
	}

	public String getDeploymentBucket() {
		final String stageBucket = getStage().deploymentBucket;
		return stageBucket != null ? stageBucket : defaultDeploymentBucket;
	}

	public String getAwsProfile() {
		final String profile = getStage().awsProfile;
		return profile != null ? profile : defaultAwsProfile;
	}

	public long getDeploymentTimestamp() {
		return deploymentTimestamp;
	}

	public AwsClientFactory getAwsClientFactory() {
		return AwsClientFactory.create(getRegion(), getAwsProfile());
	}

	@Override
	public String toString() {
		return "SamConfig [stages=" + stages + ", currentStage=" + currentStage + ", api=" + api + "]";
	}
}
