package br.com.gestpro.gestpro_backend.api.dto.modules.caixa;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AbrirCaixaRequest {
    @NotNull
    private String abertoPor;

    @NotNull
    private BigDecimal saldoInicial;

    // getters e setters usando @Data - lombok
}
