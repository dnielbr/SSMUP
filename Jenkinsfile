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
                docker network create jenkins-test-net || true

                docker rm -f test-api || true

                echo "Iniciando container da API..."
                docker run -d \
                  --network jenkins-test-net \
                  --name test-api \
                  -e SPRING_PROFILES_ACTIVE=ci \
                  -e GOOGLE_CLIENT_ID="ci-${GOOGLE_CLIENT_ID}" \
                  $IMAGE_NAME:$IMAGE_TAG

                echo "Aguardando aplicação inicializar (Health Check)..."
                
                MAX_RETRIES=12  # 12 * 5s = 60 segundos
                COUNT=0
                
                until docker run --network jenkins-test-net --rm curlimages/curl -s -f http://test-api:8080/actuator/health > /dev/null; do
                    if [ $COUNT -ge $MAX_RETRIES ]; then
                        echo "❌ Timeout: A aplicação não respondeu após 60 segundos."
                        echo "--- LOGS DO CONTAINER ---"
                        docker logs test-api
                        docker rm -f test-api
                        exit 1
                    fi
                    
                    echo "Tentativa $((COUNT+1))/$MAX_RETRIES: Aguardando 5s..."
                    sleep 5
                    COUNT=$((COUNT+1))
                done

                echo "✅ Aplicação respondeu! Validando status final..."
                docker run --network jenkins-test-net --rm curlimages/curl -s http://test-api:8080/actuator/health
                
                docker rm -f test-api
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
                    docker tag \$IMAGE_NAME:\$IMAGE_TAG \$DOCKERHUB_REPO:latest
    
                    docker push \$DOCKERHUB_REPO:\$IMAGE_TAG
                    docker push \$DOCKERHUB_REPO:latest
    
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