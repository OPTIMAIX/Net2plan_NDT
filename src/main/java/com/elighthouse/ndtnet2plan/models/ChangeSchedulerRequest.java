package com.elighthouse.ndtnet2plan.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class ChangeSchedulerRequest {
	@JsonProperty("current_topology") public CurrentTopology ietfNetworks;
	@JsonProperty("actions") public List<Action> actions;
	@JsonProperty("time") public String time;
	@JsonProperty("kpis") public KpiRequest kpis;
	@JsonProperty("traffic_matrix") public List<List<Integer>> trafficMatrix;
	
	@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
	public static class CurrentTopology{
		@JsonProperty("ietf-network:networks") public IetfNetworks ietfNetworks;
		
		@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
		public class IetfNetworks {
			@JsonProperty("network") public List<IetfNetwork> network;
			
			@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
			public static class IetfNetwork {
//				@JsonProperty("network-types") public List<String> networkTypes;
				@JsonProperty("network-id") public String networkId;
				@JsonProperty("node") public List<IetfNode> node;
				@JsonProperty("ietf-network-topology:link") public List<IetfLink> link;
				
				@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
				public static class IetfNode {
					@JsonProperty("node-id") public String nodeId;
				    @JsonProperty("termination-point") public List<IetfTerminationPoint> terminationPoint;
				}

				@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
				public static class IetfLink {
					@JsonProperty("link-id") public String linkId;
					@JsonProperty("source") public Source source;
					@JsonProperty("destination") public Destination destination;
//					public String status;
//					public int latency;
				}

				@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
				public static class IetfTerminationPoint {
					@JsonProperty ("tp-id") public String tpId;
				
					public IetfTerminationPoint() {}
					
					public IetfTerminationPoint(String name) {
						this.tpId = name;
					}
				}

				@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
				public static class Source {
					@JsonProperty("source-node") public String sourceNode;
					@JsonProperty("source-tp") public String sourceTp;
					
					public Source(String sourceNode, String sourceTp) {
						this.sourceNode = sourceNode;
						this.sourceTp = sourceTp;
					}
				}

				@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
				public static class Destination {
					@JsonProperty("dest-node") public String destNode;
					@JsonProperty("dest-tp") public String destTp;
					
					public Destination(String destNode, String destTp) {
						this.destNode = destNode;
						this.destTp = destTp;
					}
				}
			}
		}
	}
	
	@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
	public static class Action {
		@JsonProperty("action_type")
		public String actionType;
		
		@JsonProperty("element")
		public String element;
		
		@JsonProperty("ref")
		public String ref;
	}
	
	@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
	public static class KpiRequest{
		@JsonProperty("latency") public List<List<Integer>> latency;
		@JsonProperty("bandwidth") public List<List<Integer>> bandwidth;
	}
}