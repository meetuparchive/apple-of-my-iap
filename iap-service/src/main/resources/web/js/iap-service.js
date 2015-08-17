angular.module('app', ['services'])
  .controller('controller', ['$scope', '$q', 'Plans', 'Subs', 'Apple',
    function($scope, $q, Plans, Subs, Apple) {

      var loadSubs = function() {
        Subs.getSubs().success(function(res) {
          $scope.subs = res;
        });
      }

      $scope.setStatus = function(receipt) {
        Subs.setStatus(receipt, $scope.selectedSubStatus[receipt].code).success(function() {
          loadSubs();
        });
      }

      $scope.renewSub = function(receipt) {
        Subs.renewSub(receipt).success(function() {
          loadSubs();
        });
      };

      $scope.cancelSub = function(receipt) {
        Subs.cancelSub(receipt).success(function() {
          loadSubs();
        });
      }

      $scope.createSub = function() {
        Subs.createSub($scope.selectedPlan.productId).success(function(res) {
          loadSubs();
        });
      };

      $scope.refundTransaction = function(receipt, transactionId) {
        Subs.refundTransaction(receipt, transactionId).success(function() {
          loadSubs();
        });
      };

      $scope.verifyResponse = function(receipt) {
        $scope.response = {
          "receipt": receipt,
          "data": "..."
        };

        Apple.verifyResponse(receipt).success(function(res) {
          $scope.response = {
            "receipt": receipt,
            "data": JSON.stringify(res, null, 2)
          };
        });
      }

      $scope.clearSubs = function() {
        Subs.clearSubs().success(function() {
          $('#clear-data-modal').modal('hide');
          loadSubs();
        })
      };

      Plans.getPlans().success(function(res) {
        $scope.plans = res;
        $scope.selectedPlan = $scope.plans[0];
      });

      $scope.statuses = [
        {"code": "0"  , "name":"Valid Receipt"},
        {"code": "21000", "name":"Bad Envelope"},
        {"code": "21002", "name":"Bad Receipt"},
        {"code": "21003", "name":"Unauthorized Receipt"},
        {"code": "21004", "name":"Shared Secret Mismatch"},
        {"code": "21005", "name":"Server Unavailable"},
        {"code": "21006", "name":"SubscriptionExpired"},
        {"code": "21007", "name":"Test To Production"},
        {"code": "21008", "name":"Production To Test"}
      ];

      $scope.statusByCode = (function(statuses){
        var byCode = {};

        for(i = 0; i < statuses.length; i++) {
          var status = statuses[i];
          byCode[status.code] = status;
        }

        return byCode;
      })($scope.statuses)

      $scope.selectedSubStatus = {};

      loadSubs()
    }]);

angular.module('services', [])
  .factory('Apple', function($http) {
    return {
      'verifyResponse': function(receipt) {
        return $http.post("/verifyReceipt", {"receipt-data": receipt});
      }
    }
  })
  .factory('Plans', function($http) {
    return {
      'getPlans': function() {
        return $http.get("/plans");
      }
    };
  })
  .factory('Subs', function($http) {
    return {
      'getSubs': function() {
        return $http.get("/subs");
      },
      'createSub': function(productId, statusCode) {
        return $http.post("/subs", {"productId":productId});
      },
      'renewSub': function(receipt) {
        return $http.post("/subs/" + receipt + "/renew")
      },
      'cancelSub': function(receipt) {
        return $http.post("/subs/" + receipt + "/cancel")
      },
      'refundTransaction': function(receipt, transactionId) {
      	return $http.post("/subs/" + receipt + "/refund/" + transactionId)
      },
      'clearSubs': function() {
        return $http.post("/subs/clear");
      },
      'setStatus': function(receipt, statusCode) {
      	return $http.post("/subs/" + receipt, {"status": statusCode});
      }
    }
  });
