package com.elighthouse.ndtnet2plan.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class OaasDefaultController {

	private Logger logger = LoggerFactory.getLogger(OaasDefaultController.class);

	@GetMapping("/")
	public String home() {
		return "Welcome to Algorithm Repository for NDT Net2Plan! Use /docs to see the API documentation.";
	}

	@PostMapping("/callback/{id}")
	public String callback(@PathVariable("id") String id, String content) {
		logger.info("Callback " + id + ". Content: " + content);
		return content;
	}
}
