# 🎮 Pokédex Full Stack: React + Spring Boot + MongoDB

Continuação da **Atividade 03 (Pokédex com React + Vite)**. O arquivo `pokedex-data.json` foi substituído por uma **API REST em Spring Boot** que guarda os dados no **MongoDB**, populado a partir do `Script_Pokedex.sql`.

Responsáveis:
Samuel Henrique Alvarenga e Lopes /
Nicolas de Almeida

```text
Script_Pokedex.sql
        ↓
     MongoDB
        ↓
Spring Boot REST API
        ↓
   React + Vite
```

---

## ⚡ Funcionalidades

- 🔎 **Pesquisa** de Pokémon por nome ou ID.
- 🧬 **Filtro por tipo** e ordenação por qualquer atributo.
- 📄 **Detalhes** com atributos, descrição (PokéAPI) e treinadores que possuem o Pokémon.
- ➕ **Cadastro**, ✏️ **edição** e 🗑️ **exclusão** de Pokémon, salvos no MongoDB pela API.
- 🧑‍🚀 **Treinadores** e suas equipes vindos do MongoDB.
- ⚖️ Comparação entre dois Pokémon, ⭐ favoritos e 🌙 tema escuro (da Atividade 03).

## 🛠️ Tecnologias

| Camada | Tecnologias |
|---|---|
| Frontend | React 18, Vite 5, CSS |
| Backend | Java 21+, Spring Boot 3.5 (Spring Web, Spring Data MongoDB, Bean Validation) |
| Banco | MongoDB (Atlas ou local) |

## 📂 Estrutura

```text
pokedex-DIAW/
├── backend/                      # API REST Spring Boot
│   ├── src/main/java/com/pokedex/
│   │   ├── controller/           # PokemonController, TypeController, TrainerController
│   │   ├── service/              # Regras de negócio e validações
│   │   ├── repository/           # Interfaces Spring Data MongoDB
│   │   ├── model/                # Documentos: Pokemon, PokemonType, Trainer
│   │   ├── seed/                 # Leitura do Script_Pokedex.sql e carga inicial do MongoDB
│   │   ├── exception/            # Tratamento de erros (400, 404, 409)
│   │   └── config/               # CORS
│   ├── src/main/resources/application.properties
│   ├── requests.http             # Exemplos de requisições para testar a API
│   ├── .env.example
│   └── pom.xml
├── frontend/                     # Pokédex da Atividade 03 (React + Vite)
│   ├── src/services/api.js       # Cliente da API REST
│   ├── src/components/PokemonForm.jsx  # Formulário de cadastro/edição
│   ├── vite.config.js            # Proxy /api → http://localhost:8080
│   └── package.json
├── Script_Pokedex.sql            # Fonte dos dados
└── README.md
```

---

## 🗄️ Modelagem no MongoDB

O script SQL tem 5 tabelas (`Pokemon`, `Types`, `Rel_Pokemon_Type`, `Trainers`, `Rel_Trainer_Pokemon`). No MongoDB elas viraram **3 coleções**:

**`pokemons`**: os tipos ficam embutidos no documento, porque sempre são lidos junto com o Pokémon (a tabela `Rel_Pokemon_Type` deixa de existir).

```json
{
  "_id": 25,
  "name": "Pikachu",
  "hp": 35,
  "attack": 55,
  "defense": 30,
  "specialAttack": 50,
  "specialDefense": 40,
  "speed": 90,
  "types": ["Electric"]
}
```

**`types`**: lista de tipos válidos, usada para validar cadastros e montar os filtros.

```json
{ "_id": 5, "name": "Electric" }
```

**`trainers`**: a relação com os Pokémon (`Rel_Trainer_Pokemon`) vira uma lista de referências por ID. Repetições são mantidas, porque um treinador pode ter mais de um Pokémon da mesma espécie (o Koga tem três Venonat).

```json
{ "_id": 6, "name": "Koga", "badges": 5, "pokemonIds": [48, 48, 48, 49] }
```

Observações:
- O Mew não tem tipo no script, então recebe o tipo `Typeless` (ID 0 da tabela `Types`).
- Ao excluir um Pokémon, a API também o remove das equipes dos treinadores.

### Como os dados são carregados

Não é preciso MySQL. Ao iniciar, o backend lê o `Script_Pokedex.sql` (empacotado pelo Maven a partir da raiz do repositório), interpreta os `INSERT INTO ... VALUES` (`seed/SqlScriptParser.java`) e grava as coleções (`seed/DatabaseSeeder.java`).

A carga só acontece quando a coleção `pokemons` está **vazia**, para não apagar cadastros e edições feitos pela API. Para recarregar os dados originais, inicie o backend com `SEED_RESET=true`.

---

## 🔐 Configuração do MongoDB

As credenciais **não ficam no código**. O backend lê a connection string da variável de ambiente `MONGODB_URI`, ou de um arquivo `backend/.env` que está no `.gitignore`.

### Opção 1: MongoDB Atlas (recomendado)

1. Crie um cluster gratuito (M0) em [cloud.mongodb.com](https://cloud.mongodb.com).
2. Em **Database Access**, crie um usuário com senha.
3. Em **Network Access**, libere o seu IP (ou `0.0.0.0/0` para testes).
4. Em **Connect → Drivers**, copie a connection string.
5. Copie o arquivo de exemplo e preencha com a sua string:

```bash
cd backend
cp .env.example .env
```

```properties
# backend/.env
MONGODB_URI=mongodb+srv://USUARIO:SENHA@SEU-CLUSTER.mongodb.net/?retryWrites=true&w=majority
MONGODB_DATABASE=pokedex
```

> Se a senha tiver caracteres especiais (`@`, `:`, `/`, `#`...), codifique-os na URL (ex.: `@` → `%40`).

### Opção 2: MongoDB local

Sem `.env`, o backend usa `mongodb://localhost:27017/pokedex`. Basta ter o MongoDB Community Server rodando na máquina.

### Variáveis disponíveis

| Variável | Padrão | Descrição |
|---|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/pokedex` | Connection string do MongoDB |
| `MONGODB_DATABASE` | `pokedex` | Nome do banco |
| `PORT` | `8080` | Porta da API |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://127.0.0.1:5173` | Origens liberadas no CORS |
| `SEED_RESET` | `false` | `true` apaga e recarrega as coleções a partir do script |
| `SEED_ENABLED` | `true` | `false` desativa a carga inicial |

---

## 🚀 Como executar

Pré-requisitos: **Java 21+**, **Maven 3.9+**, **Node.js 18+** e um MongoDB (Atlas ou local).

### 1. Clone o repositório

```bash
git clone https://github.com/Nicolas-De-Almeida/pokedex-DIAW.git
```

```bash
cd pokedex-DIAW
```

### 2. Backend (Spring Boot)

Configure o MongoDB (veja acima) e, dentro da pasta `backend`, rode:

```bash
cd backend
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`. Na primeira execução aparece no log:

```text
MongoDB populado a partir do Script_Pokedex.sql: 151 Pokémon, 18 tipos, 12 treinadores
```

Para rodar os testes: `mvn test`.

### 3. Frontend (React + Vite)

Em outro terminal:

```bash
cd frontend
npm install
npm run dev
```

Acesse `http://localhost:5173`. O Vite encaminha as chamadas `/api` para o backend em `localhost:8080`. Para apontar para outra URL, crie `frontend/.env` com `VITE_API_URL=https://endereco-da-api`.

---

## 🌐 Endpoints da API

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/pokemons` | Lista todos os Pokémon |
| `GET` | `/api/pokemons?name=pika&type=Electric` | Busca por nome e/ou tipo (filtros opcionais) |
| `GET` | `/api/pokemons/{id}` | Busca um Pokémon pelo ID |
| `GET` | `/api/pokemons/type/{type}` | Lista os Pokémon de um tipo |
| `GET` | `/api/pokemons/{id}/trainers` | Treinadores que possuem o Pokémon |
| `POST` | `/api/pokemons` | Cadastra um Pokémon |
| `PUT` | `/api/pokemons/{id}` | Atualiza um Pokémon |
| `DELETE` | `/api/pokemons/{id}` | Remove um Pokémon |
| `GET` | `/api/types` | Lista os tipos |
| `GET` | `/api/trainers` | Lista os treinadores |
| `GET` | `/api/trainers/{id}` | Busca um treinador |
| `GET` | `/api/trainers/{id}/pokemons` | Equipe completa do treinador |

Exemplo de corpo para `POST`/`PUT` (no `POST` o `id` é opcional; sem ele, a API usa o próximo número livre):

```json
{
  "name": "Chikorita",
  "hp": 45,
  "attack": 49,
  "defense": 65,
  "specialAttack": 49,
  "specialDefense": 65,
  "speed": 45,
  "types": ["Grass"]
}
```

Regras de validação:
- `name` obrigatório e único (sem diferenciar maiúsculas de minúsculas).
- Atributos obrigatórios, entre 1 e 255.
- 1 ou 2 tipos existentes na coleção `types`.

Respostas de erro:

| Status | Quando |
|---|---|
| `400 Bad Request` | Dados inválidos (com o detalhe de cada campo em `fields`) ou tipo inexistente |
| `404 Not Found` | Pokémon ou treinador não encontrado |
| `409 Conflict` | ID ou nome já cadastrado |

O arquivo [`backend/requests.http`](backend/requests.http) tem todas as requisições prontas para testar no VS Code (extensão REST Client) ou no IntelliJ. Elas também podem ser reproduzidas no Postman, Insomnia ou Thunder Client.

---

## 📚 Objetivos de aprendizagem

- 🗄️ Adaptar um modelo relacional para documentos no MongoDB (embutir × referenciar).
- ☕ Construir uma API REST com Spring Boot em camadas (Controller → Service → Repository → Model).
- ✅ Validar dados e padronizar respostas de erro.
- 🔗 Integrar o frontend React ao backend com `fetch`.
- 🔐 Manter credenciais fora do repositório com variáveis de ambiente.

---

<div align="center">

  🎮 **Feito com ⚡ React, ☕ Spring Boot, 🍃 MongoDB e um pouco de espírito Pokémon.**

  *Mais um passo na jornada para se tornar um Mestre Pokémon!*

</div>
