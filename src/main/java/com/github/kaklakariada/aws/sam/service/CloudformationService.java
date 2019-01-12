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

import java.util.Collection;
import java.util.List;

import org.gradle.api.logging.Logger;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.AmazonCloudFormationException;
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.ChangeSetType;
import com.amazonaws.services.cloudformation.model.CreateChangeSetRequest;
import com.amazonaws.services.cloudformation.model.CreateChangeSetResult;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.ExecuteChangeSetRequest;
import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Stack;
import com.github.kaklakariada.aws.sam.DeploymentException;

public class CloudformationService {

	private final Logger logger;
	private final AmazonCloudFormation cloudFormation;

	public CloudformationService(AmazonCloudFormation cloudFormation, Logger logger) {
		this.cloudFormation = cloudFormation;
		this.logger = logger;
	}

	public String createChangeSet(String changeSetName, String stackName, ChangeSetType changeSetType,
			String templateBody, Collection<Parameter> parameters) {
		logger.info("Creating change set for stack {} with name {}, type {} and parameters {}", stackName,
				changeSetName, changeSetType, parameters);
		final CreateChangeSetRequest changeSetRequest = new CreateChangeSetRequest() //
				.withCapabilities(Capability.CAPABILITY_IAM) //
				.withStackName(stackName) //
				.withDescription(stackName) //
				.withChangeSetName(changeSetName) //
				.withChangeSetType(changeSetType) //
				.withParameters(parameters).withTemplateBody(templateBody);
		final CreateChangeSetResult result = cloudFormation.createChangeSet(changeSetRequest);
		logger.info("Change set created: {}", result);
		return result.getId();
	}

	public boolean stackExists(String stackName) {
		try {
			return describeStack(stackName).stream() //
					.peek(s -> logger.info("Found stack {}", s)) //
					.filter(s -> s.getStackName().equals(stackName)) //
					.anyMatch(s -> !s.getStackStatus().equals("REVIEW_IN_PROGRESS"));
		} catch (final AmazonCloudFormationException e) {
			if (e.getStatusCode() == 400) {
				logger.trace("Got exception {}", e.getMessage(), e);
				return false;
			}
			throw e;
		}
	}

	public List<Output> getOutputParameters(String stackName) {
		final Stack stack = describeStack(stackName).stream().findFirst()
				.orElseThrow(() -> new DeploymentException("Stack not found"));
		return stack.getOutputs();
	}

	private List<Stack> describeStack(String stackName) {
		final DescribeStacksResult result = cloudFormation
				.describeStacks(new DescribeStacksRequest().withStackName(stackName));
		return result.getStacks();
	}

	public void executeChangeSet(String changeSetArn) {
		cloudFormation.executeChangeSet(new ExecuteChangeSetRequest().withChangeSetName(changeSetArn));
		logger.lifecycle("Executing change set {}", changeSetArn);
	}

	public void deleteStack(String stackName) {
		logger.info("Delete stack '{}'", stackName);
		cloudFormation.deleteStack(new DeleteStackRequest().withStackName(stackName));
	}
}
