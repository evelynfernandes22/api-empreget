package com.empreget.api.dto.input;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioSemSouClienteInput  extends UsuarioInput{

	/*
	 * Para atualizar email e senha, sem alterar o tipo 
	 * do usuário "cliente ou não". 
	 */
	
	@Email
	@NotBlank
	private String email;

	@NotBlank
	private String senha;
	
}
