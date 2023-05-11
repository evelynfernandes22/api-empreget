package com.empreget.api.assembler;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.empreget.api.dto.ClienteResponse;
import com.empreget.domain.model.Cliente;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class ClienteAssembler {

	private ModelMapper modelMapper;
	
	public ClienteResponse toModel (Cliente cliente) {
		return modelMapper.map(cliente, ClienteResponse.class);
	}
	
	public List<ClienteResponse> toCollectionModel (List<Cliente> cliente){
		return cliente.stream()
				.map(this::toModel)
				.collect(Collectors.toList());
	}

}