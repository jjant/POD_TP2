# POD TP2: Análisis de datos de vuelos
Sistema de votación distribuido implementado en Java 8 y Hazelcast.

## Instrucciones de compilación
Para compilar el proyecto, correr desde la raíz del proyecto `mvn clean install` para construirlo y correr tests.

## Instrucciones de ejecución
Para la correcta ejecución del sistema, deben descomprimirse los Jars y darse los permisos corresponidentes a los scripts. Para eso correr el siguiente script en la raíz del proyecto: `./build.sh`.

### Instrucciones de ejecución del server
2) Ejecutar el script `./server.sh`.

### Instrucciones de ejecución de queries
2) Ejecutar el script `./query1.sh`.

### Pasaje de parámetros a clientes
Para cambiar los parámetros que se les pasa a los clientes, editar los archivos `run-*-client.sh` mencionados anteriormente. Los parámetros que puede recibir cada cliente son los definidos en el enunciado de este TPE (adjunto en este mismo proyecto como `Enunciado.pdf`).

### Documento
En este mismo repositorio se encuentra disponible el archivo `Reporte.pdf`, con una descripción general del sistema, junto con un conjunto de decisiones tomadas en el diseño.