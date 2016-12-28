package com.github.kaklakariada.aws.sam.service.poll;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;

public class CloudFormationPollingService {
	private final AmazonCloudFormation cloudFormation;
	private final StatusPollingService pollingService;

	CloudFormationPollingService(AmazonCloudFormation cloudFormation, StatusPollingService pollingService) {
		this.cloudFormation = cloudFormation;
		this.pollingService = pollingService;
	}

	public CloudFormationPollingService(AmazonCloudFormation cloudFormation) {
		this(cloudFormation, new StatusPollingService());
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
