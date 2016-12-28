package com.github.kaklakariada.aws.sam.service;

import java.util.Collection;
import java.util.List;

import org.gradle.api.logging.Logging;
import org.slf4j.Logger;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.AmazonCloudFormationException;
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.ChangeSetType;
import com.amazonaws.services.cloudformation.model.CreateChangeSetRequest;
import com.amazonaws.services.cloudformation.model.CreateChangeSetResult;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.ExecuteChangeSetRequest;
import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Stack;
import com.github.kaklakariada.aws.sam.DeploymentException;
import com.github.kaklakariada.aws.sam.config.SamConfig;

public class CloudformationService {

	private static final Logger LOG = Logging.getLogger(CloudformationService.class);
	private final AmazonCloudFormation cloudFormation;
	private final SamConfig config;

	public CloudformationService(SamConfig config, AmazonCloudFormation cloudFormation) {
		this.config = config;
		this.cloudFormation = cloudFormation;
	}

	public String createChangeSet(String changeSetName, ChangeSetType changeSetType, String templateBody,
			Collection<Parameter> parameters) {
		LOG.info("Creating change set for stack {} with name {}, type {} and parameters {}", config.getStackName(),
				changeSetName, changeSetType, parameters);
		final CreateChangeSetRequest changeSetRequest = new CreateChangeSetRequest() //
				.withCapabilities(Capability.CAPABILITY_IAM) //
				.withStackName(config.getStackName()) //
				.withDescription(config.getStackName()) //
				.withChangeSetName(changeSetName) //
				.withChangeSetType(changeSetType) //
				.withParameters(parameters).withTemplateBody(templateBody);
		final CreateChangeSetResult result = cloudFormation.createChangeSet(changeSetRequest);
		LOG.info("Change set created: {}", result);
		return result.getId();
	}

	public boolean stackExists() {
		try {
			return describeStack().stream() //
					.peek(s -> LOG.info("Found stack {}", s)) //
					.filter(s -> s.getStackName().equals(config.getStackName())) //
					.filter(s -> !s.getStackStatus().equals("REVIEW_IN_PROGRESS")) //
					.findAny() //
					.isPresent();
		} catch (final AmazonCloudFormationException e) {
			if (e.getStatusCode() == 400) {
				LOG.trace("Got exception", e);
				return false;
			}
			throw e;
		}
	}

	public List<Output> getOutputParameters() {
		final Stack stack = describeStack().stream().findFirst()
				.orElseThrow(() -> new DeploymentException("Stack not found"));
		return stack.getOutputs();
	}

	private List<Stack> describeStack() {
		final DescribeStacksResult result = cloudFormation
				.describeStacks(new DescribeStacksRequest().withStackName(config.getStackName()));
		return result.getStacks();
	}

	public void executeChangeSet(String changeSetArn) {
		cloudFormation.executeChangeSet(new ExecuteChangeSetRequest().withChangeSetName(changeSetArn));
		LOG.info("Executing change set {}", changeSetArn);
	}

	public void deleteStack() {
		cloudFormation.deleteStack(new DeleteStackRequest().withStackName(config.getStackName()));
	}

	public void waitForStackDeleted() {
	}
}
