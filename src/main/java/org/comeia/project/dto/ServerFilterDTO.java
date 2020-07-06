package org.comeia.project.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.comeia.project.search.SearchCriteria;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("serial")
@EqualsAndHashCode(callSuper=false)
public @Data class ServerFilterDTO implements Serializable {
	
	private String login;
	private String password;
	private String url;
	
	public static List<SearchCriteria> buildCriteria(ServerFilterDTO filter) {
		List<SearchCriteria> criterias = new ArrayList<>();
		
		if(filter.getLogin() != null && !filter.getLogin().isEmpty()) { 
			criterias.add(new SearchCriteria("login", "%".concat(filter.getLogin()).concat("%")));
		}
		
		if(filter.getPassword() != null && !filter.getPassword().isEmpty()) { 
			criterias.add(new SearchCriteria("password", "%".concat(filter.getPassword()).concat("%")));
		}
		
		if(filter.getUrl() != null && !filter.getUrl().isEmpty()) { 
			criterias.add(new SearchCriteria("url", "%".concat(filter.getUrl()).concat("%")));
		}
		
		return criterias;
	}

}
