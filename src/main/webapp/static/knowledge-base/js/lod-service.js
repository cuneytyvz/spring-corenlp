var lodService = (function () {

    var getItem = function ($http, $q, uri, callback) {
        dbpedia.getItem($http, uri, function (item) {
            var response = item;

            wikidata.getItem($http, $q, item.wikidataId, function (wikidataItem) {
                if (wikidataItem) {
                    response.properties = response.properties.concat(wikidataItem.properties);
                    response.shortDescription = wikidataItem.description;

                    if (!response.image)
                        response.image = wikidataItem.image;
                }

                callback(response);
            });
        });
    };

    function parseWikidataResponse($scope, wikidataItem) {
        if (wikidataItem) {
            $scope.selectedEntity.properties = $scope.selectedEntity.properties.concat(wikidataItem.properties);
            $scope.selectedEntity.shortDescription = wikidataItem.description;

            if (!$scope.selectedEntity.image)
                $scope.selectedEntity.image = wikidataItem.image;

//            if (wikidataItem.image)
//                $scope.selectedEntity.image = wikidataItem.image;
        }
    }

    var fetchPropertyFromRelevantSource = function ($http, $q, property, callback,errCallback) {
        if (property.source == 'wikidata') {
            wikidata.getItem($http, $q, property.value, function (wikidataItem) {
                if (!wikidataItem) {
                    errCallback();
                    return;
                }

                callback(wikidataItem);

            });
        } else {
            dbpedia.getItem($http, property.value, function (item) {
                if (!item) {
                    errCallback();
                    return;
                }

                var response = item;

                wikidata.getItem($http, $q, item.wikidataId, function (wikidataItem) {
                    if (wikidataItem) {
                        response.properties = response.properties.concat(wikidataItem.properties);
                        response.shortDescription = wikidataItem.description;

                        if (!response.image)
                            response.image = wikidataItem.image;
                    }

                    callback(response);
                });

            });
        }
    };

    return {
        getItem: getItem,
        fetchPropertyFromRelevantSource: fetchPropertyFromRelevantSource
    }
})();
