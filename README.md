# aws-sam-gradle
Gradle plugin for deploying Serverless Java applications using AWS [Serverless Application Models (SAM)](https://github.com/awslabs/serverless-application-model) via CloudFormation.

## Usage

### Initial setup

1. Add this plugin to your build script, see https://plugins.gradle.org/plugin/com.github.kaklakariada.aws-sam-deploy

  ```gradle
plugins {
    id 'java'
    id 'com.github.kaklakariada.aws-sam-deploy' version '0.1.1'
}
```
2. Configure your AWS credentials in `~/.aws/`, see https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html#cli-config-files
3. Create a SAM cloud formation template, see https://github.com/awslabs/serverless-application-model
4. Optionally: create a swagger interface definition, see http://swagger.io/specification/
5. Create file `gradle.properties` in your project dir with the following entries:
  * `awsDeploymentBucket`: Bucket used for uploading lambda code and swagger definition, e.g. `my-deployment-bucket`
  * `awsRegion`: AWS region in which to deploy your application, e.g. `eu-west-1`
6. Add a `serverless` section to your build script and configure your stages:

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

### Deployment

Deploy your app with `./gradlew -Pstage=<myStage> deploy -i`.

#### Delete stack

Deploy your app with `./gradlew -Pstage=<myStage> deleteStack -i`.

## Example projects
* [example-project-minimal](https://github.com/kaklakariada/aws-sam-gradle/tree/master/example-project-minimal): minimal project
* [example-project-minimal](https://github.com/kaklakariada/aws-sam-gradle/tree/master/example-project-swagger): minimal project with swagger definition
