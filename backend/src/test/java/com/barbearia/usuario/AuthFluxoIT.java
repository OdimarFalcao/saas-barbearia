package com.barbearia.usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.barbearia.common.TestcontainersConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * TEST_PLAN §4.7 — S01 (401 sem sessão), S02 (login/logout/me) e S03 (CSRF)
 * para os endpoints de autenticação.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class AuthFluxoIT {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void setUp() {
		usuarioRepository.deleteAll();
		usuarioRepository.save(new Usuario("Ana Admin", "ana@barbearia.com",
				passwordEncoder.encode("senha-correta"), Perfil.ADMIN, true));
		usuarioRepository.save(new Usuario("Iva Inativa", "iva@barbearia.com",
				passwordEncoder.encode("senha-correta"), Perfil.RECEPCAO, false));
	}

	@Test
	@DisplayName("S02: login com credenciais válidas retorna 200 com dados do usuário")
	void loginComCredenciaisValidasRetornaDadosDoUsuario() throws Exception {
		mvc.perform(login("ana@barbearia.com", "senha-correta"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nome").value("Ana Admin"))
			.andExpect(jsonPath("$.email").value("ana@barbearia.com"))
			.andExpect(jsonPath("$.perfil").value("ADMIN"));
	}

	@Test
	@DisplayName("S02: login com senha errada retorna 401 sem detalhar o motivo")
	void loginComSenhaErradaRetorna401() throws Exception {
		mvc.perform(login("ana@barbearia.com", "senha-errada"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.nome").doesNotExist());
	}

	@Test
	@DisplayName("S02: login de usuário inativo retorna 401 (mesma resposta de credencial inválida)")
	void loginDeUsuarioInativoRetorna401() throws Exception {
		mvc.perform(login("iva@barbearia.com", "senha-correta"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("S02: login com email malformado retorna 400 de validação")
	void loginComEmailMalformadoRetorna400() throws Exception {
		mvc.perform(post("/api/auth/login").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"nao-e-email\",\"senha\":\"x\"}"))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("S01: GET /api/auth/me sem sessão retorna 401")
	void meSemSessaoRetorna401() throws Exception {
		mvc.perform(get("/api/auth/me"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("S02: GET /api/auth/me com sessão de login retorna o usuário logado")
	void meComSessaoRetornaUsuarioLogado() throws Exception {
		MockHttpSession sessao = fazerLogin("ana@barbearia.com", "senha-correta");

		mvc.perform(get("/api/auth/me").session(sessao))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value("ana@barbearia.com"))
			.andExpect(jsonPath("$.perfil").value("ADMIN"));
	}

	@Test
	@DisplayName("S03: login sem token CSRF retorna 403")
	void loginSemCsrfRetorna403() throws Exception {
		mvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"ana@barbearia.com\",\"senha\":\"senha-correta\"}"))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("S02: logout retorna 204 e invalida a sessão")
	void logoutInvalidaSessao() throws Exception {
		MockHttpSession sessao = fazerLogin("ana@barbearia.com", "senha-correta");

		mvc.perform(post("/api/auth/logout").session(sessao).with(csrf()))
			.andExpect(status().isNoContent());

		assertThat(sessao.isInvalid()).isTrue();
	}

	private org.springframework.test.web.servlet.RequestBuilder login(String email, String senha) {
		return post("/api/auth/login").with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}");
	}

	private MockHttpSession fazerLogin(String email, String senha) throws Exception {
		MvcResult result = mvc.perform(login(email, senha)).andExpect(status().isOk()).andReturn();
		return (MockHttpSession) result.getRequest().getSession(false);
	}
}
