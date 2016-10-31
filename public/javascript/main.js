var app = angular.module("app", ["ui.bootstrap"]);

app.controller("AppCtrl", ["$scope", "$http", "$uibModal", "$location", function ($scope, $http, $uibModal, $location) {
    $scope.records = [];
    $scope.maxSize = 5;
    $scope.currentPage = 1;
    $scope.pageSize = 20;
    $scope.name = undefined;
    $scope.errors = [];
    $scope.onlyNumbers = /^\d+$/;

    var baseUrl = $location.protocol() + "://" + $location.host() + ":" + $location.port() + "/api/v1/record";

    /**
     * Fetch records from server
     *
     * @param limit maximum number of records to fetch
     * @param offset how many records to skip
     * @param name username search substring
     * @param resetPagination pagination 'currentPage' would be set to 1
     */
    var fetchRecords = function (limit, offset, name, resetPagination) {
        var url = baseUrl + "?";
        if (limit) {
            url = url + "limit=" + limit;
        }
        if (offset) {
            url = url + "&offset=" + offset;
        }
        if (name) {
            url = url + "&name=" + name;
        }
        $http.get(url).then(function (response) {
            if (resetPagination) {
                $scope.currentPage = 1;
            }
            $scope.records = response.data.content;
            $scope.totalItems = response.data.totalElements;
        });
    };

    /**
     * Send new record to server
     *
     * @param record new record
     * @param executeAfter function to execute after getting successful response
     */
    var saveRecord = function (record, executeAfter) {
        $http.post(baseUrl, record).then(function () {
            if (executeAfter) {
                executeAfter();
            }
        }, function (response) {
            $scope.errors = response.data.details;
        });
    };

    /**
     * Send delete record request to server
     * @param id record id
     * @param executeAfter function to execute after getting successful response
     */
    var deleteRecord = function (id, executeAfter) {
        var url = baseUrl + "/" + id;
        $http.delete(url).then(function () {
            if (executeAfter) {
                executeAfter();
            }
        });
    };

    /**
     * Open modal window for creating new record
     */
    var openModal = function () {
        $scope.modalInstance = $uibModal.open({
            templateUrl: "assets/templates/add_record.html",
            scope: $scope
        })
    };

    /**
     * Function that is called when user presses 'create' in modal window
     */
    $scope.create = function () {
        saveRecord($scope.newRecord, function () {
            $scope.modalInstance.close();
            fetchRecords($scope.pageSize, 0, $scope.name);
        });
    };

    /**
     * Dismiss modal window and cancel record creation
     */
    $scope.cancel = function () {
        $scope.modalInstance.dismiss("cancel");
    };

    /**
     * Function that is called when user selects another page
     */
    $scope.pageChanged = function () {
        fetchRecords($scope.pageSize, $scope.currentPage * $scope.pageSize, $scope.name);
    };

    /**
     * Watch changes of the search field
     */
    $scope.$watch("name", function (str) {
        if (str === $scope.name) {
            fetchRecords($scope.pageSize, 0, str, true);
        }
    });

    /**
     * Function that is called when user clicks 'add' to create new record
     */
    $scope.createRecord = function () {
        $scope.newRecord = {
            username: "",
            phone: ""
        };
        $scope.errors = [];
        openModal();
    };

    /**
     * Function that is called when user clicks on trash icon
     * @param id rcord id
     */
    $scope.deleteRecord = function (id) {
        deleteRecord(id, function () {
            fetchRecords($scope.pageSize, 0, $scope.name);
        })
    };
}]);

app.directive("restrictInput", [function () {
    return {
        restrict: "A",
        link: function (scope, element, attrs) {
            var ele = element[0];
            var regex = RegExp(attrs.restrictInput);
            var value = ele.value;
            ele.addEventListener('keyup', function (e) {
                if (regex.test(ele.value)) {
                    value = ele.value;
                } else {
                    ele.value = value;
                }
            });
        }
    }
}]);