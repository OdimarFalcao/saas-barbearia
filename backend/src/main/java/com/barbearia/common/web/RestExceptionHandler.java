package com.barbearia.common.web;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Map<String, Object> tratarValidacao(MethodArgumentNotValidException ex) {
		Map<String, String> campos = new HashMap<>();
		for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
			campos.put(erro.getField(), erro.getDefaultMessage());
		}
		return Map.of("mensagem", "Dados inválidos", "campos", campos);
	}

	// mensagem única para senha errada, usuário inexistente ou inativo
	// (evita enumeração de contas)
	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public Map<String, Object> tratarAutenticacao() {
		return Map.of("mensagem", "Credenciais inválidas");
	}
}
