var app = angular.module('app', []);

app.controller('Controller', function ($scope, $http, $q, $sce) {

    $scope.entities = [];
    $http.get('knowledgeBase/api/getAllEntities')
        .then(function (response) {
            response.data.forEach(function (item) {

                $scope.entities.push(item);

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
//                $scope.entities.push(item);

                wikidata.getItem($http, $q, item.wikidataId, function (item) {
                    $scope.selectedEntity.properties = $scope.selectedEntity.properties.concat(item.properties);
                    $scope.entities.push(item);
                });
            });
        }, change: function (event, ui) { // not-selected
            if (ui.item === null) {
                $scope.selectedEntity = {name: $("#entity-input").val()};

                if (isUrl($scope.selectedEntity.name)) {
                    $scope.selectedEntity.entityType = 'web-page';
                } else {
                    $scope.selectedEntity.entityType = 'non-semantic-web';
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
            }, function (err) {
                printError(err);
                $scope.saveResponse = err;
            });
    }
});

app.config(function ($sceDelegateProvider, $sceProvider) {
    $sceProvider.enabled(false);
});