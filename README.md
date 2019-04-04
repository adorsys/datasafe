# General information

### Branch naming strategy: 
1. Get issue id from github (**issue-id**), i.e. for https://github.com/adorsys/docusafe2/issues/17 **issue-id** is **17**
2. Your branch name should be at least be DOC2-**issue-id**, also you can use *feature/*, *bugfix/* etc. prefixes and include work description too, so that valid names are:
 - DOC2-17
 - feature/DOC2-17
 - bugfix/DOC2-17_Fix-formatting

### Commit naming strategy:

Always include **prefix** DOC2-**issue-id** in your commit message, so that valid commit message can be only:
 - DOC2-17. Refactoring
 - DOC2-17: Moving files
 
While messages like this are **invalid**:
 - Refactoring
 - Moving files
  
# Project overview
* [Project architecture](docs/architecture.md) 
* [Possible future achitecture](docs/general/docusafe_future_client.md)