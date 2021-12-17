from django.contrib import admin
from django.contrib.auth.admin import UserAdmin as CustomUserAdmin

from .models import User, Task, Offer


@admin.register(User)
class UserAdmin(CustomUserAdmin):
    fieldsets = tuple(
        (fieldset[0], {
            **{key: value for (key, value) in fieldset[1].items()
                if key != 'fields'},
            'fields': fieldset[1]['fields'] + ('bio', 'role')
        })
        if fieldset[0] == 'Personal info'
        else fieldset
        for fieldset in CustomUserAdmin.fieldsets
    )
    list_display = ['email', 'username', 'role', 'is_active']
    empty_value_display = '---'


@admin.register(Task)
class TaskAdmin(admin.ModelAdmin):
    list_display = ('id', 'name', 'description', 'created', 'price',
                    'author', 'freelancer')
    search_fields = ('name', 'description', 'author', 'freelancer', 'price')
    list_filter = ('author', 'freelancer', 'created')
    empty_value_display = '---'


@admin.register(Offer)
class OfferAdmin(admin.ModelAdmin):
    list_display = ('id', 'task', 'author', 'description')
    search_fields = ('task', 'author', 'description')
    list_filter = ('task', 'author')
    empty_value_display = '---'

