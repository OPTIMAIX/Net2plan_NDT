package com.elighthouse.oaas.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRawValue;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Model_CallbackMessage {
	private String type;
	
	@JsonRawValue
	private String content; 

	public Model_CallbackMessage() {
	}
}
