package com.barbearia.usuario.dto;

import com.barbearia.usuario.Perfil;
import com.barbearia.usuario.Usuario;

public record UsuarioLogadoResponse(Long id, String nome, String email, Perfil perfil) {

	public static UsuarioLogadoResponse de(Usuario usuario) {
		return new UsuarioLogadoResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(),
				usuario.getPerfil());
	}
}
