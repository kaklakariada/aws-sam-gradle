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
package com.github.kaklakariada.aws.sam.service.poll;

import org.gradle.api.logging.Logger;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;

public class CloudFormationPollingService {
	private final AmazonCloudFormation cloudFormation;
	private final StatusPollingService pollingService;

	CloudFormationPollingService(AmazonCloudFormation cloudFormation, StatusPollingService pollingService) {
		this.cloudFormation = cloudFormation;
		this.pollingService = pollingService;
	}

	public CloudFormationPollingService(AmazonCloudFormation cloudFormation, Logger logger) {
		this(cloudFormation, new StatusPollingService(logger));
	}

	public void waitForChangeSetReady(String changeSetArn) {
		pollingService.waitForStatus(new ChangeSetCreateCompleteWaitCondition(cloudFormation, changeSetArn));
	}

	public void waitForStackReady(String stackName) {
		pollingService.waitForStatus(new StackReadyWaitCondition(cloudFormation, stackName));
	}

	public void waitForStackDeleted(String stackName) {
		pollingService.waitForStatus(new StackDeletedWaitCondition(cloudFormation, stackName));
	}
}
