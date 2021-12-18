from enum import Enum


class WorkMode(Enum):
    DOWNLOAD = 1  # скачать файл с сервера
    UPLOAD = 2  # загрузить файл на сервер
