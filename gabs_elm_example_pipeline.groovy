pipeline {
    agent none
    triggers {pollSCM('* * * * *')}
    stages {
        stage('Preparation') {
            agent any
            steps {
                git poll: true, url: "https://github.com/ErraticTurtle35/elm-spa-example.git"
                sh "echo 'Completed preparation'"
            }
        }
        
        stage('Build') {
            agent {
                docker { image 'ubuntu:18.04' }
            }
            steps {
                sh "echo 'Completed building'"
            }
          post {
            always {
              sh "echo 'Completed testing'"
        }
        }
        }
        
        stage('Package') {
            agent any
            steps {
                sh "rm -rf *.tar"
                sh "docker build -t gabs_elm_spa_example ."
                sh "docker save -o elm_spa_example.tar gabs_elm_spa_example"
                // archiveArtifacts artifacts: '**/*tar'
                sh "echo 'Completed building'"
            }
        }

        stage('Installation approval') {
            agent any
            steps {
                input "Should we deploy this pipeline to production?"
            }
        }  

        stage('Install') {
            agent any
            steps {
                sshagent (credentials: ['studen']) {
                    sh "scp -o StrictHostKeyChecking=no elm_spa_example.tar studen@45.33.50.232:~/."
                    sh """
                        ssh -o StrictHostKeyChecking=no studen@45.33.50.232 \
                        docker rm  -f gabs_elm_spa_example || true

                    """
                    sh """
                        ssh -o StrictHostKeyChecking=no studen@45.33.50.232
                        docker import elm_spa_example.tar
                    """   
                    sh """
                        ssh -o StrictHostKeyChecking=no studen@45.33.50.232
                        docker run -d -p 10600:8069 --name gabs_elm_spa_example gabs_elm_spa_example
                    """                       
                    sh "echo 'Completed installation'"    
                }
            }
        }        
    }
}
