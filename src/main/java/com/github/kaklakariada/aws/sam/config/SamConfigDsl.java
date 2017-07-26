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

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;

import groovy.lang.Closure;

public class SamConfigDsl {
	private final Project project;

	public final NamedDomainObjectContainer<Stage> stages;
	public ApiConfigDsl api;
	public String activeStage;
	public String defaultAwsRegion;
	public String defaultAwsProfile;
	public String defaultDeployBucket;

	public SamConfigDsl(Project projct, NamedDomainObjectContainer<Stage> stages) {
		this.project = projct;
		this.stages = stages;
	}

	public void api(Closure<?> config) {
		api = (ApiConfigDsl) project.configure(new ApiConfigDsl(), config);
	}
}
