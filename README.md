[![Build Status](https://travis-ci.com/adorsys/datasafe.svg?branch=develop)](https://travis-ci.com/adorsys/datasafe)
[![codecov](https://codecov.io/gh/adorsys/datasafe/branch/develop/graph/badge.svg)](https://codecov.io/gh/adorsys/datasafe)
[![Maintainability](https://api.codeclimate.com/v1/badges/06ae7d4cafc3012cee85/maintainability)](https://codeclimate.com/github/adorsys/datasafe/maintainability)


# General information
Datasafe is a cross-platform library which allows to share and store user data and documents securely. 
This is achieved using symmetric encryption for private files and using CMS-envelope (asymmetric encryption) for
file sharing. Library is built with idea to be as configurable as possible - it uses Dagger2 for DI and modular 
architecture to combine everything into business layer, so user can override any aspect he wants - i.e. to change 
encryption algorithm or to turn path encryption off. 
Each module is as independent as it is possible - to be used separately.

- Each user has private space that can reside on s3, minio, filesystem or anything else with proper adapter. 
In his private space each document and its path is encrypted. 
- For document sharing user has inbox space, that can be accessed from outside. Another user can write the document he
 wants to share into users' inbox space using recipients' public key, so that only inbox owner can read it.
- For storage systems that do not support file versioning natively (i.e. minio) this library provides versioning 
capability too.

### Contributing
* [CodingRules](docs/codingrules/CodingRules.md)
* [Branching and commiting](docs/branching/branch-and-commit.md)
* [Deployment to maven central](docs/general/deployment_maven_central.md)

# Project overview
* [Project architecture](docs/architecture.md) 
* [Possible future achitecture](docs/general/docusafe_future_client.md)
* [Comparison to docusafe](docs/docu1_vs_docu2/comparison.md)

# Modular design overview
* [Modular design](docs/modular/modular.md)