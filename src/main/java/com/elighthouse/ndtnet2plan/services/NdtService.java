package com.elighthouse.ndtnet2plan.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.elighthouse.ndtnet2plan.models.ChangeSchedulerRequest.CurrentTopology;
import com.elighthouse.ndtnet2plan.models.ChangeSchedulerRequest.CurrentTopology.IetfNetworks.IetfNetwork;
import com.elighthouse.ndtnet2plan.models.ChangeSchedulerRequest.CurrentTopology.IetfNetworks.IetfNetwork.Destination;
import com.elighthouse.ndtnet2plan.models.ChangeSchedulerRequest.CurrentTopology.IetfNetworks.IetfNetwork.IetfLink;
import com.elighthouse.ndtnet2plan.models.ChangeSchedulerRequest.CurrentTopology.IetfNetworks.IetfNetwork.IetfNode;
import com.elighthouse.ndtnet2plan.models.ChangeSchedulerRequest.CurrentTopology.IetfNetworks.IetfNetwork.IetfTerminationPoint;
import com.elighthouse.ndtnet2plan.models.ChangeSchedulerRequest.CurrentTopology.IetfNetworks.IetfNetwork.Source;
import com.elighthouse.ndtnet2plan.models.IetfTvrTopologySchedule;
import com.elighthouse.ndtnet2plan.models.IetfTvrTopologySchedule.TopologyScheduleContent;
import com.elighthouse.ndtnet2plan.models.KpiMetricGivenLinkId;
import com.elighthouse.ndtnet2plan.models.KpiMetricGivenNodeId;
import com.elighthouse.ndtnet2plan.models.KpisInDesignPerPairNodes;
import com.elighthouse.ndtnet2plan.utils.GraphUtils;
import com.net2plan.interfaces.networkDesign.Demand;
import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.NetworkLayer;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.utils.Pair;

import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;

@Service
public class NdtService {

	public NetPlan updateCurrentTopology(NetPlan np, IetfNetwork network) {
		np.removeAllNodes();
		np.removeAllLinks();
		np.removeAllDemands();
		NetworkLayer layer = np.getNetworkLayerDefault();
		
		Map<String, Node> nodeMap = new HashMap<>();
		for (IetfNode nodeData : network.node) {
	        Node node = np.addNode(0.0, 0.0, nodeData.nodeId, null);
	        
	        if (nodeData.nodeAttributes == null) {
	        	System.err.println("Node attributes not found for node " + nodeData.nodeId + ", skipping");
	        	continue;
	        }
	        
			if (nodeData.nodeAttributes.terminationPoint == null) {
				System.err.println("Termination points not found for node " + nodeData.nodeId + ", skipping");
				continue;
			}
	        
	        for (IetfTerminationPoint tpData : nodeData.nodeAttributes.terminationPoint) {
	            node.addTag(tpData.tpId);
	            nodeMap.put(nodeData.nodeId, node);
	        }
	    }
		
		for (IetfLink linkData : network.link) {
	        Node sourceNode = nodeMap.get(linkData.source.sourceNode);
	        Node destinationNode = nodeMap.get(linkData.destination.destNode);
	        Link l = np.addLink(sourceNode, destinationNode, 1000.0, 0, 1000.0, null, layer);
		    l.setName(linkData.linkId);
		    
		    l.setAttribute("identifier", linkData.linkId);
		    l.setAttribute("sourceTerminationPoint", linkData.source.sourceTp);
	        l.setAttribute("destinationTerminationPoint", linkData.destination.destTp);
	        if (linkData.linkAttributes != null) {
	        	// Check for the state of the link
	        	if (linkData.linkAttributes.state != null) {
	        		
	        	}
	        	
	        	// Check for latency 
				if (linkData.linkAttributes.latency != 0) {
					int latency = linkData.linkAttributes.latency;	// In Ms
					setLinkDistanceForLatency(l, latency);
				}
	        	
	        	// Check for bandwidth
				if (linkData.linkAttributes.bandwidth != 0) {
					int bandwidth = linkData.linkAttributes.bandwidth;	// In Gbps
					l.setCapacity(bandwidth);
				}
	        }
	    }
		
		return np;
	}
	
	public void printDesignState(NetPlan np) {
		System.out.println("-- Nodes: ");
		for (Node node : np.getNodes()) {
			System.out.println("   Node: " + node.getName() + ", state: " + (node.isUp() ? "UP" : "DOWN"));
		}
		
		System.out.println("-- Links: ");
		for (Link link : np.getLinks()) {
			System.out.println("  Link: " + link.getAttribute("identifier") + ", state: " + (link.isUp() ? "UP" : "DOWN") + ", capacity: " + link.getCapacity() + " Gbps, latency: " + getLatencyInMs(link)+ " ms");
		}
	}
	
	public void printResumeOfDemands(NetPlan np) {
		// The idea is to go for each demand and print the offered, carried, blocked and lost traffic
		for (Demand demand : np.getDemands()) {
			System.out.println("Demand: " + demand.getIndex() + " (" + demand.getIngressNode().getName() + "-" + demand.getEgressNode().getName() + "), offered: " + demand.getOfferedTraffic()
					+ ", carried: " + demand.getCarriedTraffic() + ", blocked: " + demand.getBlockedTraffic());
		}
	}

	public NetPlan applyTvrActions(NetPlan np, TopologyScheduleContent tvrContent) {
		final NetPlan afterActions = np.copy();
		if (tvrContent == null) {
			return afterActions;
		}
		
		if (tvrContent.nodes != null) {
			for (IetfTvrTopologySchedule.Node node : tvrContent.nodes) {
				Node n = afterActions.getNodeByName(node.nodeId);
				if (n != null) {
					boolean changeTostate = node.available.defaultNodeAvailable;
					System.out.println("Setting link " + node.nodeId + " to " + changeTostate);
					this.changeNodeState(afterActions, node.nodeId, changeTostate);
				}
			}
		}
		
		if (tvrContent.links != null) {
			for (IetfTvrTopologySchedule.Link link : tvrContent.links) {
				Link l = this.findLinkByLinkIdIdentifier(np, link.sourceLinkId);
				if (l != null) {
					boolean changeTostate = link.available.defaultLinkAvailable;
					System.out.println("Setting link " + link.sourceLinkId + " to " + changeTostate);
					this.changeLinkState(afterActions, link.sourceLinkId, changeTostate);
					Link bidiPair = this.findLinkBetweenNodes(afterActions, l.getDestinationNode(), l.getOriginNode());
					if (bidiPair != null) {
						System.out.println("Setting link " + bidiPair.getAttribute("identifier") + " to " + changeTostate);
						this.changeLinkState(afterActions, bidiPair.getAttribute("identifier"), changeTostate);
					}
				}
			}
		}
		
		return afterActions;
	}
	
	public void changeLinkState(NetPlan np, String linkId, boolean state) {
		Link link = findLinkByLinkIdIdentifier(np, linkId);
		if (link != null) {
			link.setFailureState(state);
		}
	}
	
	public void changeNodeState(NetPlan np, String nodeId, boolean state) {
		Node node = np.getNodeByName(nodeId);
		if (node != null) {
			node.setFailureState(state);
		}
	}
	
	public NetPlan applyLatencyAndBandwidthGivenLinkIds(NetPlan np, 
			List<KpiMetricGivenLinkId> latency, List<KpiMetricGivenLinkId> bandwidth) {
		// For each element in each list, find the link by identifier and apply the value.
		// Latency, in Ms
		if (latency != null) {
			for (KpiMetricGivenLinkId metric : latency) {
				Link link = findLinkByLinkIdIdentifier(np, metric.linkId);
				if (link != null) {
					setLinkDistanceForLatency(link, metric.value);
				}
			}
		}
		
		// Bandwidth, in Gbps
		if (bandwidth != null) {
			for (KpiMetricGivenLinkId metric : bandwidth) {
				Link link = findLinkByLinkIdIdentifier(np, metric.linkId);
				if (link != null) {
					link.setCapacity(metric.value);
				}
			}
		}
		
		return np;
	}
	
	public NetPlan applyKpisToDesign(NetPlan np, double[][] latencyMatrix, double[][] bandwidthMatrix) {
		List<Node> nodes = np.getNodes();
		if (latencyMatrix.length != nodes.size() || latencyMatrix[0].length != nodes.size()) {
			throw new RuntimeException("Invalid latency matrix size");
		}
		
		for (int i = 0; i < nodes.size(); i++) {
			for (int j = 0; j < nodes.size(); j++) {
				if (i != j) {
					Node originNode = nodes.get(i);
					Node destinationNode = nodes.get(j);
					Optional<Link> link = originNode.getOutgoingLinks().stream()
							.filter(l -> l.getDestinationNode().equals(destinationNode)).findFirst();
					if (link.isPresent()) {
						link.get().setCapacity(bandwidthMatrix[i][j]);
						setLinkDistanceForLatency(link.get(), latencyMatrix[i][j]);
					}
				}
			}
		}
		
		return np;
	}
	
	public KpisInDesignPerPairNodes getNewKpisFromDesign(NetPlan np) {
		KpisInDesignPerPairNodes kpis = new KpisInDesignPerPairNodes();
        List<Node> nodes = np.getNodes();
        int size = nodes.size();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i != j) { // No self-demands
                    Node origin = nodes.get(i);
                    Node destination = nodes.get(j);
                    Demand demand = findDemand(np, origin, destination);

                    if (demand != null) {
                        double offeredTraffic = demand.getOfferedTraffic();
                        double totalCarriedTraffic = 0.0;
                        double totalCapacity = 0.0;
                        double totalLatency = 0.0;
                        
                        double latencyEndToEndInMs = demand.getWorstCasePropagationTimeInMs();
                        kpis.addLatencyDemand(KpiMetricGivenNodeId.ofMs(origin.getName(), destination.getName(), latencyEndToEndInMs));

                        // Correctly retrieve and iterate over forwarding rules
                        SortedMap<Pair<Demand, Link>, Double> forwardingRules = np.getForwardingRules();
                        for (Map.Entry<Pair<Demand, Link>, Double> entry : forwardingRules.entrySet()) {
                            Pair<Demand, Link> pair = entry.getKey();
                            if (pair.getFirst().equals(demand)) {
                                Link link = pair.getSecond();
                                double fraction = entry.getValue();
                                
                                double carriedTraffic = offeredTraffic * fraction;
                                totalCarriedTraffic += carriedTraffic;
                                totalCapacity += link.getCapacity() * fraction;
                                totalLatency += link.getPropagationDelayInMs() * fraction;
                                
                                double blockedTrafficInGbps = offeredTraffic > totalCarriedTraffic ? offeredTraffic - totalCarriedTraffic : 0;
                                double lostTrafficInGbps = offeredTraffic > totalCarriedTraffic ? offeredTraffic - totalCarriedTraffic : 0;
                                
                                // Add each KPI to the corresponding list in KpisInDesignPerLink
                                kpis.addOfferedTraffic(KpiMetricGivenNodeId.ofGbps(origin.getName(), destination.getName(), offeredTraffic));
                                kpis.addCarriedTraffic(KpiMetricGivenNodeId.ofGbps(origin.getName(), destination.getName(), totalCarriedTraffic));
                                kpis.addBlockedTraffic(KpiMetricGivenNodeId.ofGbps(origin.getName(), destination.getName(), blockedTrafficInGbps));
                                kpis.addLostTraffic(KpiMetricGivenNodeId.ofGbps(origin.getName(), destination.getName(), lostTrafficInGbps));
                            }
                        }
                    }
                }
            }
        }
        return kpis;
    }
	
	public NetPlan applyTrafficToDesignByNodeId(NetPlan np, List<KpiMetricGivenNodeId> traffic) {
		if (traffic != null) {
			for (KpiMetricGivenNodeId metric : traffic) {
				final Node source = np.getNodeByName(metric.sourceNodeId);
				final Node destination = np.getNodeByName(metric.destinationNodeId);
				final double offeredTraffic = metric.value;
				Demand demand = findDemand(np, source, destination);
	            if (demand == null) {
	                np.addDemand(source, destination, offeredTraffic, null, null);
	            } else {
	                demand.setOfferedTraffic(offeredTraffic);
	            }
			}
		}
		
		return np;
	}

	public NetPlan applyTrafficMatrixToDesign(NetPlan np, double[][] traffixMatrix) {
		List<Node> nodes = np.getNodes();
		if (traffixMatrix.length != nodes.size()) {
			throw new RuntimeException("Invalid latency matrix size");
		}
		
		for (int i = 0; i < nodes.size(); i++) {
	        for (int j = 0; j < nodes.size(); j++) {
	            if (i != j) { // No self-demands
	                double offeredTraffic = traffixMatrix[i][j];
	                Demand demand = findDemand(np, nodes.get(i), nodes.get(j));
	                if (demand == null) {
	                    demand = np.addDemand(nodes.get(i), nodes.get(j), offeredTraffic, null, null);
	                } else {
	                    demand.setOfferedTraffic(offeredTraffic);
	                }
	            }
	        }
	    }
		
		return np;
	}
	
	public NetPlan simulateRouting(NetPlan np) {
        NetPlan routedNetwork = np.copy(); // Make a copy of the original network

        // Prepare the collection of nodes and links
        Collection<Node> nodes = routedNetwork.getNodes();
        Collection<Link> links = routedNetwork.getLinks();

        // Define the link cost map and initialize link remaining capacity
        Map<Link, Double> linkCostMap = new HashMap<>();
        Map<Link, Double> linkRemainingCapacity = new HashMap<>();
        for (Link link : links) {
            linkCostMap.put(link, 1.0); // Modify this based on actual link metrics if needed
            linkRemainingCapacity.put(link, link.getCapacity()); // Initialize remaining capacity
        }

        // Prepare DoubleMatrix2D for forwarding rules
        DoubleMatrix2D forwardingRulesMatrix = DoubleFactory2D.sparse.make(routedNetwork.getNumberOfDemands(), routedNetwork.getNumberOfLinks(), 0.0);
        Set<Demand> updatedDemands = new HashSet<>();

        // Apply the shortest path for each demand using hop-by-hop routing
        for (Demand demand : routedNetwork.getDemands()) {
            int demandIndex = demand.getIndex();
            updatedDemands.add(demand);

            Node originNode = demand.getIngressNode();
            Node destinationNode = demand.getEgressNode();

            // Calculate the shortest path using your shortest path logic
            List<Link> shortestPath = GraphUtils.getShortestPath(nodes, links, originNode, destinationNode, linkCostMap);

            if (!shortestPath.isEmpty()) {
                double totalOfferedTraffic = demand.getOfferedTraffic();
                double trafficToDistribute = totalOfferedTraffic;

                for (Link link : shortestPath) {
                    int linkIndex = link.getIndex();
                    double availableCapacity = linkRemainingCapacity.get(link);

                    // Determine how much traffic this link can carry
                    double trafficForThisLink = Math.min(trafficToDistribute, availableCapacity);

                    // Set the forwarding rule for this link
                    if (trafficForThisLink > 0) {
                        double ratio = trafficForThisLink / totalOfferedTraffic;
                        forwardingRulesMatrix.setQuick(demandIndex, linkIndex, ratio);
                        // Update the remaining capacity of the link
                        linkRemainingCapacity.put(link, availableCapacity - trafficForThisLink);
                        // Reduce the remaining traffic to distribute
                        trafficToDistribute -= trafficForThisLink;
                    }
                    // Stop if all traffic has been distributed
                    if (trafficToDistribute <= 0) break;
                }
            }
        }

        // Set the forwarding rules for the entire network
        routedNetwork.setForwardingRules(forwardingRulesMatrix, updatedDemands);

        return routedNetwork;
    }
	
	public String getNetworkSummary(NetPlan np) {
		// Get the default network layer or specify if you work with multiple layers
        NetworkLayer defaultLayer = np.getNetworkLayerDefault();

        // Get the nodes and links from the NetPlan object
        int numberOfNodes = np.getNumberOfNodes();
        int numberOfLinks = np.getNumberOfLinks(defaultLayer);

        // Construct the summary string
        return String.format("Network Summary: %d nodes, %d links", numberOfNodes, numberOfLinks);
	}
	
	public CurrentTopology getSerializedNetworkFromNDTNetPlan(NetPlan np, String timeOfUpdatedNp) {
     	CurrentTopology currentTopology = new CurrentTopology();
        currentTopology.ietfNetworks = currentTopology.new IetfNetworks();
        currentTopology.ietfNetworks.network = new ArrayList<>();
     	
		IetfNetwork ietfNetwork = new IetfNetwork();
        ietfNetwork.networkId = "current-network_" + timeOfUpdatedNp;

        ietfNetwork.node = new ArrayList<>();
        for (Node node : np.getNodes()) {
            IetfNode ietfNode = new IetfNode();
            ietfNode.nodeId = node.getName();
            ietfNode.nodeAttributes =  new IetfNode.IetfL3UnicastTopologyNodeAttributes();
            ietfNode.nodeAttributes.terminationPoint = node.getTags().stream().map(IetfTerminationPoint::new).collect(Collectors.toList());
            ietfNode.nodeAttributes.state = node.isDown() ? "DOWN" : "UP";
            ietfNetwork.node.add(ietfNode);
        }
        
        ietfNetwork.link = new ArrayList<>();
        for (Link link : np.getLinks()) {
        	IetfLink ietfLink = new IetfLink();
			ietfLink.source = new Source(link.getOriginNode().getName(), link.getAttribute("sourceTerminationPoint"));
			ietfLink.destination = new Destination(link.getDestinationNode().getName(), link.getAttribute("destinationTerminationPoint"));
			ietfLink.linkAttributes = new IetfLink.IetfL3UnicastTopologyLinkAttributes();
			ietfLink.linkAttributes.state = link.isDown() ? "DOWN" : "UP";
			
			ietfLink.linkAttributes.latency = getLatencyInMs(link);
			ietfLink.linkAttributes.bandwidth = (int) link.getCapacity();
			
			if (link.getAttribute("identifier") == null) {
				ietfLink.linkId = link.getOriginNode().getName() + "," + ietfLink.source.sourceTp + "," +
						link.getDestinationNode().getName() + "," + ietfLink.destination.destTp;
			} else {
				ietfLink.linkId = link.getAttribute("identifier");
			}
			
			ietfNetwork.link.add(ietfLink);
		}
		
        currentTopology.ietfNetworks.network.add(ietfNetwork);
		
		// Add the traffic matrix
		currentTopology.traffic = new ArrayList<>();
		for (Demand demand : np.getDemands()) {
			KpiMetricGivenNodeId traffic = KpiMetricGivenNodeId.ofGbps(
					demand.getIngressNode().getName(), demand.getEgressNode().getName(), demand.getOfferedTraffic());
			currentTopology.traffic.add(traffic);
		}
        
        return currentTopology;
    }
	
	private Link findLinkBetweenNodes(NetPlan netPlan, Node origin, Node destination) {
	    for (Link link : netPlan.getLinks()) {
	        if (link.getOriginNode().getId() == origin.getId() && link.getDestinationNode().getId() == destination.getId()) {
	            return link; // Link found
	        }
	    }
	    return null; // No link found
	}
	
	private Demand findDemand(NetPlan np, Node origin, Node destination) {
	    for (Demand demand : np.getDemands()) {
	        if (demand.getIngressNode().equals(origin) && demand.getEgressNode().equals(destination)) {
	            return demand;
	        }
	    }
	    return null; // No demand found
	}
	
	private Link findLinkByName(NetPlan np, String linkName) {
		for (Link link : np.getLinks()) {
			if (link.getName().equals(linkName)) {
				return link;
			}
		}
		return null;
	}
	
	private Link findLinkByLinkIdIdentifier(NetPlan np, String linkName) {
		for (Link link : np.getLinks()) {
			if (link.getAttribute("identifier").equals(linkName)) {
				return link;
			}
		}
		return null;
	}
	
	private void setLinkDistanceForLatency(Link link, double desiredLatency) {
		if (link != null) {
			final double propagationSpeed = link.getPropagationSpeedInKmPerSecond();
			double distance = desiredLatency * propagationSpeed; // calculate the required distance
	        link.setLengthInKm(distance);
	    } else {
	        System.out.println("Link does not exist between the specified nodes.");
	    }
	}
	
	private boolean terminationPointExist(NetPlan np, Node node, String tpId) {
		return node.getTags().contains(tpId);
	}
	
	private boolean isTerminationPointUsedAsSource(NetPlan np, Node node, String tpId) {
		for (Link link : np.getLinks()) {
			if (link.getOriginNode().equals(node) && link.getAttribute("sourceTerminationPoint").equals(tpId)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isTerminationPointUsedAsDestination(NetPlan np, Node node, String tpId) {
		for (Link link : np.getLinks()) {
			if (link.getDestinationNode().equals(node) && link.getAttribute("destinationTerminationPoint").equals(tpId)) {
				return true;
			}
		}
		return false;
	}
	
	private int getLatencyInMs(Link l) {
		return (int) ((int) l.getLengthInKm() / l.getPropagationSpeedInKmPerSecond());
	}
}
