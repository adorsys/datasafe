[![Build Status](https://travis-ci.com/adorsys/datasafe.svg?branch=develop)](https://travis-ci.com/adorsys/datasafe)
[![codecov](https://codecov.io/gh/adorsys/datasafe/branch/develop/graph/badge.svg)](https://codecov.io/gh/adorsys/datasafe)
[![Maintainability](https://api.codeclimate.com/v1/badges/06ae7d4cafc3012cee85/maintainability)](https://codeclimate.com/github/adorsys/datasafe/maintainability)

# Secure, Encrypted and Versioned Data Storage Library

## Overview
Datasafe is a robust library tailored for developers and enterprises, offering encrypted and versioned data storage. It enhances the security of data-sensitive applications, making it ideal for mission-critical usage.

### Key Features
- **Enhanced Security**: Each user gets unique key encryption keys, reducing the risk of widespread data breaches.
- **Full Confidentiality**: Offers full or partial file path encryption.
- **Ransomware Protection**: Versioned storage safeguards against ransomware attacks.
- **Secure File Exchange**: Features asynchronous inboxes for safe user-to-user file transfers.
- **Zero Trust Environments**: Ideal as a data encryption layer in critical data processing backends.

## Technical Specifications
Datasafe uses AES-GCM (and Chacha-Poly for large files) for encryption, with CMS-envelopes ([RFC 5652](https://www.rfc-editor.org/rfc/rfc8933#RFC5652)) for encrypted content wrapping. The library's flexible design allows seamless integration and customization. For more details, refer to our [Security Whitepaper](SECURITY.WHITEPAPER.md).

### Highlights
- **Configurability**: Dagger2 for dependency injection and modular design.
- **Storage Compatibility**: Tested with Amazon S3, Minio, CEPH, and local filesystems.
- **User Privacy**: Encrypts both the document and its path in each user's private space.
- **Versioning Support**: Provides application layer versioning for systems lacking native versioning support.

## Getting Started

### Building the Project
Without tests:
```bash
mvn clean install -DskipTests=true
```

Full Build:
```bash
mvn clean install
```

### Adding Datasafe to Your Project
Include Datasafe in your Maven project:

```xml
<!-- Datasafe Business Module -->
<dependency>
    <groupId>de.adorsys</groupId>
    <artifactId>datasafe-business</artifactId>
    <version>{datasafe.version}</version>
</dependency>

<!-- S3 Storage Provider -->
<dependency>
    <groupId>de.adorsys</groupId>
    <artifactId>datasafe-storage-impl-s3</artifactId>
    <version>{datasafe.version}</version>
</dependency>

<!-- Filesystem Storage Provider for Tests-->
<dependency>
    <groupId>de.adorsys</groupId>
    <artifactId>datasafe-storage-impl-fs</artifactId>
    <version>{datasafe.version}</version>
    <scope>test</scope>
</dependency>
```

## In-depth Insights
- **Performance Analysis**: Tested on AWS, Datasafe achieves up to 50 MiB/s write and 80 MiB/s read throughput with Amazon S3. [See full report](datasafe-long-run-tests/README.md).
- **Quick Demo**: Explore Datasafe's capabilities through our [quick demo](./docs/readme/Demo.md).
- **Deployment Models**: Followings are among others [possible deployment models](./docs/readme/DeploymentModels.md).
- **How It Works**: Get a look at [detailed integration information](./docs/readme/HowItWorks.md).

## Additional Resources
- **Project Homepage**: Visit [adorsys.github.io/datasafe](https://adorsys.github.io/datasafe) for more information.
- **JavaDoc**: Access our detailed [JavaDoc here](https://adorsys.github.io/datasafe/javadoc/latest/index.html).

## Contributing to Datasafe
- **Contributor License**: See [Developer Certificate of Origin (DCO) Enforcement](https://github.com/adorsys/datasafe/discussions/253)
- **Coding Guidelines**: [CodingRules](docs/codingrules/CodingRules.md)
- **Branching and Committing Practices**: [Branching Guide](docs/branching/branch-and-commit.md)
- **Deployment Process**: [Maven Central Deployment](docs/general/deployment_maven_central.md)
- **Feature Requests and Comments**: [Open a Discussion Ticket](https://github.com/adorsys/datasafe/discussions)

## Dual-Licensing
Datasafe is available under two licenses:
- **Open-Source Projects**: [AGPL v3 License](https://www.gnu.org/licenses/agpl-3.0.en.html)
- **Commercial License**: Proprietary license available. Contact [sales@adorsys.com](mailto:sales@adorsys.com) for details.
