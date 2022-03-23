from django.contrib.auth.models import AbstractUser
from django.db import models


class User(AbstractUser):
    COMPANY = 'company'
    SUBSCRIBER = 'subscriber'
    USER_CHOICES = (
        (COMPANY, 'company'),
        (SUBSCRIBER, 'subscriber'),
    )

    role = models.CharField(
        'Роль', max_length=16, choices=USER_CHOICES, blank=False
    )

    class Meta:
        ordering = ('id',)
        verbose_name = 'Пользователь'
        verbose_name_plural = 'Пользователи'

    def __str__(self):
        return self.username

    @property
    def is_company(self):
        return self.role == self.COMPANY

    @property
    def is_sub(self):
        return self.role == self.SUBSCRIBER


class Meter(models.Model):
    WATER = 'water'
    WARM = 'warm'
    ELECTRICITY = 'electricity'
    METER_TYPE = (
        (WATER, 'water'),
        (WARM, 'warm'),
        (ELECTRICITY, 'electricity')
    )

    meter_type = models.CharField('Тип', max_length=16, choices=METER_TYPE)
    user = models.ForeignKey(User, verbose_name='Абонент',
                             on_delete=models.CASCADE,
                             related_name='meter_user')
    owner = models.ForeignKey(User, verbose_name='Управляющая компания',
                              on_delete=models.CASCADE,
                              related_name='meter_owner')

    class Meta:
        ordering = ['owner']
        verbose_name = 'Счетчик'
        verbose_name_plural = 'Счетчики'

    def __str__(self):
        return f'{self.user}-{self.meter_type}'


class Reading(models.Model):
    DONE = 'done'
    UNDONE = 'undone'
    STATUS = (
        (DONE, 'done'),
        (UNDONE, 'undone')
    )
    meter = models.ForeignKey(Meter, on_delete=models.CASCADE,
                              related_name='reading',
                              verbose_name='Счетчик')
    status = models.CharField('Статус', max_length=8, choices=STATUS)
    date = models.DateField('Дата сдачи')
    value = models.PositiveIntegerField('Показание', blank=True, null=True)

    class Meta:
        ordering = ['-date']
        verbose_name = 'Показание'
        verbose_name_plural = 'Показания'

    def __str__(self):
        return str(self.value)

    @property
    def is_undone(self):
        return self.status == self.UNDONE
