from django.shortcuts import get_object_or_404
from django_filters.rest_framework import DjangoFilterBackend
from rest_framework import mixins, permissions, status
from rest_framework.decorators import action, permission_classes
from rest_framework.filters import SearchFilter
from rest_framework.pagination import LimitOffsetPagination
from rest_framework.viewsets import GenericViewSet, ModelViewSet
from rest_framework.response import Response

from marketplace.models import User, Task, Offer

from .serializers import (UserSerializer, TaskSerializer, OfferSerializer,
                          ChangeStatusSerializer)
from .permissions import (IsCustomerOrReadOnly, IsFreelancerOrReadOnly,
                          IsOwnerOrReadOnly, IsOpenOrReadOnly)


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


class TaskViewSet(ModelViewSet):
    """
    Группа эндпоинтов для операций с заданиями.
    Также поддерживается редактирование информации
    (Для всех запросов изменяющих данные проходит проверка на права доступа)
    """
    queryset = Task.objects.all()
    serializer_class = TaskSerializer
    permission_classes = [IsOwnerOrReadOnly, IsCustomerOrReadOnly]
    pagination_class = LimitOffsetPagination

    def perform_create(self, serializer):
        serializer.save(author=self.request.user, status=Task.OPEN)

    @action(
        detail=True, methods=['post'],
        permission_classes=[IsCustomerOrReadOnly, IsOwnerOrReadOnly],
        serializer_class=ChangeStatusSerializer
    )
    def status(self, request, pk):
        """
        Эндпоинт для изменения статуса задания.
        Поддерживается только изменение статуса из состояния "in_progress",
        для переключения статуса в "in_progress" смотрите ниже.
        (Для запроса необходимо быть заказчиком)
        """
        task = get_object_or_404(Task, id=pk)
        if task.status == Task.IN_PROGRESS or task.freelancer:
            if request.data.get('status') not in (Task.OPEN, Task.CLOSED):
                return Response(
                    {"detail": 'Invalid status'},
                    status.HTTP_400_BAD_REQUEST
                )
            if request.data.get('status') == Task.OPEN:
                task.status = Task.OPEN
                task.freelancer = None
                task.save()
            if request.data.get('status') == Task.CLOSED:
                task.status = Task.CLOSED
                task.save()
            return Response(
                {'status': task.status,
                 'freelancer': str(task.freelancer)},
            )
        return Response(
            {"detail": "You do not have permission to perform this action."},
            status=status.HTTP_403_FORBIDDEN
        )


class OfferViewSet(ModelViewSet):
    """
    Группа эндпоинтов для операций с откликами.
    Также поддерживается редактирование информации
    (Для всех запросов изменяющих данные проходит проверка на права доступа)
    """
    serializer_class = OfferSerializer
    permission_classes = [
        IsOpenOrReadOnly, IsOwnerOrReadOnly, IsFreelancerOrReadOnly
    ]
    pagination_class = LimitOffsetPagination

    def get_queryset(self):
        return get_object_or_404(
            Task,
            id=self.kwargs.get('task_id')
        ).offers.all()

    def perform_create(self, serializer):
        serializer.save(
            author=self.request.user,
            task=get_object_or_404(Task, id=self.kwargs.get('task_id'))
        )

    def create(self, request, *args, **kwargs):
        if Offer.objects.filter(
                task=get_object_or_404(Task, id=self.kwargs.get('task_id')),
                author=self.request.user
        ).exists():
            return Response(
                {"detail": "You can send only one offer."},
                status=status.HTTP_403_FORBIDDEN
            )
        serializer = self.get_serializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        self.perform_create(serializer)
        headers = self.get_success_headers(serializer.data)
        return Response(
            serializer.data, status=status.HTTP_201_CREATED, headers=headers
        )

    @action(
        detail=True, methods=['post'],
        permission_classes=[IsCustomerOrReadOnly]
    )
    def pick(self, request, task_id, pk):
        """
        Эндпоинт для изменения статуса задания на "in_progress".
        Поддерживается только изменение статуса из состояния "open".
        (Для запроса необходимо быть заказчиком)
        """
        task = get_object_or_404(Task, id=task_id)
        if request.user == task.author and task.status == Task.OPEN:
            task.status = Task.IN_PROGRESS
            task.freelancer = get_object_or_404(Offer, id=pk).author
            task.save()
            return Response(
                {'status': task.status, 'freelancer': str(task.freelancer)},
            )
        return Response(
            {"detail": "You do not have permission to perform this action."},
            status=status.HTTP_403_FORBIDDEN
        )

