pipeline {
	agent any
	options {
		skipDefaultCheckout(true)
	}
	tools {
		jdk 'JDK 21'
		maven 'Maven'
	}
	stages {
		stage('Checkout') {
			steps {
				checkout scm
			}
		}
		stage('Build') {
			steps {
				sh 'mvn clean install'
			}
		}
		stage('Test') {
			steps {
				sh 'mvn test'
			}
		}
		stage('Code Coverage') {
			steps {
				sh 'mvn jacoco:report'
			}
		}
		stage('Publish Test Results') {
			steps {
				junit '**/target/surefire-reports/*.xml'
			}
		}
		stage('Publish Coverage Report') {
			steps {
				archiveArtifacts artifacts: 'target/site/jacoco/**', fingerprint: true
			}
		}
	}
}