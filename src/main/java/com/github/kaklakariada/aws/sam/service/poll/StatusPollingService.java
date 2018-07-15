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

import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;

import org.gradle.api.logging.Logging;
import org.slf4j.Logger;

import com.github.kaklakariada.aws.sam.DeploymentException;

class StatusPollingService {
	private static final Logger LOG = Logging.getLogger(StatusPollingService.class);

	public void waitForStatus(WaitCondition condition) {
		new Poller(waitingDuration -> {
			final String status = condition.getStatus();
			LOG.info("Got status {} after {}", status, waitingDuration);
			if (condition.isFailure(status)) {
				throw new DeploymentException("Got failure status " + status + ": " + condition.getFailureMessage());
			}
			return condition.isSuccess(status);
		}).waitUntilFinished();
	}

	interface WaitCondition {
		String getStatus();

		String getFailureMessage();

		boolean isSuccess(String status);

		boolean isFailure(String status);
	}

	private static class Poller {
		private final Function<Duration, Boolean> waitingFinishedFunction;

		private Poller(Function<Duration, Boolean> waitingFinishedFunction) {
			this.waitingFinishedFunction = waitingFinishedFunction;
		}

		public void waitUntilFinished() {
			final Instant start = Instant.now();
			while (true) {
				if (waitingFinishedFunction.apply(Duration.between(start, Instant.now()))) {
					return;
				}
				sleep();
			}
		}

		private void sleep() {
			try {
				Thread.sleep(2000);
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new DeploymentException("Exception while sleeping", e);
			}
		}
	}
}