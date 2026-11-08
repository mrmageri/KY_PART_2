@echo off
chcp 65001 > nul
if not exist out mkdir out
javac -d out src\*.java src\config\*.java tests\*.java
java -cp out Main %*