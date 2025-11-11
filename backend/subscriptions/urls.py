from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import (
    CategoryViewSet,
    SubscriptionViewSet,
    SubscriptionSummaryView,
    RegisterView, LoginView, LogoutView, UserDetailView,
    NotificationViewSet, CheckNotificationsView
)

# Создаем router для ViewSets
router = DefaultRouter()
router.register(r'categories', CategoryViewSet, basename='category')
router.register(r'subscriptions', SubscriptionViewSet, basename='subscription')
router.register(r'notifications', NotificationViewSet, basename='notification')


urlpatterns = [
    # Аутентификация
    path('auth/register/', RegisterView.as_view(), name='register'),
    path('auth/login/', LoginView.as_view(), name='login'),
    path('auth/logout/', LogoutView.as_view(), name='logout'),
    path('auth/user/', UserDetailView.as_view(), name='user-detail'),

    # Эндпоинт для получения всех подписок с общей суммой
    # ИЗМЕНЕННЫЙ ПУТЬ чтобы избежать конфликта с router\
    path('check-notifications/', CheckNotificationsView.as_view(), name='check-notifications'),
    path('subscription-summary/', SubscriptionSummaryView.as_view(), name='subscription-summary'),

    # Основные эндпоинты
    path('', include(router.urls)),
]