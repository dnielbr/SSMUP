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
                # 1. Garante que a rede existe (cria se não existir)
                docker network create jenkins-test-net || true
        
                # Limpeza preventiva
                docker rm -f test-api || true
        
                # 2. Inicia o container na rede criada
                echo "Iniciando container da API..."
                docker run -d \
                  --network jenkins-test-net \
                  --name test-api \
                  -e SPRING_PROFILES_ACTIVE=ci \
                  -e GOOGLE_CLIENT_ID="ci-${GOOGLE_CLIENT_ID}" \
                  $IMAGE_NAME:$IMAGE_TAG
        
                # 3. Loop de verificação (Retry Inteligente)
                echo "Aguardando aplicação inicializar (Health Check)..."
                
                MAX_RETRIES=12  # 12 * 5s = 60 segundos de timeout
                COUNT=0
                
                # O comando 'docker run ... curl' retorna 0 (sucesso) se o HTTP code for 200
                # O 'until' roda enquanto o comando falhar
                until docker run --network jenkins-test-net --rm curlimages/curl -s -f http://test-api:8080/actuator/health > /dev/null; do
                    if [ $COUNT -ge $MAX_RETRIES ]; then
                        echo "Timeout: A aplicação não respondeu após 60 segundos."
                        echo "Logs do container:"
                        docker logs test-api
                        
                        # Limpeza antes de falhar
                        docker rm -f test-api
                        exit 1
                    fi
                    
                    echo "Tentativa $((COUNT+1))/$MAX_RETRIES: Aguardando 5s..."
                    sleep 5
                    COUNT=$((COUNT+1))
                done
        
                echo "✅ Aplicação respondeu! Validando status final..."
                # Imprime o JSON do health check para log
                docker run --network jenkins-test-net --rm curlimages/curl -s http://test-api:8080/actuator/health
                
                # 4. Limpeza final
                docker rm -f test-api
                '''
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
}