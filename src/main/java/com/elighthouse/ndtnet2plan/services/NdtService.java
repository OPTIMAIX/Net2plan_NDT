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

import com.elighthouse.ndtnet2plan.models.ChangeSchedulerRequest.Action;
import com.elighthouse.ndtnet2plan.models.ChangeSchedulerRequest.CurrentTopology;
import com.elighthouse.ndtnet2plan.models.ChangeSchedulerRequest.CurrentTopology.IetfNetworks.IetfNetwork;
import com.elighthouse.ndtnet2plan.models.ChangeSchedulerRequest.CurrentTopology.IetfNetworks.IetfNetwork.Destination;
import com.elighthouse.ndtnet2plan.models.ChangeSchedulerRequest.CurrentTopology.IetfNetworks.IetfNetwork.IetfLink;
import com.elighthouse.ndtnet2plan.models.ChangeSchedulerRequest.CurrentTopology.IetfNetworks.IetfNetwork.IetfNode;
import com.elighthouse.ndtnet2plan.models.ChangeSchedulerRequest.CurrentTopology.IetfNetworks.IetfNetwork.IetfTerminationPoint;
import com.elighthouse.ndtnet2plan.models.ChangeSchedulerRequest.CurrentTopology.IetfNetworks.IetfNetwork.Source;
import com.elighthouse.ndtnet2plan.models.KpisInDesign;
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
	        for (IetfTerminationPoint tpData : nodeData.terminationPoint) {
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
	    }
		
		return np;
	}
	
	public NetPlan applyActions(NetPlan np, List<Action> actions) {
		final NetPlan afterActions = np.copy();
		
		for(Action action : actions) {
			switch(action.element.toUpperCase()) {
			case "LINK":
				applyLinkAction(afterActions, action);
				break;
			default:
				System.err.println("Unknown action element: " + action.element);
				break;
			}
		}
		
		return afterActions;
	}
	
	public void applyLinkAction(NetPlan np, Action action) {
		try {
			String[] nodes = action.ref.split(",");
			String originNodeId = nodes[0];
			String originTpId = nodes[1];
			String destinationNodeId = nodes[2];
			String destinationTpId = nodes[3];
			
			Node originNode = np.getNodeByName(originNodeId);
			Node destinationNode = np.getNodeByName(destinationNodeId);
			
			if (originNode == null || destinationNode == null) {
	            System.out.println("Node not found. Origin ID: " + originNodeId + ", Destination ID: " + destinationNodeId);
	            return;	
	        }
			
			String linkId = action.ref;
			Link link = findLinkBetweenNodes(np, originNode, destinationNode);
			
			if (link == null) {
	            System.out.println("Link not found between nodes " + originNodeId + " and " + destinationNodeId + ". Creating a new link...");
	            
	            if (!terminationPointExist(np, originNode, originTpId)) {
					System.out.println("Source termination point does not exist for node " + originNode.getName());
					return;
				}
	            
	            if (!terminationPointExist(np, destinationNode, destinationTpId)) {
	            	System.out.println("Destination termination point does not exist for node " + destinationNode.getName());
	            	return;
	            }
				
	            if (isTerminationPointUsedAsSource(np, originNode, originTpId)) {
	            	System.out.println("Source termination point is already used for node " + originNode.getName());
	            	return;
	            }
	            
				if (isTerminationPointUsedAsDestination(np, destinationNode, destinationTpId)) {
					System.out.println("Destination termination point is already used for node " + destinationNode.getName());
					return;
				}

	            final Link l = np.addLink(originNode, destinationNode, 0.0, 0, 1000.0, null, np.getNetworkLayerDefault());
	            l.setName(linkId);
	            l.setAttribute("sourceTerminationPoint", originTpId);
	            l.setAttribute("destinationTerminationPoint", destinationTpId);
	            link = l;
	        }
			
			// Apply the action
	        if ("DOWN".equalsIgnoreCase(action.actionType)) {
	        	System.out.println("Setting link down: " + linkId);
	            link.setFailureState(false);
	        } else if ("UP".equalsIgnoreCase(action.actionType)) {
	        	System.out.println("Setting link up: " + linkId);
	            link.setFailureState(true);
	        } else {
	            System.out.println("Unsupported action type: " + action.actionType);
	        }
		} catch (Exception e) {
            System.err.println("Error applying action: " + action);
            e.printStackTrace();
        }
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
	
	public KpisInDesign getKpisFromDesign(NetPlan np) {
        List<Node> nodes = np.getNodes();
        int size = nodes.size();
        KpisInDesign kpis = new KpisInDesign(size);

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
                        
                        // Correctly retrieve and iterate over forwarding rules
                        SortedMap<Pair<Demand, Link>, Double> forwardingRules = np.getForwardingRules();
                        for (Map.Entry<Pair<Demand, Link>, Double> entry : forwardingRules.entrySet()) {
                            Pair<Demand, Link> pair = entry.getKey();
                            if (pair.getFirst().equals(demand)) { // Ensure we're working with the current demand
                                Link link = pair.getSecond();
                                double fraction = entry.getValue();
                                
                                double carriedTraffic = offeredTraffic * fraction;
                                totalCarriedTraffic += carriedTraffic;
                                totalCapacity += link.getCapacity() * fraction; // Weighted capacity
                                totalLatency += link.getPropagationDelayInMs() * fraction; // Weighted latency
                            }
                        }
                        
                        // Update the matrices
                        kpis.offeredTrafficMatrixInGbps[i][j] = offeredTraffic;
                        kpis.carriedTrafficMatrixInGbps[i][j] = totalCarriedTraffic;
                        kpis.blockedTrafficMatrixInGbps[i][j] = offeredTraffic > totalCarriedTraffic ? offeredTraffic - totalCarriedTraffic : 0;
                        kpis.lostTrafficMatrixInGbps[i][j] = offeredTraffic > totalCarriedTraffic ? offeredTraffic - totalCarriedTraffic : 0;
                        kpis.capacityMatrixInGbps[i][j] = totalCapacity;
                        kpis.latencyMatrixInMs[i][j] = totalLatency;
                    }
                }
            }
        }
        return kpis;
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
            ietfNode.terminationPoint = node.getTags().stream().map(IetfTerminationPoint::new).collect(Collectors.toList());
            ietfNetwork.node.add(ietfNode);
        }
        
        ietfNetwork.link = new ArrayList<>();
        for (Link link : np.getLinks()) {
        	
        	// Check if link is down, if so, skip it
			if (!link.isUp()) {System.out.println("Skipping link: " + link.getName() + ", is down"); continue;}
        	
			IetfLink ietfLink = new IetfLink();
			ietfLink.source = new Source(link.getOriginNode().getName(), link.getAttribute("sourceTerminationPoint"));
			ietfLink.destination = new Destination(link.getDestinationNode().getName(), link.getAttribute("destinationTerminationPoint"));
			
			if (link.getAttribute("identifier") == null) {
				ietfLink.linkId = link.getOriginNode().getName() + "," + ietfLink.source.sourceTp + "," +
						link.getDestinationNode().getName() + "," + ietfLink.destination.destTp;
			} else {
				ietfLink.linkId = link.getAttribute("identifier");
			}
			
			ietfNetwork.link.add(ietfLink);
		}
		
        currentTopology.ietfNetworks.network.add(ietfNetwork);
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
}
