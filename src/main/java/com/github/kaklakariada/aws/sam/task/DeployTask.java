package com.github.kaklakariada.aws.sam.task;

import java.io.IOException;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import com.github.kaklakariada.aws.sam.config.SamConfig;
import com.github.kaklakariada.aws.sam.service.DeployService;
import com.github.kaklakariada.aws.sam.service.TemplateService;

public class DeployTask extends DefaultTask {

	@Input
	public String codeUri;
	@Input
	public String swaggerUri;
	@Input
	public SamConfig config;

	@TaskAction
	public void uploadFileToS3() throws IOException, InterruptedException {

		final String templateBody = new TemplateService().loadFile(config.api.samTemplate.toPath());

		final String stackName = getProject().getName();

		final DeployService deployService = new DeployService(config);
		deployService.deploy(stackName, templateBody, codeUri, swaggerUri);
	}
}
