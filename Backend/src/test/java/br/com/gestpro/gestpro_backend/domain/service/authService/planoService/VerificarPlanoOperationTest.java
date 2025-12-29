package br.com.gestpro.gestpro_backend.domain.service.authService.planoService;

import br.com.gestpro.gestpro_backend.domain.model.auth.Usuario;
import br.com.gestpro.gestpro_backend.domain.model.enums.StatusAcesso;
import br.com.gestpro.gestpro_backend.domain.model.enums.TipoPlano;
import br.com.gestpro.gestpro_backend.domain.repository.auth.UsuarioRepository;
import br.com.gestpro.gestpro_backend.infra.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificarPlanoOperationTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private VerificarPlanoOperation verificarPlanoOperation;

    @Test
    void deveSalvarDataPrimeiroLoginQuandoExperimentalSemData() {
        Usuario usuario = new Usuario();
        usuario.setTipoPlano(TipoPlano.EXPERIMENTAL);
        usuario.setStatusAcesso(StatusAcesso.ATIVO);
        usuario.setDataPrimeiroLogin(null);

        verificarPlanoOperation.execute(usuario);

        assertNotNull(usuario.getDataPrimeiroLogin());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deveExpirarPlanoExperimentalQuandoPeriodoAcabou() {
        Usuario usuario = new Usuario();
        usuario.setTipoPlano(TipoPlano.EXPERIMENTAL);
        usuario.setStatusAcesso(StatusAcesso.ATIVO);
        usuario.setDataPrimeiroLogin(LocalDateTime.now().minusDays(8));

        ApiException exception = assertThrows(ApiException.class, () ->
                verificarPlanoOperation.execute(usuario)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertTrue(exception.getMessage().contains("experimental"));

        verify(usuarioRepository).save(usuario);
        assertEquals(StatusAcesso.INATIVO, usuario.getStatusAcesso());
    }

    @Test
    void deveSalvarDataAssinaturaQuandoAssinanteSemData() {
        Usuario usuario = new Usuario();
        usuario.setTipoPlano(TipoPlano.ASSINANTE);
        usuario.setStatusAcesso(StatusAcesso.ATIVO);
        usuario.setDataAssinaturaPlus(null);

        verificarPlanoOperation.execute(usuario);

        assertNotNull(usuario.getDataAssinaturaPlus());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deveExpirarPlanoAssinanteQuandoAssinaturaAcabou() {
        Usuario usuario = new Usuario();
        usuario.setTipoPlano(TipoPlano.ASSINANTE);
        usuario.setStatusAcesso(StatusAcesso.ATIVO);
        usuario.setDataAssinaturaPlus(LocalDateTime.now().minusDays(40));

        ApiException exception = assertThrows(ApiException.class, () ->
                verificarPlanoOperation.execute(usuario)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertTrue(exception.getMessage().contains("Assinatura"));

        verify(usuarioRepository).save(usuario);
        assertEquals(StatusAcesso.INATIVO, usuario.getStatusAcesso());
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioJaInativo() {
        Usuario usuario = new Usuario();
        usuario.setTipoPlano(TipoPlano.EXPERIMENTAL);
        usuario.setStatusAcesso(StatusAcesso.INATIVO);

        ApiException exception = assertThrows(ApiException.class, () ->
                verificarPlanoOperation.execute(usuario)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertTrue(exception.getMessage().contains("Usuário inativo"));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void devePassarSemErroQuandoUsuarioValido() {
        Usuario usuario = new Usuario();
        usuario.setTipoPlano(TipoPlano.ASSINANTE);
        usuario.setStatusAcesso(StatusAcesso.ATIVO);
        usuario.setDataAssinaturaPlus(LocalDateTime.now());

        assertDoesNotThrow(() ->
                verificarPlanoOperation.execute(usuario)
        );

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveCalcularDiasRestantesPlanoExperimental() {
        Usuario usuario = new Usuario();
        usuario.setTipoPlano(TipoPlano.EXPERIMENTAL);
        usuario.setDataPrimeiroLogin(LocalDateTime.now().minusDays(2));

        long dias = verificarPlanoOperation.calcularDiasRestantes(usuario);

        assertTrue(dias > 0);
    }

    @Test
    void deveCalcularDiasRestantesPlanoAssinante() {
        Usuario usuario = new Usuario();
        usuario.setTipoPlano(TipoPlano.ASSINANTE);
        usuario.setDataAssinaturaPlus(LocalDateTime.now().minusDays(5));

        long dias = verificarPlanoOperation.calcularDiasRestantes(usuario);

        assertTrue(dias > 0);
    }

    @Test
    void deveRetornarZeroQuandoPlanoExpirado() {
        Usuario usuario = new Usuario();
        usuario.setTipoPlano(TipoPlano.ASSINANTE);
        usuario.setDataAssinaturaPlus(LocalDateTime.now().minusDays(60));

        long dias = verificarPlanoOperation.calcularDiasRestantes(usuario);

        assertEquals(0, dias);
    }
}
