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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AtualizarPlanoOperationTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AtualizarPlanoOperation atualizarPlanoOperation;

    private final String EMAIL = "email@test.com";

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        when(usuarioRepository.findByEmail(EMAIL))
                .thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () ->
                atualizarPlanoOperation.atualizarPlano(EMAIL, 30)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertTrue(exception.getMessage().contains("Usuário não encontrado"));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioInativo() {
        Usuario usuario = new Usuario();
        usuario.setEmail(EMAIL);
        usuario.setStatusAcesso(StatusAcesso.INATIVO);

        when(usuarioRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(usuario));

        ApiException exception = assertThrows(ApiException.class, () ->
                atualizarPlanoOperation.atualizarPlano(EMAIL, 30)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertTrue(exception.getMessage().contains("Usuário inativo"));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveAtualizarPlanoQuandoPlanoExpirado() {
        Usuario usuario = new Usuario();
        usuario.setEmail(EMAIL);
        usuario.setStatusAcesso(StatusAcesso.ATIVO);
        usuario.setDataAssinaturaPlus(LocalDateTime.now().minusDays(10));

        when(usuarioRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(usuario));

        when(usuarioRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario atualizado = atualizarPlanoOperation.atualizarPlano(EMAIL, 30);

        assertEquals(TipoPlano.ASSINANTE, atualizado.getTipoPlano());
        assertEquals(StatusAcesso.ATIVO, atualizado.getStatusAcesso());
        assertTrue(atualizado.getDataAssinaturaPlus().isAfter(LocalDateTime.now()));

        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deveSomarDiasQuandoPlanoAindaAtivo() {
        LocalDateTime dataAtual = LocalDateTime.now().plusDays(5);

        Usuario usuario = new Usuario();
        usuario.setEmail(EMAIL);
        usuario.setStatusAcesso(StatusAcesso.ATIVO);
        usuario.setDataAssinaturaPlus(dataAtual);

        when(usuarioRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(usuario));

        when(usuarioRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario atualizado = atualizarPlanoOperation.atualizarPlano(EMAIL, 30);

        assertEquals(dataAtual.plusDays(30), atualizado.getDataAssinaturaPlus());
        assertEquals(TipoPlano.ASSINANTE, atualizado.getTipoPlano());
        assertEquals(StatusAcesso.ATIVO, atualizado.getStatusAcesso());

        verify(usuarioRepository).save(usuario);
    }
}
