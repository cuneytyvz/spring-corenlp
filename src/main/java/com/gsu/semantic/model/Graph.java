package com.gsu.semantic.model;

import java.util.List;

/**
 * Created by cnytync on 21/08/2017.
 */
public class Graph {
    public List<Node> nodes;
    public List<Link> links;

    public Graph() {
    }

    public Graph(List<Node> nodes, List<Link> links) {
        this.nodes = nodes;
        this.links = links;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }
}
