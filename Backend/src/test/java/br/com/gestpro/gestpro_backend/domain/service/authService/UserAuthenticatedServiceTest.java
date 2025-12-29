package br.com.gestpro.gestpro_backend.domain.service.authService;

import br.com.gestpro.gestpro_backend.domain.model.auth.Usuario;
import br.com.gestpro.gestpro_backend.domain.model.enums.StatusAcesso;
import br.com.gestpro.gestpro_backend.domain.model.enums.TipoPlano;
import br.com.gestpro.gestpro_backend.domain.repository.auth.UsuarioRepository;
import br.com.gestpro.gestpro_backend.infra.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAuthenticatedServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UserAuthenticatedService service;

    private Usuario usuario;

    @BeforeEach
    void setup() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Matheus");
        usuario.setEmail("matheus@email.com");
        usuario.setTipoPlano(TipoPlano.EXPERIMENTAL);
        usuario.setStatusAcesso(StatusAcesso.ATIVO);
        usuario.setFoto("foto.png");
    }

    // ============================
    // getUsuarioPorToken
    // ============================

    @Test
    void deveRetornarUnauthorizedQuandoTokenForNulo() {
        ResponseEntity<?> response = service.getUsuarioPorToken(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Usuário não está logado"));
    }

    @Test
    void deveRetornarUnauthorizedQuandoTokenForInvalido() {
        when(jwtService.isTokenValid("token-invalido")).thenReturn(false);

        ResponseEntity<?> response = service.getUsuarioPorToken("token-invalido");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Token inválido"));
    }

    @Test
    void deveRetornarNotFoundQuandoUsuarioNaoExistir() {
        when(jwtService.isTokenValid("token-ok")).thenReturn(true);
        when(jwtService.getEmailFromToken("token-ok")).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail()))
                .thenReturn(Optional.empty());

        ResponseEntity<?> response = service.getUsuarioPorToken("token-ok");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Usuário não encontrado"));
    }

    @Test
    void deveRetornarDadosPublicosQuandoTokenForValido() {
        when(jwtService.isTokenValid("token-ok")).thenReturn(true);
        when(jwtService.getEmailFromToken("token-ok")).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail()))
                .thenReturn(Optional.of(usuario));

        ResponseEntity<?> response = service.getUsuarioPorToken("token-ok");

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Map<?, ?> body = (Map<?, ?>) response.getBody();

        assertEquals(usuario.getId(), body.get("id"));
        assertEquals(usuario.getNome(), body.get("nome"));
        assertEquals(usuario.getEmail(), body.get("email"));
        assertEquals(usuario.getTipoPlano(), body.get("tipoPlano"));
        assertEquals(usuario.getStatusAcesso(), body.get("statusAcesso"));
        assertEquals(usuario.getFoto(), body.get("foto"));
    }

    // ============================
    // buscarUsuarioPorId
    // ============================

    @Test
    void deveBuscarUsuarioPorIdComSucesso() {
        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        Usuario resultado = service.buscarUsuarioPorId(1L);

        assertNotNull(resultado);
        assertEquals(usuario.getEmail(), resultado.getEmail());
    }

    @Test
    void deveLancarExcecaoQuandoIdForInvalido() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.buscarUsuarioPorId(0L)
        );

        assertEquals("ID do usuário inválido.", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExistirPorId() {
        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.buscarUsuarioPorId(99L)
        );

        assertEquals("Usuário não encontrado.", exception.getMessage());
    }
}
