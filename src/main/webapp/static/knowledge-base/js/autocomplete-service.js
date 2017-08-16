var autocompleteService = (function () {

    var cache = {};

    var configureEntity = function ($scope, $http, $q, callback) {
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

            }, select: function (event, ui) {
                this.value = ui.item.value;
                $scope.searchValue = ui.item.value;

                $scope.selectedEntities = [];
                lodService.getItem($scope, $http, $q, ui.item.uri, callback);

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
                            name: $("#entity-input").val(),
                            entityType: 'non-semantic-web'
                        }
                    }
                }
            }
        });
    };

    var configureCategory = function ($scope) {

        $("#category-input").autocomplete({
            minLength: 2,
            source: function (request, response) {
                var cats = [];
                for (var i = 0; i < $scope.categories.length; i++) {
                    var c = $scope.categories[i];

                    if (c.name.toLowerCase().indexOf(request.term.toLowerCase()) != -1)
                        cats.push({id: c.id, label: c.name, value: c.name});
                }

                response(cats);
            }, select: function (event, ui) {
                this.value = ui.item.value;
                $scope.selectedCategory = ui.item.value;

                return false;
            }
        });

        $scope.$watch(function () {
                if (angular.element("#entity-category-input").is(':visible')) {

                    $("#entity-category-input").autocomplete({
                        minLength: 2,
                        source: function (request, response) {
                            var cats = [];
                            for (var i = 0; i < $scope.categories.length; i++) {
                                var c = $scope.categories[i];

                                if (c.name.toLowerCase().indexOf(request.term.toLowerCase()) != -1)
                                    cats.push({id: c.id, label: c.name, value: c.name});
                            }

                            response(cats);
                        }, select: function (event, ui) {
                            if (!_.find($scope.categories, {name: ui.item.value})) {
                                ui.item.value = _.find($scope.categories, {name: 'Other'}).name;
                            }

                            this.value = ui.item.value;
                            $scope.selectedCategory = ui.item.value;
                            $scope.selectedEntity.categoryId = ui.item.id;
                            $scope.selectedEntity.categoryName = ui.item.value;

                            return false;
                        }
                    });
                }
                return;
            }
            , function () {
            });
    }

    return {
        configureCategory: configureCategory,
        configureEntity: configureEntity
    }
})();
