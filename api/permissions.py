from rest_framework import permissions
from django.shortcuts import get_object_or_404

from marketplace.models import Task


class IsCustomerOrReadOnly(permissions.BasePermission):
    def has_permission(self, request, view):
        return (request.method in permissions.SAFE_METHODS
                or (request.user.is_authenticated and request.user.is_customer))


class IsFreelancerOrReadOnly(permissions.BasePermission):
    def has_permission(self, request, view):
        return (request.method in permissions.SAFE_METHODS
                or (request.user.is_authenticated and request.user.is_freelancer))


class IsOwnerOrReadOnly(permissions.BasePermission):
    def has_object_permission(self, request, view, obj):
        return (request.method in permissions.SAFE_METHODS
                or obj.author == request.user)


class IsOpenOrReadOnly(permissions.BasePermission):
    def has_permission(self, request, view):
        task_status = get_object_or_404(
            Task, id=int(request.parser_context.get('kwargs').get('task_id'))
        ).status
        return (request.method in permissions.SAFE_METHODS
                or task_status == Task.OPEN)