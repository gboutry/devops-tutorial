pipeline {
  agent any
  stages {
    stage('Build project') {
      parallel {
        stage('Build JVM') {
          agent {
            docker {
              image 'maven:3.6.3-jdk-11-slim'
            }

          }
          steps {
            sh 'mvn clean package -DskipTests'
            stash(includes: 'target/', name: 'target_built')
          }
        }

        stage('Build Native') {
          steps {
            withCredentials(bindings: [usernamePassword(credentialsId: 'registry', passwordVariable: 'registryPassword', usernameVariable: 'registryUser')]) {
                sh "docker login -u ${env.registryUser} -p ${env.registryPassword} registry.zouzland.com"
                sh 'docker build -f src/main/docker/Dockerfile.native --cpuset-cpus 0,1,2 -t registry.zouzland.com/boutry/devops-tutorial-native:latest .'
            }
          }
        }

      }
    }

    stage('Test JVM') {
      agent {
        docker {
          image 'maven:3.6.3-jdk-11-slim'
          args '--network="host" -e DOCKER_HOST="tcp://docker:2376" -e DOCKER_CERT_PATH="/certs/client" -e DOCKER_TLS_VERIFY=1 -v "$DOCKER_CERT_PATH":"$DOCKER_CERT_PATH"'
        }

      }
      steps {
        unstash 'target_built'
        sh 'mvn test'
      }
    }

    stage('Build Docker JVM') {
      agent any
      steps {
        unstash 'target_built'
        sh 'docker build -f src/main/docker/Dockerfile.jvm -t registry.zouzland.com/boutry/devops-tutorial-jvm:latest .'
      }
    }


    stage('Push Docker build to registry') {
      agent any
      when {
        expression {
          GIT_BRANCH = 'origin/' + sh(returnStdout: true, script: 'git rev-parse --abbrev-ref HEAD').trim()
          return GIT_BRANCH == 'origin/master' || params.FORCE_PUSH
        }

      }
      steps {
        withCredentials(bindings: [usernamePassword(credentialsId: 'registry', passwordVariable: 'registryPassword', usernameVariable: 'registryUser')]) {
          sh "docker login -u ${env.registryUser} -p ${env.registryPassword} registry.zouzland.com"
          script {
            if(params.TAG_BUILD) {
              // Push latest
              sh 'docker push registry.zouzland.com/boutry/devops-tutorial-jvm:latest'
              sh 'docker push registry.zouzland.com/boutry/devops-tutorial-native:latest'
              // Tag latest with version
              sh 'docker tag registry.zouzland.com/boutry/devops-tutorial-jvm:latest registry.zouzland.com/boutry/devops-tutorial-jvm:' + params.TAG
              sh 'docker tag registry.zouzland.com/boutry/devops-tutorial-jvm:latest registry.zouzland.com/boutry/devops-tutorial-native:' + params.TAG
              // Push tagged version
              sh 'docker push registry.zouzland.com/boutry/devops-tutorial-jvm:' + params.TAG
              sh 'docker push registry.zouzland.com/boutry/devops-tutorial-native:' + params.TAG
            } else {
              // Tag latest as Snapshot
              sh 'docker tag registry.zouzland.com/boutry/devops-tutorial-jvm:latest registry.zouzland.com/boutry/devops-tutorial-jvm:snapshot'
              sh 'docker tag registry.zouzland.com/boutry/devops-tutorial-native:latest registry.zouzland.com/boutry/devops-tutorial-native:snapshot'
              // Push to registry
              sh 'docker push registry.zouzland.com/boutry/devops-tutorial-jvm:snapshot'
              sh 'docker push registry.zouzland.com/boutry/devops-tutorial-native:snapshot'
            }
          }

        }

      }
    }

    stage('Deploy release to heroku') {
          when {
            expression {
              return params.TAG_BUILD
            }
          }
          agent {
              docker {
                image 'registry.zouzland.com/heroku-ctn:latest'
                registryCredentialsId 'registry'
                args '--network="host" -e DOCKER_HOST="tcp://docker:2376" -e DOCKER_CERT_PATH="/certs/client" -e DOCKER_TLS_VERIFY=1 -v "$DOCKER_CERT_PATH":"$DOCKER_CERT_PATH"'
              }

            }
          steps {
            withCredentials(bindings: [usernamePassword(credentialsId: 'registry', passwordVariable: 'registryPassword', usernameVariable: 'registryUser')]) {
              sh "docker login -u ${env.registryUser} -p ${env.registryPassword} registry.zouzland.com"
              sh 'docker pull registry.zouzland.com/boutry/devops-tutorial-native:' + params.TAG
              sh 'docker tag registry.zouzland.com/boutry/devops-tutorial-native:' + params.TAG + " registry.heroku.com/devops-tutorial/web"
            }
            withCredentials(bindings: [string(credentialsId: 'heroku_token', variable: 'HEROKU_API_KEY')]) {
                sh "heroku container:login"
                sh "docker push registry.heroku.com/devops-tutorial/web"
                sh "heroku container:release web -a devops-tutorial"
            }

          }
        }

  }
  post {
    always {
      deleteDir()
    }

    success {
      echo 'BUILD SUCCESS'
    }

    unstable {
      echo 'BUILD UNSTABLE'
    }

    failure {
      echo 'BUILD FAILURE'
    }

    changed {
      echo 'State changed.'
    }

  }
  parameters {
    booleanParam(defaultValue: false, description: 'Push docker image to registry', name: 'FORCE_PUSH')
    booleanParam(defaultValue: false, description: 'Tag current build ?', name: 'TAG_BUILD')
    string(defaultValue: 'latest', description: 'TAG number', name: 'TAG')
  }
}
