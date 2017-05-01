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
import java.time.Duration;
import java.time.Instant;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.github.kaklakariada.aws.sam.config.SamConfig;

public class S3UploadTask extends DefaultTask {

	@InputFile
	public File file;

	@Input
	public SamConfig config;

	@TaskAction
	public void uploadFileToS3() {
		final AmazonS3 s3Client = config.getAwsClientFactory().create(AmazonS3Client.builder());
		upload(s3Client, calculateS3Key());
	}

	@Internal
	public String getS3Url() {
		return "s3://" + config.getDeploymentBucket() + "/" + calculateS3Key();
	}

	private String calculateS3Key() {
		final String version = getProject().getVersion().toString();
		return getProject().getName() + "/" + version + "/" + config.getDeploymentTimestamp() + "/" + file.getName();
	}

	private void upload(final AmazonS3 s3Client, final String key) {
		if (!s3Client.doesObjectExist(config.getDeploymentBucket(), key)) {
			transferFileToS3(s3Client, key);
		}
	}

	private void transferFileToS3(final AmazonS3 s3Client, final String key) {
		final long fileSizeMb = file.length() / (1024 * 1024);
		getLogger().info("Uploading {} MB from file {} to {}", fileSizeMb, file, getS3Url());
		final TransferManager transferManager = TransferManagerBuilder.standard().withS3Client(s3Client).build();
		final Instant start = Instant.now();
		final Upload upload = transferManager.upload(config.getDeploymentBucket(), key, file);
		try {
			upload.waitForCompletion();
			getLogger().info("Uploaded {} to {} in {}", file, getS3Url(), Duration.between(start, Instant.now()));
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new AssertionError("Upload interrupted", e);
		}
	}
}
