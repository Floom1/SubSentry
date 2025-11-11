from django.db import models
from django.contrib.auth.models import User
from django.utils import timezone
from django.db.models.signals import post_save
from django.dispatch import receiver

class Category(models.Model):
    name = models.CharField(max_length=100, verbose_name="Название категории")
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name="categories", verbose_name="Пользователь")

    class Meta:
        verbose_name = "Категория"
        verbose_name_plural = "Категории"
        unique_together = ['name', 'user']  # Уникальность: пользователь не может создать две одинаковые категории

    def __str__(self):
        return f"{self.name} ({self.user.username})"

class Subscription(models.Model):
    name = models.CharField(max_length=200, verbose_name="Название подписки")
    price = models.DecimalField(max_digits=10, decimal_places=2, verbose_name="Цена")
    next_payment_date = models.DateField(verbose_name="Дата следующего платежа")
    category = models.ForeignKey(
        Category,
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        related_name="subscriptions",
        verbose_name="Категория"
    )
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name="subscriptions", verbose_name="Пользователь")
    notification_days_before = models.IntegerField(
        default=3,
        verbose_name="Дней для уведомления до платежа"
    )

    class Meta:
        verbose_name = "Подписка"
        verbose_name_plural = "Подписки"
        ordering = ['next_payment_date']  # Сортировка по дате платежа

    def __str__(self):
        return f"{self.name} - {self.price} руб. ({self.next_payment_date})"

    def is_upcoming(self):
        """Проверяет, является ли подписка предстоящей (в ближайшие notification_days_before дней)"""
        today = timezone.now().date()
        days_until_payment = (self.next_payment_date - today).days
        return 0 <= days_until_payment <= self.notification_days_before


class Notification(models.Model):
    STATUS_CHOICES = [
        ('pending', 'Ожидает показа'),
        ('shown', 'Показано пользователю'),
        ('dismissed', 'Проигнорировано'),
    ]

    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name="notifications", verbose_name="Пользователь")
    subscription = models.ForeignKey(Subscription, on_delete=models.CASCADE, related_name="notifications", verbose_name="Подписка")
    created_at = models.DateTimeField(auto_now_add=True, verbose_name="Дата создания")
    shown_at = models.DateTimeField(null=True, blank=True, verbose_name="Дата показа")
    status = models.CharField(max_length=20, choices=STATUS_CHOICES, default='pending', verbose_name="Статус")
    message = models.TextField(verbose_name="Текст уведомления")

    class Meta:
        verbose_name = "Уведомление"
        verbose_name_plural = "Уведомления"
        ordering = ['-created_at']
        unique_together = ['user', 'subscription', 'created_at']  # Избегаем дубликатов

    def __str__(self):
        return f"Уведомление для {self.user.username} о {self.subscription.name} ({self.status})"