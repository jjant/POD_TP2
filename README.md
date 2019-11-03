# POD TP2: Análisis de datos de vuelos

Sistema de votación distribuido implementado en Java 8 y Hazelcast.

## Instrucciones de ejecución

Para compilar el proyecto, correr desde la raíz del proyecto `./build.sh` para construirlo, correr tests, y dar permisos correspondientes a los scripts.

### Instrucciones de ejecución del server

Ejecutar `./scripts/server.sh`.

### Instrucciones de ejecución de queries

Ejecutar lo siguiente para realizar cada query.

```bash
cd ./scripts/client
./query{1,2,3,4}
```

## Ejemplos de ejecución

Para seguir estos ejemplos, posicionarse en la carpeta `scripts/client` utilizando:

```
cd ./scripts/client
```

Query 1:

```bash
./query1 -Daddresses=127.0.0.1:5701 -DinPath=../../csvs/ -DoutPath=../../results/
```

Query 2:

```bash
./query2 -Daddresses=127.0.0.1:5701 -DinPath=../../csvs/ -DoutPath=../../results/ -Dn=5
```

Query 3:

```bash
./query3 -Daddresses=127.0.0.1:5701 -DinPath=../../csvs/ -DoutPath=../../results/
```

Query 4:

```bash
./query4 -Daddresses=127.0.0.1:5701 -DinPath=../../csvs/ -DoutPath=../../results/ -Dn=5 -Doaci=SAEZ
```

## Documento

En este mismo repositorio se encuentra disponible el archivo `Reporte.pdf`, con una descripción general del sistema, junto con un conjunto de decisiones tomadas en el diseño.
