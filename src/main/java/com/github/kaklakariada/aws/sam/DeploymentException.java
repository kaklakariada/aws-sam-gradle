package com.github.kaklakariada.aws.sam;

public class DeploymentException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public DeploymentException(String message, Throwable cause) {
		super(message, cause);
	}

	public DeploymentException(String message) {
		super(message);
	}
}
