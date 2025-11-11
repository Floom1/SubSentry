from django.db.models.signals import post_save
from django.dispatch import receiver
from .models import Subscription, Notification
from django.utils import timezone

@receiver(post_save, sender=Subscription)
def create_notification_on_subscription_save(sender, instance, created, **kwargs):
    """
    Создает уведомление при сохранении подписки, если дата платежа сегодня
    """
    today = timezone.now().date()
    days_until_payment = (instance.next_payment_date - today).days

    # Создаем уведомление, если дата платежа сегодня
    if days_until_payment == 0:
        # Проверяем, нет ли уже уведомления для этой подписки сегодня
        existing_notification = Notification.objects.filter(
            user=instance.user,
            subscription=instance,
            status='pending',
            created_at__date=today
        ).first()

        if not existing_notification:
            message = (
                f"Напоминание: ваша подписка '{instance.name}' "
                f"требует оплаты {instance.next_payment_date.strftime('%d.%m.%Y')}. "
                f"Сумма: {instance.price} руб."
            )
            Notification.objects.create(
                user=instance.user,
                subscription=instance,
                message=message,
                status='pending'
            )