# SubSentry - Менеджер подписок

SubSentry - это приложение для управления подписками, которое позволяет отслеживать даты оплаты и получать уведомления о предстоящих платежах.

## Структура проекта

- **Backend (Django)**: `backend/`
- **Frontend (Android)**: `frontend/`

## Требования

- Python 3.8+
- Android Studio
- Android SDK (API Level 21+)
- Gradle 8.10.2
- Android Gradle Plugin 8.8.0

## Установка и запуск

### Backend (Django)

1. Перейдите в директорию backend:
   ```bash
   cd backend
   ```

2. Создайте виртуальное окружение и активируйте его:
   ```bash
   python -m venv venv
   source venv/bin/activate  # Linux/Mac
   # или
   venv\Scripts\activate     # Windows
   ```

3. Установите зависимости:
   ```bash
   pip install -r requirements.txt
   ```

4. Выполните миграции:
   ```bash
   python manage.py makemigrations
   python manage.py migrate
   ```

5. Запустите сервер:
   ```bash
   python manage.py runserver 0.0.0.0:8000
   ```

### Frontend (Android)

1. Откройте проект в Android Studio:
   - File → Open → выберите директорию `frontend/`

2. Дождитесь синхронизации Gradle.

3. Запустите приложение на эмуляторе или устройстве.

## Особенности

- Регистрация и авторизация пользователей
- Управление подписками (добавление, просмотр, удаление)
- Автоматическая проверка уведомлений
- Уведомления о предстоящих платежах
- Отображение статистики по подпискам

## Архитектура

### Backend
- Django REST Framework
- SQLite база данных (включена в репозиторий)

### Frontend
- Kotlin
- Android Jetpack (Navigation, View Binding, ViewModel)
- Retrofit для работы с API
- WorkManager для фоновых задач
- Material Design Components
