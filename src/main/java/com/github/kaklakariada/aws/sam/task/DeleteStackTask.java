package com.github.kaklakariada.aws.sam.task;

import java.io.IOException;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import com.github.kaklakariada.aws.sam.config.SamConfig;
import com.github.kaklakariada.aws.sam.service.DeployService;

public class DeleteStackTask extends DefaultTask {

	@Input
	public SamConfig config;

	@TaskAction
	public void uploadFileToS3() throws IOException, InterruptedException {
		final DeployService deployService = new DeployService(config);
		deployService.deleteStack(config.getStackName());
	}
}
