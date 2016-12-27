package com.github.kaklakariada.aws.sam.config;

import java.nio.file.Path;
import java.util.Objects;

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

	private Stage getStage() {
		return stages.getByName(Objects.requireNonNull(currentStage, "currentStage"));
	}

	public String getStackName() {
		return api.stackName;
	}

	public Path getBuildDir() {
		return projct.getBuildDir().toPath().toAbsolutePath();
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
		return "SamConfig [stages=" + stages + ", deploymentTimestamp=" + deploymentTimestamp + ", projct=" + projct
				+ ", currentStage=" + currentStage + ", api=" + api + ", defaultAwsRegion=" + defaultAwsRegion
				+ ", defaultAwsProfile=" + defaultAwsProfile + ", defaultDeploymentBucket=" + defaultDeploymentBucket
				+ "]";
	}
}
