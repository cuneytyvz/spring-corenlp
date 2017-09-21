var semantic = (function () {

    var instance;

    function init() {

        function Link(source, target, value) {
            this.source = source;
            this.target = target;
            this.value = value;
            this.id = source + "_" + target;
        }

        function shortenName(name) {
            return name.length < 17 ? name : name.substr(0, 17) + "...";
        }

        // Graph Object //
        var graph = {
            nodes: [],
            links: [],
            linkIndexes: [],
            merge: function (_graph) {
                var _nodes = _graph.nodes;
                var _links = _graph.links;
                for (var i = 0; i < _nodes.length; i++) {
                    // varsa ekleme
                    var index = this.nodes.map(function (node) {
                        return node.id;
                    }).indexOf(_nodes[i].id);

                    if (index === -1) {
                        this.nodes.push(_nodes[i]);
                    } else {
                        if (_nodes[i].selected)
                            this.nodes[index].selected = true;
                    }
                }
                for (i = 0; i < _links.length; i++) {

                    if (this.links.map(function (link) {
                        return link.source.id + '-' + link.target.id; // link.id
                    }).indexOf(_links[i].id) === -1) {
                        var sourceIndex = this.nodes.map(function (node) {
                            return node.id;
                        }).indexOf(_links[i].source.id);

                        var targetIndex = this.nodes.map(function (node) {
                            return node.id;
                        }).indexOf(_links[i].target.id);

                        var link = new Link(sourceIndex, targetIndex,
                            _links[i].value);

                        this.links.push(link);
                    }
                }
            }
        };
        // ************ //

        var maxNode = 6;
        var width = 960, height = 500;
        var color = d3.scale.category20();
        var force = d3.layout.force().gravity(0.05).distance(250).linkDistance(
            250).charge(-1000).size([ width, height ]).nodes(graph.nodes)
            .links(graph.links).on("tick", tick);


        var x = d3.scale.linear().domain([ 0, 1 ]).range([ 0, 1 ]);
        var y = d3.scale.linear().domain([ 1, 0 ]).range([ 1, 0 ]);

        var svg = d3.select("svg").attr("width", width).attr("height", height)
            .call(
            d3.behavior.zoom().x(x).y(y).scaleExtent([ -2, 8 ]).on(
                "zoom", tick));

        var node = svg.selectAll(".node");
        var link = svg.selectAll(".path");

        var lastSelectedNode;
        var showSimilars = false;

        var browsePath = [];

        function drawGraph(incomingGraph) {

            if (incomingGraph)
                graph.merge(incomingGraph);

//            console.log("Merged graph : " + JSON.stringify(graph));
            var test = force.nodes();

            // Join
            node = node.data(force.nodes(), function (d) {
                return d.id;
            });
            link = link.data(force.links());

            link.style({
                opacity: adjustLinkOpacity
            });

            // Links //
            link.enter().insert("svg:path", "g").attr("class", "link")
                .style(
                "stroke-width", function (d) {
                    return Math.sqrt(d.value);
                })
                .style({
                    opacity: adjustLinkOpacity});
            // ***** //

            function adjustLinkOpacity(d) {
                var showThisOne = false;
                for (var i = 0; i < graph.links.length; i++) {
                    if (graph.links[i].target.id == d.id &&
                        (graph.links[i].source.selected)) {
                        showThisOne = true; // because its neighbour is selected
                    }
                }

                for(var i = 0; i < browsePath.length; i++) {
                    if(browsePath[i].name == d.target.name) {
                        showThisOne = true;
                    }
                }

                if ((d.source.saved || d.source.selected)
                    && (d.target.saved || d.target.selected)) {
                    return 1;
                } else {
                    if (showSimilars || d.source.selected || showThisOne)
                        return 0.5;
                    else
                        return 0;
                }
            }

            node.attr("join", "update")
                .style({
                    opacity: adjustNodeOpacity
                });

            // Nodes //
            var enterNode = node.enter().append("g").attr("id", function (d) {
                return d.id;
            }).attr("class", "node")
                .style({
                    opacity: adjustNodeOpacity})
                .call(force.drag);

            function adjustNodeOpacity(d) {

                // if it is selected show its neighbours
                var showThisOne = false;
                for (var i = 0; i < graph.links.length; i++) {
                    if (graph.links[i].target.id == d.id &&
                        (graph.links[i].source.selected)) {
                        showThisOne = true; // because its neighbour is selected
                    }
                }

                for(var i = 0; i < browsePath.length; i++) {
                    if(browsePath[i].name == d.name) {
                        showThisOne = true;
                    }
                }

                if (!d.saved && !d.selected) {
                    if (showSimilars || showThisOne)
                        return 0.5;
                    else
                        return 0;
                } else {
                    return 1;
                }
            }

            var clipPath = enterNode.append("clipPath").attr("id", function (d) {
                return "clipPath_" + d.id;
            });

            clipPath.append("circle").attr("r", function (d) {
                if (d.image.height < d.image.width) {
                    return d.image.height / 2;
                } else {
                    return d.image.width / 2;
                }
            });

            /*
             * node.append("circle") .attr("r",5) .attr("r", 10) .style("fill",
             * "#FF6600");
             */

            enterNode.append("image").attr("width", function (d) {
                return d.image.width;
            }).attr("height", function (d) {
                return d.image.height;
            }).attr("xlink:href", function (d) {
                return d.image.src;
            }).attr('image-rendering', 'optimizeQuality').attr('x',
                function (d) {
                    return d.x;
                }).attr('y', function (d) {
                    return d.y;
                }).attr("clip-path", function (d) {
                    return "url(#clipPath_" + d.id + ")";
                });

            enterNode.append("text").attr("dx", function (d) {
                var circle = svg.select("#clipPath_" + d.id).select("circle");
                var tmp = getVisualLength(d.name) / 2;
                return circle.attr("cx") - tmp;
            }).attr("dy", function (d) {
                var circle = svg.select("#clipPath_" + d.id).select("circle");
                var cy = parseInt(circle.attr("cy"));
                var r = parseInt(circle.attr("r"));
                var y = cy + r + 15;
                return y;
            }).attr("class", ".text").text(function (d) {
                return shortenName(d.name);
            });

            enterNode.append("path").attr("stroke", "blue")
                .attr("stroke-width", function (d) {
                    if (d.saved) {
                        return 0; // 5
                    } else {
                        return 0;
                    }
                })
                .attr("fill", "none")
                .attr("id", function (d) {
                    return "path-" + d.id;
                }).attr("d", function (d) {
                    var path = "M 0, 0" +
                        " m 0, " + d.image.height / 2 + " " +
                        " a 15,15 0 1,0 60,0" +
                        " a 15,15 0 1,0 -60,0";

                    return path;
                });

            node.on("click", function (d, i) {
                var tmp = d.name.replace(" ", "+").toLowerCase();
                tmp = escape(tmp);

                // lastfm url
                var url = "http://www.lastfm.com.tr/music/" + tmp;

                if (d3.event.which == 1) {
//                    getSubGraph(d.name);

                    $.event.trigger('nodeClicked', d.name);

                    if (!d.saved) {
                        browsePath.push(d);
                    } else {
                        browsePath = [];
                    }

                    setTimeout(function () {
                        drawGraph();
                    }, 500);

                    // set selected opacity 1 / not working yet...
                    d.selected = true;
                    if (lastSelectedNode)
                        lastSelectedNode.selected = false;
                    lastSelectedNode = d;
                } else if (d3.event.which == 2) {
                    var win = window.open(url, "lastfm");
                    win.focus();
                }
            });

            var img = d3.select("image");
            img.on("click", function (d, i) {

            });

            $(document).bind('nodeSaved', function (e, nodeName) {
                for (var i = 0; i < graph.nodes.length; i++) {
                    if (graph.nodes[i].name == nodeName) {
                        graph.nodes[i].saved = true;
                    }
                }

                drawGraph();
            });

            force.start();
        }

        setTimeout(function () {
            drawGraph();
        }, 1000);

        $(document).bind('showHideSimilars', function (e) {
            showSimilars = !showSimilars;
            drawGraph();
        });


        function transform(d) {
            return "translate(" + (x(d.x) - d.image.width / 2) + ","
                + (y(d.y) - d.image.height / 2) + ")";
        }

        function tick() {

            link.attr('d', function (d) {
                var deltaX = x(d.target.x) - x(d.source.x);
                var deltaY = y(d.target.y) - y(d.source.y);
                var dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                var normX = deltaX / dist;
                var normY = deltaY / dist;

                var sourceX = x(d.source.x) + (normX);
                var sourceY = y(d.source.y) + (normY);
                var targetX = x(d.target.x) - (normX);
                var targetY = y(d.target.y) - (normY);

                return 'M' + sourceX + ',' + sourceY + 'L' + targetX + ','
                    + targetY;
            });

            node.attr("transform", transform);
        }

        // Kaynak :
        // http://blog.mastykarz.nl/measuring-the-length-of-a-string-in-pixels-using-javascript/
        // d3 selectionından offsetwidth'e dogrudan erişemediğimizden uzadı.
        function getVisualLength(text) {
            var ruler = d3.select("#ruler");
            var ruler2 = ruler[0];
            ruler.html(text);
            var length = ruler2[0].offsetWidth;
            ruler.html(null);
            return length;
        }

        function imgOnLoad(image, nodeId) {

            var img = svg.select("#" + nodeId).select("image");
            var clipPath = svg.select("#clipPath_" + nodeId).select("circle");
            var text = svg.select("#" + nodeId).select("text");
//            var circledTextPath = svg.

            // Image
            img.attr("width", image.width);
            img.attr("height", image.height);
            img.attr('x', function (d) {
                return 0;
//                return d.image.x - d.image.width / 2;
            }).attr('y', function (d) {
                return 0;
//                return d.image.y - d.image.height / 2;
            });

            // ClipPath
            if (image.height < image.width) {
                clipPath.attr("r", image.height / 2);
            } else {
                clipPath.attr("r", image.width / 2);
            }
            clipPath.attr("cx", image.width / 2);
            clipPath.attr("cy", image.height / 2);

            // Text
            text.attr("dx", function (d) {
                var tmp = getVisualLength(shortenName(d.name)) / 2;
                return (clipPath.attr("cx") - tmp) / 2;
            });
            text.attr("dy", function (d) {
                var cy = parseInt(clipPath.attr("cy"));
                var r = parseInt(clipPath.attr("r"));
                var y = cy + r + 15;
                return y;
            });
            text.attr("class", ".text");
        }

        function createHTMLImage(url, id) {
            var image = new Image();
            image.src = url;
            image.onload = function () {
                imgOnLoad(this, id);
            };
//            console.log("Image : (" + id + "," + url + ")");
            return image;
        }

        function processIncomingData(data) {
            // Process
            var graph = {
                nodes: [],
                links: []
            };

            // image url'inin alınarak Image() objesine dönüstürülmesi.
            var nodes = data.nodes;
            for (var i = 0; i < nodes.length; i++) {
                nodes[i].image = createHTMLImage(nodes[i].mediumImg, nodes[i].id);
            }
//            console.log("incomingGraph : " + JSON.stringify(data));

            drawGraph(data);
        }

        var getSubGraph = function (artist) {
            var lowerArtist = artist.toLowerCase();

            // sag panel'in update edilmesi.
            d3.select("#artist_text").html(lowerArtist);
//            lowerArtist = lowerArtist.replaceAll(" ", "+");

            // Get Graph
            $.ajax({
                url: encodeURI("/semantic/api/getGraph/" + lowerArtist),
                method: "GET",
                success: function (data) {
                    processIncomingData(data);
                }
            });

            return false;
        };

        var getGraphFromLastFM = function (artist) {
            var lowerArtist = artist.toLowerCase();

            // sag panel'in update edilmesi.
            d3.select("#artist_text").html(lowerArtist);
            lowerArtist = lowerArtist.replace(" ", "+");

            var nodes = [], links = [];
            for (var i = 0; i < data.similarartists.artist; i < data.similarartists.artist.length) {

            }

            // Get Graph
            $.ajax({
                url: encodeURI("http://ws.audioscrobbler.com/2.0/?method=artist.getsimilar&artist=" + lowerArtist + "&api_key=ecc2e1fc920c9fcbcc19ba8790570691&format=json"),
                method: "GET",
                success: function (data) {
                    processIncomingData(data);
                }
            });

            return false;
        };

        return {
            foo: function (artist) {
                svg.html('');
                getSubGraph(artist);
//                getGraphFromLastFM(artist);
            },

            processIncomingData: processIncomingData
        };
    }

    return {

        // Get the Singleton instance if one exists
        // or create one if it doesn't
        getInstance: function () {

            if (!instance) {
                instance = init();
            }

            return instance;
        }

    };
})
();