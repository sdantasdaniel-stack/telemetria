# Sistema de Gestão de Devices

Aplicação web para gerenciamento de empresas, usuários e devices IoT,
com atualização em tempo real via WebSocket.

## Tecnologias

- Java 21
- Spring Boot 4.0.6
- PrimeFaces 15 + JoinFaces (interface web)
- Spring Security com autenticação JWT
- Spring WebSocket com STOMP/SockJS
- Hibernate / Spring Data JPA
- MariaDB
- Google Maps API
- Apache Tomcat 10 (externo, deploy WAR)

## Funcionalidades

- Autenticação por JWT com renovação automática de token
- Controle de acesso por roles: ADMIN e USER
- CRUD completo de empresas, usuários e devices
- Atualização em tempo real via WebSocket:
  - Devices atualizam status ONLINE/OFFLINE no mapa sem recarregar a página
  - Alterações em empresas refletem automaticamente na tela do usuário
- Mapa interativo com Google Maps mostrando devices com coordenadas
- API REST documentada com autenticação Bearer Token

## Pré-requisitos

- Java 21
- Apache Tomcat 10
- MariaDB (ou XAMPP)
- Maven

## Configuração

1. Clone o repositório:
   git clone https://github.com/seu-usuario/seu-repositorio.git

2. Crie o banco de dados no MariaDB:
   CREATE DATABASE gestao_devices;

3. Configure as credenciais em src/main/resources/application.properties:
   spring.datasource.url=jdbc:mariadb://localhost:3306/gestao_devices
   spring.datasource.username=seu_usuario
   spring.datasource.password=sua_senha

4. Configure a chave secreta do JWT:
   jwt.secret=sua-chave-secreta-longa-aqui

5. Configure a chave da API do Google Maps:
   google.maps.api.key=sua-chave-aqui

## Build e Deploy

Gere o WAR (pulando os testes, pois exige banco ativo):
   mvn clean package -DskipTests

Copie o arquivo gerado em target/sistema.war para a pasta
webapps do Tomcat e inicie o servidor.

A aplicação estará disponível em:
   http://localhost:8080/sistema

## Estrutura do Projeto

src/
├── main/
│   ├── java/com/daniel/empresas/
│   │   ├── beans/          — Managed Beans do PrimeFaces (interface)
│   │   ├── config/         — Configurações (Security, WebSocket)
│   │   ├── controller/     — Controllers REST
│   │   ├── dto/            — DTOs de request e response
│   │   ├── exception/      — Exceções customizadas e GlobalExceptionHandler
│   │   ├── model/          — Entidades JPA
│   │   ├── repository/     — Repositórios Spring Data
│   │   ├── security/       — JWT, filtros de autenticação
│   │   ├── service/        — Regras de negócio
│   │   └── websocket/      — Notificadores WebSocket
│   ├── resources/
│   │   └── application.properties
│   └── webapp/
│       ├── js/             — Bibliotecas SockJS e STOMP (locais)
│       ├── pages/          — Páginas XHTML protegidas
│       │   ├── admin/      — Páginas do ADMIN
│       │   └── user/       — Páginas do USER
│       ├── WEB-INF/
│       │   └── web.xml
│       └── login.xhtml

## Perfis de Acesso

ADMIN — acesso completo:
  Gerenciar empresas, usuários e devices
  Visualizar mapa com todos os devices

USER — acesso restrito:
  Visualizar e cadastrar devices nas próprias empresas ativas
  Visualizar mapa

## API REST

A API responde em http://localhost:8080/sistema
Autenticação via header: Authorization: Bearer {token}
Obtenha o token em: POST /auth/login

Documentação completa disponível na Wiki do repositório.

## Variáveis de Ambiente Sensíveis

Não suba para o GitHub os valores reais de:
  - spring.datasource.password
  - jwt.secret
  - google.maps.api.key

Substitua por valores fictícios antes de commitar ou use
variáveis de ambiente do sistema operacional.

## Licença

Este projeto foi desenvolvido para fins de aprendizado.
