package br.com.gestpro.gestpro_backend.domain.service.authService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class UploadFotoOperationTest {

    private UploadFotoOperation uploadFotoOperation;
    private Path uploadsDir;

    @BeforeEach
    void setup() {
        uploadFotoOperation = new UploadFotoOperation();
        uploadsDir = Paths.get("uploads");
    }

    @AfterEach
    void cleanup() throws IOException {
        if (Files.exists(uploadsDir)) {
            Files.walk(uploadsDir)
                    .sorted((a, b) -> b.compareTo(a)) // apaga arquivos antes da pasta
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            // ignora, é teste
                        }
                    });
        }
    }

    // -------------------- CASOS INVÁLIDOS --------------------

    @Test
    void deveRetornarNullQuandoFotoForNula() throws IOException {
        String resultado = uploadFotoOperation.salvarFoto(null);

        assertNull(resultado);
        assertFalse(Files.exists(uploadsDir));
    }

    @Test
    void deveRetornarNullQuandoFotoForVazia() throws IOException {
        MultipartFile fotoVazia = new MockMultipartFile(
                "foto",
                "foto.png",
                "image/png",
                new byte[0]
        );

        String resultado = uploadFotoOperation.salvarFoto(fotoVazia);

        assertNull(resultado);
    }


    // -------------------- SUCESSO --------------------

    @Test
    void deveSalvarFotoComSucesso() throws IOException {
        MultipartFile foto = new MockMultipartFile(
                "foto",
                "foto.png",
                "image/png",
                "conteudo-da-imagem".getBytes()
        );

        String caminhoSalvo = uploadFotoOperation.salvarFoto(foto);

        assertNotNull(caminhoSalvo);

        Path arquivoSalvo = Paths.get(caminhoSalvo);
        assertTrue(Files.exists(arquivoSalvo));
        assertTrue(arquivoSalvo.getFileName().toString().contains("foto.png"));

        byte[] conteudo = Files.readAllBytes(arquivoSalvo);
        assertEquals("conteudo-da-imagem", new String(conteudo));
    }

    @Test
    void deveCriarDiretorioUploadsAutomaticamente() throws IOException {
        assertFalse(Files.exists(uploadsDir));

        MultipartFile foto = new MockMultipartFile(
                "foto",
                "imagem.jpg",
                "image/jpeg",
                "dados".getBytes()
        );

        uploadFotoOperation.salvarFoto(foto);

        assertTrue(Files.exists(uploadsDir));
        assertTrue(Files.isDirectory(uploadsDir));
    }
}
