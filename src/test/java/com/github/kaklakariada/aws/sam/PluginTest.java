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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.gradle.api.logging.Logging;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Test;
import org.slf4j.Logger;

public class PluginTest {
	private static final Logger LOG = Logging.getLogger(PluginTest.class);

	private static final String STAGE = "test";
	private static final File MINIMAL_PROJECT_DIR = new File("example-project-minimal");
	private static final File SWAGGER_PROJECT_DIR = new File("example-project-swagger");
	private static final File INLINE_SWAGGER_PROJECT_DIR = new File("example-project-inline-swagger");
	private BuildResult buildResult;

	@Test
	public void testDeployMinimalApp() {
		runBuild(MINIMAL_PROJECT_DIR, //
				"-Pstage" + STAGE, "clean", "deploy", "--info", "--stacktrace");
		assertEquals(buildResult.task(":deploy").getOutcome(), TaskOutcome.SUCCESS);
	}

	@Test
	public void testDeleteStack() {
		runBuild(MINIMAL_PROJECT_DIR, //
				"-Pstage" + STAGE, "clean", "deploy");
		assertEquals(buildResult.task(":deploy").getOutcome(), TaskOutcome.SUCCESS);
		runBuild(MINIMAL_PROJECT_DIR, //
				"-Pstage" + STAGE, "deleteStack");
		assertEquals(buildResult.task(":deleteStack").getOutcome(), TaskOutcome.SUCCESS);
	}

	@Test
	public void testDeploySwaggerApp() throws ClientProtocolException, IOException {
		runBuild(SWAGGER_PROJECT_DIR, //
				"-Pstage" + STAGE, "clean", "deploy", "--info", "--stacktrace");
		assertEquals(buildResult.task(":deploy").getOutcome(), TaskOutcome.SUCCESS);
		final String apiUrl = getStackOutput("ApiUrl");

		final String serviceResult = getWebServiceResult(apiUrl + "/hello");
		assertEquals("Hello world!", serviceResult);
	}

	@Test
	public void testDeployInlineSwaggerApp() throws ClientProtocolException, IOException {
		runBuild(INLINE_SWAGGER_PROJECT_DIR, //
				"-Pstage" + STAGE, "clean", "deploy", "--info", "--stacktrace");
		assertEquals(buildResult.task(":deploy").getOutcome(), TaskOutcome.SUCCESS);
		final String apiUrl = getStackOutput("ApiUrl");

		final String serviceResult = getWebServiceResult(apiUrl + "/hello");
		assertEquals("Hello world!", serviceResult);
	}

	private String getWebServiceResult(final String url) throws IOException, ClientProtocolException {
		final CloseableHttpClient httpclient = HttpClients.createDefault();
		final CloseableHttpResponse response = httpclient.execute(new HttpGet(url));
		assertEquals(200, response.getStatusLine().getStatusCode());
		return readStream(response.getEntity().getContent());
	}

	private String readStream(InputStream input) {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
			return buffer.lines().collect(Collectors.joining("\n"));
		} catch (final IOException e) {
			throw new AssertionError("Error reading input stream", e);
		}
	}

	private String getStackOutput(String outputParamName) {
		final Pattern pattern = Pattern.compile("(?ms).*^Stack output " + outputParamName + " = ([^\n\r]*)$.*");
		final Matcher matcher = pattern.matcher(buildResult.getOutput());
		assertTrue("Pattern '" + pattern + "' does not match output", matcher.matches());
		final String outputValue = matcher.group(1);
		LOG.info("Got output parameter '{}' = '{}'", outputParamName, outputValue);
		return outputValue;
	}

	private void runBuild(File projectDir, String... arguments) {
		buildResult = GradleRunner.create().withProjectDir(projectDir.getAbsoluteFile()) //
				.withPluginClasspath() //
				.withArguments(arguments) //
				.forwardOutput() //
				.build();
	}
}
