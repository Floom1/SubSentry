import datetime
from django.contrib.auth import authenticate, login, logout
from django.contrib.auth.models import User
from rest_framework import viewsets, permissions, status
from rest_framework.decorators import action
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework.permissions import AllowAny
from .models import Category, Subscription, Notification
from .serializers import UserSerializer, CategorySerializer, SubscriptionSerializer, SubscriptionSummarySerializer, NotificationSerializer
from django.db.models import Sum
from django.utils import timezone


class RegisterView(APIView):
    permission_classes = [AllowAny]

    def post(self, request):
        serializer = UserSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

class LoginView(APIView):
    permission_classes = [AllowAny]

    def post(self, request):
        username = request.data.get('username')
        password = request.data.get('password')

        user = authenticate(username=username, password=password)
        if user is not None:
            login(request, user)
            serializer = UserSerializer(user)
            return Response(serializer.data)
        return Response({'error': 'Invalid credentials'}, status=status.HTTP_401_UNAUTHORIZED)

class LogoutView(APIView):
    def post(self, request):
        logout(request)
        return Response({'message': 'Successfully logged out'}, status=status.HTTP_200_OK)

class UserDetailView(APIView):
    def get(self, request):
        serializer = UserSerializer(request.user)
        return Response(serializer.data)


class IsOwnerOrReadOnly(permissions.BasePermission):
    """
    Пользователь может редактировать только свои объекты.
    """
    def has_object_permission(self, request, view, obj):
        # Разрешаем чтение всем
        if request.method in permissions.SAFE_METHODS:
            return True
        # Разрешаем запись только владельцу
        return obj.user == request.user

class CategoryViewSet(viewsets.ModelViewSet):
    serializer_class = CategorySerializer
    permission_classes = [permissions.IsAuthenticated, IsOwnerOrReadOnly]

    def get_queryset(self):
        """Возвращаем только категории текущего пользователя"""
        return Category.objects.filter(user=self.request.user)

    def perform_create(self, serializer):
        """Автоматически устанавливаем текущего пользователя как владельца"""
        serializer.save(user=self.request.user)

class SubscriptionViewSet(viewsets.ModelViewSet):
    serializer_class = SubscriptionSerializer
    permission_classes = [permissions.IsAuthenticated, IsOwnerOrReadOnly]

    def get_queryset(self):
        """Возвращаем только подписки текущего пользователя"""
        return Subscription.objects.filter(user=self.request.user)

    def perform_create(self, serializer):
        """Автоматически устанавливаем текущего пользователя как владельца"""
        serializer.save(user=self.request.user)

class SubscriptionSummaryView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request):
        """Получаем все подписки пользователя с общей суммой"""
        subscriptions = Subscription.objects.filter(user=request.user)
        total_amount = subscriptions.aggregate(total=Sum('price'))['total'] or 0

        serializer = SubscriptionSummarySerializer({
            'subscriptions': subscriptions,
            'total_amount': total_amount
        })

        return Response(serializer.data)



class NotificationViewSet(viewsets.ModelViewSet):
    serializer_class = NotificationSerializer
    permission_classes = [permissions.IsAuthenticated, IsOwnerOrReadOnly]

    def get_queryset(self):
        """Возвращаем только уведомления текущего пользователя"""
        return Notification.objects.filter(user=self.request.user)

    @action(detail=False, methods=['get'])
    def pending(self, request):
        """Получить все ожидающие уведомления"""
        notifications = self.get_queryset().filter(status='pending')
        serializer = self.get_serializer(notifications, many=True)
        return Response(serializer.data)

    @action(detail=True, methods=['post'])
    def mark_as_shown(self, request, pk=None):
        """Пометить уведомление как показанное"""
        notification = self.get_object()
        notification.status = 'shown'
        notification.shown_at = timezone.now()
        notification.save()
        return Response({'status': 'success', 'message': 'Уведомление помечено как показанное'})

    @action(detail=True, methods=['post'])
    def mark_as_dismissed(self, request, pk=None):
        """Пометить уведомление как проигнорированное"""
        notification = self.get_object()
        notification.status = 'dismissed'
        notification.shown_at = timezone.now()
        notification.save()
        return Response({'status': 'success', 'message': 'Уведомление помечено как проигнорированное'})

class CheckNotificationsView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def post(self, request):
        """
        Проверить предстоящие платежи и создать уведомления если нужно
        Вызывается Android приложением при запуске
        """
        today = datetime.date.today()  # Используем datetime.date.today()
        notifications_created = 0

        # Получаем все подписки пользователя
        subscriptions = Subscription.objects.filter(user=request.user)

        for subscription in subscriptions:
            days_until_payment = (subscription.next_payment_date - today).days

            # Проверяем, нужно ли создать уведомление
            if 0 <= days_until_payment <= subscription.notification_days_before:
                # Проверяем, нет ли уже созданного уведомления для этой подписки
                existing_notification = Notification.objects.filter(
                    user=request.user,
                    subscription=subscription,
                    status='pending',
                    created_at__date=today  # Только сегодняшние уведомления
                ).first()

                if not existing_notification:
                    message = (
                        f"Напоминание: ваша подписка '{subscription.name}' "
                        f"требует оплаты {subscription.next_payment_date.strftime('%d.%m.%Y')}. "
                        f"Сумма: {subscription.price} руб."
                    )

                    Notification.objects.create(
                        user=request.user,
                        subscription=subscription,
                        message=message,
                        status='pending'
                    )
                    notifications_created += 1

        return Response({
            'notifications_created': notifications_created,
            'message': f'Создано {notifications_created} новых уведомлений'
        })