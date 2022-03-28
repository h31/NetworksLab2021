# Generated by Django 4.0.3 on 2022-03-21 23:09

from django.conf import settings
from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    initial = True

    dependencies = [
        migrations.swappable_dependency(settings.AUTH_USER_MODEL),
    ]

    operations = [
        migrations.CreateModel(
            name='Parking',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('name', models.CharField(max_length=100, verbose_name='Название паркинга')),
                ('address', models.CharField(max_length=200, verbose_name='Адрес паркинга')),
                ('owner', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, related_name='parkings', to=settings.AUTH_USER_MODEL, verbose_name='Владелец')),
            ],
            options={
                'verbose_name': 'Паркинг',
                'verbose_name_plural': 'Паркинги',
                'ordering': ['name'],
            },
        ),
        migrations.CreateModel(
            name='Record',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('state_number', models.CharField(max_length=8, verbose_name='Гос. номер')),
                ('time_in', models.DateTimeField(auto_now_add=True, verbose_name='Время въезда')),
                ('time_out', models.DateTimeField(blank=True, editable=False, null=True, verbose_name='Время выезда')),
                ('cost', models.PositiveIntegerField(blank=True, editable=False, null=True, verbose_name='Цена')),
                ('status', models.CharField(choices=[('open', 'open'), ('close', 'close')], max_length=8, verbose_name='Статус')),
                ('parking', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, related_name='records', to='parking.parking')),
            ],
            options={
                'verbose_name': 'Запись',
                'verbose_name_plural': 'Записи',
                'ordering': ['-time_in'],
            },
        ),
    ]
