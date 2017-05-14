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
package com.github.kaklakariada.aws.sam;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;

import java.io.File;
import java.util.function.Consumer;

import org.gradle.api.DefaultTask;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.CopySpec;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.tasks.bundling.Zip;
import org.slf4j.Logger;

import com.amazonaws.services.cloudformation.model.Parameter;
import com.github.kaklakariada.aws.sam.config.SamConfig;
import com.github.kaklakariada.aws.sam.config.Stage;
import com.github.kaklakariada.aws.sam.service.AwsMetadataService;
import com.github.kaklakariada.aws.sam.task.DeleteStackTask;
import com.github.kaklakariada.aws.sam.task.DeployTask;
import com.github.kaklakariada.aws.sam.task.ReplacePlaceholerTask;
import com.github.kaklakariada.aws.sam.task.S3UploadTask;
import com.github.kaklakariada.aws.sam.task.WriteStackOutputToFileTask;

import groovy.lang.Closure;

public class AwsSamDeployPlugin implements Plugin<Project> {
	private static final String TASK_GROUP = "deploy";
	private Project project;
	private SamConfig config;
	private Logger log;

	@Override
	public void apply(Project project) {
		this.project = project;
		this.log = project.getLogger();
		this.config = createConfigDsl();
		log.info("Initialize AwsSam plugin...");
		project.afterEvaluate((p) -> createTasks());
	}

	private SamConfig createConfigDsl() {
		log.debug("Setup serverless config DSL");
		final NamedDomainObjectContainer<Stage> stages = project.container(Stage.class);
		final SamConfig samConfig = project.getExtensions().create("serverless", SamConfig.class, project, stages);
		((ExtensionAware) samConfig).getExtensions().add("stages", stages);
		return samConfig;
	}

	private void createTasks() {
		log.info("Creating tasks using config {}", config);
		if (config.api == null) {
			throw new IllegalStateException("serverless.api configuration is missing");
		}
		final Zip zipTask = createBuildZipTask();
		final S3UploadTask uploadZipTask = createUploadZipTask(zipTask);
		S3UploadTask uploadSwaggerTask = null;
		if (config.api.swaggerDefinition != null) {
			final ReplacePlaceholerTask updateSwaggerTask = createUpdateSwaggerTask(config.api.swaggerDefinition);
			uploadSwaggerTask = createUploadSwaggerTask(updateSwaggerTask);
		}
		final DeployTask deployTask = createDeployTask(uploadZipTask, uploadSwaggerTask);
		createWriteStackOutputTask(deployTask);
		createDeleteStackTask();
	}

	private void createWriteStackOutputTask(DeployTask deployTask) {
		final WriteStackOutputToFileTask task = createTask("writeStackOutput", WriteStackOutputToFileTask.class);
		task.setDescription("Write stack output to properties file");
		task.setGroup(TASK_GROUP);
		task.config = config;
		task.outputFile = new File(project.getBuildDir(), "stack-output.properties");
		task.dependsOn(deployTask);
	}

	private DeployTask createDeployTask(S3UploadTask uploadZipTask, S3UploadTask uploadSwaggerTask) {
		final DeployTask task = createTask("deploy", DeployTask.class);
		task.setDescription("Deploy stack to AWS");
		task.setGroup(TASK_GROUP);
		if (uploadSwaggerTask != null) {
			task.dependsOn(uploadSwaggerTask);
			task.swaggerUri = uploadSwaggerTask.getS3Url();
		}
		task.dependsOn(uploadZipTask);
		task.config = config;
		task.codeUri = uploadZipTask.getS3Url();
		return task;
	}

	private ReplacePlaceholerTask createUpdateSwaggerTask(File swaggerDefinition) {
		final ReplacePlaceholerTask task = createTask("updateSwagger", ReplacePlaceholerTask.class);
		final AwsMetadataService awsMetadataService = new AwsMetadataService(config);
		task.parameters = asList(
				new Parameter().withParameterKey("region").withParameterValue(config.getRegion().getName()),
				new Parameter().withParameterKey("accountId").withParameterValue(awsMetadataService.getAccountId()));
		task.input = swaggerDefinition;
		task.output = new File(project.getBuildDir(), task.input.getName());
		return task;
	}

	private S3UploadTask createUploadZipTask(final Zip zipTask) {
		final S3UploadTask task = createTask("uploadZip", S3UploadTask.class);
		task.setDescription("Upload lambda zip to s3");
		task.setGroup(TASK_GROUP);
		task.dependsOn(zipTask);
		task.config = config;
		task.file = zipTask.getOutputs().getFiles().getSingleFile();
		return task;
	}

	private S3UploadTask createUploadSwaggerTask(final ReplacePlaceholerTask updateTask) {
		final S3UploadTask task = createTask("uploadSwager", S3UploadTask.class);
		task.setDescription("Upload swagger definition to s3");
		task.setGroup(TASK_GROUP);
		task.dependsOn(updateTask);
		task.config = config;
		task.file = updateTask.output;
		return task;
	}

	private Zip createBuildZipTask() {
		final Zip task = createTask("buildZip", Zip.class);
		task.setDescription("Build lambda zip");
		task.setGroup(TASK_GROUP);
		task.setBaseName(project.getName());
		task.into("lib", closure(task, CopySpec.class, (delegate) -> {
			delegate.from(project.getConfigurations().getByName("runtime"));
		}));
		task.into("", closure(task, CopySpec.class, (delegate) -> {
			delegate.from(project.getTasks().getByPath(":compileJava"),
					project.getTasks().getByPath(":processResources"));
		}));
		return task;
	}

	private void createDeleteStackTask() {
		final DeleteStackTask task = createTask("deleteStack", DeleteStackTask.class);
		task.setDescription("Delete cloudformation stack");
		task.setGroup(TASK_GROUP);
		task.config = config;
	}

	private <T extends DefaultTask> T createTask(String taskName, Class<T> taskType) {
		return taskType.cast(project.task(singletonMap("type", taskType), taskName));
	}

	private <T> Closure<Void> closure(Object thisObject, Class<T> delegateType, Consumer<T> code) {
		return new Closure<Void>(this, thisObject) {
			private static final long serialVersionUID = 1L;

			@Override
			public Void call() {
				final T delegate = delegateType.cast(getDelegate());
				code.accept(delegate);
				return null;
			}
		};
	}
}
