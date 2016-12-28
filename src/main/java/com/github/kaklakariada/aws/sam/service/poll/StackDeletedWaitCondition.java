package com.github.kaklakariada.aws.sam.service.poll;

import java.util.Optional;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;

class StackDeletedWaitCondition extends StackStatusWaitCondition {

	private static final String STACK_DOES_NOT_EXIST_STATUS = "STACK_DOES_NOT_EXIST_STATUS";

	StackDeletedWaitCondition(AmazonCloudFormation cloudFormation, String stackName) {
		super(cloudFormation, stackName);
	}

	@Override
	public String getStatus() {
		return getStack().flatMap(stack -> Optional.of(stack.getStackStatus())) //
				.orElse(STACK_DOES_NOT_EXIST_STATUS);
	}

	@Override
	public boolean isSuccess(String status) {
		return status.equals(STACK_DOES_NOT_EXIST_STATUS);
	}

	@Override
	public boolean isFailure(String status) {
		return status.toUpperCase().contains("FAILED") || status.equalsIgnoreCase("UPDATE_ROLLBACK_COMPLETE");
	}
}
