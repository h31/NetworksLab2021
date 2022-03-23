from django.contrib import admin
from django.contrib.auth.admin import UserAdmin as CustomUserAdmin

from .models import User, Meter, Reading


@admin.register(User)
class UserAdmin(CustomUserAdmin):
    fieldsets = tuple(
        (fieldset[0], {
            **{key: value for (key, value) in fieldset[1].items()
                if key != 'fields'},
            'fields': fieldset[1]['fields'] + ('role',)
        })
        if fieldset[0] == 'Personal info'
        else fieldset
        for fieldset in CustomUserAdmin.fieldsets
    )
    list_display = ['id', 'username', 'role', 'is_active']
    empty_value_display = '---'


@admin.register(Meter)
class MeterAdmin(admin.ModelAdmin):
    list_display = ('id', 'meter_type', 'user', 'owner')
    search_fields = ('user', 'owner')
    list_filter = ('meter_type',)
    empty_value_display = '***'


@admin.register(Reading)
class ReadingAdmin(admin.ModelAdmin):
    list_display = ('id', 'meter', 'status', 'date', 'value')
    search_fields = ('date', 'value')
    list_filter = ('meter', 'date', 'status')
    empty_value_display = '***'
