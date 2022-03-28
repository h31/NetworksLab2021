from django.shortcuts import get_object_or_404
from django.utils import timezone
from rest_framework.permissions import AllowAny
from rest_framework.pagination import LimitOffsetPagination
from rest_framework.viewsets import ModelViewSet

from parking.models import Parking, Record, User

from .serializers import ParkingSerializer, RecordSerializer, UserSerializer
from .permissions import IsOwnerOrReadOnly


class UserViewSet(ModelViewSet):
    """
    Группа эндпоинтов для операций с юзерами.
    Краткое описание процесса регистрации и авторизации:
        1. Создать пользователя post-запросом на users/
        2. Получить токен post-запросом на jwt/create/
        3. Добавить токен в headers
        4. Done
    Также поддерживается редактирование пользовательской информации
    (Для всех запросов изменяющих данные проходит проверка на права доступа)
    """
    queryset = User.objects.all()
    serializer_class = UserSerializer
    permission_classes = [IsOwnerOrReadOnly]
    search_fields = ['username']
    pagination_class = LimitOffsetPagination


class ParkingViewSet(ModelViewSet):
    """
    Группа эндпоинтов для взаимодействия с паркингом.

    Создавать запись о паркинге могут только зарегистрированные пользователи
    """
    queryset = Parking.objects.all()
    permission_classes = [IsOwnerOrReadOnly]
    serializer_class = ParkingSerializer
    pagination_class = LimitOffsetPagination

    def perform_create(self, serializer):
        serializer.save(owner=self.request.user)


class RecordsViewSet(ModelViewSet):
    """
    Группа эндпоинтов для взаимодействия с записями в паркингах

    Въезд машины - создается отправлением POST-запроса с государственным номером.
    PATCH-запрос отвечает за выезд машины. Запрос пуст (как и я внутри)
    """

    serializer_class = RecordSerializer
    permission_classes = [AllowAny]
    pagination_class = LimitOffsetPagination

    def get_queryset(self):
        return get_object_or_404(
            Parking, id=self.kwargs.get('parking_id')
        ).records.all()

    def perform_create(self, serializer):
        serializer.save(
            parking=get_object_or_404(Parking, id=self.kwargs.get('parking_id')),
            status=Record.OPEN
        )

    def update(self, request, *args, **kwargs):
        record = get_object_or_404(Record, id=kwargs['pk'])
        record.time_out = timezone.now()
        record.cost = (record.time_out - record.time_in).seconds / 12
        record.status = Record.CLOSE
        record.save()
        kwargs['partial'] = True
        return super().update(request, *args, **kwargs)