package com.elighthouse.ndtnet2plan.utils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.Function;
import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.Node;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;

public class GraphUtils {
	
	/** <p>Obtains a {@code JUNG} graph from a given set of links.</p>
	 * 
	 * @param nodes Collection of nodes
	 * @param links Collection of links
	 * @return {@code JUNG} graph */
	public static Graph<Node, Link> getGraphFromLinkMap(Collection<Node> nodes, Collection<Link> links)
	{
		Graph<Node, Link> graph = new DirectedOrderedSparseMultigraph<Node, Link>();

		for (Node node : nodes)
			graph.addVertex(node);

		if (links != null)
		{
			for (Link e : links)
			{
				if (!graph.containsVertex(e.getOriginNode())) throw new RuntimeException("Bad"); //graph.addVertex(e.getOriginNode());
				if (!graph.containsVertex(e.getDestinationNode())) throw new RuntimeException("Bad"); //graph.addVertex(e.getDestinationNode());
				graph.addEdge(e, e.getOriginNode(), e.getDestinationNode());
			}
		}

		return graph;
	}
	
	/** Obtains the sequence of links representing the (unidirectional) shortest path between two nodes. 
	 * Links with cost {@code Double.MAX_VALUE} are not considered.
	 * @param nodes Collection of nodes
	 * @param links Collection of links
	 * @param originNode Origin node
	 * @param destinationNode Destination node
	 * @param linkCostMap Cost per link, where the key is the link identifier and the value is the cost of traversing the link. No special iteration-order (i.e. ascending) is required. If <code>null</code>, the shortest path in number of traversed links is returned,
	 * @return Sequence of links in the shortest path (empty, if destination not reachable from origin) */
	public static List<Link> getShortestPath(Collection<Node> nodes, Collection<Link> links, Node originNode, Node destinationNode, Map<Link, Double> linkCostMap)
	{
		final Collection<Link> linksToUse = linkCostMap == null ? links : links.stream().filter(e -> linkCostMap.get(e) != Double.MAX_VALUE).collect(Collectors.toList());
        final Graph<Node, Link> graph = GraphUtils.getGraphFromLinkMap(nodes, linksToUse);
        if (!graph.containsVertex(originNode)) return new LinkedList<>();
        if (!graph.containsVertex(destinationNode)) return new LinkedList<>();

        // Transform into a Function for weights
        Function<Link, Double> nev = edge -> {
            Double value = linkCostMap.get(edge);
            if (value == null) throw new RuntimeException("Bad - No weight for link " + edge);
            return value;
        };

        // Instantiate Dijkstra's using the Function
        DijkstraShortestPath<Node, Link> dsp = new DijkstraShortestPath<>(graph, nev);
        List<Link> path = dsp.getPath(originNode, destinationNode);
        return path;
	}
}
