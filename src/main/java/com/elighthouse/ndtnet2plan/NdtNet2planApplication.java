package com.elighthouse.ndtnet2plan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@ComponentScan(basePackages = {"com.elighthouse.ndtnet2plan"})
public class NdtNet2planApplication {

	public static void main(String[] args) {
		SpringApplication.run(NdtNet2planApplication.class, args);
	}

}

@OpenAPIDefinition(
        info = @Info(
                title = "Network Digital Twin API",
                version = "0.5.0",
                description = "Network Digital Twin (NDT) based in Net2Plan uisng IETF data models. This API allows to interact with the NDT to perform different operations such as KPI evaluation, check network design, evaluate network changes, etc.",
                contact = @Contact(name = "Enrique Fernandez", email = "efernandez@e-lighthouse.com")
        		)
)

class OpenAPIConfiguration {
}
