from rest_framework import serializers
from parking.models import Parking, Record, User
from django.db.models import Sum


class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = '__all__'
        extra_kwargs = {
            'username': {'required': True},
        }

    def create(self, validated_data):
        user = User.objects.create_user(**validated_data)
        user.save()
        return user


class RecordSerializer(serializers.ModelSerializer):
    parking = serializers.SlugRelatedField(read_only=True, slug_field='name')

    class Meta:
        model = Record
        read_only_fields = ('parking', 'time_in', 'time_out', 'status')
        fields = ('id', 'parking', 'state_number', 'time_in', 'time_out', 'cost', 'status')


class ParkingSerializer(serializers.ModelSerializer):
    records = RecordSerializer(many=True, read_only=True)
    income = serializers.SerializerMethodField()

    class Meta:
        fields = ('id', 'name', 'address', 'owner', 'income', 'records')
        read_only_fields = ('records', 'income', 'owner')
        model = Parking

    def get_income(self, obj: Parking):
        return obj.records.aggregate(Sum('cost'))["cost__sum"]


