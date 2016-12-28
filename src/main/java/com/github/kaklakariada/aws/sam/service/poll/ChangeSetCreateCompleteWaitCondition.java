package com.github.kaklakariada.aws.sam.service.poll;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.DescribeChangeSetRequest;
import com.amazonaws.services.cloudformation.model.DescribeChangeSetResult;
import com.github.kaklakariada.aws.sam.service.poll.StatusPollingService.WaitCondition;

public class ChangeSetCreateCompleteWaitCondition implements WaitCondition {
	private final AmazonCloudFormation cloudFormation;
	private final String changeSetArn;

	public ChangeSetCreateCompleteWaitCondition(AmazonCloudFormation cloudFormation, String changeSetArn) {
		this.cloudFormation = cloudFormation;
		this.changeSetArn = changeSetArn;
	}

	@Override
	public String getStatus() {
		return getChangeSetStatus().getStatus();
	}

	@Override
	public String getFailureMessage() {
		return getChangeSetStatus().toString();
	}

	@Override
	public boolean isSuccess(String status) {
		return status.toUpperCase().endsWith("COMPLETE");
	}

	@Override
	public boolean isFailure(String status) {
		return status.toUpperCase().contains("FAILED") || status.equalsIgnoreCase("UPDATE_ROLLBACK_COMPLETE");
	}

	private DescribeChangeSetResult getChangeSetStatus() {
		return cloudFormation.describeChangeSet(new DescribeChangeSetRequest().withChangeSetName(changeSetArn));
	}
}