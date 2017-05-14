package com.github.kaklakariada.aws.sam.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.cloudformation.model.Output;
import com.github.kaklakariada.aws.sam.config.SamConfig;
import com.github.kaklakariada.aws.sam.service.DeployService;

public class WriteStackOutputToFileTask extends DefaultTask {

	@OutputFile
	public File outputFile;
	@Input
	public SamConfig config;

	@TaskAction
	public void writeStackOutput() throws IOException, InterruptedException {
		final DeployService deployService = new DeployService(config);
		final List<Output> output = deployService.getStackOutput();
		final Properties prop = new Properties();
		output.forEach(o -> prop.setProperty(o.getOutputKey(), o.getOutputValue()));

		try (Writer writer = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8)) {
			prop.store(writer, "Output of stack " + config.getStackName());
		}
	}
}
