package br.com.gestpro.gestpro_backend.domain.service.modulesService.dashboard;

import br.com.gestpro.gestpro_backend.api.dto.modules.dashboard.PlanoDTO;
import br.com.gestpro.gestpro_backend.domain.model.enums.TipoPlano;
import br.com.gestpro.gestpro_backend.domain.repository.auth.UsuarioRepository;
import br.com.gestpro.gestpro_backend.domain.repository.modules.ProdutoRepository;
import br.com.gestpro.gestpro_backend.domain.repository.modules.VendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class VisaoGeralOperation {

    private final VendaRepository vendaRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;

    public VisaoGeralOperation(VendaRepository vendaRepository,
                               ProdutoRepository produtoRepository,
                               UsuarioRepository usuarioRepository) {
        this.vendaRepository = vendaRepository;
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
    }


    @Transactional(readOnly = true)
    public Long vendasSemana(String emailUsuario) {
        LocalDate hoje = LocalDate.now();
        LocalDate inicioSemana = hoje.with(DayOfWeek.MONDAY);
        LocalDate fimSemana = hoje.with(DayOfWeek.SUNDAY);

        LocalDateTime inicio = inicioSemana.atStartOfDay();
        LocalDateTime fim = fimSemana.atTime(23, 59, 59);

        Long total = vendaRepository.countByDataVendaBetweenAndUsuarioEmail(inicio, fim, emailUsuario);
        return total != null ? total : 0L;
    }

    // ------------------------- ALERTAS ---------------------------------

    @Transactional(readOnly = true)
    public List<String> alertasProdutosZerados(String emailUsuario) {
        List<String> produtosZerados = produtoRepository.findByQuantidadeEstoqueAndUsuarioEmail(0, emailUsuario)
                .stream()
                .map(p -> "Produto " + p.getNome() + " está com estoque zerado!")
                .limit(10)
                .toList();

        if (produtosZerados.isEmpty()) {
            return List.of("Nenhum produto está com estoque zerado!");
        }

        return produtosZerados;
    }

    @Transactional(readOnly = true)
    public List<String> alertasVendasSemana(String emailUsuario) {
        if (vendasSemana(emailUsuario) < 50) {
            return List.of("Vendas da semana abaixo do esperado");
        }
        return List.of();
    }

    @Transactional(readOnly = true)
    public PlanoDTO planoUsuarioLogado(String emailUsuario) {
        return usuarioRepository.findByEmail(emailUsuario)
                .map(usuario -> {
                    TipoPlano tipoPlano = usuario.getTipoPlano();
                    LocalDate dataExpiracao = usuario.getDataExpiracaoPlano();
                    long diasRestantes = dataExpiracao != null
                            ? ChronoUnit.DAYS.between(LocalDate.now(), dataExpiracao)
                            : 0;
                    diasRestantes = Math.max(diasRestantes, 0);
                    return new PlanoDTO(tipoPlano.name(), diasRestantes);
                })
                .orElse(new PlanoDTO("NENHUM", 0));
    }


}


