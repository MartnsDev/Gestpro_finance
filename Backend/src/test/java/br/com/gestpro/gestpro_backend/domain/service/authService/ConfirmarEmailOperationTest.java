package br.com.gestpro.gestpro_backend.domain.service.authService;

import br.com.gestpro.gestpro_backend.domain.model.auth.Usuario;
import br.com.gestpro.gestpro_backend.domain.repository.auth.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmarEmailOperationTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ConfirmarEmailOperation operation;

    private Usuario usuario;

    @BeforeEach
    void setup() {
        usuario = new Usuario();
        usuario.setEmailConfirmado(false);
        usuario.setTokenConfirmacao("token-valido");
    }

    // -------------------- SUCESSO --------------------

    @Test
    void deveConfirmarEmailComTokenValido() {
        when(usuarioRepository.findByTokenConfirmacao("token-valido"))
                .thenReturn(Optional.of(usuario));

        boolean resultado = operation.execute("token-valido");

        assertTrue(resultado);
        assertTrue(usuario.isEmailConfirmado());
        assertNull(usuario.getTokenConfirmacao());

        verify(usuarioRepository).save(usuario);
    }

    // -------------------- FALHA --------------------

    @Test
    void deveRetornarFalseQuandoTokenNaoExiste() {
        when(usuarioRepository.findByTokenConfirmacao("token-invalido"))
                .thenReturn(Optional.empty());

        boolean resultado = operation.execute("token-invalido");

        assertFalse(resultado);
        verify(usuarioRepository, never()).save(any());
    }
}
