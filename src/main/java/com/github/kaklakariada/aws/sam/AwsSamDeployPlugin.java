package com.github.kaklakariada.aws.sam;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;

import java.io.File;
import java.util.function.Consumer;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.CopySpec;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.tasks.bundling.Zip;
import org.slf4j.Logger;

import com.amazonaws.services.cloudformation.model.Parameter;
import com.github.kaklakariada.aws.sam.config.SamConfig;
import com.github.kaklakariada.aws.sam.config.Stage;
import com.github.kaklakariada.aws.sam.service.AwsMetadataService;
import com.github.kaklakariada.aws.sam.task.DeployTask;
import com.github.kaklakariada.aws.sam.task.ReplacePlaceholerTask;
import com.github.kaklakariada.aws.sam.task.S3UploadTask;

import groovy.lang.Closure;

public class AwsSamDeployPlugin implements Plugin<Project> {
	private static final Logger LOG = Logging.getLogger(AwsSamDeployPlugin.class);
	private static final String TASK_GROUP = "deploy";

	@Override
	public void apply(Project project) {
		LOG.info("Initialize AwsSam plugin...");

		final SamConfig config = createConfigDsl(project);

		config.getProjct().afterEvaluate(new Action<Project>() {

			@Override
			public void execute(Project arg0) {
				final Zip zipTask = createBuildZipTask(project);

				final S3UploadTask uploadZipTask = createUploadZipTask(config, zipTask);

				S3UploadTask uploadSwaggerTask = null;
				if (config.api.swaggerDefinition != null) {
					final ReplacePlaceholerTask updateSwaggerTask = createUpdateSwaggerTask(config);
					uploadSwaggerTask = createUploadSwaggerTask(config, updateSwaggerTask);
				}
				createDeployTask(config, uploadZipTask, uploadSwaggerTask);
			}
		});
	}

	private DeployTask createDeployTask(SamConfig config, S3UploadTask uploadZipTask, S3UploadTask uploadSwaggerTask) {
		final DeployTask task = (DeployTask) config.getProjct().task(singletonMap("type", DeployTask.class), "deploy");
		task.setDescription("Deploy stack to AWS");
		task.setGroup(TASK_GROUP);
		if (uploadSwaggerTask != null) {
			task.dependsOn(uploadZipTask, uploadSwaggerTask);
			task.swaggerUri = uploadSwaggerTask.getS3Url();
		} else {
			task.dependsOn(uploadZipTask);
		}
		task.config = config;
		task.codeUri = uploadZipTask.getS3Url();

		return task;
	}

	private ReplacePlaceholerTask createUpdateSwaggerTask(final SamConfig config) {
		final ReplacePlaceholerTask task = (ReplacePlaceholerTask) config.getProjct()
				.task(singletonMap("type", ReplacePlaceholerTask.class), "updateSwagger");

		final AwsMetadataService awsMetadataService = new AwsMetadataService(config);
		task.parameters = asList(
				new Parameter().withParameterKey("region").withParameterValue(config.getRegion().getName()),
				new Parameter().withParameterKey("accountId").withParameterValue(awsMetadataService.getAccountId()));
		task.input = config.api.swaggerDefinition;
		task.output = new File(config.getProjct().getBuildDir(), task.input.getName());
		return task;
	}

	private S3UploadTask createUploadZipTask(final SamConfig config, final Zip zipTask) {
		final S3UploadTask task = (S3UploadTask) config.getProjct().task(singletonMap("type", S3UploadTask.class),
				"uploadZip");
		task.setDescription("Upload lambda zip to s3");
		task.setGroup(TASK_GROUP);
		task.dependsOn(zipTask);
		task.config = config;
		task.file = zipTask.getOutputs().getFiles().getSingleFile();
		return task;
	}

	private S3UploadTask createUploadSwaggerTask(final SamConfig config, final ReplacePlaceholerTask updateTask) {
		final S3UploadTask task = (S3UploadTask) config.getProjct().task(singletonMap("type", S3UploadTask.class),
				"uploadSwager");
		task.setDescription("Upload swagger definition to s3");
		task.setGroup(TASK_GROUP);
		task.dependsOn(updateTask);
		task.config = config;
		task.file = updateTask.output;
		return task;
	}

	private Zip createBuildZipTask(Project project) {
		final Zip task = (Zip) project.task(singletonMap("type", Zip.class), "buildZip");
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

	private SamConfig createConfigDsl(Project project) {
		final NamedDomainObjectContainer<Stage> stages = project.container(Stage.class);
		final SamConfig samConfig = project.getExtensions().create("sam", SamConfig.class, project, stages);
		((ExtensionAware) samConfig).getExtensions().add("stages", stages);
		return samConfig;
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
