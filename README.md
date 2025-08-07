# User-Service - Интеграционные тесты с TestContainers

Этот проект демонстрирует использование TestContainers для написания интеграционных тестов в Spring Boot приложении.

## Описание проекта

User-Service - это Spring Boot приложение для управления пользователями и их банковскими картами. Приложение использует:
- PostgreSQL для хранения данных
- Redis для кэширования
- Spring Data JPA для работы с базой данных
- Spring Web для REST API
- Liquibase для миграций базы данных

## Интеграционные тесты

### Структура тестов

```
src/test/java/by/osinovi/userservice/integration/
├── BaseIntegrationTest.java           # Базовый класс для всех интеграционных тестов
├── UserControllerIntegrationTest.java # Тесты REST API пользователей
├── CardControllerIntegrationTest.java # Тесты REST API карт
├── CacheIntegrationTest.java         # Тесты кэширования
├── TransactionIntegrationTest.java   # Тесты транзакций и целостности данных
├── TestContainersTest.java           # Простой тест для проверки TestContainers
└── README.md                         # Документация по тестам
```

### Технологии тестирования

- **TestContainers** - для создания изолированных контейнеров с PostgreSQL и Redis
- **Spring Boot Test** - для интеграционного тестирования
- **MockMvc** - для тестирования REST API
- **JUnit 5** - для написания тестов

### Контейнеры

- **PostgreSQL**: `postgres:15-alpine`
  - База данных: `user_service_test`
  - Пользователь: `test_user`
  - Пароль: `test_password`

- **Redis**: `redis:7.4.2`
  - Порт: `6379`

## Запуск тестов

### Предварительные требования

1. **Docker Desktop** установлен и запущен
2. **Java 21**
3. **Maven**

### Команды для запуска

```bash
# Запуск всех интеграционных тестов
./mvnw test -Dtest="*IntegrationTest"

# Запуск конкретного теста
./mvnw test -Dtest="UserControllerIntegrationTest"

# Запуск теста TestContainers
./mvnw test -Dtest="TestContainersTest"

# Запуск с подробным выводом
./mvnw test -Dtest="*IntegrationTest" -X
```

## Покрытие тестами

### UserControllerIntegrationTest
- ✅ Создание пользователя
- ✅ Получение пользователя по ID
- ✅ Получение пользователей по списку ID
- ✅ Получение пользователя по email
- ✅ Обновление пользователя
- ✅ Удаление пользователя
- ✅ Валидация данных
- ✅ Обработка ошибок

### CardControllerIntegrationTest
- ✅ Создание карты для пользователя
- ✅ Получение карты по ID
- ✅ Получение карт пользователя
- ✅ Обновление карты
- ✅ Удаление карты
- ✅ Валидация данных карт
- ✅ Обработка ошибок

### CacheIntegrationTest
- ✅ Кэширование результатов запросов
- ✅ Инвалидация кэша при обновлении
- ✅ Инвалидация кэша при удалении
- ✅ Кэширование по email
- ✅ Кэширование множественных запросов

### TransactionIntegrationTest
- ✅ Каскадное удаление карт при удалении пользователя
- ✅ Проверка уникальных ограничений
- ✅ Проверка валидации данных
- ✅ Проверка отката транзакций при ошибках

## Особенности реализации

### Изоляция тестов
- Каждый тест выполняется в изолированной среде
- База данных создается заново для каждого теста
- Кэш очищается перед каждым тестом
- Контейнеры перезапускаются между тестами

### Конфигурация
- Используется профиль `test`
- Liquibase отключен для тестов
- Hibernate создает схему автоматически
- Подробное логирование SQL запросов

### Валидация данных
- Проверка корректности валидации входных данных
- Обработка уникальных ограничений
- Правильность HTTP статус кодов
- Структура JSON ответов

## Отладка

### Просмотр логов
```bash
./mvnw test -Dtest="*IntegrationTest" -Dlogging.level.by.osinovi.userservice=DEBUG
```

### Проверка контейнеров
```bash
docker ps
```

### Очистка контейнеров
```bash
docker container prune
docker volume prune
```

## Добавление новых тестов

1. Создайте новый класс теста, наследующий от `BaseIntegrationTest`
2. Используйте `@AutoConfigureWebMvc` для тестов REST API
3. Настройте `MockMvc` в `@BeforeEach`
4. Следуйте паттерну Given-When-Then для структуры тестов
5. Добавьте тесты для позитивных и негативных сценариев

## Пример структуры теста

```java
@Test
void testMethod_ShouldReturnExpectedResult() throws Exception {
    // Given - подготовка данных
    UserRequestDto request = new UserRequestDto();
    // ... настройка request

    // When - выполнение действия
    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))

    // Then - проверка результата
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists());
}
```

## Преимущества TestContainers

1. **Изоляция** - каждый тест работает с чистой базой данных
2. **Реалистичность** - тесты используют реальные контейнеры
3. **Автоматизация** - контейнеры запускаются и останавливаются автоматически
4. **Портативность** - тесты работают на любой машине с Docker
5. **Производительность** - быстрее, чем внешние зависимости

## Заключение

Интеграционные тесты с TestContainers обеспечивают надежное тестирование приложения в условиях, максимально приближенных к продакшену, при этом сохраняя изоляцию и автоматизацию процесса тестирования. 