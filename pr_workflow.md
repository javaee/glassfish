# GlassFish Pull Request Acceptance Workflow   
        
## <a name="pre"></a>Prerequisite 
To contribute to GlassFish, you first need to sign the [Oracle Contributor Agreement](http://www.oracle.com/technetwork/community/oca-486395.html). When filling the OCA make sure to include your GitHub username. If you signed the OCA previously and your GitHub username wasn't included, just ping [David Delabassee](mailto:david.delabassee(@)oracle.com) to fix this. Once your OCA has been approved, you'll be able to work directly with the respective project/module owners or reviewers to get your Pull Request reviewed and approved. The OCA process doesn't apply to Oracle employee.

## One Time Setup
* [Setup](https://help.github.com/articles/set-up-git/) Git.
 ```
$ git config --global user.name "Your Name"
$ git config --global user.email "your.name@example.com"
```
* [Check](https://help.github.com/articles/checking-for-existing-ssh-keys/) for existing SSH keys. If existing SSH keys are not present, please [generate](https://help.github.com/articles/generating-a-new-ssh-key-and-adding-it-to-the-ssh-agent/) new SSH keys.
* Login to GitHub with your credentials.
* [Add](https://help.github.com/articles/adding-a-new-ssh-key-to-your-github-account/) your SSH key to your GitHub Account.
* [Fork](https://help.github.com/articles/fork-a-repo/) the [GlassFish](https://github.com/javaee/glassfish/) GitHub repository.
 \(If you get an error message "You have existing forks of this repository”, then it means you have already forked the repository or you have your own repository named glassfish. You can either reuse the old fork or delete your exisiting glassfish repository. In case you delete, you need to re-fork it.\)
* [Clone](https://help.github.com/articles/cloning-a-repository/) your forked repository.
```
$ git clone git@github.com:your-githubid/glassfish.git
```
* [Configure](https://help.github.com/articles/configuring-a-remote-for-a-fork/) the remote for your fork.  
```
$ git remote add upstream git@github.com:javaee/glassfish.git
$ git remote -v
origin    git@github.com:your-githubid/glassfish.git (fetch)
origin    git@github.com:your-githubid/glassfish.git (push)
upstream    git@github.com:javaee/glassfish.git (fetch)
upstream    git@github.com:javaee/glassfish.git (push)
```
* If you have configured HTTPS URLs for your remotes and want to change to GIT URLs, refer to [this doc](https://help.github.com/articles/changing-a-remote-s-url/) to re-configure the remote URLs.

## Building GlassFish

GlassFish build system relies on [Maven](https://maven.apache.org) so make sure Maven is correctly setup.
To build GlassFish, just issue the following command:
```
mvn clean install
```


## Raising a Pull Request 
* Sync the master of your fork with upstream master.  
```  
$ git fetch upstream 
$ git checkout master
$ git merge upstream/master
$ git push origin master #push local master to github fork.
```
* Create a local topic branch in your fork from your master.  
```
$ git checkout -b Iss_21702
```
* Do the development in your branch.
* [Build](wiki-archive/FullBuildInstructions.html) and test your changes. Optionally run quicklook and the developer tests.
* Commit all the changes.  
```
$ git add main/appserver/pom.xml
$ git commit -m "my commit message"
 ```
 * Push your changes in a remote branch of your fork  
 ```
 $ git push origin Iss_21702
 ```
* Before raising a Pull Request, please raise an issue if it doesn't exist. We would like every Pull Request to be associated with an issue. Submit the Pull Request referring to the issue number.
* Raise a Pull Request.
* Make sure you put a proper 'title' for the Pull Request. The title of the Pull Request would become the commit message. Instead of giving 'title' like "Iss xxxx" or "Fixes #xxxxx", consider giving a proper one line 'title' for the Pull Request like "Fixes xxx : <brief description about the issue/fix>"
* In the Pull Request description (body), please mention "Fixes #xxxxx" in order to link the Pull Request with the Issue you are fixing.

## Triggering GlassFish CI tests on your Pull Request

### <a name="tmwf"></a>Workflow for GitHub GlassFish Team Members

1. To trigger the pipeline CI run (to run all GlassFish gating tests), add a comment in the Pull Request that says **\"@glassfishrobot Run CI tests please\"** (case insensitive). You can add it as an original comment text \(with only this text\)  or a separate comment with only this text. Including this text as part of a longer comment is not supported. This step is mandatory. When you add such a comment, our infrastructure would know that you want to run all CI tests for your Pull Request.
2. Within minutes you will see a comment from **glassfishrobot** \(GlassFish CI bot\) that says **\"Starting CI tests run\"**. This indicates that all the gating tests (all GlassFish dev tests and unit tests that needs to be successful for each commit) have been triggered on your Pull Request in GlassFish CI infrastructure.
3. On completion of all the tests, **glassfishrobot** will add a comment in your Pull Request that says **\"All CI tests successful\"** if all the tests passed or **\"One or more CI tests failed\"** if there are test failures.
4. You can view the test results (junit report) for different test suites that run on your Pull Request under **http://download.oracle.com/glassfish/prs/\<your-PullRequest-number\>/test-results\-\-\<date-time\>/index.html**
5. In case of test failure(s), re-work or fix your code and request for retry on the same PR repeating steps 1-4.
6. In case you re-run the GlassFish CI pipeline, new folder should get created under http://download.oracle.com/glassfish/prs/\<your-PullRequest-number\> named test-results\-\-\<date-time\> containing results for all tests that run in GlassFish CI pipeline.
7. If CI tests pass and code review has been approved, changes will be merged by the administrator.


### Workflow for Non-GlassFish Team Contributors
1. If an external contributor, i.e. a contributor who is not a member of the [GitHub 'GlassFish' team](https://github.com/orgs/javaee/teams/glassfish/members), raises a Pull Request, **glassfishrobot** will update the Pull Request with the following two comments:  
**\"Review needed from GlassFish team members\"**   
**\"Please sign Oracle Contributor Agreement (OCA) to contribute to the GlassFish project if you have not done that already.\"**  
If you have signed the OCA, please follow the [Prerequisite](#pre).  
2. Once you sign the OCA, one of the GlassFish team members will review your Pull Request.  
3. If the code review is approved, the reviewer can start the GlassFish CI pipeline for you, i.e one of the GlassFish team member would perform step 1 of [Workflow for GitHub GlassFish Team Members](#tmwf) on your behalf.
4. Please follow step 2 to 7 of [Workflow for GitHub GlassFish Team Members](#tmwf) to merge your Pull Request with javaee/glassfish master.

Please note that the GlassFish administrator will not merge your Pull Request with the GitHub javaee/glassfish master branch if

* There are CI test failures.
* The code reviewer does not approve your changes.

In case you find any issues, please drop an email to the [GlassFish Mailing List](mailto:glassfish@javaee.groups.io).

