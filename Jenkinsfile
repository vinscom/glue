pipeline {
  agent any
  stages {
    stage('Build and Test') {
      steps {
         withSonarQubeEnv('SonarCloud') {
            withMaven(maven: 'M3') {
               sh "mvn clean package sonar:sonar -Dsonar.projectKey=vinscom_glue -Dsonar.organization=vinscom-github -Dsonar.branch.name=${GIT_BRANCH}"
            }
            junit 'target/surefire-reports/TEST-*.xml'
         }
      }
    }
    stage('Deploy Snapshot') {
      when {
        branch 'master'
      }
      steps {
        withMaven(maven: 'M3') {
          sh "mvn deploy -P pgp,release"
        }
      }
    }
    stage('Deploy Release') {
      /*
      when {
        branch 'master'
        tag '[0-9]+.[0-9]+.[0-9]+'
      }
      */
      steps {
        withMaven(maven: 'M3') {
          sh "mvn versions:set -DnewVersion=${TAG_NAME}"
        }
      }
    }
  }
}
