## GlassFish Fork Pull Request Based Workflow on GitHub

### Prerequisite 
Please sign the Oracle Contributor Agreement \([OCA](http://www.oracle.com/technetwork/community/oca-486395.html)\) and get your GitHub ID's included. If you signed the OCA but GitHub ID is not included,  please send an email to [David Delabassee](mailto:david.delabassee@oracle.com) to fix it. You can then work with the respective project owners or reviewers to get your pull requests reviewed and approved.

### One time setup
* [Setup](https://help.github.com/articles/set-up-git/)  Git
 ```
$ git config --global user.name "Your Name"
$ git config --global user.email "your.name@example.com"
```
* [Check](https://help.github.com/articles/checking-for-existing-ssh-keys/) for existing SSH keys . If existing ssh keys are not present , please [generate](https://help.github.com/articles/generating-a-new-ssh-key-and-adding-it-to-the-ssh-agent/) new ssh keys.
* Login to GitHub with your credentials
* [Add](https://help.github.com/articles/adding-a-new-ssh-key-to-your-github-account/) your ssh key to your GitHub Account.
* [Fork](https://help.github.com/articles/fork-a-repo/) the [GlassFish](https://github.com/javaee/glassfish/) GitHub repository.
 \(In case you get a error message "You have existing forks of this repository”, then either you have already forked the repo or you have your own repo named glassfish, now you will need to reuse the old fork or delete your own glassfish respectively. In case you delete, you need to re fork it.\)
* [Clone](https://help.github.com/articles/cloning-a-repository/) your forked repository
```
$ git clone git@github.com:your-githubid/glassfish.git
Cloning into 'glassfish'...
remote: Counting objects: 800145, done.
remote: Compressing objects: 100% (7/7), done.
remote: Total 800145 (delta 0), reused 0 (delta 0), pack-reused 800138
Receiving objects: 100% (800145/800145), 255.24 MiB | 2.59 MiB/s, done.
Resolving deltas: 100% (415989/415989), done.
Checking connectivity... done.
Checking out files: 100% (33230/33230), done.
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

### Raise Pull Request 
* Sync the 'master'  of your fork with upstream master
```
$ git fetch upstream
remote: Counting objects: 50, done.
remote: Total 50 (delta 27), reused 27 (delta 27), pack-reused 23
Unpacking objects: 100% (50/50), done.
From https://github.com/javaee/glassfish
 * [new branch]      embedded-experiments -> upstream/embedded-experiments
 * [new branch]      gf-jdk9    -> upstream/gf-jdk9
 * [new branch]      gf-ri-jsr375 -> upstream/gf-ri-jsr375
 * [new branch]      master     -> upstream/master
 * [new branch]      pr/21728   -> upstream/pr/21728
 
$ git checkout master
$ git merge upstream/master
$ git push origin master #push local master to github fork.
```
* Create a local topic branch in your fork from your master.
```
$ git checkout -b Iss_21702
Switched to a new branch 'Iss_21702'
```
* Do the development in your branch.
* Build and test your changes (Run quicklook and the dev tests)
* Commit Changes
```
$ git add main/appserver/pom.xml
$ git commit -m "Update Java EE version in master pom.xml to EE8"
[Iss_21702 daf0e8a259] Update Java EE version in master pom.xml to EE8
 1 file changed, 1 insertion(+), 1 deletion(-)
 ```
 * Push your changes in a remote branch of your fork
 ```
 $ git push origin Iss_21702
Counting objects: 5, done.
Delta compression using up to 4 threads.
Compressing objects: 100% (5/5), done.
Writing objects: 100% (5/5), 470 bytes | 0 bytes/s, done.
Total 5 (delta 4), reused 0 (delta 0)
remote: Resolving deltas: 100% (4/4), completed with 4 local objects.
To https://github.com/your-githubid/glassfish.git
 * [new branch]            Iss_21702 -> Iss_21702
 ```
* Before even raising a Pull Request, please raise an issue if it doesn't exist. We would like every pull request to be associated with an issue. Submit the Pull Request referring to the issue number.
* Raise a Pull Request.
* Make sure you put a proper 'title' for the Pull Request. The title of the Pull Request would become the commit message. So instead of giving 'title' like "Iss xxxx" or "Fixes #xxxxx", consider giving a proper one line 'title' for the Pull Request like "Fixes xxx : <brief description about the issue/fix>"
* In the Pull Request description(body), please mention "Fixes #xxxxx" in order to link the Pull Request with the Issue you are fixing.

### Trigger GlassFish CI pipeline on your Pull Request

#### Workflow for GitHub javaee glassfish team members
1. To trigger the pipeline CI run(to run all GlassFish devtests), add a comment on the pull request that says **\"@glassfishrobot Run CI tests please\"** (case insensitive). You can add it as an original comment text \(with only this text\)  or separate comment with only this text. Including this text as part of a longer comment is not supported. This step is mandatory. When you add such a comment, our infrastructure would know that you want to run all CI tests for your pull request.There is a polling job which runs every 5 minutes to see if there is new pull request. 
2. Within 5 minutes you would get a comment from **glassfishrobot**\(GlassFish CI bot\) that says \"Starting CI tests run\". That means all the gating tests have been triggered on your pull request in GlassFish CI infrastructure.
3. On completion of all the tests, glassfishrobot will add a comment in your pull request that says **\"All CI tests successful\"** if all the tests are passed or **\"One or more CI tests failed\"** in case there are test failures.
4. You can get the test results (junit report) for different test suites that run on your Pull Request under http://download.oracle.com/glassfish/prs/\<your-PullRequest-number\>/test-results\-\-\<date-time\>/index.html. 
5. In case of test failure(s), re-work or fix your code and request for retry on the same PR repeating steps 1-4.
6. In case you rereun the GlassFish CI pipeline, new folder would get created under http://download.oracle.com/glassfish/prs/\<your-PullRequest-number\> named test-results\-\-\<date-time\> containing tests results for all tests that run in GlassFish CI pipeline.
7. If hudson tests pass and code review has been approved, changes will be merged by the administrator.


#### Workflow for contributors who are **not** member of GitHub javaee glassfish team members
1. If an external contributor (a contributor who is not a member of the 'glassfish' team in GitHub ) raises a Pull Request, glassfishrobot would update the pull request with the following two comments  
**\"Review needed from GlassFish team members\"**   
**\"Please sign Oracle Contributor Agreement(OCA) to contribute in GlassFish project if you have not done that already.\"**  
In that case please follow the [Prerequisite](#Prerequisite) section of this document
2. Once you sign the OCA, one of the GlassFish team member would review your Pull Request  
3. If the code review is approved, the reviewer would start the GlassFish CI pipeline for you \(i.e one of the GlassFish team member would perform step 1 of [Workflow for GitHub javaee glassfish team members](#Workflow-for-GitHub-javaee-glassfish-team-members) on your behalf\)
4. Please follow step 2 to 7 of [Workflow for GitHub javaee glassfish team members](#Workflow-for-GitHub-javaee-glassfish-team-members) to merge your Pull Request with javaee/glassfish master

Please note that the GlassFish administrator will not merge your pull request with the GitHub javaee/glassfish master branch if

* There are test failures
* The code reviewer does not approve your changes
