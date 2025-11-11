from django.contrib import admin
from .models import Category, Subscription, Notification

@admin.register(Category)
class CategoryAdmin(admin.ModelAdmin):
    list_display = ('name', 'user')
    search_fields = ('name', 'user__username')
    list_filter = ('user',)

@admin.register(Subscription)
class SubscriptionAdmin(admin.ModelAdmin):
    list_display = ('name', 'price', 'next_payment_date', 'category', 'user', 'notification_days_before')
    list_filter = ('user', 'category', 'next_payment_date')
    search_fields = ('name', 'category__name', 'user__username')
    date_hierarchy = 'next_payment_date'

    # Группировка полей в форме
    fieldsets = (
        ('Основная информация', {
            'fields': ('name', 'price', 'user')
        }),
        ('Категория и уведомления', {
            'fields': ('category', 'notification_days_before')
        }),
        ('Даты', {
            'fields': ('next_payment_date',)
        }),
    )

@admin.register(Notification)
class NotificationAdmin(admin.ModelAdmin):
    list_display = ('user', 'subscription', 'status', 'created_at', 'shown_at')
    list_filter = ('status', 'created_at', 'user')
    search_fields = ('user__username', 'subscription__name', 'message')
    date_hierarchy = 'created_at'

    def get_queryset(self, request):
        """Показываем только уведомления текущего пользователя (если не админ)"""
        qs = super().get_queryset(request)
        if request.user.is_superuser:
            return qs
        return qs.filter(user=request.user)