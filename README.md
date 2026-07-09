# Aegis AI: An Agentic Business Operating System

Aegis AI is a production-quality enterprise AI application designed as an **Agentic Business Operating System**. It leverages autonomous agent orchestration, pgvector semantic search, and Google Gemini to coordinate workflows and execute memory processes.

---

## Technical Stack

- **Frontend**: Next.js 15 (App Router), React 19, TypeScript, Tailwind CSS
- **Backend**: Java 17, Spring Boot 3.5.x, Spring AI (Google GenAI Gemini), Spring Data JPA, Maven
- **Database**: PostgreSQL with `pgvector` extension

---

## Directory Structure

```text
aegis-ai/
├── docs/                     # System architecture & specs docs
│   ├── architecture/         # System design details
│   ├── api/                  # API contracts
│   ├── database/             # Schemas and vector indices
│   ├── diagrams/             # Mermaid and visual flows
│   └── sprints/              # Sprint-by-sprint release logs
│
├── aegis-backend/            # Spring Boot Maven application
│   ├── pom.xml               # Backend dependency definitions
│   └── src/main/java/com/aegis/backend/
│       ├── agent/            # Agent state machines and models
│       ├── ai/               # LLM clients & Embeddings configs
│       ├── workflow/         # Multi-agent coordination flows
│       ├── rag/              # Retrieval Augmented Generation logic
│       ├── memory/           # Vector episodic & semantic storage
│       ├── tool/             # Executable tools for agents
│       ├── security/         # Spring Security & JWT auth (Sprint 2)
│       ├── config/           # Application Configuration
│       ├── controller/       # Rest API Controllers
│       ├── dto/              # Request / Response Data Transfer Objects
│       ├── entity/           # Database JPA Entities
│       ├── repository/       # Database JPA Repositories
│       ├── service/          # Business logic Services
│       ├── exception/        # Exception handlers and custom exceptions
│       └── util/             # Utility classes
│
├── aegis-frontend/           # Next.js 15 frontend application
│   ├── package.json          # Node dependencies
│   ├── tailwind.config.ts    # Modern SaaS theme settings
│   └── src/
│       ├── app/              # Next.js App Router (Layouts & Pages)
│       └── lib/              # Client-side helpers (Axios/fetch)
│
├── .gitignore                # Global Git Ignore rules
├── .editorconfig             # Editor layout properties
├── .gitattributes            # LF Normalization
└── .env.example              # Development credentials template
```

---

## Local Setup and Requirements

### 1. Prerequisites
- **Java JDK**: Version 17 or higher
- **Maven**: Version 3.8+
- **Node.js**: Version 20.x or higher
- **PostgreSQL**: With the `pgvector` extension installed

### 2. Configure Environment Variables
Copy `.env.example` to `.env` in the root folder (Note: `.env` is ignored by Git):
```bash
cp .env.example .env
```
Fill in your database URL, credentials, and your Google AI Studio Gemini API key:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_AI_GEMINI_API_KEY`

Expose these variables in your active shell or terminal profile.

### 3. Build & Run Backend
Navigate to `aegis-backend/` and run:
```bash
# Build and compile
mvn clean compile

# Run the boot application
mvn spring-boot:run
```
The server starts on port `8080` (or the environment specified `PORT`), exposing servlet path `/api`.

### 4. Build & Run Frontend
Navigate to `aegis-frontend/` and run:
```bash
# Install dependencies
npm install

# Run the development server
npm run dev
```
Open [http://localhost:3000](http://localhost:3000) to view the SaaS UI.
- Landing page: `/`
- Dashboard Demo: `/dashboard`

---

## Engineering Guidelines

- **Clean Architecture**: Decouple logic, keep controllers thin, and enforce the DTO pattern.
- **Strict Exception Handling**: Never return raw spring stack traces. Use `AegisException` wrappers caught by `GlobalExceptionHandler`.
- **Environment Driven**: Do not hardcode connection endpoints or API credentials. Use environment-based property placeholders.
- **Conventional Commits**: Every commit follows structure `<type>: <description>` (e.g. `feat: scaffold frontend setup`).

---

## Multi-LLM Provider Architecture

Aegis AI supports multiple LLM providers dynamically using a runtime abstraction layer. Active providers are instantiated only if their corresponding credentials are set in environment variables.

### Supported Providers
- **Gemini**: Exposes both `CHAT` and `EMBEDDING` capabilities. Requires `SPRING_AI_GEMINI_API_KEY`.
- **OpenAI**: Exposes both `CHAT` and `EMBEDDING` capabilities. Requires `OPENAI_API_KEY`.
- **Groq**: Exposes `CHAT` only. Requires `GROQ_API_KEY`.
- **OpenRouter**: Exposes `CHAT` only. Requires `OPENROUTER_API_KEY`.
- **Ollama**: Exposes both `CHAT` and `EMBEDDING` capabilities locally. Automatically active if `OLLAMA_BASE_URL` is configured.

### Fallback Strategies
Configure `ai.fallback-strategy` in `application.yml` or via the `AI_FALLBACK_STRATEGY` environment variable:
1. `PRIMARY_ONLY`: Try only the configured primary provider.
2. `FAILOVER`: Try the primary provider first, fallback to remaining prioritized providers.
3. `PRIORITY_CHAIN`: Chain directly through the list configured in `ai.priority-list`.

### Configuration Example (`application.yml` / `.env`):
```yaml
ai:
  provider: gemini
  fallback-strategy: FAILOVER
  priority-list:
    - gemini
    - openai
    - groq
    - openrouter
    - ollama
  providers:
    gemini:
      api-key: ${SPRING_AI_GEMINI_API_KEY}
      model: gemini-1.5-flash
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: https://api.openai.com/v1
      model: gpt-4o
```
Masking is applied to all `apiKey` values. To inspect instantiated providers, query `GET /api/v1/ai/providers`.

---

## Docker & Production Deployment

Aegis AI supports production containerization via Docker Compose.

### Dockerized Services
1. **`db` (aegis-postgres)**: PostgreSQL database container utilizing `ankane/pgvector` pre-configured with the `pgvector` extension.
2. **`backend` (aegis-backend)**: Spring Boot container built using a multi-stage `Dockerfile`. Utilizes `layertools` for optimized dependency caching and executes as a non-root user `spring:spring` for enhanced security.
3. **`ollama` (aegis-ollama)**: Local LLM service. Optional container run under the `ollama` compose profile.

### Configuration (`.env`)
Copy the sample `.env.example` in the root directory to `.env`:
```bash
cp .env.example .env
```
Fill in the database password, JWT secret, and LLM API keys. Docker Compose automatically injects these values on startup.

### Start the Application
Start the database and backend application containers:
```bash
docker compose up -d --build
```
This command builds the multi-stage, non-root backend image, waits for the PostgreSQL database container health check to be `healthy`, runs Flyway schema migrations, and exposes the backend service on port `8080`.

To start the optional local Ollama container profile alongside the services:
```bash
docker compose --profile ollama up -d
```

### Health & Monitoring Inside Containers
- **Actuator Health Check**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- **Actuator Prometheus Metrics**: [http://localhost:8080/actuator/prometheus](http://localhost:8080/actuator/prometheus)
- **Swagger Documentation**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Container logs**: Mounted to the host `./logs` directory for easy troubleshooting and audit archiving.


