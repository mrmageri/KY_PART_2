@echo off
REM Собираем проект, если папки out нет
if not exist out (
    echo Сборка проекта...
    mkdir out
    javac -d out src\*.java src\config\*.java tests\*.java
)

REM Запускаем Main, передавая все аргументы
java -cp out Main %*