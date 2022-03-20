[![semantic-release](https://img.shields.io/badge/semantic-release-e10079.svg?logo=semantic-release)](https://github.com/semantic-release/semantic-release)

# lsd-distributed-generator
![GitHub](https://img.shields.io/github/license/lsd-consulting/lsd-distributed-generator)
![Codecov](https://img.shields.io/codecov/c/github/lsd-consulting/lsd-distributed-generator)

[![CI](https://github.com/lsd-consulting/lsd-distributed-generator/actions/workflows/ci.yml/badge.svg)](https://github.com/lsd-consulting/lsd-distributed-generator/actions/workflows/ci.yml)
[![Nightly Build](https://github.com/lsd-consulting/lsd-distributed-generator/actions/workflows/nightly.yml/badge.svg)](https://github.com/lsd-consulting/lsd-distributed-generator/actions/workflows/nightly.yml)
[![GitHub release](https://img.shields.io/github/release/lsd-consulting/lsd-distributed-generator)](https://github.com/lsd-consulting/lsd-distributed-generator/releases)
![Maven Central](https://img.shields.io/maven-central/v/io.github.lsd-consulting/lsd-distributed-generator)

A Liquid Sequence Diagram generator from data gathered by the `lsd-distributed-interceptors` library and stored in a database. 

Here is a sample of an LSD that this library can generate:

![LSD](https://github.com/lsd-consulting/lsd-distributed-generator/blob/main/image/lsd-example.png?raw=true)

along with the PlantUML source for the LSD:

![LSD Source](https://github.com/lsd-consulting/lsd-distributed-generator/blob/main/image/lsd-source-example.png?raw=true)

It also generates a component diagram:

![Component diagram](https://github.com/lsd-consulting/lsd-distributed-generator/blob/main/image/lsd-component-diagram-example.png?raw=true)

# Usage

To use the `lsd-distributed-generator` library just add it to the dependencies:

```groovy
implementation "io.github.lsd-consulting:lsd-distributed-generator:+"
```

## Properties

The following properties can be overridden by setting System or Environment properties.

| Property Name        | Default     | Required | Description |
| ----------- | ----------- | ------------ | ------------ |
| lsd.dist.db.connectionString | N/A | YES | Connection string to the database, eg. mongodb://localhost:27017 |
| lsd.dist.db.connectionTimeout.millis | 500 | NO | Database connection timeout. |
| lsd.dist.db.trustStoreLocation | N/A | NO | The location of the trust store containing the certificate of the signing authority (only required for TLS where the certificate if provided). |
| lsd.dist.db.trustStorePassword | N/A | NO | The password to the trust store containing the certificate of the signing authority. |
