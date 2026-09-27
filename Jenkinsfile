pipeline {
	agent any
	stages {
		stage('Checkout') {
			steps {
				git branch: 'main'
				git 'https://github.com/SKW5/Ohjelmistotuotantoprojekti-1-R2'
			}
		}
		stage('Build') {
			steps {
				sh 'mvn clean install' // sh for linux and ios
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
				jacoco()
			}
		}
	}
}