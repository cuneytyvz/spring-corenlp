var app = angular.module('app', ['ngDialog']);

app.controller('Controller', function ($scope, $http, $q, $sce, $timeout, ngDialog) {

        $scope.searchInput = 'Led Zeppelin';

        $http.get("/semantic/api/listGraphs/1")
            .then(function (response) {
                $scope.graphs = response.data;

            }, printError);

        $scope.search = function () {
            $('.album-box-edge').animate({left: '-290px'}, 300);

            if ($scope.graph && $scope.graph.nodes) {
                var name = null;
                for (var i = 0; i < $scope.graph.nodes.length; i++) {
                    if ($scope.searchInput.toLowerCase() == $scope.graph.nodes[i].name.toLowerCase()) {
                        name = $scope.graph.nodes[i].name;
                        break;
                    }
                }

                if (name) {
                    $.event.trigger('goToNodePosition', name.toLowerCase());
                } else {
                    fetchData($scope.searchInput.toLowerCase());
                }
            } else {
                fetchData($scope.searchInput.toLowerCase());
            }
        };

        $scope.newGraph = function () {
            ngDialog.open({
                template: 'new-graph-popup',
                controller: ['$scope', function ($ss) {

                    $ss.closeWindow = function () {
                        $ss.closeThisDialog();
                    };

                    $ss.createGraph = function () {

                        $http.get('/semantic/api/createGraph/' + $ss.graphName)
                            .then(function (response) {
                                $scope.graphs.push({name: $ss.graphName, id: response.data});

                                $ss.closeThisDialog();
                            }, function (err) {
                            });
                    };

                }]});
        };

        $scope.detectCommunities = function () {
            var nodes = [], links = [];

            for (var i = 0; i < $scope.graph.links.length; i++) {
                var l = $scope.graph.links[i];
                if (l.source.saved && l.target.saved) {
                    links.push({
                        source: l.source.id,
                        target: l.target.id,
                        weight: 1
                    });
                }
            }

            for (var i = 0; i < $scope.graph.nodes.length; i++) {
                var n = $scope.graph.nodes[i];
                if (n.saved) {
                    nodes.push(n.id);
                }
            }

            var community = jLouvain().nodes(nodes).edges(links);
            var result = community();
        };

        var fetchData = function (node) {
            $http.post("/semantic/api/getGraph", {nodeName: node})
                .then(function (response) {
//                $scope.graph = response.data; // modify this part to append nodes and links instead of directly assigning new values...

                    var incomingGraph = response.data;

                    if ($scope.graph == null) {
                        $scope.graph = {nodes: [], links: []};
                    }

                    for (var i = 0; i < response.data.nodes.length; i++) {
                        var n = _.find($scope.graph.nodes, {dbId: response.data.nodes[i].dbId});

                        if (!n) {
                            $scope.graph.nodes.push(response.data.nodes[i]);
                        }
                    }

                    $scope.graph.links.push(response.data.links);

                    for (var i = 0; i < $scope.graph.nodes.length; i++) {
                        var n = $scope.graph.nodes[i];

                        if (n.name.toLowerCase() == node) {
                            n.selected = true;
                            $scope.selectedNode = n;
                            incomingGraph.selectedNode = n;
                        }
                    }

                    semantic.getInstance().processIncomingData(incomingGraph);

                    $http.post("/semantic/api/albums", {nodeName: $scope.selectedNode.name})
                        .then(function (response) {
                            $scope.albums = response.data;
                            $scope.selectedNode.albums = response.data;

                            $.event.trigger('dataFetched');
                            $timeout(function () {
                                setupAccordion();
                                $('.info-text').find('a').attr('target', '_blank');
                            });

                        }, printError);
                }, printError);
        };

        function setupAccordion() {
            var acc = document.getElementsByClassName("accordion");
            var i;

            for (i = 0; i < acc.length; i++) {
                acc[i].onclick = function () {
                    /* Toggle between adding and removing the "active" class,
                     to highlight the button that controls the panel */
                    this.classList.toggle("active");

                    /* Toggle between hiding and showing the active panel */
                    var panel = this.nextElementSibling;
                    if (panel.style.display === "block") {
                        panel.style.display = "none";
                    } else {
                        panel.style.display = "block";
                    }
                }
            }
        }

        var $graphsMenu = $('.graph-box-edge.graphs');
        var $albumsMenu = $('.graph-box-edge.albums');

        $scope.sideMenus = [];
        $scope.sideMenus.push({$el: $graphsMenu, isOpen: false});
        $scope.sideMenus.push({$el: $albumsMenu, isOpen: false});

        $scope.openMenu = null;

        $scope.openGraphMenu = function () {
            putOtherMenusBack($graphsMenu);

            if (isOpen($graphsMenu)) {
                $('.graph-box-edge').animate({left: '-290px'}, 300);
            }
            else {
                $('.graph-box-edge').animate({left: '0px'}, 300);
            }

            setOpen($graphsMenu, !isOpen($graphsMenu));
        };

        $scope.albumMenuOpen = false;
        $scope.openAlbumMenu = function () {
            putOtherMenusBack($albumsMenu);

            if (isOpen($albumsMenu))
                $('.graph-box-edge').animate({left: '-290px'}, 300);
            else
                $('.graph-box-edge').animate({left: '0px'}, 300);

            setOpen($albumsMenu, !isOpen($albumsMenu));
        };

        function putOtherMenusBack($thisMenu) {
            $thisMenu.css({'z-index': 999});

            _.each($scope.sideMenus, function (menu) {
                if ($thisMenu[0] != menu.$el[0]) {
                    menu.$el.css({'z-index': 1});
                    menu.isOpen = false;
                }
            });
        }

        function setOpen($el, isOpen) {
            _.each($scope.sideMenus, function (item) {
                if (item.$el[0] == $el[0]) {
                    item.isOpen = isOpen;
                }
            });
        }

        function isOpen($el) {
            var ret = false;
            _.each($scope.sideMenus, function (item) {
                if (item.$el[0] == $el[0]) {
                    ret = item.isOpen;
                }
            });

            return ret;
        }

        function lastOpenMenuItem() {
            var lastItem = null;
            _.each($scope.sideMenus, function (item) {
                if (item.$el[0] == $el[0]) {
                    if (item.isOpen) lastItem = item;
                }
            });

            return lastItem;
        }

        $scope.getSavedGraph = function (graph) {
            $('.album-box-edge').animate({left: '-290px'}, 300);

            $scope.graph = {nodes: [], links: []};

            $http.get("/semantic/api/getSavedGraph/" + graph.id)
                .then(function (response) {
                    semantic.createNewInstance();

                    $scope.selectedGraph = response.data;
                    $scope.graph = response.data;
                    if (!response.data.nodes || response.data.nodes.length == 0) {
                        return;
                    }

                    $scope.selectedNode = response.data.nodes[0];
                    $scope.selectedNode.selected = true;

                    var incomingGraph = response.data;
                    incomingGraph.selectedNode = $scope.selectedNode;

                    semantic.getInstance().processIncomingData(incomingGraph);

                }, printError);
        };

        $scope.showTracks = function (release, artist) {
            if (release.clicked) {
                release.clicked = false;
                return;
            }

            $http.get("/semantic/api/tracks?artist=" + encodeURIComponent(artist.name) + "&album=" + encodeURIComponent(release.name))
                .then(function (response) {
                    release.tracks = response.data;

                }, printError);
        };

        $scope.saveNode = function () {
            if ($scope.selectedGraph) {
                $http.get("/semantic/api/saveGraphNode?nodeId="
                    + $scope.selectedNode.dbId
                    + "&graphId=" + $scope.selectedGraph.id)
                    .then(function (response) {
                        $.event.trigger('nodeSaved', $scope.selectedNode.name);
                        $scope.selectedNode.saved = true;
                    }, printError);
            } else {
                $http.get("/semantic/api/saveNode/" + $scope.selectedNode.dbId)
                    .then(function (response) {
                        $.event.trigger('nodeSaved', $scope.selectedNode.name);
                        $scope.selectedNode.saved = true;
                    }, printError);
            }
        };

        $scope.removeNode = function () {
            if ($scope.selectedGraph) {
                $http.get("/semantic/api/removeGraphNode?graphId= " + $scope.selectedGraph.id + "&nodeId="
                    + $scope.selectedNode.dbId)
                    .then(function (response) {
                        $.event.trigger('nodeRemoved', $scope.selectedNode.name);
                    }, printError);
            } else {
                $http.get("/semantic/api/removeNode/" + $scope.selectedNode.dbId)
                    .then(function (response) {
                        $.event.trigger('nodedRemoved', $scope.selectedNode.name);
                    }, printError);
            }
        };

        $scope.getUserGraph = function () {
            $http.get("/semantic/api/getUserGraph/")
                .then(function (response) {
                    $scope.graph = response.data;

                    $scope.selectedNode = response.data.nodes[0];

                    semantic.getInstance().processIncomingData(response.data);

                }, printError);
        };

        $(document).bind('nodeClicked', function (e, nodeName) {
            $('.album-box-edge').animate({left: '-290px'}, 300);

            if (nodeName != $scope.selectedNode.name) {
                fetchData(nodeName.toLowerCase());
//            $scope.selectedNode = _.find($scope.graph.nodes, {name: nodeName});
            }
        });

        $scope.isNodeSaved = function () {
            return $scope.selectedNode.saved;
        };

        $scope.showHideSimilars = function () {
            $.event.trigger('showHideSimilars');
        };

        $scope.showHideNames = function () {
            $.event.trigger('showHideNames', $scope.showNames);
        };

        $("#search-input").autocomplete({
            minLength: 2,
            source: function (request, response) {
                var term = request.term;

                $http.get("/semantic/api/lookup/" + encodeURIComponent(term))
                    .then(function (resp) {
                        var results = [];
                        for (var i = 0; i < resp.data.length; i++) {
                            var d = resp.data[i];

                            results.push({
                                id: d.id,
                                musicgraphId: d.id,
                                label: d.name,
                                value: d.name
                            })
                        }

                        response(results);
                    }, printError);

            }, select: function (event, ui) {
                this.value = ui.item.value;
                $scope.searchInput = ui.item.value;

            }, change: function (event, ui) { // not-selected
                if (ui.item === null) {

                }
            }
        });

        setInterval(function () {
            $('body')
                .animate({
                    backgroundColor: '#2affc'
                }, 2000)
                .animate({
                    backgroundColor: '#8987ff'
                }, 2000);
        }, 4);

//    yourNumber.toString(16);
    }
)
;

app.config(function ($sceDelegateProvider, $sceProvider) {
    $sceProvider.enabled(false);
});

$(document).ready(function () {
    $('#wrapper').show();
});