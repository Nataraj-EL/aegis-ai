# Contributing to Aegis AI

Welcome! Thank you for contributing to Aegis AI (An Agentic Business Operating System). To maintain code quality and engineering standards, please adhere to these guidelines.

## Code of Conduct
Please be respectful and professional in all communications, pull requests, and discussions.

## Tech Stack Expectations
- **Java**: Java 21, Spring Boot 3.5.x, Maven, Spring Security 6.x. Follow standard SOLID and clean code architectures.
- **Next.js**: Next.js 15 App Router, React 19, TypeScript, Tailwind CSS. Focus on a modern, clean SaaS UI inspired by Linear, Notion, and Vercel.

## Coding Standards
- Maintain strict separation of concerns (DTO pattern, Services, Repositories).
- Avoid raw exceptions. Use `AegisException` and define appropriate subclasses.
- Ensure all styling uses the designated design system, not inline ad-hoc styles.

## Branch and Commit Guidelines
- Commit messages must follow **Conventional Commits**:
  - `feat:` for new features
  - `fix:` for bug fixes
  - `docs:` for documentation updates
  - `refactor:` for code restructurings
  - `chore:` for dependency upgrades or project settings
- Do not check in active secrets, database credentials, or `.env` files. Ensure they are added to `.gitignore`.
