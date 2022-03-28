from django.contrib import admin

from .models import Parking, Record


@admin.register(Parking)
class ParkingAdmin(admin.ModelAdmin):
    list_display = ('id', 'name', 'address', 'owner')
    search_fields = ('name', 'address', 'owner')
    list_filter = ('name', 'address', 'owner')
    empty_value_display = '---'


@admin.register(Record)
class RecordAdmin(admin.ModelAdmin):
    list_display = (
        'id', 'parking', 'state_number', 'time_in',
        'time_out', 'cost', 'status'
    )
    search_fields = ('parking', 'state_number', 'status')
    list_filter = ('status', 'parking')
    empty_value_display = '---'
