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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
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

	@Internal
	private AmazonS3 s3Client;

	public S3UploadTask() {
		getOutputs().upToDateWhen(this::upToDateWhen);
	}

	private boolean upToDateWhen(Task task) {
		return objectExistsInBucket(calculateS3Key());
	}

	private static byte[] createChecksum(File file) {
		try (InputStream fis = new FileInputStream(file)) {

			final byte[] buffer = new byte[1024];
			final MessageDigest complete = MessageDigest.getInstance("MD5");
			int numRead;

			do {
				numRead = fis.read(buffer);
				if (numRead > 0) {
					complete.update(buffer, 0, numRead);
				}
			} while (numRead != -1);

			return complete.digest();
		} catch (final IOException | NoSuchAlgorithmException e) {
			throw new RuntimeException("Error calculating md5 sum for file " + file, e);
		}
	}

	private static String getMD5Checksum(File file) {
		final byte[] b = createChecksum(file);
		return convertBytesToString(b);
	}

	private static String convertBytesToString(final byte[] b) {
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	@TaskAction
	public void uploadFileToS3() {
		upload(calculateS3Key());
	}

	private AmazonS3 getS3Client() {
		if (s3Client == null) {
			s3Client = config.getAwsClientFactory().create(AmazonS3Client.builder());
		}
		return s3Client;
	}

	@Internal
	public String getS3Url() {
		return "s3://" + config.getDeploymentBucket() + "/" + calculateS3Key();
	}

	private String calculateS3Key() {
		final String version = getProject().getVersion().toString();
		final String md5Checksum = getMD5Checksum(file);
		return getProject().getName() + "/" + version + "/" + md5Checksum + "/" + file.getName();
	}

	private void upload(final String key) {
		if (!objectExistsInBucket(key)) {
			transferFileToS3(key);
		}
	}

	private boolean objectExistsInBucket(final String key) {
		return getS3Client().doesObjectExist(config.getDeploymentBucket(), key);
	}

	private void transferFileToS3(final String key) {
		final long fileSizeMb = file.length() / (1024 * 1024);
		getLogger().info("Uploading {} MB from file {} to {}", fileSizeMb, file, getS3Url());
		final TransferManager transferManager = createTransferManager();
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

	private TransferManager createTransferManager() {
		return TransferManagerBuilder.standard().withS3Client(getS3Client()).build();
	}
}
