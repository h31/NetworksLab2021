# Generated by Django 3.2.9 on 2021-12-08 00:08

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('marketplace', '0004_auto_20211207_1908'),
    ]

    operations = [
        migrations.AddConstraint(
            model_name='offer',
            constraint=models.UniqueConstraint(fields=('task', 'author'), name='unique_task_author'),
        ),
    ]
