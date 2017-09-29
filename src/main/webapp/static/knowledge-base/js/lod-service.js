var lodService = (function () {

    var getItem = function ($scope, $http, $q, uri, callback) {
        $scope.propertiesLoading = true;
        dbpedia.getItem($http, uri, function (item) {
            $scope.selectedEntity = item;

            wikidata.getItem($http, $q, item.wikidataId, function (wikidataItem) {
                parseWikidataResponse($scope, wikidataItem);

                $scope.propertiesLoading = false;

                callback($scope);
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

    var fetchPropertyFromRelevantSource = function ($scope, $http, $q, property, callback) {
        $scope.propertiesLoading = true;

        if (property.source == 'wikidata') {
            wikidata.getItem($http, $q, property.value, function (wikidataItem) {
                if (!wikidataItem) {
                    $scope.propertiesLoading = false;
                    return;
                }

                $scope.selectedEntity = wikidataItem;

                callback($scope);
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
                    parseWikidataResponse($scope,wikidataItem);
                    callback($scope);

                    $scope.propertiesLoading = false;
                });

            });
        }
    };

    return {
        getItem: getItem,
        fetchPropertyFromRelevantSource: fetchPropertyFromRelevantSource
    }
})();
