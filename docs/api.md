# Documentação da API 

**Base URL:** `http://localhost:8080/sistema`  
**Header obrigatório** (exceto login): `Authorization: Bearer {token}`

---

## Autenticação

### `POST /auth/login` — Público

Realiza login e retorna o token JWT.

**Body:**
```json
{
  "email": "seu@email.com",
  "senha": "suasenha"
}
```

**Retorno:** token JWT, nome do usuário e empresas vinculadas.

---

## Empresas

> Todos os endpoints exigem role `ADMIN`, exceto `GET /empresas/ativas`.

| Método | Endpoint | Role | Descrição |
|--------|----------|------|-----------|
| `GET` | `/empresas` | ADMIN | Lista todas as empresas (ativas e inativas) |
| `GET` | `/empresas/ativas` | USER | Lista apenas as empresas ativas |
| `GET` | `/empresas/{id}` | ADMIN | Busca uma empresa pelo ID |
| `POST` | `/empresas` | ADMIN | Cadastra uma nova empresa |
| `PUT` | `/empresas/{id}` | ADMIN | Atualiza os dados de uma empresa (todos os campos obrigatórios) |
| `DELETE` | `/empresas/{id}` | ADMIN | Desativa uma empresa — retorna `409` se já estiver inativa |
| `PATCH` | `/empresas/{id}/reativar` | ADMIN | Reativa uma empresa desativada — retorna `409` se já estiver ativa |
| `DELETE` | `/empresas/{id}/deletar` | ADMIN | Deleta permanentemente a empresa e todos os seus devices |

**Body — POST/PUT:**
```json
{
  "nome": "Nome da Empresa",
  "cnpj": "12345678000199",
  "email": "empresa@email.com"
}
```

---

## Usuários

> Todos os endpoints exigem role `ADMIN`.

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/usuarios` | Lista todos os usuários cadastrados |
| `GET` | `/usuarios/{id}` | Busca um usuário pelo ID |
| `POST` | `/usuarios` | Cadastra um novo usuário |
| `PUT` | `/usuarios/{id}` | Atualiza os dados de um usuário (todos os campos obrigatórios) |
| `DELETE` | `/usuarios/{id}` | Desativa um usuário — retorna `409` se já estiver inativo |
| `PATCH` | `/usuarios/{id}/reativar` | Reativa um usuário desativado — retorna `409` se já estiver ativo |
| `DELETE` | `/usuarios/{id}/deletar` | Deleta permanentemente um usuário do banco |

**Body — POST:**
```json
{
  "nome": "Nome do Usuário",
  "email": "usuario@email.com",
  "senha": "senha123",
  "role": "USER",
  "empresasIds": [1, 2]
}
```

> O campo `role` aceita apenas `ADMIN` ou `USER`.

**Body — PUT:**
```json
{
  "nome": "Novo Nome",
  "email": "novo@email.com",
  "senha": "novasenha",
  "role": "USER",
  "empresasIds": [1]
}
```

---

## Devices

> `ADMIN` acessa tudo.  
> `USER` pode listar devices ativos e cadastrar apenas nas suas próprias empresas ativas.

| Método | Endpoint | Role | Descrição |
|--------|----------|------|-----------|
| `GET` | `/devices` | ADMIN | Lista todos os devices (ativos e inativos) |
| `GET` | `/devices/ativos` | USER | Lista apenas os devices ativos |
| `GET` | `/devices/{id}` | ADMIN | Busca um device pelo ID |
| `GET` | `/devices/empresa/{empresaId}` | ADMIN + USER | Lista devices de uma empresa específica |
| `POST` | `/devices` | ADMIN + USER | Cadastra um novo device |
| `PUT` | `/devices/{id}` | ADMIN | Atualiza os dados de um device (todos os campos obrigatórios) |
| `DELETE` | `/devices/{id}` | ADMIN | Desativa um device — retorna `409` se já estiver inativo |
| `PATCH` | `/devices/{id}/reativar` | ADMIN | Reativa um device desativado — retorna `409` se já estiver ativo |
| `DELETE` | `/devices/{id}/deletar` | ADMIN | Deleta permanentemente um device do banco |

**Observação — `GET /devices/empresa/{empresaId}`:**  
`USER` só acessa empresas às quais pertence e que estejam ativas, vendo apenas devices ativos. `ADMIN` vê todos os devices independentemente do status.

**Body — POST:**
```json
{
  "nome": "Nome do Device",
  "identificador": "DEV-001",
  "empresaId": 1,
  "latitude": -23.5505,
  "longitude": -46.6333
}
```

> Os campos `latitude` e `longitude` são opcionais.

**Body — PUT:**
```json
{
  "nome": "Novo Nome",
  "identificador": "DEV-001",
  "empresaId": 1,
  "latitude": -23.5505,
  "longitude": -46.6333
}
```

---

## Respostas de Erro

**Formato padrão:**
```json
{
  "status": 400,
  "mensagem": "descrição do problema",
  "momento": "2026-05-29T10:00:00"
}
```

| Código | Descrição |
|--------|-----------|
| `400` | Dados inválidos ou JSON malformado |
| `401` | Não autenticado (token ausente ou inválido) |
| `403` | Sem permissão (role insuficiente) |
| `404` | Recurso não encontrado |
| `409` | Conflito (registro duplicado ou estado inválido) |
