from django.contrib.auth.hashers import make_password
from django.contrib.auth.models import update_last_login
from django.core.exceptions import ValidationError
from rest_framework import serializers
from rest_framework.exceptions import ValidationError
from rest_framework.relations import SlugRelatedField
from marketplace.models import User, Task, Offer


class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = [
            'id', 'email', 'username', 'bio', 'role', 'first_name', 'last_name', 'password',
        ]
        extra_kwargs = {
            'email': {'required': True},
            'username': {'required': True},
            'role': {'required': True},
        }

    def create(self, validated_data):
        user = User.objects.create_user(**validated_data)
        user.save()
        return user


class TaskSerializer(serializers.ModelSerializer):
    author = SlugRelatedField(slug_field='username', read_only=True)
    freelancer = SlugRelatedField(slug_field='username', read_only=True)

    class Meta:
        model = Task
        read_only_fields = ('status', 'author', 'freelancer')
        fields = ('id', 'name', 'description', 'created', 'price', 'status',
                  'author', 'freelancer')


class OfferSerializer(serializers.ModelSerializer):
    author = serializers.SlugRelatedField(
        read_only=True,
        slug_field='username',
        default=serializers.CurrentUserDefault()
    )
    task = serializers.SlugRelatedField(slug_field='name', read_only=True)

    class Meta:
        model = Offer
        read_only_fields = ('task', 'author')
        fields = ('id', 'task', 'author', 'description')

    def validate_task(self, value):
        if not value.is_open:
            raise ValidationError('Прием заявок уже закрыт')
        return value


class ChangeStatusSerializer(serializers.ModelSerializer):

    class Meta:
        model = Task
        read_only_fields = ('id', 'name', 'description', 'created', 'price',
                            'author', 'freelancer')
        fields = ('status', 'freelancer')