# Graph Tool

CLI-инструмент визуализации графа зависимостей для менеджера пакетов.

**Этап 1** — минимальный прототип с конфигурацией через опции командной строки.

## Требования

- JDK 17 или новее

## Сборка и запуск

```bash
make build                          # компиляция
make run ARGS="--help"              # справка
make run ARGS="--package com.example --repo /tmp/pkg.json --mode local"
make test                           # тесты
make clean                          # очистка сборки