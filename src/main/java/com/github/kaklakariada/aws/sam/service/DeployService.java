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

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.gradle.api.logging.Logger;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.ChangeSetType;
import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.github.kaklakariada.aws.sam.config.SamConfig;
import com.github.kaklakariada.aws.sam.service.poll.CloudFormationPollingService;

public class DeployService {

	private final Logger logger;
	private final CloudformationService cloudFormationService;
	private final TemplateService templateService;
	private final SamConfig config;

	private final CloudFormationPollingService pollingService;

	public DeployService(SamConfig config, CloudformationService cloudFormationService,
			CloudFormationPollingService pollingService, TemplateService templateService, Logger logger) {
		this.config = config;
		this.cloudFormationService = cloudFormationService;
		this.pollingService = pollingService;
		this.templateService = templateService;
		this.logger = logger;
	}

	public DeployService(SamConfig config, AmazonCloudFormation cloudFormation, Logger logger) {
		this(config, new CloudformationService(cloudFormation, logger),
				new CloudFormationPollingService(cloudFormation, logger), new TemplateService(logger), logger);
	}

	public DeployService(SamConfig config, Logger logger) {
		this(config, config.getAwsClientFactory().create(AmazonCloudFormationClientBuilder.standard()), logger);
	}

	public void deploy(String templateBody, String codeUri, String swaggerDefinitionUri) {
		final String stackName = config.getStackName();
		final String changeSetName = stackName + "-" + System.currentTimeMillis();
		final ChangeSetType changeSetType = cloudFormationService.stackExists(stackName) ? ChangeSetType.UPDATE
				: ChangeSetType.CREATE;
		final String newTemplateBody = updateTemplateBody(templateBody, codeUri, swaggerDefinitionUri);
		final String changeSetArn = cloudFormationService.createChangeSet(changeSetName, stackName, changeSetType,
				newTemplateBody, emptyList());
		pollingService.waitForChangeSetReady(changeSetArn);
		cloudFormationService.executeChangeSet(changeSetArn);
		pollingService.waitForStackReady(stackName);
		logStackOutput();
	}

	public void deleteStack(String stackName) {
		cloudFormationService.deleteStack(stackName);
		pollingService.waitForStackDeleted(stackName);
	}

	private void logStackOutput() {
		getStackOutput().forEach(
				output -> logger.lifecycle("Stack output {} = {}", output.getOutputKey(), output.getOutputValue()));
	}

	public List<Output> getStackOutput() {
		return cloudFormationService.getOutputParameters(config.getStackName());
	}

	private String updateTemplateBody(String templateBody, String codeUri, String swaggerDefinitionUri) {
		final Collection<Parameter> parameters = new ArrayList<>();
		parameters.add(new Parameter().withParameterKey("CodeUri").withParameterValue(Objects.requireNonNull(codeUri)));
		parameters.add(new Parameter().withParameterKey("stage").withParameterValue(config.getStageName()));
		if (swaggerDefinitionUri != null) {
			parameters.add(new Parameter().withParameterKey("DefinitionUri").withParameterValue(swaggerDefinitionUri));
		}
		final String newTemplateBody = templateService.replaceParameters(templateBody, parameters);
		templateService.writeFile(newTemplateBody, config.getBuildDir().resolve("updated-template.yaml"));
		return newTemplateBody;
	}
}
