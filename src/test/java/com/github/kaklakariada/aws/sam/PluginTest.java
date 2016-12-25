package com.github.kaklakariada.aws.sam;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Test;

public class PluginTest {

	@Test
	public void test() {
		final BuildResult buildResult = GradleRunner.create()
				.withProjectDir(new File("example-project-minimal").getAbsoluteFile()) //
				.withPluginClasspath() //
				.withArguments("deploy", "-is") //
				.withDebug(true) //
				.build();
		assertEquals(buildResult.task(":deploy").getOutcome(), TaskOutcome.SUCCESS);
	}
}
