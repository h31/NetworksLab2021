from django.shortcuts import get_object_or_404
from django_filters.rest_framework import DjangoFilterBackend
from rest_framework.filters import SearchFilter
from rest_framework.pagination import LimitOffsetPagination
from rest_framework.viewsets import ModelViewSet

from payment.models import User, Meter

from .serializers import UserSerializer, MeterSerializer, ReadingSerializer
from .permissions import (IsCompanyOrReadOnly, IsSubscriberOrReadOnly,
                          IsOwnerOrReadOnly, IsUndoneOrReadOnly)


class UserViewSet(ModelViewSet):
    """
    Группа эндпоинтов для операций с юзерами.
    Краткое описание процесса регистрации и авторизации:
        1. Создать пользователя post-запросом на users/
        2. Получить токен post-запросом на jwt/create/
        3. Добавить токен в headers
    """
    queryset = User.objects.all()
    serializer_class = UserSerializer
    http_method_names = ['get', 'post', 'put', 'patch', 'delete']
    permission_classes = [IsOwnerOrReadOnly]
    search_fields = ['username']
    pagination_class = LimitOffsetPagination


class MeterViewSet(ModelViewSet):
    """
    Группа эндпоинтов для взаимодействия с счетчиками.

    Счетчики могут создавать и назначать лишь комунальные компании. При создании счетчика
    автоматически создается первая "запись" от нынешней даты.
    """
    serializer_class = MeterSerializer
    http_method_names = ['get', 'post', 'delete']
    permission_classes = [IsCompanyOrReadOnly, IsOwnerOrReadOnly]
    pagination_class = LimitOffsetPagination
    filter_backends = (DjangoFilterBackend, SearchFilter,)
    filterset_fields = ('meter_type', 'user')
    search_fields = ('meter_type',)

    def get_queryset(self):
        if self.request.user.is_sub:
            return Meter.objects.filter(user=self.request.user)
        if self.request.user.is_company:
            return Meter.objects.filter(owner=self.request.user)
        return Meter.objects.all()

    def perform_create(self, serializer):
        serializer.save(owner=self.request.user)


class ReadingViewSet(ModelViewSet):
    """
    Группа эндпоинтов для взаимодействия с записями счетчиков.

    Только пользователи могут изменять показания счетчиков, и лишь пока показания
    находятся в открытом состоянии. Подача показаний осуществляется отправкой
    value методом PATCH. При этом автоматически создается новая запись со
    смещением даты на месяц.
    """
    serializer_class = ReadingSerializer
    http_method_names = ['get', 'patch', 'delete']
    permission_classes = [
        IsUndoneOrReadOnly, IsSubscriberOrReadOnly
    ]
    pagination_class = LimitOffsetPagination

    def get_queryset(self):
        return get_object_or_404(
            Meter,
            id=self.kwargs.get('meter_id'),
        ).reading.all()

    def perform_create(self, serializer):
        serializer.save(
            meter=get_object_or_404(Meter, id=self.kwargs.get('meter_id'))
        )


