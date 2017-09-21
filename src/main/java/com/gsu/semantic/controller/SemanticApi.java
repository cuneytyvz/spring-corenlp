package com.gsu.semantic.controller;

import com.gsu.semantic.model.Graph;
import com.gsu.semantic.model.Link;
import com.gsu.semantic.model.Node;
import com.gsu.semantic.repository.SemanticGraphDao;
import com.gsu.semantic.service.lastfm.Album;
import com.gsu.semantic.service.lastfm.Lastfm;
import com.gsu.semantic.service.lastfm.Track;
import com.gsu.semantic.service.musicbrainz.Musicbrainz;
import com.gsu.semantic.service.musicbrainz.Release;
import com.gsu.semantic.service.musicgraph.LookupResponseItem;
import com.gsu.semantic.service.musicgraph.MusicGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by cnytync on 21/12/14.
 */
@Controller("SemanticApi")
@RequestMapping(value = "semantic/api")
public class SemanticApi {

    @Autowired
    private Lastfm lastfm;

    @Autowired
    private SemanticGraphDao semanticGraphDao;

    @Autowired
    private Musicbrainz musicbrainz;

    @Autowired
    private MusicGraph musicGraph;

    @RequestMapping(value = "/getGraph/{node:.*}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object getGraph(@PathVariable(value = "node") String nodeName) throws Exception {
        Graph dbGraph = semanticGraphDao.getGraphByNodeName(nodeName);

        if (dbGraph != null) {
            return dbGraph;
        }

        Node node = lastfm.artistInfo(nodeName);
        List<Node> similarNodes = lastfm.similarArtists(nodeName);
        similarNodes = similarNodes.subList(0, 5);

        List<Node> nodes = new ArrayList<>();
        nodes.add(node);
        nodes.addAll(similarNodes);

        List<Link> links = new ArrayList<>();
        for (Node n : similarNodes) {
            links.add(new Link(node, n, 1.0));
        }

        final Graph graph = new Graph(nodes, links);

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                for (Node node : graph.getNodes()) {
                    Node dbNode = semanticGraphDao.getNodeByName(node.getName());

                    if (dbNode == null) {
                        Integer id = semanticGraphDao.saveNode(node);
                        node.setDbId(id);
                    } else if (dbNode.getBio() == null) {
                        node.setDbId(dbNode.getDbId());
                        Integer id = semanticGraphDao.updateNode(node);
                    } else {
                        node.setDbId(dbNode.getDbId());
                    }
                }

                for (Node node : graph.getNodes()) {
                    for (Link link : graph.getLinks()) {
                        if (link.getSource().getName().equals(node.getName())) {
                            link.setSource(node);
                        }

                        if (link.getTarget().getName().equals(node.getName())) {
                            link.setTarget(node);
                        }
                    }
                }

                for (Link link : graph.getLinks()) {
                    Link dbLink = semanticGraphDao.getLink(link.getSource().getName(), link.getTarget().getName());

                    if (dbLink == null) {
                        semanticGraphDao.saveLink(link.getSource().getDbId(), link.getTarget().getDbId());
                    }
                }
            }
        });

        return graph;
    }

    @RequestMapping(value = "/saveNode/{nodeId}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    void saveNode(@PathVariable(value = "nodeId") Integer nodeId) throws Exception {
        if (!semanticGraphDao.isUserNodeSaved(1, nodeId))
            semanticGraphDao.saveUserNode(1, nodeId);
    }

    @RequestMapping(value = "/getUserGraph/", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object getUserGraph() throws Exception {
        List<Node> nodes = semanticGraphDao.getUserNodes(1);
        List<Link> links = semanticGraphDao.getLinksForSourceNodes(nodes);

        for (Link link : links) {
            if (!nodes.contains(link.getTarget())) {
                nodes.add(link.getTarget());
            } else {
                link.getTarget().setSaved(true);
            }

            if (nodes.contains(link.getSource())) {
                link.getSource().setSaved(true);
            }
        }

        return new Graph(nodes, links);
    }

    @RequestMapping(value = "/getAlbumReleases/{mbid}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object getUserGraph(@PathVariable String mbid) throws Exception {
        List<Release> releases = musicbrainz.releases(mbid);

        return releases;
    }

    @RequestMapping(value = "/lookup/{prefix}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object lookup(@PathVariable String prefix) throws Exception {
        List<LookupResponseItem> items = musicGraph.lookup(prefix, null, null);

        return items;
    }

//    @RequestMapping(value = "/albums/{id}", method = RequestMethod.GET,
//            produces = MediaType.APPLICATION_JSON_VALUE)
//    public
//    @ResponseBody
//    Object albums(@PathVariable String id) throws Exception {
//        List<Album> albums = musicGraph.albums(id);
//
//        return albums;
//    }
//
//    @RequestMapping(value = "/tracks/{id}", method = RequestMethod.GET,
//            produces = MediaType.APPLICATION_JSON_VALUE)
//    public
//    @ResponseBody
//    Object tracks(@PathVariable String id) throws Exception {
//        List<Track> tracks = musicGraph.tracks(id);
//
//        return tracks;
//    }

    @RequestMapping(value = "/albums/{artist}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object albums(@PathVariable String artist) throws Exception {
        List<Album> albums = lastfm.topAlbums(artist);

        return albums;
    }

    @RequestMapping(value = "/tracks", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object tracks(@RequestParam String artist, @RequestParam String album) throws Exception {
        Album alb = lastfm.albumInfo(artist, album);

        return alb;
    }
}