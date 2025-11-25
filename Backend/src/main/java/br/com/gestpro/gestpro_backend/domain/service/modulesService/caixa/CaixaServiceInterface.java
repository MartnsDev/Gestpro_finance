package br.com.gestpro.gestpro_backend.domain.service.modulesService.caixa;

import br.com.gestpro.gestpro_backend.api.dto.modules.caixa.AbrirCaixaRequest;
import br.com.gestpro.gestpro_backend.api.dto.modules.caixa.CaixaResponse;
import br.com.gestpro.gestpro_backend.api.dto.modules.caixa.FecharCaixaRequest;
import org.springframework.dao.OptimisticLockingFailureException;

public interface CaixaServiceInterface {
    CaixaResponse abrirCaixa(AbrirCaixaRequest req);

    CaixaResponse fecharCaixa(FecharCaixaRequest req) throws OptimisticLockingFailureException;

    CaixaResponse obterResumo(Long caixaId);
}
