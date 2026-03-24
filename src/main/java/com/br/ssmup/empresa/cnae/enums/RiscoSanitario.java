package com.br.ssmup.empresa.cnae.enums;

public enum RiscoSanitario {
    RISCO_I_BAIXO,  // Dispensado ou Liberação Automática (sem vistoria)
    RISCO_II_MEDIO, // Liberação Automática (vistoria posterior)
    RISCO_III_ALTO  // Requer Vistoria Prévia (não emite automático)
}
