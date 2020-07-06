package org.comeia.project.enumerator;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFilter("attributeFilter")
public enum DocumentType {

	txt(".txt"),
	pdf(".pdf"),
	docx(".docx"),
	png(".png"),
	jpeg(".jpeg");
	
	public final String displayType;
	
	private DocumentType(String displayType) {
		this.displayType = displayType;
	}
	
	@JsonCreator
	public static DocumentType forValue(String value) {
		return DocumentType.valueOf(value);
	}
	
	@JsonValue
	public HashMap<String, String> jsonValue() {
		HashMap<String, String> map = new HashMap<>();
		map.put("name", this.name());
		map.put("displayType", this.displayType);
		return map;
	}
}
