var app = angular.module('app', []);

app.controller('Controller', function ($scope, $http, $q, $sce) {

    $scope.items = [];
    $scope.entities = [];
    $scope.webPages = [];
    $scope.selectedEntities = [];

    $http.get('knowledgeBase/api/getAllMetaproperties')
        .then(function (response) {
            $scope.metaProperties = response.data;
        }, printError);

    $http.get('knowledgeBase/api/getAllEntities')
        .then(function (response) {
            response.data.forEach(function (item) {
                $scope.groupProperties(item);

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
                    if (wikidataItem) {
                        $scope.selectedEntity.properties = $scope.selectedEntity.properties.concat(wikidataItem.properties);
                        $scope.selectedEntity.shortDescription = wikidataItem.description;

//                        $scope.selectedEntity.image = wikidataItem.image;
                    }

                    for (var i = 0; i < $scope.selectedEntity.properties.length; i++) {
                        var p = $scope.selectedEntity.properties[i];

                        var mp = _.find($scope.metaProperties, {uri: p.uri});
                        if (mp)
                            p.visible = mp.visible; // Add filter to the view
                        else
                            p.visible = false;
                    }

                    $scope.selectedEntity.propertyGroups = [];
                    $scope.groupProperties($scope.selectedEntity);

                    $scope.selectedEntities = [$scope.selectedEntity];
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
        $scope.selectedEntities = [e];
    };

    $scope.saveEntity = function () {
        delete $scope.selectedEntity.propertyGroups;

        $http.post('knowledgeBase/api/saveEntity', $scope.selectedEntity)
            .then(function (response) {
                $scope.saveResponse = 'Saved';
                location.reload();
            }, function (err) {
                printError(err);
                $scope.saveResponse = err;
            });
    };

    $scope.isPropertyLink = function (property) {
        return (property.source == "wikidata" && property.datatype == "wikibase-item") || ( property.source == "dbpedia" && isUrl(property.value));
    };

    $scope.goTo = function (property) {
        if (property.source == "wikidata" && property.datatype == "wikibase-item") {
            return "https://www.wikidata.org/wiki/" + property.value;
        } else if (property.source == "dbpedia" && isUrl(property.value)) {
            return property.value;
        } else {
        }
    };

    $scope.openProperty = function (property) {
        $scope.selectedEntity = {name: property.valueLabel};

        var e = _.find($scope.entities, {dbpediaUri: property.value}); // is property(entity) included in database?
        var ee = _.find($scope.selectedEntities, {dbpediaUri: property.value}); // is it included in selected list?
        if (ee) {
            var i = _.indexOf($scope.selectedEntities, ee);

            for (var j = 0; j < $scope.selectedEntities.length - i; j++) {
                $scope.selectedEntities.pop();
            }

            $scope.selectedEntity = ee;

            return;
        } else if (e) {
            $scope.selectedEntity = e;
            $scope.selectedEntities.push(e);

            return;
        }

        $scope.propertiesLoading = true;
        if (property.source == 'wikidata') {
            wikidata.getItem($http, $q, property.value, function (wikidataItem) {
                if (!wikidataItem) {
                    $scope.propertiesLoading = false;
                    return;
                }

                $scope.selectedEntity = wikidataItem;

                for (var i = 0; i < $scope.selectedEntity.properties.length; i++) {
                    var p = $scope.selectedEntity.properties[i];

                    var mp = _.find($scope.metaProperties, {uri: p.uri});
                    if (mp)
//                        p.visible = mp.visible; // Add filter to the view
                        p.visible = true; // Add filter to the view
                    else
//                        p.visible = false;
                        p.visible = true;
                }

                $scope.groupProperties($scope.selectedEntity);

                $scope.selectedEntities.push($scope.selectedEntity);
                $scope.items.push($scope.selectedEntity);
                $scope.propertiesLoading = false;
            });
        } else {
            dbpedia.getItem($http, property.value, function (item) {
                if (!item) {
                    $scope.propertiesLoading = false;
                    return;
                }
                $scope.selectedEntity = item;

                wikidata.getItem($http, $q, item.wikidataId, function (wikidataItem) {
                    if (wikidataItem) {
                        $scope.selectedEntity.properties = $scope.selectedEntity.properties.concat(wikidataItem.properties);
                    $scope.selectedEntity.shortDescription = wikidataItem.description;

                        $scope.selectedEntity.image = wikidataItem.image;
                    }

                    $scope.selectedEntity.propertyGroups = [];
                    for (var i = 0; i < $scope.selectedEntity.properties.length; i++) {
                        var p = $scope.selectedEntity.properties[i];

                        var mp = _.find($scope.metaProperties, {uri: p.uri});
                        if (mp)
                            p.visible = mp.visible; // Add filter to the view
                        else
                            p.visible = false;
                    }

                    $scope.groupProperties($scope.selectedEntity);

                    $scope.selectedEntities.push($scope.selectedEntity);
                    $scope.items.push($scope.selectedEntity);
                    $scope.propertiesLoading = false;
                });

            });
        }
    };

    $scope.isEntitySaved = function () {
        if (!$scope.selectedEntity) {
            return true;
        }

        var e = _.find($scope.entities, {dbpediaUri: $scope.selectedEntity.dbpediaUri});

        return e !== null && e !== undefined;
    };

    $scope.openLastEntity = function () {
        $scope.selectedEntity = $scope.selectedEntities[$scope.selectedEntities.length - 2];
        $scope.selectedEntities.pop();
    };

    $scope.openLastSecondEntity = function () {
        $scope.selectedEntity = $scope.selectedEntities[$scope.selectedEntities.length - 3];
        $scope.selectedEntities.pop();
        $scope.selectedEntities.pop();
    };

    $scope.propertyValue = function (property) {
        if (!property.valueLabel.isEmpty) {
            return property.valueLabel;
        } else {
            return property.value;
        }
    };

    $scope.groupProperties = function (entity) {
        for (var i = 0; i < entity.properties.length; i++) {
            var p = entity.properties[i];

            if (!entity.propertyGroups)
                entity.propertyGroups = [];

            var pp = _.find(entity.propertyGroups, {uri: p.uri});
            if (pp) {
                pp.values.push(p);
            } else {
                entity.propertyGroups.push({uri: p.uri, name: p.name, visible: p.visible, values: [p]});
            }
        }
    }
});

app.config(function ($sceDelegateProvider, $sceProvider) {
    $sceProvider.enabled(false);
});

$(document).ready(function () {
    $('.wrapper').show();
});