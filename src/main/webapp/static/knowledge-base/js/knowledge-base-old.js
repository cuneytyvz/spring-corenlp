var app = angular.module('app', []);

app.controller('Controller', function ($scope, $http, $q, $sce, $timeout) {

    $scope.items = [];
    $scope.entities = [];
    $scope.webPages = [];
    $scope.selectedEntities = [];

    $http.get('knowledgeBase/api/getAllMetaproperties')
        .then(function (response) {
            $scope.metaProperties = response.data;
        }, printError);

    $http.get('knowledgeBase/api/getEntityList')
        .then(function (response) {
            response.data.forEach(function (item) {
//                groupProperties(item);

                $scope.items.push(item);

                if (item.entityType == 'web-page') {
                    $scope.webPages.push(item);
                } else {
                    $scope.entities.push(item);
                }

                $http.get('knowledgeBase/api/getAllCategories')
                    .then(function (response) {
                        $scope.categories = response.data;
                        $scope.categories[0].selected = 'selected';
                        $scope.selectedCategoryToShow = $scope.categories[0];

                        getEntitiesByCategory();
                    }, printError);
            });
        }, printError);

    autocompleteService.configureEntity($scope, $http, $q, afterEntityResponseParsed);
    autocompleteService.configureCategory($scope);

    function afterEntityResponseParsed($scope) {
        for (var i = 0; i < $scope.selectedEntity.properties.length; i++) {
            var p = $scope.selectedEntity.properties[i];

            var mp = _.find($scope.metaProperties, {uri: p.uri});
            if (mp)
                p.visibility = mp.visibility; // Add filter to the view
            else
                p.visibility = 0;
        }

        $scope.selectedEntity.propertyGroups = [];
        groupProperties($scope.selectedEntity);

        $scope.selectedEntities.push($scope.selectedEntity);
        $scope.items.push($scope.selectedEntity);

        $scope.existingElements = getExistingProperties();
    }

    $scope.showEntity = function (e) {
        $scope.propertiesFiltered = true;

        $http.get('knowledgeBase/api/getEntityById/' + e.id)
            .then(function (response) {
                var entity = response.data;
                groupProperties(entity);

                $scope.selectedEntity = entity;
                $scope.selectedEntities = [entity];

                if ($scope.selectedEntity.entityType == 'web-page') {
                    $scope.existingEntities = getExistingEntities();
                } else {
                    $scope.existingElements = getExistingProperties();
                }
            }, function (err) {
                printError(err);
            });
    };

    $scope.saveEntity = function () {
        var pg = $scope.selectedEntity.propertyGroups;
        delete $scope.selectedEntity.propertyGroups;

        if (!$scope.selectedEntity.categoryId || $scope.selectedEntity.categoryId == 0) {
            $scope.selectedEntity.categoryId = 1;
            $scope.selectedEntity.categoryName = "Other";
        }

        $scope.propertiesLoading = true;
        $http.post('knowledgeBase/api/saveEntity', $scope.selectedEntity)
            .then(function (response) {
                $scope.saveResponse = 'Saved';
                $scope.propertiesLoading = false;

                $scope.selectedEntity.propertyGroups = pg;
                $scope.items.push($scope.selectedEntity);
                $scope.entities.push($scope.selectedEntity);
            }, function (err) {
                printError(err);
                $scope.saveResponse = err;
            });
    };

    $scope.isPropertyLink = function (property) {
        return (property.source == "wikidata" && (property.datatype == "wikibase-item") || (property.datatype == "external-id"))
            || (property.source == "dbpedia" && isUrl(property.value));
    };

    $scope.isDbpediaResource = function (property) {
        return property.value.indexOf('dbpedia') != -1;
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
        $scope.propertiesFiltered = true;
        $scope.selectedEntity = {name: property.valueLabel};

        var e = _.find($scope.entities, {dbpediaUri: property.value}); // is property(entity) included in database?
        e = e ? e : _.find($scope.entities, {wikidataId: property.value});

        var ee = _.find($scope.selectedEntities, {dbpediaUri: property.value}); // is it included in selected list?
        ee = ee ? ee : _.find($scope.selectedEntities, {wikidataId: property.value});

        if (ee) {
            var i = _.indexOf($scope.selectedEntities, ee);

            for (var j = 0; j < $scope.selectedEntities.length - i; j++) {
                $scope.selectedEntities.pop();
            }

            $scope.selectedEntity = ee;

            return;
        } else if (e) {
            $http.get('knowledgeBase/api/getEntityById/' + e.id)
                .then(function (response) {
                    var entity = response.data;
                    groupProperties(entity);

                    $scope.selectedEntity = entity;
                    $scope.selectedEntities.push(entity);

                    $scope.existingElements = getExistingProperties();
                }, function (err) {
                    printError(err);
                });
            return;
        }

        lodService.fetchPropertyFromRelevantSource($scope, $http, $q, property, afterEntityResponseParsed);
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

    $scope.removePropertyFilter = function () {
        $scope.propertiesFiltered = false;
    };

    $scope.addPropertyFilter = function () {
        $scope.propertiesFiltered = true;
    };

    $scope.propertiesFiltered = true;
    $scope.filteredProperties = function () {
        if (!$scope.selectedEntity || !$scope.selectedEntity.propertyGroups) {
            return [];
        }

        var props = [];
        if (!$scope.propertiesFiltered) {
            for (var i = 0; i < $scope.selectedEntity.propertyGroups.length; i++) {
                if ($scope.selectedEntity.propertyGroups[i].visibility != 0) {
                    props.push($scope.selectedEntity.propertyGroups[i]);
                }
            }
        } else {
            for (var i = 0; i < $scope.selectedEntity.propertyGroups.length; i++) {
                if ($scope.selectedEntity.propertyGroups[i].visibility == 3) {
                    props.push($scope.selectedEntity.propertyGroups[i]);
                }
            }
        }

        return props;
    };

    $scope.showCategory = function (c) {
        if (c.hidden) {
            $('#entity-row-' + c.id).slideDown();
            c.hidden = false;
        }
        else {
            $('#entity-row-' + c.id).slideUp();
            c.hidden = true;
        }
    };

    $scope.showCategoryItems = function ($event,c) {
        $scope.categories.forEach(function (cc) {
            cc.selected = undefined;
        });

        c.selected = 'selected';
        $scope.selectedCategoryToShow = c;
    };

    function groupProperties(entity) {
        $scope.existing = [];
        for (var i = 0; i < entity.properties.length; i++) {
            var p = entity.properties[i];

            if (!entity.propertyGroups)
                entity.propertyGroups = [];

            var pp = _.find(entity.propertyGroups, {uri: p.uri});
            if (pp) {
                pp.values.push(p);
            } else {
                entity.propertyGroups.push({uri: p.uri, name: p.name, visibility: p.visibility, values: [p]});
            }
        }
    };

    function getExistingEntities() {
        if (!$scope.selectedEntity) {
            return [];
        }

        var elements = [];
        for (var i = 0; i < $scope.selectedEntity.annotationEntities.length; i++) {
            var ae = $scope.selectedEntity.annotationEntities[i];

            for (var k = 0; k < $scope.items.length; k++) {
                var item = $scope.items[k];

                if (ae.name == item.name) {
                    elements.push(item);
                }
            }
        }

        return elements;
    }

    function getExistingProperties() {
        if (!$scope.selectedEntity || !$scope.selectedEntity.propertyGroups) {
            return [];
        }

        var elements = [];
        for (var i = 0; i < $scope.selectedEntity.propertyGroups.length; i++) {
            var pg = $scope.selectedEntity.propertyGroups[i];
            var newPg = {uri: pg.uri, name: pg.name, visibility: pg.visibility, values: []};

            for (var j = 0; j < pg.values.length; j++) {
                var p = pg.values[j];

                for (var k = 0; k < $scope.entities.length; k++) {
                    var item = $scope.entities[k];

                    if (p.value && ((p.source == 'dbpedia' && item.dbpediaUri == p.value) || (p.source == 'wikidata' && item.wikidataId == p.value))) {
                        newPg.values.push(p);
                    }
                }
            }

            if (newPg.values.length > 0) {
                elements.push(newPg);
            }
        }

        return elements;
    };

    function getEntitiesByCategory() {
        for (var i = 0; i < $scope.categories.length; i++) {
            var c = $scope.categories[i];
            c.entities = [];

            for (var j = 0; j < $scope.entities.length; j++) {
                var e = $scope.entities[j];

                if (e.categoryId == c.id) {
                    c.entities.push(e);
                }
            }

            c.entities2row = [
                []
            ];
            c.entities2row[0] = c.entities.slice(c.entities.length / 2);
            c.entities2row[1] = c.entities.slice(0, c.entities.length / 2);
        }
    };

    $scope.mouseOver = function ($event, entity) {
        var $img = $($event.target);

        var w = $img[0].width;
        var h = $img[0].height;

        var hShrink = Math.round(h * 2 / 100);
        var wShrink = Math.round(w * 2 / 100);
        $img.animate({
            width: '-=' + wShrink,
            height: '-=' + hShrink,
            'margin-left': '+=' + wShrink,
            'margin-top': '+=' + hShrink / 2
        }, {
            duration: 300
        });

        if (entity.smallImage) {
            var $text = $img.parent().parent().find('.entity-text');
            $text.fadeIn(300);
        }

        var $overlay = $img.parent().find('.overlay');
        $overlay.css('left', '15px');
        $overlay.css('top', hShrink);
        $overlay.css('width', w);
        $overlay.css('height', h);
//        $overlay.show();
    };

    $scope.mouseLeave = function ($event, entity) {
        var $img = $($event.target);

        var w = $img[0].width;
        var h = $img[0].height;

        var hShrink = Math.round(h * 2 / 100);
        var wShrink = Math.round(w * 2 / 100);
        $img.animate({
            width: '+=' + wShrink,
            height: '+=' + hShrink,
            'margin-left': '-=' + wShrink,
            'margin-top': '-=' + hShrink / 2
        }, {
            duration: 300
        });

        if (entity.smallImage) {
            var $text = $img.parent().parent().find('.entity-text');
            $text.fadeOut(300);
        }

//        $overlay = $($event.target).parent().find('.overlay').hide();
    };
});

app.config(function ($sceDelegateProvider, $sceProvider) {
    $sceProvider.enabled(false);
});

$(document).ready(function () {
    $('.wrapper').show();
});

app.directive('orientable', function () {
    return {
        link: function (scope, element, attrs) {

            element.bind("load", function (e) {
                var RATIO = 0.75;
                var DISPLAY_HEIGHT = 240;

                var parent = jQuery(this).parent().parent();
                if (this.height <= DISPLAY_HEIGHT) {
                    var pad = (DISPLAY_HEIGHT - this.height) / 2;
                    parent.css("padding-top", pad);
                    parent.css("padding-bottom", pad);
                } else {
                    //jQuery(this).css("height", 234);
                }

                if (this.naturalHeight > this.naturalWidth) {
                    if (this.className.indexOf('vertical') == -1)
                        this.className += " vertical";

                    if (this.naturalWidth / this.naturalHeight < RATIO) {
                        var expectedWidth = this.naturalHeight * RATIO;
                        var naturalPad = (expectedWidth - this.naturalWidth) / 2;
                        var pad = DISPLAY_HEIGHT / this.naturalHeight * naturalPad;

                        parent.css("padding-right", pad);
                        parent.css("padding-left", pad);

                        if (this.height <= DISPLAY_HEIGHT) {
                            var pad = (DISPLAY_HEIGHT - this.height);
                            parent.css("padding-top", pad);
                            parent.css("padding-bottom", 0);
                        } else {
                            //jQuery(this).css("height", 234);
                        }
                    }
                } else {
                    if (this.className.indexOf('horizontal') == -1)
                        this.className += " horizontal";
                }
            });
        }
    }
});

app.directive('orientablediv', function () {
    return {
        link: function (scope, element, attrs) {

            element.bind("load", function (e) {
                var RATIO = 0.75;
                var DISPLAY_HEIGHT = 240;

                var parent = jQuery(this).parent();
                if (this.height <= DISPLAY_HEIGHT) {
                    var pad = (DISPLAY_HEIGHT - this.height) / 2;
                    parent.css("padding-top", pad);
                    parent.css("padding-bottom", pad);
                } else {
                    //jQuery(this).css("height", 234);
                }

                if (this.naturalHeight > this.naturalWidth) {
                    if (this.className.indexOf('vertical') == -1)
                        this.className += " vertical";

                    if (this.naturalWidth / this.naturalHeight < RATIO) {
                        var expectedWidth = this.naturalHeight * RATIO;
                        var naturalPad = (expectedWidth - this.naturalWidth) / 2;
                        var pad = DISPLAY_HEIGHT / this.naturalHeight * naturalPad;

                        parent.css("padding-right", pad);
                        parent.css("padding-left", pad);

                        if (this.height <= DISPLAY_HEIGHT) {
                            var pad = (DISPLAY_HEIGHT - this.height);
                            parent.css("padding-top", pad);
                            parent.css("padding-bottom", 0);
                        } else {
                            //jQuery(this).css("height", 234);
                        }
                    }
                } else {
                    if (this.className.indexOf('horizontal') == -1)
                        this.className += " horizontal";
                }
            });
        }
    }
});