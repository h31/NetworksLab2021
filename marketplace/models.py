from django.contrib.auth.models import AbstractUser
from django.db import models


class User(AbstractUser):
    CUSTOMER = 'customer'
    FREELANCER = 'freelancer'
    USER_CHOICES = (
        (CUSTOMER, 'customer'),
        (FREELANCER, 'freelancer'),
    )

    bio = models.TextField('О себе', blank=True, null=True)
    role = models.CharField('Роль', max_length=16, choices=USER_CHOICES, blank=False)

    class Meta:
        ordering = ('id',)
        verbose_name = 'Пользователь'
        verbose_name_plural = 'Пользователи'

    def __str__(self):
        return self.username

    @property
    def is_customer(self):
        return self.role == self.CUSTOMER

    @property
    def is_freelancer(self):
        return self.role == self.FREELANCER


class Task(models.Model):
    OPEN = 'open'
    IN_PROGRESS = 'in progress'
    CLOSED = 'closed'
    STATUS_CHOICES = (
        (OPEN, 'open'),
        (IN_PROGRESS, 'in progress'),
        (CLOSED, 'closed')
    )

    name = models.CharField('Название предложения', max_length=100)
    description = models.TextField('Описание предложения')
    created = models.DateTimeField('Дата создания', auto_now_add=True)
    price = models.PositiveIntegerField('Цена', null=True, blank=True)
    status = models.CharField('Статус', max_length=16, choices=STATUS_CHOICES)
    author = models.ForeignKey(User, verbose_name='Заказчик',
                               on_delete=models.CASCADE,
                               related_name='offer_customer')
    freelancer = models.ForeignKey(User, verbose_name='Исполнитель',
                                   related_name='offer_freelancer',
                                   on_delete=models.SET_NULL,
                                   blank=True, null=True)

    class Meta:
        ordering = ['created']
        verbose_name = 'Задание'
        verbose_name_plural = 'Задания'

    def __str__(self):
        return self.name

    @property
    def is_open(self):
        return self.status == self.OPEN


class Offer(models.Model):
    task = models.ForeignKey(Task, on_delete=models.CASCADE,
                             related_name='offers',
                             verbose_name='Задание')
    author = models.ForeignKey(User, on_delete=models.CASCADE,
                               related_name='offers',
                               verbose_name='Исполнитель')
    description = models.TextField('Описание предложения')

    class Meta:
        verbose_name = 'Предложение'
        verbose_name_plural = 'Предложения'
        constraints = [
            models.UniqueConstraint(
                fields=['task', 'author'],
                name='unique_task_author',
            )
        ]

    def __str__(self):
        return str(self.author)
