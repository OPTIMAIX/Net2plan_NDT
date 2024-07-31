package com.elighthouse.ndtnet2plan.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;


// TODO
// Maybe 
// {
//   "kpis": {},
//   "other-params": {}
// }


@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class KpiEvaluationResponse {
	@JsonProperty("ethernet-ports") public List<EthernetPort> ethernetPorts;
	@JsonProperty("ip-interface") public List<IpInterface> ipInterface;
	
	@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
	public static class EthernetPort {
		@JsonProperty("termination-point-id") public String terminationPointId;
		@JsonProperty("carried-traffic") public CarriedTraffic carriedTraffic;
		@JsonProperty("capacity") public double capacity;
		@JsonProperty("is-congested") public boolean isCongested;
		
		@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
		public static class CarriedTraffic {
			@JsonProperty("transmission-gbps") public double transmissionGbps;
			@JsonProperty("reception-gbps") public double receptionGbps;
		}
	}
	
	@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
	public static class IpInterface {
		@JsonProperty("termination-point-id") public String terminationPointId;
		@JsonProperty("ip-address") public String ipAddress;
		@JsonProperty("outgoing-traffic-gbps") public double outgoingTrafficGbps;
	}
}
