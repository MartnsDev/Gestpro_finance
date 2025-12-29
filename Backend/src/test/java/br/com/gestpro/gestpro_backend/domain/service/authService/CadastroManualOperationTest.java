package br.com.gestpro.gestpro_backend.domain.service.authService;


import br.com.gestpro.gestpro_backend.domain.model.auth.Usuario;
import br.com.gestpro.gestpro_backend.domain.model.enums.StatusAcesso;
import br.com.gestpro.gestpro_backend.domain.model.enums.TipoPlano;
import br.com.gestpro.gestpro_backend.domain.repository.auth.UsuarioRepository;
import br.com.gestpro.gestpro_backend.domain.service.EmailService;
import br.com.gestpro.gestpro_backend.infra.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CadastroManualOperationTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UploadFotoOperation uploadFotoOperation;

    @Mock
    private MultipartFile foto;

    @InjectMocks
    private CadastroManualOperation cadastroManualOperation;

    private final String BASE_URL = "http://localhost:8080";
    private final String PATH = "/auth/cadastro";

    @Test
    void deveCriarUsuarioNovoComSucesso() throws IOException {
        when(usuarioRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(anyString()))
                .thenReturn("senha-criptografada");

        when(foto.isEmpty()).thenReturn(true);

        Usuario usuario = cadastroManualOperation.execute(
                "Matheus",
                "email@test.com",
                "123456",
                foto,
                BASE_URL,
                PATH
        );

        assertNotNull(usuario);
        assertEquals("Matheus", usuario.getNome());
        assertEquals("email@test.com", usuario.getEmail());
        assertEquals(TipoPlano.EXPERIMENTAL, usuario.getTipoPlano());
        assertEquals(StatusAcesso.ATIVO, usuario.getStatusAcesso());
        assertFalse(usuario.isEmailConfirmado());
        assertFalse(usuario.isLoginGoogle());
        assertNotNull(usuario.getTokenConfirmacao());

        verify(usuarioRepository).save(usuario);
        verify(emailService).enviarConfirmacao(eq("email@test.com"), contains("/auth/confirmar"));
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaCadastradoViaGoogle() {
        Usuario existente = new Usuario();
        existente.setLoginGoogle(true);

        when(usuarioRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(existente));

        ApiException exception = assertThrows(ApiException.class, () ->
                cadastroManualOperation.execute(
                        "Matheus",
                        "email@test.com",
                        "123456",
                        null,
                        BASE_URL,
                        PATH
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getMessage().contains("cadastro via Google"));

        verify(usuarioRepository, never()).save(any());
        verify(emailService, never()).enviarConfirmacao(any(), any());
    }

    @Test
    void deveLancarExcecaoQuandoEmailNaoConfirmado() {
        Usuario existente = new Usuario();
        existente.setLoginGoogle(false);
        existente.setEmailConfirmado(false);

        when(usuarioRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(existente));

        ApiException exception = assertThrows(ApiException.class, () ->
                cadastroManualOperation.execute(
                        "Matheus",
                        "email@test.com",
                        "123456",
                        null,
                        BASE_URL,
                        PATH
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getMessage().contains("aguarda confirmação"));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaConfirmado() {
        Usuario existente = new Usuario();
        existente.setLoginGoogle(false);
        existente.setEmailConfirmado(true);

        when(usuarioRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(existente));

        ApiException exception = assertThrows(ApiException.class, () ->
                cadastroManualOperation.execute(
                        "Matheus",
                        "email@test.com",
                        "123456",
                        null,
                        BASE_URL,
                        PATH
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getMessage().contains("já cadastrado"));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoSenhaInvalida() {
        ApiException exception = assertThrows(ApiException.class, () ->
                cadastroManualOperation.execute(
                        "Matheus",
                        "email@test.com",
                        "123",
                        null,
                        BASE_URL,
                        PATH
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getMessage().contains("senha"));

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void deveLancarExcecaoQuandoEmailVazio() {
        ApiException exception = assertThrows(ApiException.class, () ->
                cadastroManualOperation.execute(
                        "Matheus",
                        "",
                        "123456",
                        null,
                        BASE_URL,
                        PATH
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getMessage().contains("e-mail"));

        verifyNoInteractions(usuarioRepository);
    }
}
