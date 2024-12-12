package com.elighthouse.ndtnet2plan.models;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class KpisInDesignPerPairNodes {
	@JsonProperty("offered_traffic")
	public List<KpiMetricGivenNodeId> offeredTraffictInGbps;

	@JsonProperty("carried_traffic")
	public List<KpiMetricGivenNodeId> carriedTrafficInGbps;
	
	@JsonProperty("blocked_traffic")
	public List<KpiMetricGivenNodeId> blockedTrafficInGbps;
	
	@JsonProperty("lost_traffic")
	public List<KpiMetricGivenNodeId> lostTrafficInGbps;
	
	@JsonProperty("latency_demand")
	public List<KpiMetricGivenNodeId> latencyDemandInMs;

	public KpisInDesignPerPairNodes() {
		this.offeredTraffictInGbps = new ArrayList<>();
		this.carriedTrafficInGbps = new ArrayList<>();
		this.blockedTrafficInGbps = new ArrayList<>();
		this.lostTrafficInGbps = new ArrayList<>();
		this.latencyDemandInMs = new ArrayList<>();
	}

	public void addOfferedTraffic(KpiMetricGivenNodeId kpi) {
		this.offeredTraffictInGbps.add(kpi);
	}

	public void addCarriedTraffic(KpiMetricGivenNodeId kpi) {
		this.carriedTrafficInGbps.add(kpi);
	}

	public void addBlockedTraffic(KpiMetricGivenNodeId kpi) {
		this.blockedTrafficInGbps.add(kpi);
	}

	public void addLostTraffic(KpiMetricGivenNodeId kpi) {
		this.lostTrafficInGbps.add(kpi);
	}
	
	public void addLatencyDemand(KpiMetricGivenNodeId kpi) {
		this.latencyDemandInMs.add(kpi);
	}
}
