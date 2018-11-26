pipeline {
    agent any
    triggers {
        githubPullRequest {
            admin('arindam-bandyopadhyay')
            userWhitelist('glassfishrobot')
            cron('H/5 * * * *')
            triggerPhrase('OK to test')
            onlyTriggerPhrase()
            extensions {
                commitStatus {
                    context('Jenkins')
                    triggeredStatus('starting deployment to staging site...')
                    startedStatus('deploying to staging site...')
                    completedStatus('SUCCESS', 'All is well')
                    completedStatus('FAILURE', 'Something went wrong. Investigate!')
                    completedStatus('PENDING', 'still in progress...')
                    completedStatus('ERROR', 'Something went really wrong. Investigate!')
                }
            }
        }
    }
    stages{
        stage('build'){
            steps{
                sh "echo hi"
            }
        }
    }
}
