package com.github.kaklakariada.aws.sam.service;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import org.gradle.api.logging.Logging;
import org.slf4j.Logger;

import com.amazonaws.services.cloudformation.model.ChangeSetType;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.github.kaklakariada.aws.sam.config.SamConfig;

public class DeployService {
	private static final Logger LOG = Logging.getLogger(DeployService.class);

	private final CloudformationService cloudFormationService;
	private final TemplateService templateService;
	private final SamConfig config;

	public DeployService(SamConfig config, CloudformationService cloudFormationService,
			TemplateService templateService) {
		this.config = config;
		this.cloudFormationService = cloudFormationService;
		this.templateService = templateService;
	}

	public DeployService(SamConfig config) {
		this(config, new CloudformationService(config), new TemplateService());
	}

	public void deploy(String stackName, String templateBody, String codeUri, String swaggerDefinitionUri) {
		final String changeSetName = stackName + "-" + System.currentTimeMillis();
		final ChangeSetType changeSetType = cloudFormationService.stackExists() ? ChangeSetType.UPDATE
				: ChangeSetType.CREATE;
		final String newTemplateBody = updateTemplateBody(templateBody, codeUri, swaggerDefinitionUri);
		final String changeSetArn = cloudFormationService.createChangeSet(changeSetName, changeSetType, newTemplateBody,
				emptyList());
		cloudFormationService.waitForChangeSetReady(changeSetArn);
		cloudFormationService.executeChangeSet(changeSetArn);
		cloudFormationService.waitForStackReady();
		logStackOutput();
	}

	private void logStackOutput() {
		cloudFormationService.getOutputParameters()
				.forEach(output -> LOG.info("Stack output {} = {}", output.getOutputKey(), output.getOutputValue()));
	}

	private String updateTemplateBody(String templateBody, String codeUri, String swaggerDefinitionUri) {
		final Collection<Parameter> parameters = new ArrayList<>();
		parameters.add(new Parameter().withParameterKey("CodeUri").withParameterValue(Objects.requireNonNull(codeUri)));
		parameters.add(new Parameter().withParameterKey("stage").withParameterValue(config.currentStage));
		if (swaggerDefinitionUri != null) {
			parameters.add(new Parameter().withParameterKey("DefinitionUri").withParameterValue(swaggerDefinitionUri));
		}
		return templateService.replaceParameters(templateBody, parameters);
	}
}
