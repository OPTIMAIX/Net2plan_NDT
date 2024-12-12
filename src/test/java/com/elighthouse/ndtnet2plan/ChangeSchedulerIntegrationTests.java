package com.elighthouse.ndtnet2plan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ChangeSchedulerIntegrationTests {

	@Autowired
    private TestRestTemplate restTemplate;

	@Test
    public void testGetCurrentTopology() throws Exception {
        ResponseEntity<String> response_current_topology = restTemplate.getForEntity("/current-topology", String.class);
        assertEquals(HttpStatus.OK, response_current_topology.getStatusCode());
        
        System.out.println(response_current_topology.getBody());
    }
	
    @Test
    public void testPostSimpleKpiEvaluation() throws Exception {
        String jsonContent = new String(Files.readAllBytes(Paths.get("src/main/resources/simple_ndt_request.json")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonContent, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/kpi-evaluation", request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Additional assertions can be added here to check the content of the response
        
        System.out.println(response.getBody());
        
        ResponseEntity<String> response_current_topology = restTemplate.getForEntity("/current-topology", String.class);
        assertEquals(HttpStatus.OK, response_current_topology.getStatusCode());
        
        System.out.println(response_current_topology.getBody());
    }
    
    @Test
    public void testPostSimpleKpiEvaluationWithLosses() throws Exception {
        String jsonContent = new String(Files.readAllBytes(Paths.get("src/main/resources/simple_ndt_request_with_losses.json")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonContent, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/kpi-evaluation", request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Additional assertions can be added here to check the content of the response
        
        System.out.println(response.getBody());
        
        ResponseEntity<String> response_current_topology = restTemplate.getForEntity("/current-topology", String.class);
        assertEquals(HttpStatus.OK, response_current_topology.getStatusCode());
        
        System.out.println(response_current_topology.getBody());
    }
    
    @Test
    public void testPostChangeSchedulerKpiEvaluationWithLosses() throws Exception {
        String jsonContent = new String(Files.readAllBytes(Paths.get("src/main/resources/change_scheduler_ndt_req.json")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonContent, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/kpi-evaluation", request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Additional assertions can be added here to check the content of the response
        
        System.out.println(response.getBody());
        
        ResponseEntity<String> response_current_topology = restTemplate.getForEntity("/current-topology", String.class);
        assertEquals(HttpStatus.OK, response_current_topology.getStatusCode());
        
        System.out.println(response_current_topology.getBody());
    }
}
