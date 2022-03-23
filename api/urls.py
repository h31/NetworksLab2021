from django.urls import include, path
from rest_framework.routers import SimpleRouter
from rest_framework import permissions
from drf_yasg.views import get_schema_view
from drf_yasg import openapi

from .views import UserViewSet, MeterViewSet, ReadingViewSet

schema_view = get_schema_view(
    openapi.Info(
        title="Utility Payment",
        default_version='v1',
        description="Сервис для коммунальных служб и их клиентов",
    ),
    public=True,
    permission_classes=(permissions.AllowAny,),
)

router_v1 = SimpleRouter()
router_v1.register('users', UserViewSet)
router_v1.register('meters', MeterViewSet, basename='meters')
router_v1.register(r'meters/(?P<meter_id>\d+)/readings', ReadingViewSet,
                   basename='readings')

urlpatterns = [
    path('swagger/', schema_view.with_ui('swagger', cache_timeout=0),
         name='schema-swagger-ui'),
    path('v1/', include('djoser.urls.jwt')),
    path('v1/', include(router_v1.urls))
]