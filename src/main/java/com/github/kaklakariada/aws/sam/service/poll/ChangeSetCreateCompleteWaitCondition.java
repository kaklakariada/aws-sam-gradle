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
		final DescribeChangeSetResult changeSetStatus = getChangeSetStatus();
		return changeSetStatus.getStatusReason() + " " + changeSetStatus.toString();
	}

	@Override
	public boolean isSuccess(String status) {
		return status.toUpperCase().endsWith("COMPLETE");
	}

	@Override
	public boolean isFailure(String status) {
		return status.toUpperCase().contains("FAILED") //
				|| status.equalsIgnoreCase("UPDATE_ROLLBACK_COMPLETE") //
				|| status.equalsIgnoreCase("ROLLBACK_IN_PROGRESS");
	}

	private DescribeChangeSetResult getChangeSetStatus() {
		return cloudFormation.describeChangeSet(new DescribeChangeSetRequest().withChangeSetName(changeSetArn));
	}
}