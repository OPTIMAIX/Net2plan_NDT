package com.elighthouse.ndtnet2plan.models;

import com.elighthouse.ndtnet2plan.models.ChangeSchedulerRequest.CurrentTopology;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class ChangeSchedulerResponse {
	@JsonProperty("previous_topology")
	public CurrentTopology previousTopology;
	
	@JsonProperty("current_topology") 
	public CurrentTopology currentTopology;
	
	@JsonProperty("expected_kpis")
	public KpisInDesignPerPairNodes kpis;
	
	public ChangeSchedulerResponse() {
		this.currentTopology = new CurrentTopology();
		this.kpis = new KpisInDesignPerPairNodes();
	}
}
