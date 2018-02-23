var app = angular.module('app', ['ngDialog']);

app.controller('Controller', function ($scope, $http, $q, $sce, $timeout, ngDialog) {

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
                        $scope.entities.push(item);
                    } else {
                        $scope.entities.push(item);
                    }

                    $http.get('knowledgeBase/api/getAllCategories')
                        .then(function (response) {
                            $scope.categories = response.data;
                            $scope.categories[0].selected = 'selected';

                            for (var i = 0; i < $scope.categories.length; i++) {
                                if ($scope.categories[i].id != 1)
                                    $scope.categories[i].subCategories.unshift({id: -2, name: 'Other'}); // second element

                                $scope.categories[i].subCategories.unshift({id: -1, name: 'All'}); // first element
                            }

                            $scope.selectedCategoryToShow = $scope.categories[0];
                            $scope.selectedCategoryToShow.selectedSubCategory = $scope.categories[0].subCategories[0];

                            getEntitiesByCategory();
                            getEntitiesBySubCategory();
                        }, printError);
                });
            }, printError);

        autocompleteService.configureEntity($scope, $http, $q, afterEntityResponseFetched);
        autocompleteService.configureCategory($scope);

        function afterEntityResponseFetched(response) {
            $scope.propertiesLoading = false;

            $scope.selectedEntity = response;
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

        $scope.entityDoubleClicked = function (e) {
            $scope.showEntity(e);
            $scope.openDetailPopup(e);

        };

        $scope.showEntity = function (e) {
            $scope.showProperties = 0;

            $http.get('knowledgeBase/api/getEntityById/' + e.id)
                .then(function (response) {

                    var entity = response.data;

                    if (entity.properties && entity.properties.length > 0)
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

            if ($scope.selectedCategory) {
                $scope.selectedEntity.categoryId = $scope.selectedCategory.id;
                $scope.selectedEntity.categoryName = $scope.selectedCategory.value;
            } else {
                $scope.selectedEntity.categoryId = 1;
                $scope.selectedEntity.categoryName = "Other";
            }

            $scope.selectedEntity.source = "memory-item";

            $scope.propertiesLoading = true;
            $http.post('knowledgeBase/api/saveEntity', $scope.selectedEntity)
                .then(function (response) {
                    $scope.saveResponse = 'Saved';
                    $scope.propertiesLoading = false;

                    $scope.items.push(response.data);
                    $scope.entities.push(response.data);

                    $scope.selectedEntity = response.data;
                    $scope.selectedEntity.saved = true;
                    $scope.selectedEntity.propertyGroups = pg;

                    getEntitiesByCategory();
                    getEntitiesBySubCategory();
                }, function (err) {
                    printError(err);
                    $scope.saveResponse = err;
                    $scope.propertiesLoading = false;
                });
        };

        $scope.isPropertyLink = function (property) {
            return (property.source == "wikidata" && (property.datatype == "wikibase-item") || (property.datatype == "external-id"))
                || (property.source == "dbpedia" && isUrl(property.value))
                || (property.source == "custom" && isUrl(property.value));
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
            $scope.showProperties = 0;
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
            } else {
                $scope.propertiesLoading = true;

                lodService.fetchPropertyFromRelevantSource($http, $q, property, afterEntityResponseFetched, function () {
                    $scope.propertiesLoading = false;
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

        $scope.showOnlyCustomProperties = function () {
            $('.property-filter .filter a.custom').addClass('selected');
            $('.property-filter .filter a.filtered').removeClass('selected');
            $('.property-filter .filter a.all').removeClass('selected');

            $scope.showProperties = 2;
        };

        $scope.showOnlyFilteredProperties = function () {
            $('.property-filter .filter a.custom').removeClass('selected');
            $('.property-filter .filter a.filtered').addClass('selected');
            $('.property-filter .filter a.all').removeClass('selected');

            $scope.showProperties = 0;
        };

        $scope.showAllProperties = function () {
            $('.property-filter .filter a.custom').removeClass('selected');
            $('.property-filter .filter a.filtered').removeClass('selected');
            $('.property-filter .filter a.all').addClass('selected');

            $scope.showProperties = 1;
        };

        $scope.showProperties = 0; // 0: filtered, 1: all, 2: custom
        $scope.filteredProperties = function () {
            if (!$scope.selectedEntity || !$scope.selectedEntity.propertyGroups) {
                return [];
            }

            var props = [];
            if ($scope.showProperties == 1) {
                for (var i = 0; i < $scope.selectedEntity.propertyGroups.length; i++) {
                    if ($scope.selectedEntity.propertyGroups[i].visibility != 0) {
                        props.push($scope.selectedEntity.propertyGroups[i]);
                    }
                }
            } else if ($scope.showProperties == 0) {

                for (var i = 0; i < $scope.selectedEntity.propertyGroups.length; i++) {
                    if ($scope.selectedEntity.propertyGroups[i].visibility == 3) {
                        props.push($scope.selectedEntity.propertyGroups[i]);
                    }
                }
            }
            else { // 2
                for (var i = 0; i < $scope.selectedEntity.propertyGroups.length; i++) {
                    if ($scope.selectedEntity.propertyGroups[i].source == 'custom') {
                        props.push($scope.selectedEntity.propertyGroups[i]);
                    }
                }
            }

            return props;
        };

        $scope.image = function (e) {
            <!--<img ng-if="!isEntitySaved() && selectedEntity.image"-->
            <!--ng-src="{{selectedEntity.image}}"/>-->
            <!--<img ng-if="isEntitySaved() && selectedEntity.image"-->
            <!--ng-src="knowledgeBase/api/image/{{selectedEntity.image}}"/>-->
            <!--<img ng-if="!selectedEntity.image && selectedEntity.smallImage"-->
            <!--ng-src="knowledgeBase/api/image/{{selectedEntity.smallImage}}"/>-->

            if (!$scope.isEntitySaved() && selectedEntity.image) {
                return e.image;
            } else if ($scope.isEntitySaved() && selectedEntity.image) {
                return "knowledgeBase/api/image/" + e.image;
            } else if (!selectedEntity.image && selectedEntity.smallImage) {
                return "knowledgeBase/api/image/" + e.smallImage;
            }
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

        $scope.removeEntityFromCategory = function (category) {

            if ($scope.clickedEntity.categoryId != 1 && ($scope.clickedEntity.subCategoryId == null || $scope.clickedEntity.subCategoryId == 0)) {
                $http.get('knowledgeBase/api/removeEntityFromCategory?entityId=' + $scope.clickedEntity.id)
                    .then(function (response) {

                        $scope.clickedEntity.categoryId = 1;

                        getEntitiesByCategory();
                        getEntitiesBySubCategory();
                        $('.custom-context-menu.memory-box').fadeOut(200);
                    }, function (err) {
                        printError(err);
                    });
            } else if ($scope.clickedEntity.categoryId != 1 && !($scope.clickedEntity.subCategoryId == null || $scope.clickedEntity.subCategoryId == 0)) {
                $http.get('knowledgeBase/api/removeEntityFromSubCategory?entityId=' + $scope.clickedEntity.id)
                    .then(function (response) {
                        $scope.clickedEntity.subCategoryId = null;

                        getEntitiesByCategory();
                        getEntitiesBySubCategory();
                        $('.custom-context-menu.memory-box').fadeOut(200);
                    }, function (err) {
                        printError(err);
                    });
            } else {
                console.log("ERROR category is already in the other group!!");
            }
        };

        $scope.addEntityToCategory = function (category) {

            if (category.type == 'category') {
                $http.get('knowledgeBase/api/addEntityToCategory?entityId=' + $scope.clickedEntity.id + "&categoryId=" + category.item.id)
                    .then(function (response) {

                        $scope.clickedEntity.categoryId = category.item.id;

                        getEntitiesByCategory();
                        getEntitiesBySubCategory();
                        $('.custom-context-menu.memory-box').fadeOut(200);
                    }, function (err) {
                        printError(err);
                    });
            } else if (category.type == 'subcategory') {
                $http.get('knowledgeBase/api/addEntityToSubCategory?entityId=' + $scope.clickedEntity.id + "&subCategoryId=" + category.item.id)
                    .then(function (response) {
                        $scope.clickedEntity.subCategoryId = category.item.id;

                        getEntitiesByCategory();
                        getEntitiesBySubCategory();
                        $('.custom-context-menu.memory-box').fadeOut(200);
                    }, function (err) {
                        printError(err);
                    });
            } else {
                console.log("ERROR memory box item does not have category type !!!!");
            }
        };

        $scope.showCategoryItems = function ($event, c) {
            $scope.categories.forEach(function (cc) {
                cc.selected = undefined;
            });

            c.selected = 'selected';
            $scope.selectedCategoryToShow = c;
            $scope.selectedCategoryToShow.selectedSubCategory = c.subCategories[0];

            // set up initial memory box context menu items as all subcategories below this category
            $scope.memoryBoxContextMenuItems = [];
            if ($scope.selectedCategoryToShow.id != 1) {
                _.each($scope.selectedCategoryToShow.subCategories, function (sc) {
                    if (sc.id != -1 && sc.id != -2) {
                        $scope.memoryBoxContextMenuItems.push({type: 'subcategory', item: sc});
                    }
                });
            } else {
                _.each($scope.categories, function (c) {
                    if (c.id != 1) {
                        $scope.memoryBoxContextMenuItems.push({type: 'category', item: c});
                    }
                });
            }

            $timeout(function () {
                configureMemoryBoxContextMenu();
            }, 0);
        };

        $scope.memoryBoxContextMenuItems = [];

        function configureMemoryBoxContextMenu() {
            var selectionX, selectionY;
            $(".entity").on("mousedown", function (e) {
                selectionX = e.pageX;
                selectionY = e.pageY;
            });

            $(".entity").on("contextmenu", function (e) {
                return false;
            });

            $(".entity").bind("mousedown", function (e) {

                var id = parseInt($(this).attr('id'));
                _.each($scope.entities, function (e) {
                    if (e.id == id) {
                        $scope.clickedEntity = e;
                    }
                });

                if (e.ctrlKey || e.button == 2 || e.which == 2) {
                    $('.custom-context-menu.memory-box').css({
                        'left': selectionX + 5,
                        'top': selectionY
                    }).fadeIn(200);

                    var l = $scope.memoryBoxContextMenuItems.length;
                    var height = 26 + 26 * l;

                    if ($scope.memoryBoxContextMenuItems.length == 0 && $scope.selectedCategoryToShow.subCategories.length > 2)
                        height -= 21;

                    if ($scope.selectedCategoryToShow.id != 1 && $scope.selectedCategoryToShow.subCategories.length > 2)
                        height += 26;

                    $('.custom-context-menu.memory-box').css('height', height);

                }


            });

            $(".entity").bind("mouseleave", function (e) {
                var t = $(".custom-context-menu.memory-box").position().top;
                var l = $(".custom-context-menu.memory-box").position().left;

                if (e.pageX < l || e.pageY < t) {
                    $('.custom-context-menu.memory-box').fadeOut(200);
                }
            });

            $(".custom-context-menu.memory-box").bind("mouseleave", function (e) {
                $('.custom-context-menu.memory-box').fadeOut(200);
            });
        }

        $scope.showSubCategoryItems = function ($event, sc) {
            $scope.selectedCategoryToShow.subCategories.forEach(function (cc) {
                cc.selected = undefined;
            });

            sc.selected = 'selected';
            $scope.selectedCategoryToShow.selectedSubCategory = sc;

            $scope.memoryBoxContextMenuItems = [];

            $scope.memoryBoxContextMenuItems = [];
            if ($scope.selectedCategoryToShow.id != 1) {
                _.each($scope.selectedCategoryToShow.subCategories, function (sc) {
                    if (sc.id != -1 && sc.id != -2) {
                        $scope.memoryBoxContextMenuItems.push({type: 'subcategory', item: sc});
                    }
                });
            }

            $timeout(function () {
                configureMemoryBoxContextMenu();
            }, 0);
        };

        $scope.openDetailPopup = function () {
            if (!$scope.selectedEntity.id) {
                return;
            }

            ngDialog.open({
                template: 'detail-popup',
                controller: ['$scope', function ($ss) {
                    $ss.selectedEntity = $scope.selectedEntity;
                    $ss.isEntitySaved = $scope.isEntitySaved;

                    $ss.isPropertyLink = $scope.isPropertyLink;
                    $ss.isDbpediaResource = $scope.isDbpediaResource;
                    $ss.saveEntity = $scope.saveEntity;

                    $ss.selectedSideEntities = [];

                    $ss.isWebPageEntity = function () {
                        return $ss.selectedEntity.entityType == 'web-page';
                    };

                    function getInfoSummary(entity) {
                        if (isPerson(entity)) {
                            var birthDateArray = _.filter(entity.properties, function (p) {
                                return p.uri == 'http://dbpedia.org/property/birthDate'; // http://dbpedia.org/ontology/birthDate https://www.wikidata.org/wiki/Property:P569
                            });

                            if (birthDateArray.length == 0) {
                                birthDateArray = _.filter(entity.properties, function (p) {
                                    return p.uri == 'https://www.wikidata.org/wiki/Property:P569'; // http://dbpedia.org/ontology/birthDate https://www.wikidata.org/wiki/Property:P569
                                });
                            }

                            var betterBirthDateString = '';
                            if (birthDateArray.length > 0) {
                                if (new Date(birthDateArray[0].value) == 'Invalid Date') {
                                    betterBirthDateString = birthDateArray[0].value;
                                } else {
                                    var birthDate = new Date(birthDateArray[0].value);
                                    betterBirthDateString = formatDate(birthDate);
                                }
                            }

                            var birthPlaceArray = _.filter(entity.properties, function (p) {
                                return p.uri == 'https://www.wikidata.org/wiki/Property:P19'
                            });

                            var birthPlace = '';
                            if (birthPlaceArray.length > 0)
                                var birthPlace = birthPlaceArray[0].valueLabel;

                            var occupations = _.filter(entity.properties, function (p) {
                                return p.uri == 'https://www.wikidata.org/wiki/Property:P106'
                            });

                            var infoSummary = betterBirthDateString;
                            if (infoSummary.length > 0 && birthPlace.length > 0)
                                infoSummary += ', ';

                            if (birthPlace.length > 0)
                                infoSummary += birthPlace;

                            if (infoSummary.length > 0 && occupations.length > 0)
                                infoSummary += ', ';

                            var len = occupations.length < 1 ? occupations.length : 1; // only the first one
                            for (var i = 0; i < len; i++) {
                                infoSummary += firstLettersUppercase(occupations[i].valueLabel);

                                if (i != len - 1)
                                    infoSummary += ',';
                            }

                            return infoSummary;
                        }
                    }

                    function isPerson(entity) {
                        var personProperty = _.filter(entity.properties, function (p) {
                            return p.uri == 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type' &&
                                (p.value == 'http://xmlns.com/foaf/0.1/Person' || p.value == 'http://dbpedia.org/ontology/Person');
                        });

                        return personProperty && personProperty.length > 0;
                    }

                    $ss.selectedEntity.infoSummary = getInfoSummary($scope.selectedEntity);

                    $scope.$on('ngDialog.opened', function (e, $dialog) {
                        autocompleteService.configureCustomObject($scope.entities, $http, $q, function (response) {
                            setEditTopicMargin();

                            if (response == null || response.entityType == 'non-semantic-web') {
                                $scope.$apply(function () {
                                    $ss.customObject = response;
                                    $ss.selectedSideEntity = response;
                                });
                            } else {
                                $ss.customObject = response;
                                $ss.selectedSideEntity = response;
                            }
                        });

                        autocompleteService.configureCustomProperty($http, $q, function (response) {
                            $ss.customProperty = response;
                        });


                        if ($ss.filteredProperties().length == 0) {
                            $ss.addCustomProperties();
                        }
                    });

                    $ss.showCustomProperties = function () {
                        $('.add-custom-properties').hide();
                        $('.show-custom-properties').show();

                        $('.custom-property-add-title .show').removeClass('not-selected');
                        $('.custom-property-add-title .add').addClass('not-selected');
                    };

                    $ss.addCustomProperties = function () {
                        $('.show-custom-properties').hide();
                        $('.add-custom-properties').show();

                        $('.custom-property-add-title .add').removeClass('not-selected');
                        $('.custom-property-add-title .show').addClass('not-selected');
                    };

                    $ss.showProperties = 0; // 0: filtered, 1: all, 2: custom
                    $ss.filteredProperties = function () {
                        if (!$ss.selectedEntity || !$ss.selectedEntity.propertyGroups) {
                            return [];
                        }

                        var props = [];
                        if ($ss.showProperties == 1) {
                            for (var i = 0; i < $ss.selectedEntity.propertyGroups.length; i++) {
                                if ($ss.selectedEntity.propertyGroups[i].visibility != 0) {
                                    props.push($ss.selectedEntity.propertyGroups[i]);
                                }
                            }
                        } else if ($ss.showProperties == 0) {
                            for (var i = 0; i < $ss.selectedEntity.propertyGroups.length; i++) {
                                if ($ss.selectedEntity.propertyGroups[i].source == 'custom'
                                    || $ss.selectedEntity.propertyGroups[i].visibility == 3) {
                                    props.push($ss.selectedEntity.propertyGroups[i]);
                                }
                            }
                        } else { // 2
                            for (var i = 0; i < $ss.selectedEntity.propertyGroups.length; i++) {
                                if ($ss.selectedEntity.propertyGroups[i].source == 'custom') {
                                    props.push($ss.selectedEntity.propertyGroups[i]);
                                }
                            }
                        }

                        return props;
                    };

                    $ss.showMore = function () {
                        $ss.descriptionShown = $ss.description;
                        $ss.displayShowMore = false;
                        $ss.displayShowLess = true;
                    };

                    $ss.showLess = function () {
                        $ss.descriptionShown = $ss.description.slice(0, 1315);
                        $ss.displayShowLess = false;
                        $ss.displayShowMore = true;
                    };

                    $ss.showOnlyCustomProperties = function () {
                        $('.show-add-custom-properties .filter a.custom').addClass('selected');
                        $('.show-add-custom-properties .filter a.filtered').removeClass('selected');
                        $('.show-add-custom-properties .filter a.all').removeClass('selected');

                        $ss.showProperties = 2;
                    };

                    $ss.showOnlyFilteredProperties = function () {
                        $('.show-add-custom-properties .filter a.custom').removeClass('selected');
                        $('.show-add-custom-properties .filter a.filtered').addClass('selected');
                        $('.show-add-custom-properties .filter a.all').removeClass('selected');

                        $ss.showProperties = 0;
                    };

                    $ss.showAllProperties = function () {
                        $('.show-add-custom-properties .filter a.custom').removeClass('selected');
                        $('.show-add-custom-properties .filter a.filtered').removeClass('selected');
                        $('.show-add-custom-properties .filter a.all').addClass('selected');

                        $ss.showProperties = 1;
                    };

                    $ss.selectedSideEntityIndex = -1;
                    $ss.openLastProperty = function() {
                        $ss.selectedSideEntity = $ss.selectedSideEntities[--$ss.selectedSideEntityIndex];
                    };

                    $ss.openNextProperty = function() {
                        $ss.selectedSideEntity = $ss.selectedSideEntities[++$ss.selectedSideEntityIndex];
                    };

                    $ss.popItemsUntilLastSelectedEntity = function() {
                            for (var j = 0; j < $ss.selectedSideEntities.length - $ss.selectedSideEntityIndex - 1; j++) {
                                $ss.selectedSideEntities.pop();
                            }
                    };

                    // could be called through text (annotationitem index) or
                    // through property box (property object)
                    $ss.openProperty = function (parameter) {
                        if (parameter && parameter.value) {
                            var uri = parameter.value;
                            source = parameter.source;
                        } else {
                            var uri = $ss.annotationEntities[parameter].uri;
                            source = 'dbpedia';
                        }

                        if ($ss.selectedEntity.dbpediaUri == uri) {
                            return;
                        }

                        $ss.popItemsUntilLastSelectedEntity();
                        $ss.selectedSideEntityIndex++;

                        var e = _.find($scope.entities, {dbpediaUri: uri}); // is property(entity) included in database?

                        var ee = _.find($ss.selectedSideEntities, {dbpediaUri: uri}); // is it included in selected list?
                        ee = ee ? ee : _.find($ss.selectedSideEntities, {wikidataId: uri});

                        if (ee) {
                            $ss.selectedSideEntities.push(ee);
                            $ss.selectedSideEntity = ee;

                            return;
                        } else if (e) {

                            $ss.annotationEntityLoading = true;

                            $http.get('knowledgeBase/api/getEntityById/' + e.id)
                                .then(function (response) {
                                    var entity = response.data;

                                    $ss.annotationEntityLoading = false;
                                    $ss.selectedSideEntity = entity;
                                    $ss.selectedSideEntities.push(entity);

                                    setEditTopicMargin();
                                }, function (err) {
                                    printError(err);
                                });
                            return;
                        } else {
                            lodService.fetchPropertyFromRelevantSource($http, $q,
                                {value: uri, source: source},
                                function (response) {
                                    setEditTopicMargin();

                                    $ss.annotationEntityLoading = false;
                                    $ss.selectedSideEntity = response;
                                    $ss.selectedSideEntities.push(response);
                                },
                                function () {
                                    $ss.annotationEntityLoading = false;
                                });
                        }

                    };

                    function setEditTopicMargin() {
                        $timeout(function () {
                            $(".sub-topic .info-image img").on('load', function () {
                                var marginTop = $('.sub-topic').height() - $('.edit-topic').height() - $('.topic').height();
                                if (marginTop < 0) marginTop = 0;
                                $('.edit-topic').css('margin-top', marginTop);
                            });
                        }, 0);
                    }

                    $ss.isAnnotationEntitySaved = function () {
                        if (!$ss.selectedSideEntity) {
                            return true;
                        }

                        var e = _.find($scope.entities, {dbpediaUri: $ss.selectedSideEntity.dbpediaUri});

                        return e !== null && e !== undefined;
                    };

                    var getSelected = function () {
                        var t = '';
                        if (window.getSelection) {
                            t = window.getSelection();
                        } else if (document.getSelection) {
                            t = document.getSelection();
                        } else if (document.selection) {
                            t = document.selection.createRange().text;
                        }
                        return t;
                    }

                    var selectionX, selectionY, lastSelectedText = '';
                    $(document).on("mousedown", function (e) {
                        selectionX = e.pageX;
                        selectionY = e.pageY;
                    });

                    $(document).bind("mouseup", function () {
                        var selectedText = getSelected();
                        if (selectedText != '' && selectedText.toString() != lastSelectedText) {
                            lastSelectedText = selectedText.toString();

                            dbpedia.prefixSearch($http, selectedText, function (results) {
                                $('.custom-context-menu.annotation-results').css({
                                    'left': selectionX + 5,
                                    'top': selectionY - 55
                                }).fadeIn(200);

                                $ss.customAnnotationResults = results;
                            });
                        } else {
                            $('.custom-context-menu.annotation-results').fadeOut(200);
                        }
                    });

                    $ss.openCustomAnnotation = function (annotation) {
                        $('.custom-annotation-results').fadeOut(200);
                        $ss.openProperty({value: annotation.uri});
                    };

                    $ss.saveCustomAnnotation = function (item) {
                        var annotation = {
                            surfaceForm: lastSelectedText,
                            label: item.label,
                            uri: item.uri,
                            referenced_entity_id: $ss.selectedEntity.id
                        };

                        $http.post('knowledgeBase/api/saveCustomAnnotation', annotation)
                            .then(function (response) {

                            }, printError);
                    };


                    if ($scope.selectedEntity.id) {
                        $http.get('knowledgeBase/api/getAnnotationEntities/' + $scope.selectedEntity.id)
                            .then(function (response) {
                                $ss.annotationEntities = response.data;

                                $ss.annotationEntities = $ss.annotationEntities.sort(function (a, b) {
                                    if (a.offset > b.offset)
                                        return 1;
                                    else if (a.offset < b.offset) {
                                        return -1
                                    } else {
                                        return 0;
                                    }
                                });

                                if ($scope.selectedEntity.entityType == 'web-page-annotation')
                                    $scope.selectedEntity.description = $scope.selectedEntity.webPageText;

                                $ss.description = $ss.selectedEntity.description;
                                for (var i = 0; i < $ss.annotationEntities.length; i++) {
                                    console.log('');

                                    var add = i == 0 ? 0 : 43;

                                    var bugAdd = i >= 11 ? 1 : 0;

                                    var before = $ss.description.slice(0, $ss.annotationEntities[i].offset + bugAdd * (i - 10) + add * i);
                                    var after = $ss.description.slice($ss.annotationEntities[i].offset + bugAdd * (i - 10) + $ss.annotationEntities[i].surfaceForm.length + add * i, $ss.description.length);

                                    $ss.description = before + '<a href="#" ng-click="openProperty(' + i + ')">' + $ss.annotationEntities[i].surfaceForm + '</a>' + after;
                                }

                                $ss.descriptionShown = $ss.description;
//                        if ($ss.selectedEntity.description.length > 1315) {
//                            $ss.descriptionShown = $ss.description.slice(0, 1315) + "...";
//                            $ss.displayShowMore = true;
//                        } else {
//                            $ss.descriptionShown = $ss.description;
//                            $ss.displayShowMore = false;
//                        }

//                        $scope.$digest();
                            }, printError);
                    } else {
                        $ss.description = $ss.selectedEntity.description;
                    }

                    $ss.saveCustomProperty = function () {
                        var subject = $.extend({}, $ss.selectedEntity);
                        delete subject.propertyGroups;

//                    if ($ss.customProperty.id) {
//                        $ss.customProperty.metaPropertyId = $ss.customProperty.id;
//                        $ss.customProperty.id = null;
//                    }

                        $http.post('knowledgeBase/api/saveCustomProperty', {subject: subject, predicate: $ss.customProperty, object: $ss.customObject})
                            .then(function (response) {
                                $('#custom-property-input').val('');
                                $('#custom-object-input').val('');

                                $ss.selectedEntity.properties.push(response.data);
                                groupProperties($ss.selectedEntity);

                                $ss.showCustomProperties();
                                $ss.showOnlyCustomProperties();
                            }, printError)
                    };

                    $ss.displayShowNotes = false;
                    $ss.displayHideNotes = true;

                    $scope.showNotes = function () {
                        $('textarea').show();
                        $('textarea').animate({height: '100px'}, 300);

                        $ss.displayShowNotes = false;
                        $ss.displayHideNotes = true;
                    };

                    $scope.hideNotes = function () {
                        $('textarea').animate({height: '0px'}, 300);
                        setTimeout(function () {
                            $('textarea').show()
                        }, 300);

                        $ss.displayShowNotes = true;
                        $ss.displayHideNotes = false;
                    };

                    $ss.getImageUrl = $scope.getImageUrl;

                    $ss.updateEntity = function () {
                        var pg = $ss.selectedEntity.propertyGroups;
                        delete $ss.selectedEntity.propertyGroups;

//                    if (!$scope.selectedEntity.categoryId || $scope.selectedEntity.categoryId == 0) {
//                        $scope.selectedEntity.categoryId = 1;
//                        $scope.selectedEntity.categoryName = "Other";
//                    }

                        $ss.propertiesLoading = true;
                        $http.post('knowledgeBase/api/updateEntity', $ss.selectedEntity)
                            .then(function (response) {
                                $ss.saveResponse = 'Saved';
                                $ss.propertiesLoading = false;

                                $ss.selectedEntity.propertyGroups = pg;
                                $scope.selectedEntity = $ss.selectedEntity;

                                getEntitiesByCategory();
                            }, function (err) {
                                printError(err);
                                $ss.saveResponse = err;
                                $scope.propertiesLoading = false;
                            });
                    };

                    $ss.openRemoveConfirmation = function () {
                        ngDialog.open({
                            template: 'confirmation-popup',
                            controller: ['$scope', function ($sss) {

                                $sss.closeWindow = function () {
                                    $sss.closeThisDialog();
                                };

                                $sss.removeEntity = function () {

                                    $http.get('knowledgeBase/api/removeEntity/' + $scope.selectedEntity.id)
                                        .then(function (response) {
                                            $sss.saveResponse = 'Saved';
                                            $sss.propertiesLoading = false;

                                            var index = -1;
                                            for (var i = 0; i < $scope.entities.length; i++) {
                                                if ($scope.entities[i].id == $scope.selectedEntity.id) {
                                                    index = i;
                                                }
                                            }

                                            window.location.reload(); // TODO without reloading.
                                            $scope.entities.splice(index, 1);
                                            $scope.selectedEntity = null;

                                            getEntitiesByCategory();

                                            $sss.closeThisDialog();
                                            $ss.closeThisDialog();
                                        }, function (err) {
                                            printError(err);
                                            $sss.saveResponse = err;
                                        });
                                };

                            }]});
                    };

                }]});
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
                    entity.propertyGroups.push({uri: p.uri, name: p.name, visibility: p.visibility, values: [p], source: p.source});
                }
            }


            entity.propertyGroups = sortPropertyGroups(entity.propertyGroups);
        };

        function sortPropertyGroups(propertyGroups) {
            var ordered = [], lastCustomIndex = 0, lastDbpediaIndex = 0, lastWikidataIndex = 0;

            for (var i = 0; i < propertyGroups.length; i++) {
//            for (var j = i; j < propertyGroups.length; j++) {
                if (propertyGroups[i].source == 'custom') {
                    ordered.splice(lastCustomIndex++, 0, propertyGroups[i]);
                    lastDbpediaIndex++;
                    lastWikidataIndex++;
                } else if (propertyGroups[i].source == 'dbpedia') {
                    ordered.splice(lastDbpediaIndex++, 0, propertyGroups[i]);
                    lastWikidataIndex++;
                } else if (propertyGroups[i].source == 'wikidata') {
                    ordered.splice(lastWikidataIndex++, 0, propertyGroups[i]);
                }
//            }
            }

            return ordered;
        }

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
            }

            // Initially context menu items are set to all categories, because initial selected category is other
            // and has no subcategories...
            $timeout(function () {
                $scope.memoryBoxContextMenuItems = [];
                _.each($scope.categories, function (c) {
                    if (c.id != 1) {
                        $scope.memoryBoxContextMenuItems.push({type: 'category', item: c});
                    }
                });

                configureMemoryBoxContextMenu();
            }, 0);
        };

        $scope.getImageUrl = function (e) {
            var prefix = "";

            if (!$scope.isEntitySaved() && e.image) {
                return e.image;
            } else if ($scope.isEntitySaved() && e.smallImage) {
                if (!isUrl(e.smallImage))
                    prefix = "knowledgeBase/api/image/";

                return prefix + e.smallImage;
            } else if (!e.smallImage && e.image) {
                if (!isUrl(e.image))
                    prefix = "knowledgeBase/api/image/";

                return prefix + e.image;
            }
        };

        function getEntitiesBySubCategory() {
            for (var i = 0; i < $scope.categories.length; i++) {
                var c = $scope.categories[i];

                for (var k = 0; k < c.subCategories.length; k++) {
                    var sc = c.subCategories[k];
                    sc.entities = [];

                    for (var j = 0; j < $scope.entities.length; j++) {
                        var e = $scope.entities[j];

                        if (sc.id == -2 && c.id == e.categoryId && (e.subCategoryId == 0 || e.subCategoryId == null)) {
                            sc.entities.push(e);
                        } else if (sc.id == -1 && c.id == e.categoryId) {
                            sc.entities.push(e);
                        } else if (e.subCategoryId == sc.id && c.id == e.categoryId) {
                            sc.entities.push(e);
                        }
                    }
                }
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
    }
)
;

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

app.directive('compile', ['$compile', function ($compile) {
    return function (scope, element, attrs) {
        scope.$watch(
            function (scope) {
                return scope.$eval(attrs.compile);
            },
            function (value) {
                element.html(value);
                $compile(element.contents())(scope);
            }
        )
    };
}])