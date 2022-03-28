from django.contrib.auth import get_user_model
from django.db import models

User = get_user_model()


class Parking(models.Model):
    name = models.CharField('Название паркинга', max_length=100)
    address = models.CharField('Адрес паркинга', max_length=200)
    owner = models.ForeignKey(User, verbose_name='Владелец',
                              on_delete=models.CASCADE, related_name='parkings')

    class Meta:
        ordering = ['name']
        verbose_name = 'Паркинг'
        verbose_name_plural = 'Паркинги'

    def __str__(self):
        return self.name


class Record(models.Model):
    OPEN = 'open'
    CLOSE = 'close'
    STATUS_CHOICES = (
        (OPEN, 'open'),
        (CLOSE, 'close')
    )

    parking = models.ForeignKey(Parking, on_delete=models.CASCADE, related_name='records')
    state_number = models.CharField('Гос. номер', max_length=8)
    time_in = models.DateTimeField('Время въезда', auto_now_add=True)
    time_out = models.DateTimeField('Время выезда', editable=False, blank=True, null=True)
    cost = models.PositiveIntegerField('Цена', editable=False, blank=True, null=True)
    status = models.CharField('Статус', max_length=8, choices=STATUS_CHOICES)

    class Meta:
        ordering = ['-time_in']
        verbose_name = 'Запись'
        verbose_name_plural = 'Записи'

    def __str__(self):
        return f'{self.state_number} in {self.time_in.time()}'

