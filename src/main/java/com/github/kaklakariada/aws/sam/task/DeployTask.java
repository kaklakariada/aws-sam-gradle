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
package com.github.kaklakariada.aws.sam.task;

import java.util.function.Supplier;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import com.github.kaklakariada.aws.sam.config.SamConfig;
import com.github.kaklakariada.aws.sam.service.DeployService;
import com.github.kaklakariada.aws.sam.service.TemplateService;

public class DeployTask extends DefaultTask {

	@Input
	public Supplier<String> codeUri;
	@Input
	public Supplier<String> swaggerUri;
	@Input
	public SamConfig config;

	@TaskAction
	public void uploadFileToS3() {
		final String templateBody = new TemplateService().loadFile(config.getSamTemplate());
		final DeployService deployService = new DeployService(config);
		deployService.deploy(templateBody, codeUri.get(), swaggerUri.get());
	}
}
