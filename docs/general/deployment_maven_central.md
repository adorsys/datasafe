# Deployment to maven central

To make a project deployable the root pom must at least contain the following tags:
```
<name>
<description>
<url>
<license>
<develop>
<scm>
```

To create a new release 

```
git submodule add https://github.com/borisskert/release-scripts
git submodule init
git submodule update
 
./release-scripts/release.sh current-version next-version```
