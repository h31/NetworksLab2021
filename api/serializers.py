from django.core.exceptions import ValidationError
from rest_framework import serializers
from rest_framework.exceptions import ValidationError
from rest_framework.relations import SlugRelatedField
import calendar
import datetime as dt
from payment.models import User, Meter, Reading


class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = [
            'id', 'email', 'username', 'role', 'first_name', 'last_name',
            'password'
        ]
        extra_kwargs = {
            'email': {'required': True},
            'username': {'required': True},
            'role': {'required': True},
        }

    def create(self, validated_data):
        user = User.objects.create_user(**validated_data)
        user.save()
        return user


class MeterSerializer(serializers.ModelSerializer):
    owner = SlugRelatedField(slug_field='username', read_only=True)
    user = SlugRelatedField(slug_field='username', required=True,
                            queryset=User.objects.filter(role=User.SUBSCRIBER))
    increment = serializers.SerializerMethodField()

    class Meta:
        model = Meter
        read_only_fields = ('owner', 'user', 'increment')
        fields = '__all__'

    def get_increment(self, meter):
        readings = meter.reading.all()
        if readings.count() >= 3:
            readings = readings[:3]
            return readings[1].value - readings[2].value
        return None

    def create(self, validated_data):
        meter = Meter.objects.create(**validated_data)
        Reading.objects.create(
            meter=meter,
            status=Reading.UNDONE,
            date=dt.date.today(),
            value=None
        ).save()
        return meter


class ReadingSerializer(serializers.ModelSerializer):

    class Meta:
        model = Reading
        read_only_fields = ('meter', 'date', 'status')
        fields = '__all__'

    def update(self, instance, validated_data):
        submission_date = instance.date
        if dt.date.today() <= submission_date:
            date = submission_date + dt.timedelta(
                days=calendar.monthrange(
                    submission_date.year, submission_date.month
                )[1]
            )
            Reading.objects.create(
                meter=instance.meter,
                status=Reading.UNDONE,
                date=date,
            ).save()
            instance.value = validated_data.get('value', instance.value)
            instance.status = Reading.DONE
            instance.save()
            return instance
        raise ValidationError(
            f'Вы должны сдать показания после {submission_date}'
        )
