<div class="tocmenu">
* auto-gen TOC:
{:toc}
</div>


[![Build Status](https://travis-ci.com/adorsys/datasafe.svg?branch=develop)](https://travis-ci.com/adorsys/datasafe)
[![codecov](https://codecov.io/gh/adorsys/datasafe/branch/develop/graph/badge.svg)](https://codecov.io/gh/adorsys/datasafe)
[![Maintainability](https://api.codeclimate.com/v1/badges/06ae7d4cafc3012cee85/maintainability)](https://codeclimate.com/github/adorsys/datasafe/maintainability)

Datasafe is a cross-platform library that allows sharing and storing user data and documents securely. 
This is achieved using **CMS-envelopes** for symmetric and asymmetric encryption. Symmetric encryption is used for private files. 
Asymmetric encryption is used for file sharing. 

Libraries are built with the idea to be as configurable as possible - it uses Dagger2 for dependency injection and modular architecture to combine everything into a business layer, so the user can override any aspect he wants - i.e. to change encryption algorithm or to turn path encryption off. Each module is as independent as it is possible - to be used separately.

- Each user has private space that can reside on s3, minio, filesystem or anything else with proper adapter. 
In his private space, each document and its path is encrypted. 
- For document sharing user has inbox space, that can be accessed from outside. Another user can write the document he wants to share into users' inbox space using the recipients' public key so that only the inbox owner can read it.
- For storage systems that do not support file versioning natively (i.e. minio) this library provides versioning capability too.

# Project overview
* [Possible future achitecture](general/docusafe_future_client.md)

# JavaDoc
You can read JavaDoc [here](javadoc/0.0.9/index.html)

Command to generate JavaDoc from sources:
```mvn clean javadoc:aggregate -P javadoc ```

# Usage examples

# Modular design overview
* [Modular design](modular/modular.md)

# Contributing
* [CodingRules](codingrules/CodingRules.md)
* [Branching and commiting](branching/branch-and-commit.md)
* [Deployment to maven central](general/deployment_maven_central.md)