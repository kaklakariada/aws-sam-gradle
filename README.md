# aws-sam-gradle
Gradle plugin for deploying Serverless Java applications using AWS [Serverless Application Models (SAM)](https://github.com/awslabs/serverless-application-model) via CloudFormation.

## Usage

1. [Add this plugin](https://plugins.gradle.org/plugin/com.github.kaklakariada.aws-sam-deploy) to your build script
```gradle
plugins {
    id 'java'
    id 'com.github.kaklakariada.aws-sam-deploy' version '0.0.3'
}
```
2. Configure your AWS credentials in `~/.aws/`, see https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html#cli-config-files
3. Create a SAM cloud formation template, see https://github.com/awslabs/serverless-application-model
4. Optionally: create a swagger interface definition, see http://swagger.io/specification/
5. Add a `serverless` section to your build script
```gradle
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
            awsProfile = 'default'
            deploymentBucket = awsDeploymentBucket
        }
    }
    api {
        stackName = "${project.name}-${deployStage}"
        samTemplate = file('template.yml')
    }
}
```
6. Deploy your app with `./gradlew -Pstage=<myStage> deploy -i`
