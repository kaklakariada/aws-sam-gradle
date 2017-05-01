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
		return "SamConfig [stages=" + stages.getAsMap() + ", deploymentTimestamp=" + deploymentTimestamp + ", projct="
				+ projct + ", currentStage=" + currentStage + ", api=" + api + ", defaultAwsRegion=" + defaultAwsRegion
				+ ", defaultAwsProfile=" + defaultAwsProfile + ", defaultDeploymentBucket=" + defaultDeploymentBucket
				+ "]";
	}
}
