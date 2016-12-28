package com.github.kaklakariada.aws.sam.service.poll;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;

class StackDeletedWaitCondition extends StackStatusWaitCondition {

	StackDeletedWaitCondition(AmazonCloudFormation cloudFormation, String stackName) {
		super(cloudFormation, stackName);
	}

	@Override
	public boolean isSuccess(String status) {
		return status.toUpperCase().endsWith("COMPLETE");
	}

	@Override
	public boolean isFailure(String status) {
		return status.toUpperCase().contains("FAILED") || status.equalsIgnoreCase("UPDATE_ROLLBACK_COMPLETE");
	}
}
