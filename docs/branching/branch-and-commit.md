# Branch naming strategy: 
1. Get issue id from github (**issue-id**), i.e. for https://github.com/adorsys/datasafe/issues/17 **issue-id** is **17**
2. Your branch should have *feature/DAT-2* or *bugfix/DAT-* as a prefix and than the issue id.
Optionally the issue id can have suffix with a short-term description of the issue itself.

example:
```
 git checkout -b feature/DAT-17_Fix-formatting
 git checkout -b bugfix/DAT-17_Fix-formatting
```


# Commit naming strategy:

Always include **prefix** DOC2-**issue-id** in your commit message, so that valid commit message can be only:
```
 git commit -a -m "DAT-17 Refactoring"
 git commit -a -m "DAT-17 Moving files"
```
 
The Term after the issue id is not mandatory the same as the branch-short-term for the issue. It describes the changes done. 
  