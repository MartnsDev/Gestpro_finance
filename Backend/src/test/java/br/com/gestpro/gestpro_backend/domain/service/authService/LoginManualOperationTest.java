package br.com.gestpro.gestpro_backend.domain.service.authService;

import br.com.gestpro.gestpro_backend.api.dto.auth.AuthDTO.LoginResponse;
import br.com.gestpro.gestpro_backend.api.dto.auth.AuthDTO.LoginUsuarioDTO;
import br.com.gestpro.gestpro_backend.domain.model.auth.Usuario;
import br.com.gestpro.gestpro_backend.domain.model.enums.TipoPlano;
import br.com.gestpro.gestpro_backend.domain.repository.auth.UsuarioRepository;
import br.com.gestpro.gestpro_backend.domain.service.authService.jwtService.JwtTokenServiceInterface;
import br.com.gestpro.gestpro_backend.domain.service.authService.planoService.VerificarPlanoOperation;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginManualOperationTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private VerificarPlanoOperation verificarPlano;

    @Mock
    private JwtTokenServiceInterface jwtTokenService;

    @InjectMocks
    private LoginManualOperation operation;

    private Usuario usuario;
    private LoginUsuarioDTO loginDTO;

    @BeforeEach
    void setup() {
        usuario = new Usuario();
        usuario.setNome("Matheus");
        usuario.setEmail("teste@email.com");
        usuario.setSenha("senha-criptografada");
        usuario.setEmailConfirmado(true);
        usuario.setLoginGoogle(false);
        usuario.setTipoPlano(TipoPlano.EXPERIMENTAL);

        loginDTO = new LoginUsuarioDTO(
                "teste@email.com",
                "senha-plana"
        );
    }

    // -------------------- ERROS --------------------
    

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        when(usuarioRepository.findByEmail(any()))
                .thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> operation.execute(loginDTO, "/login"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        verifyNoInteractions(verificarPlano, jwtTokenService);
    }

    @Test
    void deveLancarExcecaoQuandoEmailNaoConfirmado() {
        usuario.setEmailConfirmado(false);

        when(usuarioRepository.findByEmail(any()))
                .thenReturn(Optional.of(usuario));

        ApiException ex = assertThrows(ApiException.class,
                () -> operation.execute(loginDTO, "/login"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verifyNoInteractions(verificarPlano, jwtTokenService);
    }

    @Test
    void deveLancarExcecaoQuandoSenhaInvalida() {
        when(usuarioRepository.findByEmail(any()))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches(any(), any()))
                .thenReturn(false);

        ApiException ex = assertThrows(ApiException.class,
                () -> operation.execute(loginDTO, "/login"));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        verify(verificarPlano, never()).execute(any());
        verify(jwtTokenService, never()).gerarToken(any());
    }

    @Test
    void devePropagarExcecaoQuandoPlanoInvalido() {
        when(usuarioRepository.findByEmail(any()))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches(any(), any()))
                .thenReturn(true);

        doThrow(new ApiException(
                "Usuário inativo",
                HttpStatus.FORBIDDEN,
                "/pagamento"
        )).when(verificarPlano).execute(usuario);

        ApiException ex = assertThrows(ApiException.class,
                () -> operation.execute(loginDTO, "/login"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        verify(jwtTokenService, never()).gerarToken(any());
    }

    // -------------------- SUCESSO --------------------

    @Test
    void deveRealizarLoginManualComSucesso() {
        when(usuarioRepository.findByEmail(any()))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches(any(), any()))
                .thenReturn(true);

        when(jwtTokenService.gerarToken(any()))
                .thenReturn("token-ok");

        LoginResponse response = operation.execute(loginDTO, "/login");

        assertNotNull(response);
        assertEquals("token-ok", response.token());

        verify(verificarPlano).execute(usuario);
        verify(jwtTokenService).gerarToken(usuario);
    }

    @Test
    void deveConverterLoginGoogleParaManualELogar() {
        usuario.setLoginGoogle(true);

        when(usuarioRepository.findByEmail(any()))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.encode(any()))
                .thenReturn("senha-nova");

        when(jwtTokenService.gerarToken(any()))
                .thenReturn("token-google");

        LoginResponse response = operation.execute(loginDTO, "/login");

        assertEquals("token-google", response.token());
        assertFalse(usuario.isLoginGoogle());

        verify(passwordEncoder).encode(any());
        verify(usuarioRepository).save(usuario);
        verify(verificarPlano).execute(usuario);
        verify(jwtTokenService).gerarToken(usuario);
    }

}
