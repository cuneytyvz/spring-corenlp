var app = angular.module('app', []);

app.controller('Controller', function ($scope, $http, $q, $sce) {

    $scope.items = [];
    $scope.entities = [];
    $scope.webPages = [];
    $http.get('knowledgeBase/api/getAllEntities')
        .then(function (response) {
            response.data.forEach(function (item) {
                $scope.items.push(item);

                if (item.entityType == 'web-page') {
                    $scope.webPages.push(item);
                } else {
                    $scope.entities.push(item);
                }

            });
        }, printError);

    var cache = {};

    $("#entity-input").autocomplete({
        minLength: 2,
        source: function (request, response) {
            var term = request.term;
            if (term in cache) {
                response(cache[ term ]);
                return;
            }

            dbpedia.prefixSearch($http, request.term, function (results) {
                cache[ term ] = response;
                response(results);
            });

//            wikidata.prefixSearch($http, request.term, function (results) {
//                cache[ term ] = response;
//                response(results);
//            });

        }, select: function (event, ui) {
            this.value = ui.item.value;
            $scope.searchValue = ui.item.value;

//            wikidata.getItem($http, $q, ui.item.wikidataId, function (item) {
//                $scope.selectedEntity = item;
//                $scope.entities.push(item);
//            });

            dbpedia.getItem($http, ui.item.uri, function (item) {
                $scope.selectedEntity = item;

                wikidata.getItem($http, $q, item.wikidataId, function (wikidataItem) {
                    $scope.selectedEntity.properties = $scope.selectedEntity.properties.concat(wikidataItem.properties);
                    $scope.selectedEntity.description = wikidataItem.description;
                    $scope.items.push($scope.selectedEntity);
                });
            });
        }, change: function (event, ui) { // not-selected
            if (ui.item === null) {
                if (isUrl($("#entity-input").val())) {
                    $scope.selectedEntity = {
                        name: '',
                        webUri: $("#entity-input").val(),
                        entityType: 'web-page'
                    };

//                    $http.jsonp( $scope.selectedEntity.description)
//                        .then(function (response) {
//                            $scope.selectedEntity.name = response.data;
//                        },
//                        function (err) {
//                            console.error("ERROR: " + JSON.stringify(err));
//                        });
                } else {
                    $scope.selectedEntity = {
                        description: $("#entity-input").val(),
                        entityType: 'non-semantic-web'
                    }
                }
            }
        }
    });

    $("#category-input").autocomplete({
        minLength: 2,
        source: function (request, response) {
            var term = request.term;
            if (term in cache) {
                response(cache[ term ]);
                return;
            }

            $http.get('knowledgeBase/api/getAllCategories')
                .then(function (resp) {
                    var results = [];
                    resp.data.forEach(function (r) {
                        results.push({id: r.id, label: r.name, value: r.name});
                    });

                    response(results);
                }, printError);
        }, select: function (event, ui) {
            this.value = ui.item.value;
            $scope.selectedCategory = ui.item.value;

            return false;
        }
    });

    $scope.showEntity = function (e) {
        $scope.selectedEntity = e;
    };

    $scope.saveEntity = function () {
        $http.post('knowledgeBase/api/saveEntity', $scope.selectedEntity)
            .then(function (response) {
                $scope.saveResponse = 'Saved';
                location.reload();
            }, function (err) {
                printError(err);
                $scope.saveResponse = err;
            });
    }
});

app.config(function ($sceDelegateProvider, $sceProvider) {
    $sceProvider.enabled(false);
});

$(document).ready(function() {
    $('.wrapper').show();
});