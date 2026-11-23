package br.edu.cantina.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public final class Dinheiro {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private Dinheiro() {
    }

    public static String formatar(BigDecimal valor) {
        BigDecimal v = valor == null ? BigDecimal.ZERO : valor.setScale(2, RoundingMode.HALF_UP);
        return String.format(PT_BR, "R$ %,.2f", v);
    }
}
