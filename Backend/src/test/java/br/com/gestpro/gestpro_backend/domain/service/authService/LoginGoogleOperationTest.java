package br.com.gestpro.gestpro_backend.domain.service.authService;

import br.com.gestpro.gestpro_backend.domain.model.auth.Usuario;
import br.com.gestpro.gestpro_backend.domain.model.enums.StatusAcesso;
import br.com.gestpro.gestpro_backend.domain.model.enums.TipoPlano;
import br.com.gestpro.gestpro_backend.domain.repository.auth.UsuarioRepository;
import br.com.gestpro.gestpro_backend.domain.service.authService.planoService.VerificarPlanoOperation;
import br.com.gestpro.gestpro_backend.infra.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginGoogleOperationTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private VerificarPlanoOperation verificarPlano;

    @InjectMocks
    private LoginGoogleOperation operation;

    private Usuario usuarioExistente;

    @BeforeEach
    void setup() {
        usuarioExistente = new Usuario();
        usuarioExistente.setEmail("teste@email.com");
        usuarioExistente.setNome("Nome Antigo");
        usuarioExistente.setFoto("foto-antiga");
        usuarioExistente.setEmailConfirmado(false);
        usuarioExistente.setLoginGoogle(false);
        usuarioExistente.setTipoPlano(TipoPlano.EXPERIMENTAL);
        usuarioExistente.setStatusAcesso(StatusAcesso.ATIVO);
        usuarioExistente.setDataPrimeiroLogin(LocalDateTime.now().minusDays(1));
    }

    // -------------------- USUÁRIO EXISTENTE --------------------

    @Test
    void deveAtualizarUsuarioExistenteELogarViaGoogle() {
        when(usuarioRepository.findByEmail(any()))
                .thenReturn(Optional.of(usuarioExistente));

        when(usuarioRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = operation.execute(
                "teste@email.com",
                "Nome Novo",
                "foto-nova"
        );

        assertEquals("Nome Novo", resultado.getNome());
        assertEquals("foto-nova", resultado.getFoto());
        assertTrue(resultado.isEmailConfirmado());
        assertTrue(resultado.isLoginGoogle());

        verify(verificarPlano).execute(usuarioExistente);
        verify(usuarioRepository).save(usuarioExistente);
    }

    @Test
    void naoDeveAlterarLoginGoogleSeJaForGoogle() {
        usuarioExistente.setLoginGoogle(true);

        when(usuarioRepository.findByEmail(any()))
                .thenReturn(Optional.of(usuarioExistente));

        when(usuarioRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = operation.execute(
                "teste@email.com",
                "Nome Novo",
                "foto-nova"
        );

        assertTrue(resultado.isLoginGoogle());
        verify(verificarPlano).execute(usuarioExistente);
    }

    @Test
    void deveConfirmarEmailSeUsuarioNaoConfirmado() {
        when(usuarioRepository.findByEmail(any()))
                .thenReturn(Optional.of(usuarioExistente));

        operation.execute(
                "teste@email.com",
                "Nome",
                "foto"
        );

        assertTrue(usuarioExistente.isEmailConfirmado());
        assertNull(usuarioExistente.getTokenConfirmacao());
    }

    @Test
    void devePropagarExcecaoSePlanoInvalido() {
        when(usuarioRepository.findByEmail(any()))
                .thenReturn(Optional.of(usuarioExistente));

        doThrow(new ApiException(
                "Usuário inativo",
                org.springframework.http.HttpStatus.FORBIDDEN,
                "/pagamento"
        )).when(verificarPlano).execute(any());

        assertThrows(ApiException.class, () ->
                operation.execute(
                        "teste@email.com",
                        "Nome",
                        "foto"
                )
        );

        verify(usuarioRepository, never()).save(any());
    }

    // -------------------- NOVO USUÁRIO --------------------

    @Test
    void deveCriarNovoUsuarioQuandoNaoExistir() {
        when(usuarioRepository.findByEmail(any()))
                .thenReturn(Optional.empty());

        when(usuarioRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        Usuario novo = operation.execute(
                "novo@email.com",
                "Novo Usuario",
                "foto"
        );

        assertEquals("Novo Usuario", novo.getNome());
        assertEquals("novo@email.com", novo.getEmail());
        assertEquals(TipoPlano.EXPERIMENTAL, novo.getTipoPlano());
        assertTrue(novo.isEmailConfirmado());
        assertTrue(novo.isLoginGoogle());
        assertEquals(StatusAcesso.ATIVO, novo.getStatusAcesso());
        assertNotNull(novo.getDataCriacao());
        assertNotNull(novo.getDataPrimeiroLogin());

        verify(verificarPlano, never()).execute(any());
        verify(usuarioRepository).save(any());
    }
}
