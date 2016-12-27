package com.github.kaklakariada.aws.sam;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Test;

public class PluginTest {

	private static final String STAGE = "test";
	private static final File MINIMAL_PROJECECT_DIR = new File("example-project-minimal");

	@Test
	public void testBuildSuccess() {
		final BuildResult buildResult = runBuild(MINIMAL_PROJECECT_DIR, //
				"-Pstage" + STAGE, "deploy", "--info", "--stacktrace");
		assertEquals(buildResult.task(":deploy").getOutcome(), TaskOutcome.SUCCESS);
	}

	private BuildResult runBuild(File projectDir, String... arguments) {
		final BuildResult buildResult = GradleRunner.create().withProjectDir(projectDir.getAbsoluteFile()) //
				.withPluginClasspath() //
				.withArguments(arguments) //
				.forwardOutput() //
				.build();
		return buildResult;
	}
}
