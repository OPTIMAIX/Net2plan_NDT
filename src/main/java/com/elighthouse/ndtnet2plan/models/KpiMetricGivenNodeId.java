package com.elighthouse.ndtnet2plan.models;

import com.elighthouse.ndtnet2plan.models.KpiMetricGivenLinkId.Unit;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class KpiMetricGivenNodeId {
	@JsonProperty("source-node-id") public String sourceNodeId;
	@JsonProperty("destination-node-id") public String destinationNodeId;
	@JsonProperty("value") public double value;
	@JsonProperty("unit") public String unit;
	
	public KpiMetricGivenNodeId() {}
	
	public KpiMetricGivenNodeId(String sourceNodeId, String destinationNodeId, double value, String unit) {
		this.sourceNodeId = sourceNodeId;
		this.destinationNodeId = destinationNodeId;
		this.value = value;
		this.unit = unit;
	}
	
	public static KpiMetricGivenNodeId ofGbps(String sourceNodeId, String destinationNodeId, double value) {
		return new KpiMetricGivenNodeId(sourceNodeId, destinationNodeId, value, Unit.GBPS.toString());
	}
	
	public static KpiMetricGivenNodeId ofMs(String sourceNodeId, String destinationNodeId, double value) {
		return new KpiMetricGivenNodeId(sourceNodeId, destinationNodeId, value, Unit.MS.toString());
	}
}
