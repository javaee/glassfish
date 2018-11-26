job('example') {
    scm {
        git {
            remote {
                github('arindam-bandyopadhyay/glassfish')
                refspec('+refs/pull/*:refs/remotes/origin/pr/*')
            }
            branch('${sha1}')
        }
    }
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
}
