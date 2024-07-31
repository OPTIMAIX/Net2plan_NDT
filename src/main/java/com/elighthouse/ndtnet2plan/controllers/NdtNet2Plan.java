package com.elighthouse.ndtnet2plan.controllers;

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
import com.elighthouse.ndtnet2plan.models.KpisInDesign;
import com.elighthouse.ndtnet2plan.services.NdtService;
import com.elighthouse.ndtnet2plan.services.UtilsService;
import com.net2plan.interfaces.networkDesign.NetPlan;

@RestController
@RequestMapping("/")
public class NdtNet2Plan {

	private Logger logger = LoggerFactory.getLogger(NdtNet2Plan.class);
	
	private NetPlan updatedNp;
	private String timeOfUpdatedNp;
	
	@Autowired
	private NdtService ndtService;
	
	@Autowired
	private UtilsService utilsService;
	
	@GetMapping("/current-topology")
	public ResponseEntity<?> getCurrentTopology() {
		if (updatedNp == null) {
			return ResponseEntity.unprocessableEntity().body("Current Topology is not available");
		}
		
		final CurrentTopology currentTopology = ndtService.getSerializedNetworkFromNDTNetPlan(updatedNp, timeOfUpdatedNp);
		System.out.println("Returning topology of time: " + timeOfUpdatedNp + " with " + currentTopology.ietfNetworks.network.get(0).node.size() + " nodes and " + currentTopology.ietfNetworks.network.get(0).link.size() + " links.");
		return ResponseEntity.ok(currentTopology);
	}
	
	@PostMapping("/kpi-evaluation")
	public ResponseEntity<?> ChangeSchedulerKpiEvaluation(@RequestBody ChangeSchedulerRequest request) {
		final NetPlan np = new NetPlan(); // Current Topology
		 
		// Update the current topology
		this.updatedNp = ndtService.updateCurrentTopology(np, request.ietfNetworks.ietfNetworks.network.get(0));
		timeOfUpdatedNp = request.time;
		
		 // Apply the current given kpis to the topology
        this.updatedNp = ndtService.applyKpisToDesign(np, 
				utilsService.convertListToDoubleArray(request.kpis.latency), 
				utilsService.convertListToDoubleArray(request.kpis.bandwidth));
		
        // Apply the traffic matrix
        this.updatedNp = ndtService.applyTrafficMatrixToDesign(updatedNp, 
        		utilsService.convertListToDoubleArray(request.trafficMatrix));
        
        // Perform the actions
        this.updatedNp = ndtService.applyActions(updatedNp, request.actions);
        
        // Simulate the design with routes and demands
        this.updatedNp = ndtService.simulateRouting(updatedNp);
        
        // Evaluate the new kpis
		final KpisInDesign newKpis = ndtService.getKpisFromDesign(updatedNp);
		
		// Return KPIs!
		//final KpiEvaluationResponse res = new KpiEvaluationResponse();
		return ResponseEntity.ok(newKpis);	
	}
}
