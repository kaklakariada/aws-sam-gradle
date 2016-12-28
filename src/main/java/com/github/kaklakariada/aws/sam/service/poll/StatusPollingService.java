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
		new Poller((waitingDuration) -> {
			final String status = condition.getStatus();
			LOG.info("Got status {} after {}", status, waitingDuration);
			if (condition.isFailure(status)) {
				throw new DeploymentException("Got failure status " + status + ": " + condition.getFailureMessage());
			}
			if (condition.isSuccess(status)) {
				return true;
			}
			return false;
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
				throw new DeploymentException("Exception while sleeping", e);
			}
		}
	}
}