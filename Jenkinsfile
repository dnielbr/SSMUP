pipeline {
    agent any

    environment {
        IMAGE_NAME = "sanimup-api"
        IMAGE_TAG = "${BUILD_NUMBER}"
        // Credencial segura do Jenkins
        GOOGLE_CLIENT_ID = credentials('id-google-secret')
        DOCKERHUB_REPO = "everaldodaniel123/sanimup-api"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
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
                docker network create jenkins-test-net || true
        
                # ============================
                # Limpa containers antigos
                # ============================
                docker rm -f test-api test-redis test-solr || true
        
                # ============================
                # Subindo Redis
                # ============================
                echo "Subindo Redis..."
                docker run -d \
                  --network jenkins-test-net \
                  --name test-redis \
                  redis:7-alpine
        
                # ============================
                # Subindo Solr
                # ============================
                echo "Subindo Solr..."
                docker run -d \
                  --network jenkins-test-net \
                  --name test-solr \
                  solr:9.4 solr-precreate empresas
        
                echo "Aguardando Solr iniciar..."
                sleep 15
        
                # ============================
                # Subindo API
                # ============================
                echo "Subindo API..."
                docker run -d \
                  --network jenkins-test-net \
                  --name test-api \
                  -e SPRING_PROFILES_ACTIVE=ci \
                  -e SPRING_REDIS_HOST=test-redis \
                  -e SPRING_REDIS_PORT=6379 \
                  -e SPRING_DATA_SOLR_HOST=http://test-solr:8983/solr \
                  -e SPRING_DATASOURCE_URL=jdbc:h2:mem:testdb \
                  -e GOOGLE_CLIENT_ID="ci-${GOOGLE_CLIENT_ID}" \
                  $IMAGE_NAME:$IMAGE_TAG
        
                echo "Aguardando aplicação inicializar (Health Check)..."
                
                MAX_RETRIES=12
                COUNT=0
                
                until docker run --network jenkins-test-net --rm curlimages/curl -s -f http://test-api:8080/actuator/health > /dev/null; do
                    if [ $COUNT -ge $MAX_RETRIES ]; then
                        echo "❌ Timeout: aplicação não respondeu."
                        echo "--- LOGS API ---"
                        docker logs test-api
                        echo "--- LOGS SOLR ---"
                        docker logs test-solr
                        echo "--- LOGS REDIS ---"
                        docker logs test-redis
                        docker rm -f test-api test-solr test-redis
                        exit 1
                    fi
                    
                    echo "Tentativa $((COUNT+1))/$MAX_RETRIES..."
                    sleep 5
                    COUNT=$((COUNT+1))
                done
        
                echo "✅ Aplicação respondeu!"
                docker run --network jenkins-test-net --rm curlimages/curl -s http://test-api:8080/actuator/health
        
                # Cleanup
                docker rm -f test-api test-solr test-redis
                '''
            }
        }
        stage('Push in Docker Hub') {
            steps {
                withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-cred',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh """
                    echo "Iniciando login no Docker Hub..."
    
                    echo \$DOCKER_PASS | docker login -u \$DOCKER_USER --password-stdin
    
                    docker tag \$IMAGE_NAME:\$IMAGE_TAG \$DOCKERHUB_REPO:\$IMAGE_TAG
                    #docker tag \$IMAGE_NAME:\$IMAGE_TAG \$DOCKERHUB_REPO:latest
    
                    docker push \$DOCKERHUB_REPO:\$IMAGE_TAG
                    #docker push \$DOCKERHUB_REPO:latest
    
                    docker logout
    
                    echo "Login finalizado com sucesso"
                """
                }
            }
        }


    } // <--- ESSA CHAVE ESTAVA FALTANDO OU NO LUGAR ERRADO

    post {
        success {
            echo "✅ Build e validação concluídos com sucesso!"
        }
        failure {
            echo "❌ Pipeline falhou!"
        }
    }
}