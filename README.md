# aws-sam-gradle
Gradle plugin for deploying Serverless Java applications using AWS [Serverless Application Models (SAM)](https://github.com/awslabs/serverless-application-model) via CloudFormation templates.

## Usage

### Initial setup

1. Add this plugin to your build script, see https://plugins.gradle.org/plugin/com.github.kaklakariada.aws-sam-deploy

  ```groovy
plugins {
	id 'java'
	id 'com.github.kaklakariada.aws-sam-deploy' version '0.3.0'
}
```
2. Configure your AWS credentials in `~/.aws/`, see https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html#cli-config-files
3. Create a SAM cloud formation template, see https://github.com/awslabs/serverless-application-model
4. Optionally: create a swagger interface definition, see http://swagger.io/specification/
5. Create file `gradle.properties` in your project dir with the following entries:
   * `awsDeploymentBucket`: Bucket used for uploading lambda code and swagger definition, e.g. `my-deployment-bucket`
   * `awsRegion`: AWS region in which to deploy your application, e.g. `eu-west-1`
6. Add a `serverless` section to your build script and configure your stages:

  ```groovy
ext {
	deployStage = project.hasProperty('stage') ? project.properties['stage'] : 'test'
}

serverless {
	currentStage = deployStage
	defaultAwsProfile = 'default'
	defaultAwsRegion = awsRegion
	defaultDeploymentBucket = awsDeploymentBucket
	stages {
		test {
			// use default values
		}
		prelive {
			awsRegion = 'eu-west-1'
			awsProfile = 'prelive-profile'
			deploymentBucket = 'prelive-bucket'
		}
	}
	api {
		stackName = "${project.name}-${deployStage}"
		samTemplate = file('template.yml')
	}
}
```

### Required permissions

```bash
aws --profile $profile iam create-policy-version --policy-arn $policy_arn --policy-document file://iam-deploy-policy.json --set-as-default
```

### Deployment

Deploy your app with `./gradlew -Pstage=<myStage> deploy -i`.

This will
* Build a zip of your application including dependencies
* Upload the zip to the configured S3 bucket
* Replace some placeholders in your swagger definition and upload it to S3
* Replace some placeholders in your CloudFormation template and create a change set
* Execute the change set to create/update the CloudFormation stack

### Delete stack

Delete your complete stack with `./gradlew -Pstage=<myStage> deleteStack -i`.

Be careful, this will may also delete DynamoDB tables.

## Example projects
* [example-project-minimal](https://github.com/kaklakariada/aws-sam-gradle/tree/master/example-project-minimal): minimal project
* [example-project-swagger](https://github.com/kaklakariada/aws-sam-gradle/tree/master/example-project-swagger): minimal project with swagger definition in separate file
* [example-project-inline-swagger](https://github.com/kaklakariada/aws-sam-gradle/tree/master/example-project-inline-swagger): minimal project with inline swagger definition in CloudFormation template

## Development

```bash
$ git clone https://github.com/kaklakariada/aws-sam-gradle.git
```

### Using eclipse

Import into eclipse using [buildship](https://projects.eclipse.org/projects/tools.buildship).

### Generate license header for added files:

```bash
$ ./gradlew licenseFormat
```
### Publish to [plugins.gradle.org](https://plugins.gradle.org)

See https://plugins.gradle.org/docs/submit for details.

1. Add API Key from https://plugins.gradle.org to `~/.gradle/gradle.properties`:

    ```
    gradle.publish.key = ...
    gradle.publish.secret = ...
    ```
2. Run `./gradlew publishPlugins`
