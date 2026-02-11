pipeline {
    agent any

    environment {
        IMAGE_NAME = "sanimup-api"
        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Jar (Maven)') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t $IMAGE_NAME:$IMAGE_TAG .'
            }
        }

        stage('Validate Image') {
            steps {
                sh '''
                docker rm -f test-api || true

                docker run -d \
                  --name test-api \
                  -e SPRING_PROFILES_ACTIVE=ci \
                  -p 8082:8080 \
                  $IMAGE_NAME:$IMAGE_TAG

                echo "Aguardando aplicação subir..."
                sleep 20

                curl --fail http://localhost:9999/actuator/health

                docker rm -f test-api
                '''
            }
        }
    }

    post {
        success {
            echo "✅ Build e validação concluídos com sucesso!"
        }
        failure {
            echo "❌ Pipeline falhou!"
        }
    }
}