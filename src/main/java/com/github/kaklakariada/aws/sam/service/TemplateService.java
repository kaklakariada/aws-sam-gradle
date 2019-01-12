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
package com.github.kaklakariada.aws.sam.service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;

import org.slf4j.Logger;

import com.amazonaws.services.cloudformation.model.Parameter;
import com.github.kaklakariada.aws.sam.DeploymentException;

public class TemplateService {

	private final Logger logger;

	public TemplateService(Logger logger) {
		this.logger = logger;
	}

	private static final Charset CHARSET_UTF8 = StandardCharsets.UTF_8;

	public String loadFile(Path path) {
		logger.debug("Loading file {}", path);
		try {
			return new String(Files.readAllBytes(path), CHARSET_UTF8);
		} catch (final IOException e) {
			throw new DeploymentException("Error reading from " + path.toAbsolutePath(), e);
		}
	}

	public void writeFile(String content, Path path) {
		logger.debug("Writing file to {}", path);
		try {
			if (Files.notExists(path.getParent())) {
				Files.createDirectories(path.getParent());
			}
			Files.write(path, content.getBytes(CHARSET_UTF8), StandardOpenOption.CREATE);
		} catch (final IOException e) {
			throw new DeploymentException("Error writing to " + path.toAbsolutePath(), e);
		}
	}

	public String replaceParameters(String original, Collection<Parameter> parameters) {
		String result = original;
		for (final Parameter param : parameters) {
			logger.debug("Replace '{}' with '{}'", param.getParameterKey(), param.getParameterValue());
			result = result.replace("${" + param.getParameterKey() + "}", param.getParameterValue());
		}
		return result;
	}
}
