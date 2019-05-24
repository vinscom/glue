pipeline {
  agent any
  stages {
    stage('Build and Test') {
      steps {
         withSonarQubeEnv('SonarCloud') {
            withMaven(maven: 'M3') {
               sh "mvn clean package sonar:sonar -Dsonar.projectKey=vinscom_glue -Dsonar.organization=vinscom-github -Dsonar.branch.name=${GIT_BRANCH}"
               sh "echo '-----------------------------------------------${GIT_TAG_NAME}'"
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
  }
}
