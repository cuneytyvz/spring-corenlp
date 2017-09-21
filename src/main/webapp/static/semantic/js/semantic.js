var app = angular.module('app', []);

app.controller('Controller', function ($scope, $http, $q, $sce, $timeout) {

    $scope.searchInput = 'Led Zeppelin';

    $scope.search = function () {
        fetchData($scope.searchInput.toLowerCase());
    };

    $(document).bind('nodeClicked', function (e, node) {
        fetchData(node.toLowerCase());
    });

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
        $http.get("/semantic/api/getGraph/" + escape(node))
            .then(function (response) {
                $scope.graph = response.data;

                for (var i = 0; i < response.data.nodes.length; i++) {
                    var n = response.data.nodes[i];

                    if (n.name.toLowerCase() == node) {
                        $scope.selectedNode = n;
                        n.selected = true;
                    }
                }

                semantic.getInstance().processIncomingData(response.data);

                $http.get("/semantic/api/albums/" + $scope.selectedNode.name)
                    .then(function (response) {
                        $scope.albums = response.data;
                        $scope.selectedNode.albums = response.data;

                        $timeout(function () {
                            setupAccordion();
                            $('.info-text').find('a').attr('target','_blank');
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

    $scope.showTracks = function (release, artist) {
        if (release.clicked) {
            release.clicked = false;
            return;
        }

        $http.get("/semantic/api/tracks?artist=" + artist.name + "&album=" + release.name)
            .then(function (response) {
                release.tracks = response.data;

            }, printError);
    }

    $scope.saveNode = function () {
        $http.get("/semantic/api/saveNode/" + $scope.selectedNode.dbId)
            .then(function (response) {
                $.event.trigger('nodeSaved', $scope.selectedNode.name);
            }, printError);
    };

    $scope.getUserGraph = function () {
        $http.get("/semantic/api/getUserGraph/")
            .then(function (response) {
                $scope.graph = response.data;

                $scope.selectedNode = response.data.nodes[0];

                semantic.getInstance().processIncomingData(response.data);

            }, printError);
    };

    $scope.isNodeSaved = function () {
        return $scope.selectedNode.saved;
    };

    $scope.showHideSimilars = function () {
        $.event.trigger('showHideSimilars');
    };

    $("#search-input").autocomplete({
        minLength: 2,
        source: function (request, response) {
            var term = request.term;

            $http.get("/semantic/api/lookup/" + term)
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
});

app.config(function ($sceDelegateProvider, $sceProvider) {
    $sceProvider.enabled(false);
});

$(document).ready(function () {
    $('#wrapper').show();
});