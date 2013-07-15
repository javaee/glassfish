## Checking Out the sources


#### Working with old SVN repository (1.x/2.0.x/2.1.x)

Previous version of HK2 were using Subversion. The repository can be browsed [here][hk2svn]. Read-only copy can be obtained by executing:

```bash
svn checkout https://svn.java.net/svn/hk2~svn/trunk/hk2
```


#### Checking out new sources (2.2.x)

HK2 2.2.x uses modern, distributed  GIT version control system.
The repository can be cloned in a read-only mode by invoking:

```bash
git clone git://java.net/hk2~git
```

If you're only interested in reading the latest version of the sources and do not wish
to a) contribute code back to the repository or b) do not care about the history,
you can speed up the clone process by invoking:

```bash
git clone --depth 1 git://java.net/hk2~git
```
instead. This may speed up the clone process considerably.

If you're interested in developer access, you will have to setup your ssh keys in your java.net account.
Then, one could clone the repo by invoking:

```bash
git clone ssh://[jvnet_user]@git.java.net/hk2~git
```bash

[jira]: http://java.net/jira/browse/HK2
[hk2svn]: http://java.net/projects/hk2/sources/svn/show/
