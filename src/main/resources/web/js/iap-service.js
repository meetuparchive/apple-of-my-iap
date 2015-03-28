angular.module('app', ['services'])
  .controller('controller', ['$scope', 'Plans',
    function($scope, Plans) {
      Plans.getPlans().success(function(res) {
        $scope.plans = res
        $scope.selectedPlan = $scope.plans[0]
      });

      $scope.createReceipt = function() {
        //Receipts.create($scope.receipt)
        console.log($scope.selectedPlan)
      };
    }]);

angular.module('services', [])
  .factory('Plans', function($http) {
    return {
      'getPlans': function() {
        return $http.get("/plans");
      }
    };
  });
  // .factory('Receipts', function ($resource) {
  //     return $resource('/createReceipt', {}, {
  //         create: { method: 'POST' }
  //     })
  // });