# Система управления банковскими картами

---

## 🚀 Быстрый старт (через Docker)

Перед запуском убедись, что установлены:

- [Docker](https://www.docker.com/)
- [Docker Compose](https://docs.docker.com/compose/)

---

## 📁 Шаг 1. Клонируй репозиторий

```bash
git clone https://github.com/your-username/bank-cards-service.git
cd bank-cards-service
```

---

## 🔐 Шаг 2. Сгенерируй ключи

Перед сборкой необходимо создать директорию `src/main/resources/keys` и сгенерировать туда ключи. Ключи не хранятся в репозитории, поэтому создай их вручную:

```bash
mkdir -p src/main/resources/keys

# Сгенерируй RSA-ключ для шифрования номеров карт
openssl genpkey -algorithm RSA -out src/main/resources/keys/card.key -pkeyopt rsa_keygen_bits:2048

# Сгенерируй RSA-ключ для подписи JWT-токенов
openssl genpkey -algorithm RSA -out src/main/resources/keys/jwt.key -pkeyopt rsa_keygen_bits:2048
```
---


## ⚙️ Шаг 3. Переменные окружения

Переменные задаются в `.env` и используются при сборке приложения:

| Переменная          | Назначение          |
|---------------------|---------------------|
| `APP_NAME`          | Название приложения |
| `APP_PORT`          | Порт Spring Boot    |
| `POSTGRES_PORT`     | Порт базы данных    |
| `POSTGRES_DB`       | Имя базы данных     |
| `POSTGRES_USER`     | Логин PostgreSQL    |
| `POSTGRES_PASSWORD` | Пароль PostgreSQL   |

---

## 🐳 Шаг 4. Собери и запусти контейнеры

```bash
docker-compose up --build
```

Приложение будет доступно по адресу:  
[http://localhost:8080](http://localhost:8080)

---

## 📚 Swagger API Docs

Swagger UI доступен после запуска по адресу:

```
http://localhost:8080/swagger-ui.html
```

---

## ✅ Готово

После запуска ты должен увидеть лог Spring Boot о старте приложения и применении миграций Liquibase. API готов к использованию!
