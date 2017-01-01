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
