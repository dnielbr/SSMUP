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

                # Inicia o container
                docker run -d \
                  --name test-api \
                  -e SPRING_PROFILES_ACTIVE=ci \
                  -p 8082:8080 \
                  $IMAGE_NAME:$IMAGE_TAG

                echo "Aguardando aplicação inicializar..."

                # Loop de verificação: Tenta conectar a cada 5 segundos, por até 1 minuto
                MAX_RETRIES=12
                COUNT=0
                until curl -s -f http://localhost:8082/actuator/health > /dev/null; do
                    if [ $COUNT -ge $MAX_RETRIES ]; then
                        echo "Timeout: A aplicação não respondeu após 60 segundos."
                        docker logs test-api # Mostra logs para debug em caso de falha
                        exit 1
                    fi
                    echo "Tentativa $((COUNT+1))/$MAX_RETRIES: Aguardando 5s..."
                    sleep 5
                    COUNT=$((COUNT+1))
                done

                echo "Aplicação respondeu! Validando status..."
                
                # Validação final para garantir que o retorno é o esperado
                curl --fail http://localhost:8082/actuator/health

                # Limpeza
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