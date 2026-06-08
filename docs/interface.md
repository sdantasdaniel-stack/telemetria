# Manual do Usuário — Sistema de Gestão de Devices

---

## Acesso ao Sistema

Abra o navegador e acesse o endereço do sistema. Na tela de login, informe seu **e-mail** e **senha** e clique em **Entrar**.

O sistema possui dois tipos de usuário:

| Tipo | Permissões |
|------|-----------|
| `ADMIN` | Acesso completo a todas as telas e funcionalidades |
| `USER` | Acesso apenas às suas empresas e devices |

Após o login, cada tipo de usuário é direcionado automaticamente para a sua tela inicial.

---

## Perfil ADMIN

### Dashboard (Tela Inicial)

Após o login, o ADMIN tem acesso a cinco opções:

| Botão | Ação |
|-------|------|
| **Empresas** | Abre a tela de gerenciamento de empresas |
| **Usuários** | Abre a tela de gerenciamento de usuários |
| **Devices** | Abre a tela de gerenciamento de devices |
| **Mapa** | Abre o mapa com os devices cadastrados |
| **Sair** | Encerra a sessão e volta para o login |

---

### Tela de Empresas

Exibe a lista de **todas** as empresas cadastradas (ativas e inativas).

<details>
<summary><strong>Cadastrar empresa</strong></summary>

- Preencha os campos **Nome**, **CNPJ** e **E-mail**.
- Clique em **Cadastrar**.
- A empresa nasce **ativa** automaticamente.

</details>

<details>
<summary><strong>Editar empresa</strong></summary>

- Clique no botão de edição na linha da empresa desejada.
- Altere os campos necessários e confirme.

</details>

<details>
<summary><strong>Desativar empresa</strong></summary>

- Clique em **Desativar** na linha da empresa.
- A empresa não é apagada, apenas marcada como inativa.
- Empresas inativas não aparecem para usuários do tipo `USER`.
- Não é possível desativar uma empresa já inativa.

</details>

<details>
<summary><strong>Reativar empresa</strong></summary>

- Clique em **Reativar** na linha da empresa inativa.
- Não é possível reativar uma empresa já ativa.

</details>

<details>
<summary><strong>Deletar empresa</strong></summary>

- Clique em **Deletar** na linha da empresa.
- **Atenção:** esta ação é **permanente** e apaga também todos os devices vinculados à empresa.

</details>

---

### Tela de Usuários

Exibe a lista de todos os usuários cadastrados no sistema.

<details>
<summary><strong>Cadastrar usuário</strong></summary>

- Preencha **Nome**, **E-mail**, **Senha** e selecione a **Role** (`ADMIN` ou `USER`).
- Vincule o usuário a pelo menos uma empresa.
- Clique em **Cadastrar**.
- O usuário nasce **ativo** automaticamente.

</details>

<details>
<summary><strong>Editar usuário</strong></summary>

- Clique no botão de edição na linha do usuário desejado.
- Altere os campos necessários e confirme.
- Todos os campos são obrigatórios na edição.

</details>

<details>
<summary><strong>Desativar usuário</strong></summary>

- Clique em **Desativar** na linha do usuário.
- O usuário não é apagado, apenas bloqueado — não conseguirá mais fazer login.
- Não é possível desativar um usuário já inativo.

</details>

<details>
<summary><strong>Reativar usuário</strong></summary>

- Clique em **Reativar** na linha do usuário inativo.
- O usuário volta a conseguir fazer login normalmente.
- Não é possível reativar um usuário já ativo.

</details>

<details>
<summary><strong>Deletar usuário</strong></summary>

- Clique em **Deletar** na linha do usuário.
- **Atenção:** esta ação é **permanente**.

</details>

---

### Tela de Devices

Exibe a lista de **todos** os devices cadastrados (ativos e inativos).

<details>
<summary><strong>Cadastrar device</strong></summary>

- Preencha **Nome**, **Identificador** e selecione a **Empresa**.
- **Latitude** e **Longitude** são opcionais — devices sem coordenadas não aparecem no mapa.
- Clique em **Cadastrar**.
- O device nasce **ativo** e com status **OFFLINE** automaticamente.

</details>

<details>
<summary><strong>Editar device</strong></summary>

- Clique no botão de edição na linha do device desejado.
- Altere os campos necessários e confirme.

</details>

<details>
<summary><strong>Desativar device</strong></summary>

- Clique em **Desativar** na linha do device.
- O device não é apagado, apenas marcado como inativo.
- Não é possível desativar um device já inativo.

</details>

<details>
<summary><strong>Reativar device</strong></summary>

- Clique em **Reativar** na linha do device inativo.
- Não é possível reativar um device já ativo.

</details>

<details>
<summary><strong>Deletar device</strong></summary>

- Clique em **Deletar** na linha do device.
- **Atenção:** esta ação é **permanente**.

</details>

---

### Mapa (ADMIN)

Exibe no mapa todos os devices que:

- Possuem **latitude e longitude** cadastrados.
- Estão **ativos**.
- Estão vinculados a **empresas ativas**.

> Devices sem coordenadas não aparecem no mapa.

---

## Perfil USER

### Tela Inicial

Após o login, o USER visualiza:

- Uma **saudação** com seu nome.
- Um **menu suspenso** para selecionar a empresa desejada.
- Botões de acesso ao **Mapa** e **Sair**.

Apenas empresas **ativas** e **vinculadas ao seu cadastro** aparecem no menu suspenso.

**Para carregar os devices de uma empresa:**
1. Selecione a empresa no menu suspenso.
2. Clique em **Carregar Devices**.
3. A tabela de devices daquela empresa será exibida.

> A tela atualiza automaticamente quando um ADMIN realiza alterações nas empresas — sem precisar recarregar a página.

---

### Cadastrar Device (USER)

Com uma empresa selecionada, um formulário de cadastro aparece na tela.

- Preencha **Nome** e **Identificador**.
- **Latitude** e **Longitude** são opcionais.
- Clique em **Cadastrar**.

**Restrições:**
- Só é possível cadastrar devices em empresas vinculadas ao seu cadastro.
- Não é possível cadastrar devices em empresas inativas.
- O identificador deve ser **único** no sistema.

---

### Mapa (USER)

Exibe no mapa os devices **ativos**, com **coordenadas cadastradas**, pertencentes às suas **empresas ativas**.

---

## Observações Gerais

- Campos obrigatórios não podem ser enviados em branco.
- E-mails e CNPJs duplicados não são aceitos pelo sistema.
- Identificadores de device duplicados não são aceitos.
- Sessões expiram automaticamente por inatividade — ao expirar, o sistema redireciona para o login.
