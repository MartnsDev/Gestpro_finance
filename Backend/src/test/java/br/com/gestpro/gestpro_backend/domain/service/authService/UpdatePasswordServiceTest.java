package br.com.gestpro.gestpro_backend.domain.service.authService;

import br.com.gestpro.gestpro_backend.domain.model.auth.Usuario;
import br.com.gestpro.gestpro_backend.domain.repository.auth.UsuarioRepository;
import br.com.gestpro.gestpro_backend.domain.service.EmailService;
import br.com.gestpro.gestpro_backend.infra.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdatePasswordServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UpdatePasswordService updatePasswordService;

    private Usuario usuario;

    @BeforeEach
    void setup() {
        usuario = new Usuario();
        usuario.setEmail("teste@email.com");
        usuario.setNome("Usuário Teste");
        usuario.setSenha("senha-antiga");
    }

    // ============================
    // sendVerificationCode
    // ============================

    @Test
    void deveEnviarCodigoQuandoEmailExistir() {
        when(usuarioRepository.findByEmail(usuario.getEmail()))
                .thenReturn(Optional.of(usuario));

        updatePasswordService.sendVerificationCode(usuario.getEmail());

        verify(usuarioRepository).save(usuario);
        verify(emailService).enviarEmail(
                eq(usuario.getEmail()),
                eq(usuario.getNome()),
                eq("Código de recuperação"),
                contains("Seu código é")
        );

        assertNotNull(usuario.getCodigoRecuperacao());
    }

    @Test
    void deveLancarExcecaoQuandoEmailNaoExistir() {
        when(usuarioRepository.findByEmail("inexistente@email.com"))
                .thenReturn(Optional.empty());

        ApiException exception = assertThrows(
                ApiException.class,
                () -> updatePasswordService.sendVerificationCode("inexistente@email.com")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    // ============================
    // resetPassword
    // ============================

    @Test
    void deveAtualizarSenhaQuandoCodigoForValido() {
        when(usuarioRepository.findByEmail(usuario.getEmail()))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.encode("novaSenha"))
                .thenReturn("senha-criptografada");

        // primeiro envia o código
        updatePasswordService.sendVerificationCode(usuario.getEmail());
        String codigo = usuario.getCodigoRecuperacao();

        updatePasswordService.resetPassword(usuario.getEmail(), codigo, "novaSenha");

        verify(usuarioRepository, times(2)).save(usuario);
        assertEquals("senha-criptografada", usuario.getSenha());
    }

    @Test
    void deveLancarExcecaoQuandoCodigoForInvalido() {
        when(usuarioRepository.findByEmail(usuario.getEmail()))
                .thenReturn(Optional.of(usuario));

        updatePasswordService.sendVerificationCode(usuario.getEmail());

        ApiException exception = assertThrows(
                ApiException.class,
                () -> updatePasswordService.resetPassword(usuario.getEmail(), "000000", "novaSenha")
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExistirNoReset() {
        when(usuarioRepository.findByEmail(usuario.getEmail()))
                .thenReturn(Optional.empty());

        ApiException exception = assertThrows(
                ApiException.class,
                () -> updatePasswordService.resetPassword(usuario.getEmail(), "123456", "novaSenha")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}
