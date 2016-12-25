package com.github.kaklakariada.aws.sam.task;

import java.io.File;
import java.util.Collection;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.cloudformation.model.Parameter;
import com.github.kaklakariada.aws.sam.service.TemplateService;

public class ReplacePlaceholerTask extends DefaultTask {
	@InputFile
	public File input;
	@OutputFile
	public File output;
	@Input
	public Collection<Parameter> parameters;

	@TaskAction
	public void replace() {
		final TemplateService service = new TemplateService();
		final String content = service.loadFile(input.toPath());
		final String result = service.replaceParameters(content, parameters);
		service.writeFile(result, output.toPath());
	}
}
