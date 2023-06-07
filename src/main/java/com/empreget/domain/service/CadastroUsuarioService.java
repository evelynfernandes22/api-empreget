package com.empreget.domain.service;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.empreget.domain.exception.NegocioException;
import com.empreget.domain.exception.UsuarioNaoEncontradoException;
import com.empreget.domain.model.Grupo;
import com.empreget.domain.model.Usuario;
import com.empreget.domain.repository.UsuarioRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class CadastroUsuarioService {

	private UsuarioRepository usuarioRepository;
	private CadastroGrupoService cadastroGrupoService;

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
	
	@Transactional
	public void desassociarGrupo(Long usuarioId, Long grupoId) {
	    Usuario usuario = buscarOuFalhar(usuarioId);
	    Grupo grupo = cadastroGrupoService.buscarOuFalhar(grupoId);
	    
	    usuario.removerGrupo(grupo);
	}

	@Transactional
	public void associarGrupo(Long usuarioId, Long grupoId) {
	    Usuario usuario = buscarOuFalhar(usuarioId);
	    Grupo grupo = cadastroGrupoService.buscarOuFalhar(grupoId);
	    
	    usuario.adicionarGrupo(grupo);
	}
	
	public Usuario buscarOuFalhar(Long usuarioId) {
		return usuarioRepository.findById(usuarioId)
				.orElseThrow(() -> new UsuarioNaoEncontradoException(usuarioId));
	}
}
