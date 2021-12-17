from django.urls import include, path
from rest_framework.routers import SimpleRouter
from rest_framework import permissions
from drf_yasg.views import get_schema_view
from drf_yasg import openapi

from .views import UserViewSet, TaskViewSet, OfferViewSet


schema_view = get_schema_view(
   openapi.Info(
      title="Snippets API",
      default_version='v1',
      description="Test description",
      terms_of_service="https://www.google.com/policies/terms/",
      contact=openapi.Contact(email="contact@snippets.local"),
      license=openapi.License(name="BSD License"),
   ),
   public=True,
   permission_classes=(permissions.AllowAny,),
)

router_v1 = SimpleRouter()
router_v1.register('users', UserViewSet)
router_v1.register('tasks', TaskViewSet)
router_v1.register(r'tasks/(?P<task_id>\d+)/offers', OfferViewSet,
                   basename='offers')


urlpatterns = [
    #path('swagger(?P<format>\.json|\.yaml)', schema_view.without_ui(cache_timeout=0), name='schema-json'),
    path('swagger/', schema_view.with_ui('swagger', cache_timeout=0), name='schema-swagger-ui'),
    path('redoc/', schema_view.with_ui('redoc', cache_timeout=0), name='schema-redoc'),
    path('v1/', include('djoser.urls.jwt')),
    path('v1/', include(router_v1.urls))
]