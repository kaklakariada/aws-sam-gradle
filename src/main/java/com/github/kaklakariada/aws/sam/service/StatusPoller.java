package com.github.kaklakariada.aws.sam.service;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;
import java.util.function.Supplier;

import org.gradle.api.logging.Logging;
import org.slf4j.Logger;

import com.github.kaklakariada.aws.sam.DeploymentException;

class StatusPoller {
	private static final Logger LOG = Logging.getLogger(StatusPoller.class);

	private final Function<Duration, Boolean> waitingFinishedFunction;

	private StatusPoller(Function<Duration, Boolean> waitingFinishedFunction) {
		this.waitingFinishedFunction = waitingFinishedFunction;
	}

	public static StatusPoller create(Supplier<String> statusSupplier, Supplier<String> failureMessageSupplier) {
		return new StatusPoller((waitingDuration) -> {
			final String status = statusSupplier.get();
			LOG.info("Got status {} after {}", status, waitingDuration);
			if (isFailed(status)) {
				throw new DeploymentException("Got failure status " + status + ": " + failureMessageSupplier.get());
			}
			if (isSuccess(status)) {
				return true;
			}
			return false;
		});
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

	private static boolean isSuccess(String status) {
		return status.toUpperCase().endsWith("COMPLETE");
	}

	private static boolean isFailed(String status) {
		return status.toUpperCase().contains("FAILED") || status.equalsIgnoreCase("UPDATE_ROLLBACK_COMPLETE");
	}
}