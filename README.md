# GestPro

Sistema completo de gestão para mercados e lojas, desenvolvido com arquitetura moderna utilizando Next.js 14+ no frontend e Spring Boot 3 no backend.

[![License](https://img.shields.io/badge/license-All%20Rights%20Reserved-red.svg)](LICENSE)

## 📋 Sobre o Projeto

GestPro é uma solução completa para gestão comercial que oferece controle de produtos, estoque, vendas, clientes e relatórios através de uma interface intuitiva e moderna.

### Principais Funcionalidades

- **Autenticação completa**: Login com email/senha e OAuth2 (Google)
- **Gestão de usuários**: Cadastro, recuperação de senha e confirmação por email
- **Controle de acesso**: Sistema de planos (EXPERIMENTAL/ASSINANTE) e status de usuário
- **Dashboard**: Visão geral e atalhos rápidos para funcionalidades principais
- **Gestão comercial**: Produtos, estoque, vendas e clientes
- **Relatórios**: Análises e indicadores de performance

### Screenshots

#### Tela de Login
![Login](https://raw.githubusercontent.com/MartnsDev/Gest-Pro/b22799e9e53523f9b9442e41db645f729c92247c/Img/gestpro-login.png)

#### Dashboard
![Dashboard](https://github.com/MartnsDev/Gest-Pro/blob/c7f08fcf4571fefae78d8af88cb5fca656c48328/Img/Gest-Pro_Dashboard.png)

## 🚀 Tecnologias

### Frontend
- **Next.js 14+** com App Router
- **TypeScript**
- **Tailwind CSS** para estilização
- **shadcn/ui** como biblioteca de componentes
- **Lucide Icons**

### Backend
- **Java 17+**
- **Spring Boot 3.x**
- **Spring Security** com autenticação JWT
- **OAuth2** para login social (Google)
- **MySQL 8+**
- **Redis** para caching
- **Maven** para gerenciamento de dependências
- **Swagger** para documentação da API

## 📂 Estrutura do Repositório

```
GestPro/
├── frontend/          # Aplicação Next.js
├── backend/           # API Spring Boot
├── Img/               # Assets do README
└── README.md
```

## ⚙️ Configuração e Instalação

### Pré-requisitos

- Java 17+
- Node.js 18+
- MySQL 8+
- Redis
- Maven

### 1. Clone o Repositório

```bash
git clone https://github.com/MartnsDev/Gest-Pro.git
cd GestPro
```

### 2. Configuração do Backend

#### 2.1 Variáveis de Ambiente

O projeto utiliza variáveis de ambiente para configuração. Crie e configure as seguintes variáveis:

**Windows (PowerShell):**
```powershell
# Database
setx DB_URL "jdbc:mysql://localhost:3306/gestpro_db"
setx DB_USERNAME "root"
setx DB_PASSWORD "sua_senha"

# Server
setx SERVER_PORT "8080"
setx APP_BASE_URL "http://localhost:8080"

# JPA/Hibernate
setx JPA_HBM_DDL "update"
setx JPA_SHOW_SQL "true"
setx JPA_FORMAT_SQL "true"
setx JPA_OPEN_IN_VIEW "false"

# Swagger
setx SWAGGER_API_DOCS_PATH "/v3/api-docs"
setx SWAGGER_UI_PATH "/swagger-ui.html"

# JWT
setx JWT_SECRET "sua_chave_secreta_jwt_minimo_256_bits"
setx JWT_EXPIRATION "86400000"

# Basic Auth
setx BASIC_AUTH_USER "admin"
setx BASIC_AUTH_PASSWORD "admin"
setx BASIC_AUTH_ROLE "ADMIN"

# OAuth2 Google (ver seção OAuth2)
setx GOOGLE_CLIENT_ID "seu_client_id"
setx GOOGLE_CLIENT_SECRET "seu_client_secret"
setx GOOGLE_SCOPE "openid,email,profile"
setx GOOGLE_REDIRECT_URI "http://localhost:8080/login/oauth2/code/google"
setx GOOGLE_AUTH_URI "https://accounts.google.com/o/oauth2/v2/auth"
setx GOOGLE_TOKEN_URI "https://oauth2.googleapis.com/token"
setx GOOGLE_USERINFO_URI "https://www.googleapis.com/oauth2/v3/userinfo"
setx GOOGLE_USERNAME_ATTR "sub"

# Email (ver seção Email)
setx MAIL_HOST "smtp.gmail.com"
setx MAIL_PORT "587"
setx MAIL_USERNAME "seu_email@gmail.com"
setx MAIL_PASSWORD "senha_de_app_google"
setx MAIL_SMTP_AUTH "true"
setx MAIL_SMTP_STARTTLS "true"
```

**Linux/macOS (bash/zsh):**

Adicione ao arquivo `~/.bashrc` ou `~/.zshrc`:

```bash
# Database
export DB_URL="jdbc:mysql://localhost:3306/gestpro_db"
export DB_USERNAME="root"
export DB_PASSWORD="sua_senha"

# Server
export SERVER_PORT="8080"
export APP_BASE_URL="http://localhost:8080"

# JPA/Hibernate
export JPA_HBM_DDL="update"
export JPA_SHOW_SQL="true"
export JPA_FORMAT_SQL="true"
export JPA_OPEN_IN_VIEW="false"

# Swagger
export SWAGGER_API_DOCS_PATH="/v3/api-docs"
export SWAGGER_UI_PATH="/swagger-ui.html"

# JWT
export JWT_SECRET="sua_chave_secreta_jwt_minimo_256_bits"
export JWT_EXPIRATION="86400000"

# Basic Auth
export BASIC_AUTH_USER="admin"
export BASIC_AUTH_PASSWORD="admin"
export BASIC_AUTH_ROLE="ADMIN"

# OAuth2 Google
export GOOGLE_CLIENT_ID="seu_client_id"
export GOOGLE_CLIENT_SECRET="seu_client_secret"
export GOOGLE_SCOPE="openid,email,profile"
export GOOGLE_REDIRECT_URI="http://localhost:8080/login/oauth2/code/google"
export GOOGLE_AUTH_URI="https://accounts.google.com/o/oauth2/v2/auth"
export GOOGLE_TOKEN_URI="https://oauth2.googleapis.com/token"
export GOOGLE_USERINFO_URI="https://www.googleapis.com/oauth2/v3/userinfo"
export GOOGLE_USERNAME_ATTR="sub"

# Email
export MAIL_HOST="smtp.gmail.com"
export MAIL_PORT="587"
export MAIL_USERNAME="seu_email@gmail.com"
export MAIL_PASSWORD="senha_de_app_google"
export MAIL_SMTP_AUTH="true"
export MAIL_SMTP_STARTTLS="true"
```

Aplique as alterações:
```bash
source ~/.bashrc  # ou source ~/.zshrc
```

#### 2.2 Configuração do Banco de Dados

```bash
# Crie o banco de dados MySQL
mysql -u root -p
CREATE DATABASE gestpro_db;
exit;
```

#### 2.3 Execute o Backend

```bash
cd backend
./mvnw spring-boot:run
```

O backend estará disponível em `http://localhost:8080`

### 3. Configuração do Frontend

```bash
cd frontend
npm install
npm run dev
```

O frontend estará disponível em `http://localhost:3000`

## 🔐 Configurações Adicionais

### OAuth2 - Google Login

Para habilitar login com Google:

1. Acesse o [Google Cloud Console](https://console.cloud.google.com)
2. Crie um novo projeto
3. Ative a **OAuth consent screen**:
   - Tipo: Externo
   - Adicione nome do app e email de suporte
4. Crie credenciais OAuth 2.0:
   - Tipo: Aplicativo da Web
   - URI de redirecionamento: `http://localhost:8080/login/oauth2/code/google`
5. Copie o **Client ID** e **Client Secret** para as variáveis de ambiente

![Google Auth Setup](Img/Create-project_googleAuth.png)

### Envio de Email (Gmail)

Para habilitar envio de emails:

1. Acesse [Google Account Security](https://myaccount.google.com/security)
2. Ative a **verificação em duas etapas**
3. Gere uma **senha de app**:
   - Nome sugerido: "GestPro Spring Boot"
   - Use a senha gerada na variável `MAIL_PASSWORD`

![Email Sender Setup](Img/emailsender-1.png)

**Funcionalidades de Email:**
- Confirmação de cadastro
- Recuperação de senha
- Notificações do sistema

## 📚 Documentação da API

A documentação interativa da API está disponível via Swagger:

```
http://localhost:8080/swagger-ui.html
```

![Swagger Documentation](https://github.com/MartnsDev/Gest-Pro/blob/2ced41f10df3341faa91cdcd0596061cfdcbc920/Img/Documenta%C3%A7%C3%A3o-Swagger.png)

## 🔒 Segurança

- Autenticação JWT com tokens de refresh
- OAuth2 para login social
- Senhas criptografadas com BCrypt
- Validação de email obrigatória
- Códigos de verificação com expiração
- Proteção CSRF
- Rate limiting

## ⚠️ Observações Importantes

- **Nunca commit credenciais**: Todas as informações sensíveis devem estar em variáveis de ambiente
- **JWT Secret**: Use uma chave forte com no mínimo 256 bits
- **Email dedicado**: Use um email específico para o sistema, não seu email pessoal
- **Redis**: Necessário para caching e otimização de performance

## 📖 Links Úteis

- [Código Frontend](https://github.com/MartnsDev/Gest-Pro/tree/2ced41f10df3341faa91cdcd0596061cfdcbc920/FrontEnd)
- [Código Backend](https://github.com/MartnsDev/Gest-Pro/tree/2ced41f10df3341faa91cdcd0596061cfdcbc920/Backend)

## 📝 Licença

Todos os direitos reservados © 2025 Matheus Martins (MartnsDev)

Este projeto não pode ser copiado, reproduzido ou utilizado sem autorização expressa do autor.

## 👤 Autor

**Matheus Martins**

- LinkedIn: [@matheusmartnsdev](https://www.linkedin.com/in/matheusmartnsdev/)
- GitHub: [@MartnsDev](https://github.com/MartnsDev)

---

Desenvolvido com 💚 por Matheus Martins
