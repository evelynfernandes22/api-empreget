package com.empreget.domain.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.empreget.domain.exception.NegocioException;
import com.empreget.domain.exception.UsuarioNaoEncontradoException;
import com.empreget.domain.model.Usuario;
import com.empreget.domain.repository.UsuarioRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class CadastroUsuarioService {

	private UsuarioRepository usuarioRepository;

	@Transactional
	public Usuario salvar(Usuario usuario) {
		boolean emailEmUso = usuarioRepository.findByEmail(usuario.getEmail()).stream()
				.anyMatch(clienteExistente -> !clienteExistente.equals(usuario));

		if (emailEmUso) {
			throw new NegocioException(String.format("Já existe um usuário cadastrado com o e-mail %s.", usuario.getEmail()));
		}
				
		return usuarioRepository.save(usuario);
	}

	@Transactional
	public void alterarSenha(Long usuarioId, String senhaAtual, String novaSenha) {
		Usuario usuario = buscarOuFalhar(usuarioId);
		if(usuario.senhaNaoCoincidemCom(senhaAtual)) {
			throw new NegocioException("Senha atual informada não coincide com a senha do usuário");
		}
		usuario.setSenha(novaSenha);
	}
	
		
	public Usuario buscarOuFalhar(Long usuarioId) {
		return usuarioRepository.findById(usuarioId)
				.orElseThrow(() -> new UsuarioNaoEncontradoException(usuarioId));
	}
	
	
	@Transactional
	public Usuario verificarCredenciais(String email, String senha) {
		List<Usuario> usuarios = usuarioRepository.findAll();

		for (Usuario usuario : usuarios) {
			if (usuario.getEmail().equals(email) && usuario.getSenha().equals(senha)) {
				return usuario;
			}
		}

		throw new NegocioException("E-mail ou senha não cadastrados, ou inválidos. Tente novamente.");
	}
	
	@Transactional
	 public void entrarNoAmbiente(Usuario usuario, boolean souCliente) {

	        if (souCliente) {
	            System.out.println(String.format("Entrando no ambiente de prestador com o e-mail: %s ", usuario.getEmail()));
	        } else {
	            System.out.println(String.format("Entrando no ambiente de cliente com o e-mail: %s ", usuario.getEmail()));
	        }
	    }
}
