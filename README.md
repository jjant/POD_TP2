# POD TP2: Análisis de datos de vuelos
Sistema de votación distribuido implementado en Java 8 y Hazelcast.

## Instrucciones de compilación
Para compilar el proyecto, correr desde la raíz del proyecto `mvn clean install` para construirlo y correr tests.

## Instrucciones de ejecución
Para la correcta ejecución del sistema, deben descomprimirse los Jars y darse los permisos corresponidentes a los scripts. Para eso correr el siguiente script en la raíz del proyecto: `./build.sh`.

### Instrucciones de ejecución del server
1) Entrar al directorio `scripts/`.
2) Ejecutar el script `./server.sh`.

### Instrucciones de ejecución de queries
1) Entrar al directorio `scripts/client/`.
2) Ejecutar alguno de los scripts `./query{1,2,3,4}`.

### Pasaje de parámetros a clientes
Para cambiar los parámetros que se les pasa a los clientes, editar los archivos `run-*-client.sh` mencionados anteriormente. Los parámetros que puede recibir cada cliente son los definidos en el enunciado de este TPE (adjunto en este mismo proyecto como `Enunciado.pdf`).

### Documento
En este mismo repositorio se encuentra disponible el archivo `Reporte.pdf`, con una descripción general del sistema, junto con un conjunto de decisiones tomadas en el diseño.

## Ejemplos de ejecución

Query 1:
```
./query1 -Daddresses=127.0.0.1:5701 -DinPath=/Users/rama/Documents/POD_TP2/csvs/ -DoutPath=/Users/rama/Documents/POD_TP2/results/
```

Query 2:
```
./query2 -Daddresses=127.0.0.1:5701 -DinPath=/Users/rama/Documents/POD_TP2/csvs/ -DoutPath=/Users/rama/Documents/POD_TP2/results/ -Dn=5
```

Query 3:
```
./query3 -Daddresses=127.0.0.1:5701 -DinPath=/Users/rama/Documents/POD_TP2/csvs/ -DoutPath=/Users/rama/Documents/POD_TP2/results/
```

Query 4:
```
./query4 -Daddresses=127.0.0.1:5701 -DinPath=/Users/rama/Documents/POD_TP2/csvs/ -DoutPath=/Users/rama/Documents/POD_TP2/results/ -Dn=5 -Doaci=SAEZ
```