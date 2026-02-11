pipeline {
    agent any

    environment {
        IMAGE_NAME = "sanimup-api"
        IMAGE_TAG = "${BUILD_NUMBER}"
        GOOGLE_CLIENT_ID = credentials('id-google-secret')
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
                # 1. Cria uma rede para o teste
                docker network create jenkins-test-net || true
        
                # 2. Roda a API nessa rede com um NOME fixo
                docker run -d \
                  --network jenkins-test-net \
                  --name test-api \
                  -e SPRING_PROFILES_ACTIVE=ci \
                  -e GOOGLE_CLIENT_ID=ci-${GOOGLE_CLIENT_ID} \
                  $IMAGE_NAME:$IMAGE_TAG
        
                # 3. Roda o CURL a partir de OUTRO container na MESMA rede
                # Isso evita o problema do localhost
                echo "Aguardando..."
                sleep 15 
                
                docker run --network jenkins-test-net --rm curlimages/curl \
                  curl -s -f http://test-api:8080/actuator/health
                
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