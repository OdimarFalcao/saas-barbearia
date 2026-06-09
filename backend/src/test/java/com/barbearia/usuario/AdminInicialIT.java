package com.barbearia.usuario;

import static org.assertj.core.api.Assertions.assertThat;

import com.barbearia.common.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Bootstrap do primeiro acesso: com banco vazio e credenciais configuradas
 * (ADMIN_INICIAL_*), o sistema cria o usuário ADMIN inicial na subida.
 */
@SpringBootTest(properties = {
		"app.admin-inicial.email=dono@barbearia.com",
		"app.admin-inicial.senha=primeiro-acesso-123"
})
@Import(TestcontainersConfig.class)
class AdminInicialIT {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void criaAdminInicialQuandoBancoVazio() {
		Usuario admin = usuarioRepository.findByEmailIgnoreCase("dono@barbearia.com").orElseThrow();

		assertThat(admin.getPerfil()).isEqualTo(Perfil.ADMIN);
		assertThat(admin.isAtivo()).isTrue();
		assertThat(passwordEncoder.matches("primeiro-acesso-123", admin.getSenhaHash())).isTrue();
		assertThat(admin.getSenhaHash()).doesNotContain("primeiro-acesso-123");
	}
}
