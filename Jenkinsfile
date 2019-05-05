pipeline {
  agent any
  stages {
    stage('package') {
      steps {
         withSonarQubeEnv('SonarCloud') {
            withMaven(maven: 'M3') {
               sh 'mvn clean package sonar:sonar -Dsonar.projectKey=vinscom_glue -Dsonar.organization=vinscom-github'
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
