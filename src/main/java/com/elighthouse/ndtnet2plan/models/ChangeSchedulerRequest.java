package com.elighthouse.ndtnet2plan.models;

import java.util.List;

import com.elighthouse.ndtnet2plan.models.IetfTvrTopologySchedule.TopologyScheduleContent;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class ChangeSchedulerRequest {
	@JsonProperty("current_topology") 
	public CurrentTopology currentTopology;
	
	@JsonProperty("ietf-tvr-topology:topology-schedule")
	public TopologyScheduleContent topologySchedule;
	
	@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
	public static class CurrentTopology{
		@JsonProperty("ietf-network:networks") 
		public IetfNetworks ietfNetworks;
		
		@JsonProperty("traffic_demand")
		public List<KpiMetricGivenNodeId> traffic;
		
		@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
		public class IetfNetworks {
			@JsonProperty("network") public List<IetfNetwork> network;
			
			@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
			public static class IetfNetwork {
				//@JsonProperty("network-types") public List<String> networkTypes;
				@JsonProperty("network-id") public String networkId;
				@JsonProperty("node") public List<IetfNode> node;
				@JsonProperty("ietf-network-topology:link") public List<IetfLink> link;
				
				@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
				public static class IetfNode {
					@JsonProperty("node-id") public String nodeId;
				    //@JsonProperty("termination-point") public List<IetfTerminationPoint> terminationPoint;
				    @JsonProperty("ietf-l3-unicast-topology:l3-node-attributes") public IetfL3UnicastTopologyNodeAttributes nodeAttributes;
				
				    @JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
					public static class IetfL3UnicastTopologyNodeAttributes {
				    	//@JsonProperty("name") public String name;
				    	@JsonProperty("router-id") public String routerId;
				    	@JsonProperty("termination-point") public List<IetfTerminationPoint> terminationPoint;
				    	@JsonProperty("prefix") public List<String> prefix;
				    	@JsonProperty("state") public String state;
				    }
				}

				@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
				public static class IetfLink {
					@JsonProperty("link-id") public String linkId;
					@JsonProperty("source") public Source source;
					@JsonProperty("destination") public Destination destination;
					@JsonProperty("ietf-l3-unicast-topology:l3-link-attributes") public IetfL3UnicastTopologyLinkAttributes linkAttributes;
					
					public static class IetfL3UnicastTopologyLinkAttributes {
						@JsonProperty("routingcost") public int routingCost;
						@JsonProperty("latency") public int latency;
						@JsonProperty("bandwidth") public int bandwidth;
						@JsonProperty("state") public String state;
						@JsonProperty("tefsdn-topology:domain-id") public String domainId;
						@JsonProperty("tefsdn-topology:link-attributes") public TefSdnTopologyLinkAttributes tefLinkAttributes;
					}
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
				
				@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
				public static class TefSdnTopologyLinkAttributes {
					@JsonProperty("level") public int level;
					
					public TefSdnTopologyLinkAttributes() {}
				}
			}
		}
	}
}