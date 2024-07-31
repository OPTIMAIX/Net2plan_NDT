package com.elighthouse.oaas.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRawValue;

@JsonInclude(JsonInclude.Include.NON_NULL) // Exclude null values from JSON
public class Model_CallbackMessage {
    private String type;
    @JsonRawValue // This tells Jackson to insert the JSON string directly
    private String content; // To store either dict (as JSON string) or plain text

    // Constructors, Getters, and Setters
	public Model_CallbackMessage() {
	}
}
