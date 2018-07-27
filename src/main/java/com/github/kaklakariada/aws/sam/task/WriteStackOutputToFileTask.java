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
import com.github.kaklakariada.aws.sam.DeploymentException;
import com.github.kaklakariada.aws.sam.config.SamConfig;
import com.github.kaklakariada.aws.sam.service.DeployService;

public class WriteStackOutputToFileTask extends DefaultTask {

	@OutputFile
	public File outputFile;
	@Input
	public SamConfig config;

	@TaskAction
	public void writeStackOutput() {
		final DeployService deployService = new DeployService(config);
		final List<Output> output = deployService.getStackOutput();
		final Properties prop = new Properties();
		output.forEach(o -> prop.setProperty(o.getOutputKey(), o.getOutputValue()));

		try (Writer writer = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8)) {
			prop.store(writer, "Output of stack " + config.getStackName());
		} catch (final IOException e) {
			throw new DeploymentException("Error writing to file " + outputFile, e);
		}
	}
}
