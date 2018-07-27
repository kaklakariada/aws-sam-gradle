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

import java.util.List;
import java.util.Optional;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.AmazonCloudFormationException;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.Stack;
import com.github.kaklakariada.aws.sam.DeploymentException;
import com.github.kaklakariada.aws.sam.service.poll.StatusPollingService.WaitCondition;

abstract class StackStatusWaitCondition implements WaitCondition {
	private final AmazonCloudFormation cloudFormation;
	private final String stackName;

	StackStatusWaitCondition(AmazonCloudFormation cloudFormation, String stackName) {
		this.cloudFormation = cloudFormation;
		this.stackName = stackName;
	}

	@Override
	public String getStatus() {
		final Optional<Stack> stack = getStack();
		if (!stack.isPresent()) {
			throw new DeploymentException("Stack " + stackName + " not found");
		}
		return stack.get().getStackStatus();
	}

	@Override
	public String getFailureMessage() {
		return getStack().toString();
	}

	protected Optional<Stack> getStack() {
		final List<Stack> stacks;
		try {
			stacks = cloudFormation.describeStacks(new DescribeStacksRequest().withStackName(stackName)).getStacks();
		} catch (final AmazonCloudFormationException e) {
			if (e.getStatusCode() == 400) {
				return Optional.empty();
			}
			throw e;
		}
		if (stacks.isEmpty()) {
			return Optional.empty();
		}
		if (stacks.size() > 1) {
			throw new DeploymentException("Found more than one stack for name '" + stackName + "'");
		}
		final Stack stack = stacks.get(0);
		return Optional.of(stack);
	}
}