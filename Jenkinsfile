pipeline {
  agent any
  stages {
    stage('package') {
      steps {
        withMaven(maven: 'M3') {
          sh 'java -version'
          sh 'mvn clean install'
          sh 'mvn sonar:sonar'
        }
      }
    }
    stage('Result') {
      steps {
        junit 'target/surefire-reports/TEST-*.xml'
      }
    }
  }
}
