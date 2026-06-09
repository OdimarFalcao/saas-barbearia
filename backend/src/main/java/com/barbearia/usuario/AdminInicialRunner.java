package com.barbearia.usuario;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Primeiro acesso: se o banco não tem nenhum usuário e ADMIN_INICIAL_EMAIL /
 * ADMIN_INICIAL_SENHA estão configurados, cria o ADMIN inicial. Nunca roda
 * quando já existe usuário — não serve para reset de senha.
 */
@Component
public class AdminInicialRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(AdminInicialRunner.class);

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final String email;
	private final String senha;

	public AdminInicialRunner(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
			@Value("${app.admin-inicial.email:}") String email,
			@Value("${app.admin-inicial.senha:}") String senha) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.email = email;
		this.senha = senha;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (!StringUtils.hasText(email) || !StringUtils.hasText(senha)) {
			return;
		}
		if (usuarioRepository.count() > 0) {
			return;
		}
		usuarioRepository.save(new Usuario("Administrador", email,
				passwordEncoder.encode(senha), Perfil.ADMIN, true));
		log.info("Usuário ADMIN inicial criado para {}", email);
	}
}
