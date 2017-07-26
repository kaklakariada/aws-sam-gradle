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

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;

import org.gradle.api.Project;

import com.amazonaws.regions.Regions;
import com.github.kaklakariada.aws.sam.service.AwsClientFactory;

public class SamConfig {
	private final Instant deploymentTimestamp;
	private final Project projct;
	private final SamConfigDsl configDsl;

	public SamConfig(Project projct, SamConfigDsl configDsl) {
		this.projct = projct;
		this.configDsl = configDsl;
		this.deploymentTimestamp = Instant.now();
	}

	public Stage getStage() {
		return configDsl.stages.getByName(Objects.requireNonNull(getStageName(), "currentStage"));
	}

	public String getStageName() {
		return configDsl.activeStage;
	}

	public String getStackName() {
		return configDsl.api.stackName;
	}

	public Path getBuildDir() {
		return projct.getBuildDir().toPath().toAbsolutePath();
	}

	public Regions getActiveRegion() {
		final String stageRegion = getStage().awsRegion;
		return Regions.fromName(stageRegion != null ? stageRegion : configDsl.defaultAwsRegion);
	}

	public String getDeploymentBucket() {
		final String stageBucket = getStage().deployBucket;
		return stageBucket != null ? stageBucket : configDsl.defaultDeployBucket;
	}

	private String getActiveAwsProfile() {
		final String profile = getStage().awsProfile;
		return profile != null ? profile : configDsl.defaultAwsProfile;
	}

	public Instant getDeploymentTimestamp() {
		return deploymentTimestamp;
	}

	public AwsClientFactory getAwsClientFactory() {
		return AwsClientFactory.create(getActiveRegion(), getActiveAwsProfile());
	}

	public File getSwaggerDefinition() {
		return getApi().swaggerDefinition;
	}

	public Path getSamTemplate() {
		return getApi().samTemplate.toPath();
	}

	private ApiConfigDsl getApi() {
		if (configDsl.api == null) {
			throw new IllegalStateException("serverless.api configuration is missing");
		}
		return configDsl.api;
	}
}
