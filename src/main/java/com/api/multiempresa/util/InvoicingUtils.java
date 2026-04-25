package com.api.multiempresa.util;

import java.math.BigDecimal;
import java.math.BigInteger;

public class InvoicingUtils {
  private static final String[] UNIDADES = {
      "", "UNO", "DOS", "TRES", "CUATRO", "CINCO",
      "SEIS", "SIETE", "OCHO", "NUEVE", "DIEZ",
      "ONCE", "DOCE", "TRECE", "CATORCE", "QUINCE",
      "DIECISÉIS", "DIECISIETE", "DIECIOCHO", "DIECINUEVE", "VEINTE"
  };

  private static final String[] DECENAS = {
      "", "", "VEINTI", "TREINTA", "CUARENTA",
      "CINCUENTA", "SESENTA", "SETENTA", "OCHENTA", "NOVENTA"
  };

  private static final String[] CENTENAS = {
      "", "CIENTO", "DOSCIENTOS", "TRESCIENTOS", "CUATROCIENTOS",
      "QUINIENTOS", "SEISCIENTOS", "SETECIENTOS", "OCHOCIENTOS", "NOVECIENTOS"
  };

  public static String convertir(BigDecimal monto) {
    return convertir(monto, "SOLES");
  }

  public static String convertir(BigDecimal monto, String currencyLabel) {
    BigInteger parteEntera = monto.toBigInteger();
    int centavos = monto.remainder(BigDecimal.ONE).movePointRight(2).intValue();

    String letras = convertirNumero(parteEntera.longValue());

    String centavosStr = centavos < 10 ? "0" + centavos : String.valueOf(centavos);
    return letras + " CON " + centavosStr + "/100 " + currencyLabel;
  }

  private static String convertirNumero(long numero) {
    if (numero == 0) return "CERO";

    if (numero == 100) return "CIEN";

    StringBuilder resultado = new StringBuilder();

    if (numero >= 1_000_000) {
      long millones = numero / 1_000_000;
      resultado.append(convertirNumero(millones)).append(" MILLONES ");
      numero %= 1_000_000;
    }

    if (numero >= 1_000) {
      long miles = numero / 1_000;
      if (miles == 1) {
        resultado.append("MIL ");
      } else {
        resultado.append(convertirNumero(miles)).append(" MIL ");
      }
      numero %= 1_000;
    }

    if (numero >= 100) {
      int centenas = (int) numero / 100;
      resultado.append(CENTENAS[centenas]).append(" ");
      numero %= 100;
    }

    if (numero > 20) {
      int decena = (int) numero / 10;
      int unidad = (int) numero % 10;
      resultado.append(DECENAS[decena]);
      if (unidad > 0) {
        if (decena == 2) {
          resultado.append(UNIDADES[unidad]);
        } else {
          resultado.append(" Y ").append(UNIDADES[unidad]);
        }
      }
    } else if (numero > 0) {
      resultado.append(UNIDADES[(int) numero]);
    }

    return resultado.toString().trim();
  }
}
