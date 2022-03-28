from django.urls import include, path
from rest_framework.routers import SimpleRouter
from rest_framework import permissions
from drf_yasg.views import get_schema_view
from drf_yasg import openapi

from .views import UserViewSet, ParkingViewSet, RecordsViewSet


schema_view = get_schema_view(
   openapi.Info(
      title="Parking Service",
      default_version='v1',
      description="Vasiliy Maksem"
   ),
   public=True,
   permission_classes=(permissions.AllowAny,),
)

router_v1 = SimpleRouter()
router_v1.register('users', UserViewSet)
router_v1.register('parking', ParkingViewSet)
router_v1.register(r'parking/(?P<parking_id>\d+)/record', RecordsViewSet,
                   basename='records')


urlpatterns = [
    path('swagger/', schema_view.with_ui('swagger', cache_timeout=0), name='schema-swagger-ui'),
    path('redoc/', schema_view.with_ui('redoc', cache_timeout=0), name='schema-redoc'),
    path('v1/', include('djoser.urls.jwt')),
    path('v1/', include(router_v1.urls))
]