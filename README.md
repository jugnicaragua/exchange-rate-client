# ncb-exchange-rate-client

Es una librería que consulta el servicio web SOAP o el sitio web del Banco Central de Nicaragua (BCN) para obtener la tasa de cambio para la fecha o mes-año consultado. Si por alguna razón el servicio web del BCN no estuviera disponible, la librería extraería los datos requeridos del sitio web del BCN.

Este proyecto se puede usar como una librería de terceros o una aplicación de consola. La librería consta de tres métodos para obtener la tasa de cambio: consultar el servicio web del BCN, consultar el sitio web del BCN y extraer los datos del HTML y un tercer método que consiste en una combinación de los dos primeros (si un método falla se utiliza el siguiente como respaldo). El `scraper` se introdujo como una necesidad por las caídas momentaneas, observadas durantes nuestras pruebas, del servicio web del BCN.

Se creo la interfaz `ExchangeRateClient.java` como el contrato a ser implementado por los 3 métodos para obtener las tasas de cambio. Estas son las implementaciones de la inferfaz: `ExchangeRateWSClient.java`, `ExchangeRateScraper.java`, `ExchangeRateFailsafeClient.java`.

#### Observacion

El servicio web del BCN tiene una restricción con el año que se puede consultar: sólo se pueden obtener las tasas de cambio a partir del año `2012`. Esta validación está contemplada dentro del proyecto, pero este valor puede ser cambiado arbitrariamente y sin previo aviso por el BCN.

## Stack

- Java 8+.
- Maven 3+.
- JUnit 5+.
- El IDE de tu preferencia: el proyecto no incluye ningún archivo específico de un IDE, pero requiere el uso de un IDE con soporte `maven`.

Los pasos enumerados en los siguientes apartados donde se describe el uso de la librería requieren tener `maven` instalado.

## Usar el proyecto como una librería

Esta es la razón de ser del proyecto. Si el proyecto en el que estás trabajando es un proyecto basado en `maven`, se deben seguir los siguientes pasos:

1. Clonar el proyecto o descargarlo como zip. Si se descarga el zip, descomprimirlo en una ruta específica.

        git clone https://github.com/jug-ni/ncb-exchange-rate-client.git
        cd ncb-exchange-rate-client
        # Instalar el artefacto en el repositorio local
        mvn install
        # Si no se desean ejecutar los test unitarios durante la instalación del jar en el repositorio local
        mvn install -DskipTests

2. Incluir la librería como dependencia en el archivo pom.xml de tu proyecto:

        <dependency>
            <groupId>ni.jug</groupId>
            <artifactId>ncb-exchange-rate-client</artifactId>
            <version>${version.descargada}</version>
        </dependency>

Si no estás seguro sobre el número de versión una vez clonado el proyecto (el número de versión está en el archivo pom.xml), dirigirse a su repositorio local de `maven` y explorar la ruta `.m2/repository/ni/jug` en tu cuenta de usuario del SO y tomar nota de la versión del jar.

Si tu proyecto no usa `maven`, ejecutar solamente el paso 1 e importar el jar desde tu repositorio local de `maven` a tu proyecto.

Código de ejemplo para usar la librería:

        ExchangeRateClient client = new ExchangeRateFailsafeClient();
        Assertions.assertEquals(new BigDecimal("31.9396"), client.getExchangeRate(LocalDate.of(2018, 10, 1)));

        MonthlyExchangeRate monthlyExchangeRate = client.getMonthlyExchangeRate(2018, 10);
        Assertions.assertEquals(31, monthlyExchangeRate.size());
        Assertions.assertEquals(new BigDecimal("31.9396"), monthlyExchangeRate.getFirstExchangeRate());
        Assertions.assertEquals(new BigDecimal("32.0679"), monthlyExchangeRate.getLastExchangeRate());
        Assertions.assertEquals(new BigDecimal("31.9994"), monthlyExchangeRate.getExchangeRate(LocalDate.of(2018, 10, 15)));
        Assertions.assertEquals(BigDecimal.ZERO, monthlyExchangeRate.getExchangeRate(LocalDate.of(2018, 9, 30)));
        Assertions.assertFalse(monthlyExchangeRate.getThereIsAGap());

Referirse a los [test unitarios][test unitario] para más ejemplos.

## Uso del CLI (Línea de comandos o Terminal)

Si se prefiere usar el proyecto como una aplicación cli, se tiene la opción de solicitar la tasa de cambio para: una fecha, rango de fechas, lista de fechas; para un mes-año, rango de mes-año, lista de mes-año. El cli internamente utiliza la implementacion `ExchangeRateFailsafeClient.java` (3er método para obtener los datos). Para ejecutar el cli se requiere tener instalado maven y java con las versiones indicadas en el apartado `Stack`.

Opciones disponibles:

- date: una fecha, un rango o una lista. La fecha debe ser ingresada en formato ISO: yyyy-MM-dd.
- ym: un año-mes, un rango o una lista. La fecha debe ser ingresada en formato ISO: yyyy-MM.
- help: Muestra las opciones disponibles y ejemplos de uso.

Pasos para instalar la librería:

        git clone https://github.com/jug-ni/ncb-exchange-rate-client.git
        cd ncb-exchange-rate-client
        mvn package
        # Si no se desean ejecutar los test unitarios durante el empaquetamiento del jar
        mvn package -DskipTests
        # Ejecutar la aplicación. Referirse a los ejemplos anteriores para mayor información
        cd target/
        java -jar ncb-exchange-rate-client-1.0-SNAPSHOT.jar -date=2018-10-23

Ejemplos de ejecución del cli:

        java -jar ncb-exchange-rate-client-<version>.jar -date=2018-10-14
        java -jar ncb-exchange-rate-client-<version>.jar -date=2018-10-14:
        java -jar ncb-exchange-rate-client-<version>.jar -date=2018-10-14:2018-10-16
        java -jar ncb-exchange-rate-client-<version>.jar -date=2018-10-14:2018-10-16,2018-10-31

        java -jar ncb-exchange-rate-client-<version>.jar -ym=2018-10
        java -jar ncb-exchange-rate-client-<version>.jar -ym=2018-09:2018-10
        java -jar ncb-exchange-rate-client-<version>.jar -ym=2018-01,2018-09:2018-10

        java -jar ncb-exchange-rate-client-<version>.jar -date=2018-10-14: -ym=2018-10

        java -jar ncb-exchange-rate-client-<version>.jar --help

## Licencia

This software is covered under the MIT Licence (http://opensource.org/licenses/MIT).

Puedes leer el archivo de la licencia en [LICENSE][license]

Copyright (c) 2018-present, JUG Nicaragua Armando Alaniz

**Free Software, Hell Yeah!**

[license]: LICENSE.txt
[test unitario]: src/test/java/ni/jug/ncb/exchangerate/ExchangeRateWSClientTest.java
