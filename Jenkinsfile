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
         timeout(time: 1, unit: 'HOURS') {
            // Parameter indicates whether to set pipeline to UNSTABLE if Quality Gate fails
            // true = set pipeline to UNSTABLE, false = don't
            // Requires SonarQube Scanner for Jenkins 2.7+
            waitForQualityGate abortPipeline: true
         }
         junit 'target/surefire-reports/TEST-*.xml'
      }
    }
  }
}
