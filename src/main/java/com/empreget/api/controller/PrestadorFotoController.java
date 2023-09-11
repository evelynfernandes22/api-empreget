package com.empreget.api.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.validation.Valid;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.empreget.api.assembler.FotoPrestadorDtoAssembler;
import com.empreget.api.dto.FotoPrestadorResponse;
import com.empreget.api.dto.input.FotoPrestadorInput;
import com.empreget.domain.exception.EntidadeNaoEncontradaException;
import com.empreget.domain.model.FotoPrestador;
import com.empreget.domain.model.Prestador;
import com.empreget.domain.service.CatalogoPrestadorFotoService;
import com.empreget.domain.service.CatalogoPrestadorService;
import com.empreget.domain.service.FotoStorageService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;

@Api(tags = "Foto")
@AllArgsConstructor
@RestController
@RequestMapping("/prestadores/{prestadorId}/foto")
public class PrestadorFotoController {

	private CatalogoPrestadorService catalogoPrestadorService;
	private CatalogoPrestadorFotoService catalogoPrestadorFotoService;
	private FotoPrestadorDtoAssembler fotoPrestadorDtoAssembler;
	private FotoStorageService fotoStorageService;
		
	@ApiOperation("Atualiza foto do prestador")
	@PreAuthorize("@acessoService.verificarAcessoProprioPrestador(#prestadorId)")
	@PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public FotoPrestadorResponse atualizarFoto(@ApiParam(value = "Id de um prestador") @PathVariable Long prestadorId,
			@Valid FotoPrestadorInput fotoPrestadorInput) throws IOException {

		Prestador prestador = catalogoPrestadorService.buscarOuFalhar(prestadorId);
		MultipartFile arquivo = fotoPrestadorInput.getArquivo();
		
		
		FotoPrestador fotoPrestador = new FotoPrestador();
		fotoPrestador.setNomeArquivo(arquivo.getOriginalFilename());
		fotoPrestador.setContentType(arquivo.getContentType());
		fotoPrestador.setTamanho(arquivo.getSize());
		fotoPrestador.setPrestador(prestador);
		
		prestador.setImgUrl(arquivo.getOriginalFilename());
		
		FotoPrestador fotoSalva = catalogoPrestadorFotoService.salvar(fotoPrestador,arquivo.getInputStream());
	
		return fotoPrestadorDtoAssembler.toModel(fotoSalva);	
	}
	
	@ApiOperation("Busca foto por prestador")
	@PreAuthorize("@acessoService.verificarAcessoProprioPrestador(#prestadorId) or hasAnyRole('ADMIN', 'CLIENTE')")
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public FotoPrestadorResponse buscar(@ApiParam(value = "Id de um prestador") @PathVariable Long prestadorId) {
		return fotoPrestadorDtoAssembler.toModel(catalogoPrestadorFotoService
				.buscarOuFalhar(prestadorId));
	}
	
	@ApiOperation("Mostra a foto do prestador registrada no sistema")
	@PreAuthorize("@acessoService.verificarAcessoProprioPrestador(#prestadorId) or hasAnyRole('ADMIN', 'CLIENTE')")
	@GetMapping
	public ResponseEntity<InputStreamResource> mostrarFoto(@ApiParam(value = "Id de um prestador") @PathVariable Long prestadorId, 
			@RequestHeader(name = "accept") String acceptHeader) throws HttpMediaTypeNotAcceptableException {
		
		try {
			FotoPrestador fotoPrestador = catalogoPrestadorFotoService
					.buscarOuFalhar(prestadorId);
			
			MediaType mediaTypeFoto = MediaType.parseMediaType(fotoPrestador.getContentType());
			List<MediaType> mediaTypesAceitas = MediaType.parseMediaTypes(acceptHeader);
			
			verificarCompatibilidadeMediaType(mediaTypeFoto, mediaTypesAceitas);
			
			InputStream inputStream = fotoStorageService.recuperar(fotoPrestador.getNomeArquivo());
			
			return ResponseEntity.ok()
					.contentType(mediaTypeFoto)
					.body(new InputStreamResource(inputStream));
		}catch(EntidadeNaoEncontradaException e) {
			return ResponseEntity.notFound().build();
		}
	}

	private void verificarCompatibilidadeMediaType(MediaType mediaTypeFoto, 
			List<MediaType> mediaTypesAceitas) throws HttpMediaTypeNotAcceptableException {
		
		boolean compativel = mediaTypesAceitas.stream()
						.anyMatch(MediaTypeAceita -> MediaTypeAceita.isCompatibleWith(mediaTypeFoto));
		
		if(!compativel) {
			throw new HttpMediaTypeNotAcceptableException(mediaTypesAceitas);
		}
	}
	
	@ApiOperation("Exclui foto do prestador")
	@DeleteMapping
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void excluir(@ApiParam(value = "Id de um prestador") @PathVariable Long prestadorId) {
		
		catalogoPrestadorFotoService.remover(prestadorId);
	}
}
