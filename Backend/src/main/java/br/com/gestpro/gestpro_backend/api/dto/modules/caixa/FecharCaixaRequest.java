package br.com.gestpro.gestpro_backend.api.dto.modules.caixa;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FecharCaixaRequest {

    @NotNull
    private Long caixaId;

    @NotNull
    private String fechadoPor;

    @NotNull
    private BigDecimal saldoFinal;
}
