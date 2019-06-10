# exchange-rate-client

La librería es un cliente http que obtiene el tipo de cambio oficial del Banco Central de Nicaragua (BCN) y la compra/venta de divisas de los bancos comerciales que operan en Nicaragua. Para el caso del BCN, la libreria expone las mismas operaciones disponibles en el servicio web del BCN: una vez obtenidos los datos del tipo de cambio, se pueden consultar por fecha y mes-año. En el caso de los bancos comerciales, se realiza `scraping` de los siguientes bancos: BANPRO, FICOHSA, AVANZ, BAC, BDF, LAFISE. Si el `scraping` de alguno de los bancos falla, la libreria realiza 3 intentos (cada intento consiste en extraer los datos de todos los bancos) y retorna los resultados del mejor intento (aquel intento con mayor cantidad de bancos disponibles).

La libreria puede ser empaquetada a traves de `maven` para crear un `cli` (aplicacion de consola). Para construir el proyecto como `cli` se debe activar un perfil declarado en el pom.xml del proyecto (mas detalles en los siguientes apartados).

#### Observacion

El servicio web del BCN tiene una restricción con el año que se puede consultar: sólo se pueden obtener las tasas de cambio a partir del año `2012`. Esta validación está contemplada dentro del proyecto, pero este valor puede ser cambiado arbitrariamente y sin previo aviso por el BCN.

Los pasos enumerados en los siguientes apartados donde se describe el uso de la librería requieren tener `maven` instalado.

## Stack

- Java 8+.
- Maven 3+.
- JUnit 5+.
- El IDE de tu preferencia: el proyecto no incluye ningún archivo específico de un IDE, pero requiere el uso de un IDE con soporte `maven`.

## Agregar el proyecto como una librería

Esta es la razón de ser del proyecto.

1. Clonar el proyecto.

        git clone https://github.com/jugnicaragua/exchange-rate-client.git
        cd exchange-rate-client
        # Instalar el artefacto en el repositorio local
        mvn install
        # Si no se desean ejecutar los test unitarios durante la instalación del jar en el repositorio local
        mvn install -DskipTests

2. Incluir la librería como dependencia en el archivo pom.xml de tu proyecto:

        <dependency>
            <groupId>ni.jug</groupId>
            <artifactId>exchange-rate-client</artifactId>
            <version>${version.descargada}</version>
        </dependency>

Si no estás seguro sobre el número de versión una vez clonado el proyecto (el número de versión está en el archivo pom.xml), dirigirse a su repositorio local de `maven` y explorar la ruta `.m2/repository/ni/jug` en tu cuenta de usuario del SO y tomar nota de la versión del jar.

Código de ejemplo para obtener el tipo de cambio oficial del BCN:

        // Consultar por fecha
        ExchangeRateNCBClient client = new ExchangeRateScraper();

        Assertions.assertEquals(new BigDecimal("31.9396"), client.getExchangeRate(LocalDate.of(2018, 10, 1)));
        Assertions.assertEquals(new BigDecimal("32.0679"), client.getExchangeRate(LocalDate.of(2018, 10, 31)));

        // Consultar por periodo
        ExchangeRateNCBClient client = new ExchangeRateScraper();
        MonthlyExchangeRate monthlyExchangeRate = client.getMonthlyExchangeRate(2018, 10);

        Assertions.assertEquals(31, monthlyExchangeRate.size());
        Assertions.assertEquals(new BigDecimal("31.9396"), monthlyExchangeRate.getFirstExchangeRate());
        Assertions.assertEquals(new BigDecimal("32.0679"), monthlyExchangeRate.getLastExchangeRate());
        Assertions.assertEquals(new BigDecimal("31.9994"), monthlyExchangeRate.getExchangeRate(LocalDate.of(2018, 10, 15)));
        Assertions.assertEquals(BigDecimal.ZERO, monthlyExchangeRate.getExchangeRate(LocalDate.of(2018, 9, 30)));
        Assertions.assertFalse(monthlyExchangeRate.isIncomplete());

Código de ejemplo para obtener las tasas de los bancos comerciales:

        ExchangeRateCBClient client = ExchangeRateCBClient.scrapAndRepeatIfNecessary();
        StringBuilder result = new StringBuilder("\n");
        for (ExchangeRateTrade trade : client.trades()) {
            result.append(String.format("%-15s", trade.bank()));
            String sell = trade.sell().toPlainString() + (trade.isBestSellPrice() ? "*" : "");
            result.append(String.format("%12s", sell));
            String buy = trade.buy().toPlainString() + (trade.isBestBuyPrice() ? "*" : "");
            result.append(String.format("%12s", buy));
            result.append(String.format("%12s", bcnExchangeRate.toPlainString()));
            result.append("\n");
        }

Referirse a los [test unitarios][test unitario] para más ejemplos.

## Uso del CLI (Línea de comandos o Terminal)

Para ejecutar el cli se requiere tener instalado maven y java con las versiones indicadas en el apartado `Stack`. Cuando se construya el proyecto se debe incluir la propiedad de sistema `-Dcli` para activar el perfil del cli. Lo anterior hara que se cree un archivo `MANIFEST.MF` con la configuracion del classpath y la clase principal (sin este archivo no es posible ejecutar el cli).

Opciones disponibles:

- date: una fecha, un rango o una lista. La fecha debe ser ingresada en formato ISO: yyyy-MM-dd.
- ym: un año-mes, un rango o una lista. La fecha debe ser ingresada en formato ISO: yyyy-MM.
- bank: compra/venta de los bancos comerciales
- help: Muestra las opciones disponibles y ejemplos de uso.

Pasos para instalar la librería:

        git clone https://github.com/jugnicaragua/exchange-rate-client.git
        cd exchange-rate-client
        mvn package -Dcli
        # Si no se desean ejecutar los test unitarios durante el empaquetamiento del jar
        mvn package -Dcli -DskipTests
        # Ejecutar la aplicación. Referirse a los ejemplos anteriores para mayor información
        cd target/
        java -jar exchange-rate-client-1.0-SNAPSHOT.jar -date=2018-10-23

Ejemplos de ejecución del cli:

        java -jar exchange-rate-client-<version>.jar -date=2018-10-14
        java -jar exchange-rate-client-<version>.jar -date=2018-10-14:
        java -jar exchange-rate-client-<version>.jar -date=2018-10-14:2018-10-16
        java -jar exchange-rate-client-<version>.jar -date=2018-10-14:2018-10-16,2018-10-31

        java -jar exchange-rate-client-<version>.jar -ym=2018-10
        java -jar exchange-rate-client-<version>.jar -ym=2018-09:2018-10
        java -jar exchange-rate-client-<version>.jar -ym=2018-01,2018-09:2018-10

        java -jar exchange-rate-client-<version>.jar -date=2018-10-14: -ym=2018-10

        java -jar exchange-rate-client-<version>.jar -bank

        java -jar exchange-rate-client-<version>.jar --help

## Licencia

This software is covered under the MIT Licence (http://opensource.org/licenses/MIT).

Puedes leer el archivo de la licencia en [LICENSE][license]

Copyright (c) 2018-present, JUG Nicaragua Armando Alaniz

**Free Software, Hell Yeah!**

[license]: LICENSE.txt
[test unitario]: src/test/java/ni/jug/ncb/exchangerate/ExchangeRateScraperTest.java
