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
	private BuildResult buildResult;

	@Test
	public void testDeployMinimalApp() {
		runBuild(MINIMAL_PROJECT_DIR, //
				"-Pstage" + STAGE, "deploy", "--info", "--stacktrace");
		assertEquals(buildResult.task(":deploy").getOutcome(), TaskOutcome.SUCCESS);
	}

	@Test
	public void testDeploySwaggerApp() throws ClientProtocolException, IOException {
		runBuild(SWAGGER_PROJECT_DIR, //
				"-Pstage" + STAGE, "deploy", "--info", "--stacktrace");
		assertEquals(buildResult.task(":deploy").getOutcome(), TaskOutcome.SUCCESS);
		final String apiUrl = getStackOutput("ApiUrl");

		final String serviceResult = getWebServiceResult(apiUrl + "/hello");
		assertEquals("blubb", serviceResult);
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
		final Pattern pattern = Pattern.compile("(?m)^Stack output " + outputParamName + " = (.*)$");
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
