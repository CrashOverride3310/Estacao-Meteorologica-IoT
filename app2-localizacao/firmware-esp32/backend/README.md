# Backend — Kotlin e Spring Boot

Backend da Estação Meteorológica IoT, desenvolvido com Kotlin, Spring Boot e Java 21.

## Funcionalidades

- Comunicação com o broker MQTT HiveMQ Cloud.
- Recebimento dos dados meteorológicos enviados pela ESP32.
- Integração das coordenadas GPS transmitidas pelo App2.
- Persistência dos dados em PostgreSQL hospedado no Neon.
- Disponibilização de API REST para o aplicativo Android principal.

## Tecnologias

- Kotlin
- Spring Boot
- Java 21
- Spring Data JPA / Hibernate
- PostgreSQL
- HiveMQ Cloud (MQTT)
- DigitalOcean (Ubuntu Linux)

## API REST

Endpoints de consulta meteorológica:

- `GET /api/estacao/dados`
- `GET /api/estacao/ultima`

## Infraestrutura

O backend está implantado em um servidor Ubuntu na DigitalOcean, integrado ao banco PostgreSQL Neon.

**Status:** backend desenvolvido, implantado e funcional.

O código-fonte será adicionado após revisão das configurações e remoção de credenciais sensíveis.
