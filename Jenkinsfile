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
				bat 'mvn clean install'
			}
		}
		stage('Test') {
			steps {
				bat 'mvn test'
			}
		}
		stage('Code Coverage') {
			steps {
				bat 'mvn jacoco:report'
			}
		}
		stage('Publish Test Results') {
			steps {
				junit '**/target/surefire-reports/*.xml'
			}
		}
		stage('Publish Coverage Report') {
			steps {
				jacoco()
			}
		}
	}
}