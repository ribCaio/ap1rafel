package org.comeia.project.converter;

import static java.util.Optional.ofNullable;

import java.util.Objects;

import org.comeia.project.domain.Server;
import org.comeia.project.dto.ServerDTO;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ServerConverter implements Converter<Server, ServerDTO> {

	@Override
	public ServerDTO from(Server entity) {
		
		if(Objects.isNull(entity)) {
			return null;
		}
		
		ServerDTO dto = new ServerDTO();
		
		ofNullable(entity.getId())
			.ifPresent(dto::setId);
		
		ofNullable(entity.getLogin())
			.ifPresent(dto::setLogin);
		
		ofNullable(entity.getPassword())
			.ifPresent(dto::setPassword);
		
		ofNullable(entity.getUrl())
		.ifPresent(dto::setUrl);
		
		return dto;
	}
	
	@Override
	public Server to(ServerDTO dto, Server entity) {
		
		if(Objects.isNull(dto)) {
			return null;
		}

		if(Objects.isNull(entity)) {
			entity = new Server();
		}
		
		ofNullable(dto.getId())
		.ifPresent(entity::setId);
	
		ofNullable(dto.getLogin())
		.ifPresent(entity::setLogin);
	
		ofNullable(dto.getPassword())
		.ifPresent(entity::setPassword);
	
		ofNullable(dto.getUrl())
		.ifPresent(entity::setUrl);
		
		return entity;
	}
}
