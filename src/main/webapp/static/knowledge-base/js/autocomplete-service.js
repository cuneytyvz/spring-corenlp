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
                        cats.push({id: c.id, label: c.name, value: c.name, subCategories : c.subCategories});
                }

                response(cats);
            }, select: function (event, ui) {
                $("#entity-category-input").val(ui.item.value);
                this.value = ui.item.value;

                $scope.selectedCategory = {id: ui.item.id, value: ui.item.value, subCategories: ui.item.subCategories};
                $scope.selectedEntity.categoryId = ui.item.id;
                $scope.selectedEntity.categoryName = ui.item.value;

                console.log(JSON.stringify(ui.item));

                return false;
            }, change: function (event, ui) { // not-selected
                $("#entity-category-input").val($("#category-input").val());
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
                                    cats.push({id: c.id, label: c.name, value: c.name, subCategories : c.subCategories});
                            }

                            response(cats);
                        }, select: function (event, ui) {
                            if (!_.find($scope.categories, {name: ui.item.value})) {
                                ui.item.value = _.find($scope.categories, {name: 'Other'}).name;
                            }

                            $("#category-input").val(ui.item.value);
                            this.value = ui.item.value;

                            $scope.selectedCategory = {id: ui.item.id, value: ui.item.value, subCategories: ui.item.subCategories};
                            $scope.selectedEntity.categoryId = ui.item.id;
                            $scope.selectedEntity.categoryName = ui.item.value;

                            console.log(JSON.stringify(ui.item));

                            return false;
                        }, change: function (event, ui) { // not-selected
                            $("#category-input").val($("#entity-category-input").val());
                        }
                    });
                }
                return;
            }
            , function () {
            });

        $("#subcategory-input").autocomplete({
            minLength: 2,
            source: function (request, response) {
                var subcats = [];
                for (var i = 0; i < $scope.selectedCategory.subCategories.length; i++) {
                    var sc = $scope.selectedCategory.subCategories[i];

                    if (sc.categoryId == $scope.selectedCategory.id && sc.name.toLowerCase().indexOf(request.term.toLowerCase()) != -1)
                        subcats.push({id: sc.id, label: sc.name, value: sc.name});
                }

                response(subcats);
            }, select: function (event, ui) {
                $("#entity-subcategory-input").val(ui.item.value);
                this.value = ui.item.value;

                $scope.selectedSubCategory = {id: ui.item.id, value: ui.item.value};
                $scope.selectedEntity.subCategoryId = ui.item.id;
                $scope.selectedEntity.subCategoryName = ui.item.value;

                console.log(JSON.stringify(ui.item));

                return false;
            }, change: function (event, ui) { // not-selected
                $("#entity-subcategory-input").val($("#subcategory-input").val());
            }
        });

        $scope.$watch(function () {
                if (angular.element("#entity-subcategory-input").is(':visible')) {

                    $("#entity-subcategory-input").autocomplete({
                        minLength: 2,
                        source: function (request, response) {
                            var subcats = [];
                            for (var i = 0; i < $scope.selectedCategory.subCategories.length; i++) {
                                var sc = $scope.selectedCategory.subCategories[i];

                                if (sc.categoryId == $scope.selectedCategory.id && sc.name.toLowerCase().indexOf(request.term.toLowerCase()) != -1)
                                    subcats.push({id: sc.id, label: sc.name, value: sc.name});
                            }

                            response(subcats);
                        }, select: function (event, ui) {
                            if (!_.find($scope.categories, {name: ui.item.value})) {
                                ui.item.value = _.find($scope.categories, {name: 'Other'}).name;
                            }

                            $("#subcategory-input").val(ui.item.value);
                            this.value = ui.item.value;

                            $scope.selectedSubCategory = {id: ui.item.id, value: ui.item.value};
                            $scope.selectedEntity.subcategoryId = ui.item.id;
                            $scope.selectedEntity.subcategoryName = ui.item.value;

                            console.log(JSON.stringify(ui.item));

                            return false;
                        }, change: function (event, ui) { // not-selected
                            $("#subcategory-input").val($("#entity-subcategory-input").val());
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
