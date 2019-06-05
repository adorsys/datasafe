# Datasafe business

This module contains ready-to-use Datasafe services:
- Default Datasafe service that uses storage adapter to store everything
- Versioned Datasafe service that provides additional safety by storing file versions (software-based)

You can import this module directly or use [interfaces as templates](src/main/java/de/adorsys/datasafe/business/impl/service) 
to build desired services using [Dagger2](https://github.com/google/dagger) compile-time dependency injection.