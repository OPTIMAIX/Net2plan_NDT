package com.elighthouse.ndtnet2plan.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.elighthouse.ndtnet2plan.models.ChangeSchedulerRequest;
import com.elighthouse.ndtnet2plan.models.ChangeSchedulerRequest.CurrentTopology;
import com.elighthouse.ndtnet2plan.models.ChangeSchedulerResponse;
import com.elighthouse.ndtnet2plan.services.NdtService;
import com.net2plan.interfaces.networkDesign.NetPlan;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/")
public class NdtNet2Plan {

	private Logger logger = LoggerFactory.getLogger(NdtNet2Plan.class);
	
	private NetPlan updatedNp;
	private String timeOfUpdatedNp;
	
	@Autowired
	private NdtService ndtService;
	
	@Operation(summary = "Get Current Topology", description = "Get Current Topology")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = CurrentTopology.class)) }),
            @ApiResponse(responseCode = "422", description = "Unprocessable entity", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))})})
	@GetMapping("/current-topology")
	public ResponseEntity<?> getCurrentTopology() {
		logger.info("Getting current topology...");
		if (updatedNp == null) {
			logger.warn("Current Topology is not available");
			return ResponseEntity.unprocessableEntity().body("Current Topology is not available");
		}
		
		final CurrentTopology currentTopology = ndtService.getSerializedNetworkFromNDTNetPlan(updatedNp, timeOfUpdatedNp);
		logger.info("Returning topology of time: " + timeOfUpdatedNp + " with " + currentTopology.ietfNetworks.network.get(0).node.size() + " nodes and " + currentTopology.ietfNetworks.network.get(0).link.size() + " links.");
		return ResponseEntity.ok(currentTopology);
	}
	
	@Operation(summary = "Change Scheduler KPI Evaluation", description = "Change Scheduler KPI Evaluation")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = ChangeSchedulerResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad request", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = String.class))
		})})
	@PostMapping("/kpi-evaluation")
	public ResponseEntity<?> ChangeSchedulerKpiEvaluation(@RequestBody ChangeSchedulerRequest request) {
		logger.info("Request received to evaluate network design...");
		final NetPlan np = new NetPlan(); // Current Topology

		// Update the current topology
		logger.info("Updating the current topology...");
		if (request.currentTopology.ietfNetworks == null) {
			logger.error("The ietf-network is null");
			return ResponseEntity.badRequest().body("The ietf-network is null");
		}
		
		if (request.currentTopology.ietfNetworks.network.size() != 1) {
			logger.error("The current topology should have only one network");
			return ResponseEntity.badRequest().body("The current topology should have only one network");
		}
		
		logger.info("Importing IETF network...");
		this.updatedNp = ndtService.updateCurrentTopology(np, request.currentTopology.ietfNetworks.network.get(0));
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // customize the pattern as needed
		timeOfUpdatedNp = LocalDateTime.now().format(formatter);
		logger.info("Current topology updated...");
		
        // Apply the traffic matrix
		logger.info("Applying the traffic matrix...");
        this.updatedNp = ndtService.applyTrafficToDesignByNodeId(updatedNp, request.currentTopology.traffic);
        logger.info("Traffic matrix applied...");
        
        // Print resume of nodes and links
        System.out.println("Before applying the actions...");
        ndtService.printDesignState(updatedNp);
        //ndtService.printResumeOfDemands(updatedNp);
        
        // Perform the actions
        logger.info("Applying the TVR actions...");
        this.updatedNp = ndtService.applyTvrActions(updatedNp, request.topologySchedule);
        logger.info("TVR actions applied...");
        
        // Print resume of nodes and links
        System.out.println("After applying the actions...");
        ndtService.printDesignState(updatedNp);
        
        // Simulate the design with routes and demands
        logger.info("Simulating the design with routes and demands...");
        this.updatedNp = ndtService.simulateRouting(updatedNp);
        logger.info("Design simulated...");
        
		// Evaluate the new kpis
		logger.info("Getting the new KPIs from the design...");
        final ChangeSchedulerResponse response = new ChangeSchedulerResponse();
        response.previousTopology = request.currentTopology;
		response.currentTopology = ndtService.getSerializedNetworkFromNDTNetPlan(updatedNp, timeOfUpdatedNp);
		response.kpis = ndtService.getNewKpisFromDesign(updatedNp);
		logger.info("New KPIs obtained...");
        
		// Print the resume of demands
		ndtService.printResumeOfDemands(updatedNp);
		
		// Return KPIs!
		logger.info("Returning the new KPIs and the current topology...");
		return ResponseEntity.ok(response);	
	}
}
