package br.com.gestpro.gestpro_backend.api.dto.modules.dashboard;

import java.util.List;

public record DashboardVisaoGeralResponse(
        Long totalVendasHoje,
        Long produtosEmEstoque,
        Long produtosZerados,
        Long clientesAtivos,
        Long vendasSemana,
        PlanoDTO planoUsuario,
        List<String> alertas
) {
}
