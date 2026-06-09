package com.barbearia.usuario;

import com.barbearia.usuario.dto.LoginRequest;
import com.barbearia.usuario.dto.UsuarioLogadoResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final SecurityContextRepository securityContextRepository;
	private final UsuarioRepository usuarioRepository;
	private final SecurityContextHolderStrategy holderStrategy =
			SecurityContextHolder.getContextHolderStrategy();

	public AuthController(AuthenticationManager authenticationManager,
			SecurityContextRepository securityContextRepository,
			UsuarioRepository usuarioRepository) {
		this.authenticationManager = authenticationManager;
		this.securityContextRepository = securityContextRepository;
		this.usuarioRepository = usuarioRepository;
	}

	@PostMapping("/login")
	public UsuarioLogadoResponse login(@Valid @RequestBody LoginRequest body,
			HttpServletRequest request, HttpServletResponse response) {
		Authentication autenticacao = authenticationManager.authenticate(
				UsernamePasswordAuthenticationToken.unauthenticated(body.email(), body.senha()));

		// proteção contra session fixation no login programático
		if (request.getSession(false) != null) {
			request.changeSessionId();
		}

		SecurityContext contexto = holderStrategy.createEmptyContext();
		contexto.setAuthentication(autenticacao);
		holderStrategy.setContext(contexto);
		securityContextRepository.saveContext(contexto, request, response);

		return buscarPorEmail(autenticacao.getName());
	}

	@GetMapping("/me")
	public UsuarioLogadoResponse me(Authentication autenticacao) {
		return buscarPorEmail(autenticacao.getName());
	}

	private UsuarioLogadoResponse buscarPorEmail(String email) {
		return usuarioRepository.findByEmailIgnoreCase(email)
			.map(UsuarioLogadoResponse::de)
			.orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));
	}
}
