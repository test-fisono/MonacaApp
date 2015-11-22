var app = angular.module('angular_sample', ['onsen', 'ngRoute']);

// app.config(function($routeProvider){
//     $routeProvider.when('/user', {templateUrl: 'user.html', controller: function(){}});
//     $routeProvider.otherwise({redirectTo: '/'});
// });

app.factory('AuthService', function($q, $timeout){
    var _user = null;
    return {
        login: function(email, password){
            var deferred = $q.defer();
            $timeout(function(){
                if (email != null && password != null)
                {
                    _user = {email: email};
                    deferred.resolve();
                }
                else
                {
                    deferred.reject();
                }
            }, 500);
            return deferred.promise;
        },
        logout: function(){
            _user = null;
            return $q.all();
        }
    };
});

app.controller('loginCtrl', function($scope, $location, AuthService){
    $scope.login = function(){
        $scope.disabled = true;
        AuthService.login($scope.email, $scope.password)
            .then(function(){
                //$location.path('/');
                $scope.alert = {msg: ""};
                myNavigator.pushPage("user.html", { animation: "slide" });
            })
            .catch(function(){
                $scope.alert = {msg: "Login failed"};
            })
            .finally(function(){
                $scope.email = "";
                $scope.password = "";
                $scope.disabled = false;
            })
        ;
    };
});