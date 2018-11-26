pipeline {
    agent any
     triggers {
        issueCommentTrigger('.*test this please.*')
    }
    stages{
        stage('build'){
            steps{
                sh "echo hi"
            }
        }
    }
}
