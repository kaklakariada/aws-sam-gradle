package com.github.kaklakariada.aws.sam.service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;

import org.gradle.api.logging.Logging;
import org.slf4j.Logger;

import com.amazonaws.services.cloudformation.model.Parameter;
import com.github.kaklakariada.aws.sam.DeploymentException;

public class TemplateService {

	private static final Logger LOG = Logging.getLogger(TemplateService.class);
	private static final Charset CHARSET_UTF8 = StandardCharsets.UTF_8;

	public String loadFile(Path path) {
		LOG.debug("Loading file {}", path);
		try {
			return new String(Files.readAllBytes(path), CHARSET_UTF8);
		} catch (final IOException e) {
			throw new DeploymentException("Error reading from " + path.toAbsolutePath(), e);
		}
	}

	public void writeFile(String content, Path path) {
		LOG.debug("Writing file to {}", path);
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
			LOG.debug("Replace '{}' with '{}'", param.getParameterKey(), param.getParameterValue());
			result = result.replace("${" + param.getParameterKey() + "}", param.getParameterValue());
		}
		return result;
	}
}
