from django.contrib.auth.models import User
from rest_framework import serializers

from .models import Category, Subscription, Notification


class UserSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True)

    class Meta:
        model = User
        fields = ('id', 'username', 'email', 'password')
        extra_kwargs = {
            'username': {'help_text': 'Ваш никнейм'},
            'email': {'help_text': 'Ваш email адрес'}
        }

    def create(self, validated_data):
        user = User.objects.create_user(
            username=validated_data['username'],
            email=validated_data['email'],
            password=validated_data['password']
        )
        return user

class CategorySerializer(serializers.ModelSerializer):
    class Meta:
        model = Category
        fields = ('id', 'name', 'user')
        read_only_fields = ('user',)

class SubscriptionSerializer(serializers.ModelSerializer):
    category_name = serializers.CharField(source='category.name', read_only=True)  # Для отображения названия категории

    class Meta:
        model = Subscription
        fields = (
            'id', 'name', 'price', 'next_payment_date',
            'category', 'category_name', 'user',
            'notification_days_before'
        )
        read_only_fields = ('user',)

class SubscriptionSummarySerializer(serializers.Serializer):
    subscriptions = SubscriptionSerializer(many=True)
    total_amount = serializers.DecimalField(max_digits=10, decimal_places=2)


class NotificationSerializer(serializers.ModelSerializer):
    subscription_name = serializers.CharField(source='subscription.name', read_only=True)

    class Meta:
        model = Notification
        fields = ('id', 'subscription', 'subscription_name', 'message', 'status', 'created_at', 'shown_at')
        read_only_fields = ('created_at', 'shown_at')