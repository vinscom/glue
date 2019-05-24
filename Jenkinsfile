pipeline {
  agent any
  stages {
    stage('Deploy Snapshot') {
      steps {
         withSonarQubeEnv('SonarCloud') {
            withMaven(maven: 'M3') {
               sh "mvn clean deploy sonar:sonar -Dsonar.projectKey=vinscom_glue -Dsonar.organization=vinscom-github -Dsonar.branch.name=${GIT_BRANCH} -Ppgp"
            }
         }
      }
    }
    stage("Quality Gate") {
      steps {
         junit 'target/surefire-reports/TEST-*.xml'
      }
    }
  }
}
