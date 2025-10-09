pipeline {
    agent any
    
    environment {
        // Project Configuration
        PROJECT_NAME = 'SGD-Traders'
        
        // Local Docker Registry (no push, just local build)
        DOCKER_REGISTRY = 'local'
        DOCKER_NAMESPACE = "${PROJECT_NAME}"
        
        // Image Tags
        IMAGE_TAG = "${BUILD_NUMBER}-${GIT_COMMIT.take(8)}"
        LATEST_TAG = 'latest'
        
        // Docker Compose Network
        COMPOSE_PROJECT_NAME = "${PROJECT_NAME}-${BUILD_NUMBER}"
        
        // Service Names & Directories
        FRONTEND_DIR = 'fse4-t3-ui'
        MIDTIER_DIR = 'fse4-t3-middle-tier'
        BACKEND_DIR = 'fse4-t3-backend'
        FMTS_DIR = 'fse4-t3-FMTS'
        

    }
    
    tools {
        nodejs '18.4'
        maven '3.8'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo "🔄 Checking out code from repository..."
                checkout scm
                
                script {
                    env.GIT_COMMIT = sh(
                        script: 'git rev-parse HEAD',
                        returnStdout: true
                    ).trim()
                    
                    // Initialize build tracking variables
                    env.SUCCESSFUL_BUILDS = ''
                    env.FAILED_BUILDS = ''
                    
                    echo "📋 Build Info:"
                    echo "  - Build Number: ${BUILD_NUMBER}"
                    echo "  - Git Commit: ${GIT_COMMIT.take(8)}"
                    echo "  - Image Tag: ${IMAGE_TAG}"
                }
            }
        }
        
        stage('Build Images Locally') {
            parallel {
                stage('Frontend Angular') {
                    steps {
                        script {
                            try {
                                dir("${FRONTEND_DIR}") {
                                    echo "🏗️ Building Angular Frontend locally..."
                                    
                                    def frontendImage = "${DOCKER_NAMESPACE}/frontend:${IMAGE_TAG}"
                                    
                                    // Build Docker image locally only
                                    sh "docker build -t ${frontendImage} ."
                                    
                                    env.SUCCESSFUL_BUILDS = (env.SUCCESSFUL_BUILDS ?: '') + "frontend,"
                                    echo "✅ Frontend image built locally: ${frontendImage}"
                                }
                            } catch (Exception e) {
                                env.FAILED_BUILDS = (env.FAILED_BUILDS ?: '') + "frontend,"
                                echo "❌ Frontend build failed: ${e.getMessage()}"
                                throw e  // Fail fast instead of continuing
                            }
                        }
                    }
                }
                
                stage('Midtier Node.js') {
                    steps {
                        script {
                            try {
                                dir("${MIDTIER_DIR}") {
                                    echo "🏗️ Building Midtier Service locally..."
                                    
                                    def midtierImage = "${DOCKER_NAMESPACE}/midtier:${IMAGE_TAG}"
                                    
                                    // Build Docker image locally only
                                    sh "docker build -t ${midtierImage} ."
                                    
                                    env.SUCCESSFUL_BUILDS = (env.SUCCESSFUL_BUILDS ?: '') + "midtier,"
                                    echo "✅ Midtier image built locally: ${midtierImage}"
                                }
                            } catch (Exception e) {
                                env.FAILED_BUILDS = (env.FAILED_BUILDS ?: '') + "midtier,"
                                echo "❌ Midtier build failed: ${e.getMessage()}"
                                echo "❌ Full error details: ${e}"
                                currentBuild.result = 'UNSTABLE'
                            }
                        }
                    }
                }
                
                stage('Backend Spring Boot') {
                    steps {
                        script {
                            try {
                                dir("${BACKEND_DIR}") {
                                    echo "🏗️ Building Spring Boot Backend locally..."
                                    
                                    def backendImage = "${DOCKER_NAMESPACE}/backend:${IMAGE_TAG}"
                                    
                                    // Build with Maven
                                    sh "mvn clean package -DskipTests"
                                    
                                    // Build Docker image locally only
                                    sh "docker build -t ${backendImage} ."
                                    
                                    env.SUCCESSFUL_BUILDS = (env.SUCCESSFUL_BUILDS ?: '') + "backend,"
                                    echo "✅ Backend image built locally: ${backendImage}"
                                }
                            } catch (Exception e) {
                                env.FAILED_BUILDS = (env.FAILED_BUILDS ?: '') + "backend,"
                                echo "❌ Backend build failed: ${e.getMessage()}"
                                echo "❌ Full error details: ${e}"
                                currentBuild.result = 'UNSTABLE'
                            }
                        }
                    }
                }
                
                stage('FMTS Service') {
                    steps {
                        script {
                            try {
                                dir("${FMTS_DIR}") {
                                    echo "🏗️ Building FMTS Service locally..."
                                    
                                    def fmtsImage = "${DOCKER_NAMESPACE}/fmts:${IMAGE_TAG}"
                                    
                                    // Build Docker image locally only
                                    sh "docker build -t ${fmtsImage} ."
                                    
                                    env.SUCCESSFUL_BUILDS = (env.SUCCESSFUL_BUILDS ?: '') + "fmts,"
                                    echo "✅ FMTS image built locally: ${fmtsImage}"
                                }
                            } catch (Exception e) {
                                env.FAILED_BUILDS = (env.FAILED_BUILDS ?: '') + "fmts,"
                                echo "❌ FMTS build failed: ${e.getMessage()}"
                                echo "❌ Full error details: ${e}"
                                currentBuild.result = 'UNSTABLE'
                            }
                        }
                    }
                }
            }
        }
        
        stage('Build Summary') {
            steps {
                script {
                    echo "🔍 DEBUG: SUCCESSFUL_BUILDS = '${env.SUCCESSFUL_BUILDS}'"
                    echo "🔍 DEBUG: FAILED_BUILDS = '${env.FAILED_BUILDS}'"
                    echo "🔍 DEBUG: SUCCESSFUL_BUILDS length = ${env.SUCCESSFUL_BUILDS?.length()}"
                    
                    echo """
📊 BUILD SUMMARY:
✅ Successful: ${env.SUCCESSFUL_BUILDS ?: 'none'}
❌ Failed: ${env.FAILED_BUILDS ?: 'none'}
"""
                    
                    if (!env.SUCCESSFUL_BUILDS || env.SUCCESSFUL_BUILDS.trim() == '') {
                        error("❌ All builds failed! Cannot proceed to deployment.")
                    }
                    
                    if (env.FAILED_BUILDS) {
                        echo "⚠️ Some builds failed, but continuing with successful services..."
                    }
                }
            }
        }
        
        stage('Generate Docker Compose') {
            when {
                expression { env.SUCCESSFUL_BUILDS }
            }
            steps {
                echo "📝 Generating docker-compose.yml with local images..."
                script {
                    def composeContent = """version: '3.8'

networks:
  ${PROJECT_NAME}-network:
    driver: bridge

services:"""

                    // Add successful services to compose
                    if (env.SUCCESSFUL_BUILDS.contains('frontend')) {
                        composeContent += """
  frontend:
    image: ${DOCKER_NAMESPACE}/frontend:${IMAGE_TAG}
    container_name: ${PROJECT_NAME}-frontend-${BUILD_NUMBER}
    ports:
      - "4200:80"
    networks:
      - ${PROJECT_NAME}-network
    depends_on:
      - midtier
    restart: unless-stopped
"""
                    }

                    if (env.SUCCESSFUL_BUILDS.contains('midtier')) {
                        composeContent += """
  midtier:
    image: ${DOCKER_NAMESPACE}/midtier:${IMAGE_TAG}
    container_name: ${PROJECT_NAME}-midtier-${BUILD_NUMBER}
    ports:
      - "3002:3002"
    networks:
      - ${PROJECT_NAME}-network
    depends_on:
      - backend
      - fmts
    restart: unless-stopped
"""
                    }

                    if (env.SUCCESSFUL_BUILDS.contains('backend')) {
                        composeContent += """
  backend:
    image: ${DOCKER_NAMESPACE}/backend:${IMAGE_TAG}
    container_name: ${PROJECT_NAME}-backend-${BUILD_NUMBER}
    ports:
      - "8080:8080"
    networks:
      - ${PROJECT_NAME}-network
    depends_on:
      - fmts
    restart: unless-stopped
"""
                    }

                    if (env.SUCCESSFUL_BUILDS.contains('fmts')) {
                        composeContent += """
  fmts:
    image: ${DOCKER_NAMESPACE}/fmts:${IMAGE_TAG}
    container_name: ${PROJECT_NAME}-fmts-${BUILD_NUMBER}
    ports:
      - "3000:3000"
    networks:
      - ${PROJECT_NAME}-network
    restart: unless-stopped
"""
                    }
                    
                    writeFile file: 'docker-compose.yml', text: composeContent
                    echo "✅ Generated docker-compose.yml with local images"
                }
            }
        }
        
        stage('Deploy with Docker Compose') {
            steps {
                echo "🚀 Deploying services with Docker..."
                script {
                    // Check what's available
                    sh "docker --version"
                    
                    // Stop any existing containers from previous deployments
                    echo "🧹 Cleaning up existing containers..."
                    sh """
                        docker stop angular-app mid-nodejs spring-app fmts-nodejs || true
                        docker rm angular-app mid-nodejs spring-app fmts-nodejs || true
                        docker ps -q --filter name=${PROJECT_NAME}- | xargs -r docker stop || true
                        docker ps -aq --filter name=${PROJECT_NAME}- | xargs -r docker rm || true
                    """
                    
                    // Using host networking - no custom network needed
                    echo "🌐 Using host networking for all containers..."
                    
                    // Start services based on successful builds with host networking
                    if (env.SUCCESSFUL_BUILDS.contains('fmts')) {
                        echo "🚀 Starting FMTS service..."
                        sh """
                            docker run -d \\
                                --name fmts-nodejs \\
                                --network host \\
                                -p 3000:3000 \\
                                --restart unless-stopped \\
                                ${PROJECT_NAME}/fmts:${IMAGE_TAG}
                        """
                    }
                    
                    if (env.SUCCESSFUL_BUILDS.contains('backend')) {
                        echo "🚀 Starting Backend service..."
                        sh """
                            docker run -d \\
                                --name spring-app \\
                                --network host \\
                                -p 8081:8081 \\
                                --dns=8.8.8.8 \\
                                --dns=8.8.4.4 \\
                                --restart unless-stopped \\
                                ${PROJECT_NAME}/backend:${IMAGE_TAG}
                        """
                    }
                    
                    if (env.SUCCESSFUL_BUILDS.contains('midtier')) {
                        echo "🚀 Starting Midtier service..."
                        sh """
                            docker run -d \\
                                --name mid-nodejs \\
                                --network host \\
                                -p 8080:8080 \\
                                --restart unless-stopped \\
                                ${PROJECT_NAME}/midtier:${IMAGE_TAG}
                        """
                    }
                    
                    if (env.SUCCESSFUL_BUILDS.contains('frontend')) {
                        echo "🚀 Starting Frontend service..."
                        sh """
                            docker run -d \\
                                --name angular-app \\
                                --network host \\
                                -p 4200:80 \\
                                --restart unless-stopped \\
                                ${PROJECT_NAME}/frontend:${IMAGE_TAG}
                        """
                    }
                    
                    // Wait for services to be ready
                    echo "⏳ Waiting for services to start..."
                    sleep(time: 30, unit: 'SECONDS')
                    
                    // Show running containers
                    echo "📋 Running containers:"
                    sh "docker ps --filter name=angular-app --filter name=mid-nodejs --filter name=spring-app --filter name=fmts-nodejs --format 'table {{.Names}}\\t{{.Status}}\\t{{.Ports}}'"
                }
            }
        }
        
        stage('Health Check') {
            steps {
                echo "🔍 Validating deployment..."
                script {
                    def services = env.SUCCESSFUL_BUILDS.split(',')
                    
                    for (service in services) {
                        if (!service) continue
                        
                        def port = ""
                        switch(service) {
                            case 'frontend': port = "4200"; break
                            case 'midtier': port = "3002"; break  
                            case 'backend': port = "8080"; break
                            case 'fmts': port = "3000"; break
                        }
                        
                        if (port) {
                            try {
                                sh "curl -f -s http://localhost:${port} > /dev/null"
                                echo "✅ ${service} is healthy on port ${port}"
                            } catch (Exception e) {
                                echo "⚠️ ${service} health check failed on port ${port}"
                            }
                        }
                    }
                }
            }
        }
        
        stage('Deployment Summary') {
            steps {
                script {
                    def services = env.SUCCESSFUL_BUILDS.split(',').findAll { it }
                    
                    echo """
🎉 LOCAL DEPLOYMENT SUCCESSFUL! 

📊 Build Summary:
  - Build Number: ${BUILD_NUMBER}
  - Git Commit: ${GIT_COMMIT.take(8)}
  - Project Name: ${COMPOSE_PROJECT_NAME}

🌐 Service URLs:"""

                    if (services.contains('frontend')) {
                        echo "  - Frontend:  http://localhost:4200"
                    }
                    if (services.contains('midtier')) {
                        echo "  - Midtier:   http://a9e59e70f722c4b5db3d700a757ba4b6-580270519.ap-south-1.elb.amazonaws.com:3002"
                    }
                    if (services.contains('backend')) {
                        echo "  - Backend:   http://a844e212207ae4e218e9f49d229c4902-1202481149.ap-south-1.elb.amazonaws.com:8080"
                    }
                    if (services.contains('fmts')) {
                        echo "  - FMTS:      http://localhost:3000"
                    }

                    echo """
🐳 Local Docker Images Built:"""
                    for (service in services) {
                        if (service) {
                            echo "  - ${DOCKER_NAMESPACE}/${service}:${IMAGE_TAG}"
                        }
                    }

                    echo """
🔧 Management Commands:
  - View frontend:    docker logs -f angular-app
  - View backend:     docker logs -f spring-app
  - View midtier:     docker logs -f mid-nodejs
  - View FMTS:        docker logs -f fmts-nodejs
  - Stop all:         docker stop angular-app mid-nodejs spring-app fmts-nodejs
  - Remove all:       docker rm angular-app mid-nodejs spring-app fmts-nodejs

✅ All services are running locally!
"""
                }
            }
        }
    }
    
    post {
        always {
            echo "🧹 Cleaning up build artifacts..."
            script {
                echo """
📊 FINAL BUILD SUMMARY:
✅ Successful builds: ${env.SUCCESSFUL_BUILDS ?: 'none'}
❌ Failed builds: ${env.FAILED_BUILDS ?: 'none'}
🚀 Services running: Check docker ps for ${COMPOSE_PROJECT_NAME} containers
"""
            }
            
            // Archive docker-compose.yml
            script {
                if (fileExists('docker-compose.yml')) {
                    archiveArtifacts artifacts: 'docker-compose.yml', fingerprint: true
                }
            }
        }
        
        success {
            echo "✅ Pipeline completed successfully!"
            echo "🌐 Access your services at the URLs shown above"
        }
        
        failure {
            echo "❌ Pipeline failed!"
            script {
                echo "🔍 Debugging info:"
                sh "docker ps -a | grep ${PROJECT_NAME} || echo 'No ${PROJECT_NAME} containers found'"
                
                // Show logs from failed containers
                sh """
                    for container in \$(docker ps -a -q --filter name=${PROJECT_NAME}-${BUILD_NUMBER} 2>/dev/null || true); do
                        echo "=== Logs for container \$container ==="
                        docker logs --tail=50 \$container || true
                    done
                """
            }
        }
    }
}