[![semantic-release](https://img.shields.io/badge/semantic-release-e10079.svg?logo=semantic-release)](https://github.com/semantic-release/semantic-release)

# lsd-distributed-generator
![GitHub](https://img.shields.io/github/license/lsd-consulting/lsd-distributed-generator)
[![CI](https://github.com/lsd-consulting/lsd-distributed-generator/actions/workflows/ci.yml/badge.svg)](https://github.com/lsd-consulting/lsd-distributed-generator/actions/workflows/ci.yml)
![Codecov](https://img.shields.io/codecov/c/github/lsd-consulting/lsd-distributed-generator)
[![Publish](https://github.com/lsd-consulting/lsd-distributed-generator/actions/workflows/publish.yml/badge.svg)](https://github.com/lsd-consulting/lsd-distributed-generator/actions/workflows/publish.yml)
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

and configure through app properties:

```properties
# the trust store configuration is optional
lsd.dist.db.trustStorePassword={password}
lsd.dist.db.trustStoreLocation={location}

lsd.dist.db.connectionString={someUrl}

# Optional
lsd.dist.db.connectionTimeout={millis}
```
