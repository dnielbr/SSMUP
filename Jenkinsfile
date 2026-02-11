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
                docker rm -f test-api || true

                # Inicia o container com a variável GOOGLE_CLIENT_ID FAKE
                docker run -d \
                  --name test-api \
                  -e SPRING_PROFILES_ACTIVE=ci \
                  -e GOOGLE_CLIENT_ID=ci-${GOOGLE_CLIENT_ID}  \
                  -p 8082:8080 \
                  $IMAGE_NAME:$IMAGE_TAG

                echo "Aguardando aplicação inicializar..."

                # Loop de verificação (Retry)
                MAX_RETRIES=12
                COUNT=0
                until curl -s -f http://localhost:8082/actuator/health > /dev/null; do
                    if [ $COUNT -ge $MAX_RETRIES ]; then
                        echo "Timeout: A aplicação não respondeu após 60 segundos."
                        docker logs test-api
                        exit 1
                    fi
                    echo "Tentativa $((COUNT+1))/$MAX_RETRIES: Aguardando 5s..."
                    sleep 5
                    COUNT=$((COUNT+1))
                done

                echo "Aplicação respondeu! Validando status..."
                curl --fail http://localhost:8082/actuator/health

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