'use strict';
define('job-request-module', ['angular'], function (angular) {
  var customModule = angular.module('kcell.custom.module', []);
  customModule.controller('JobRequest', ['$scope', function ($scope) {
    console.log($scope);
    $scope.var1 = 'First variable';
    $scope.var2 = 'Second variable';
  }]);
  customModule.directive('jobRequest', function () {
    return {
      restrict: 'E',
      template: 
        '<div class="well">'+
          '<div class="row">'+
              '<div class="col-md-4">Requested date: {{requestedDate.value | date: \'dd.MM.yyyy HH:mm\'}}</div>'+
              '<div class="col-md-4">Reason: {{reasonsTitle[reason.value]}}</div>'+
              '<div class="col-md-4">Materials required: {{materialsRequired.value}}</div>'+
          '</div>'+
          '<div class="row">'+
              '<div class="col-md-4">Validity date: {{validityDate.value | date: \'dd.MM.yyyy\'}}</div>'+
              '<div class="col-md-4">Contract: {{servicesTitle[contract.value]}}</div>'+
              '<div class="col-md-4">Leasing required: {{leasingRequired.value}}</div>'+
          '</div>'+
          '<div class="row">'+
              '<div class="col-md-4">SA&O Complaint Id: {{soaComplaintId.value}}</div>'+
              '<div class="col-md-4">Contractor: {{contractorsTitle[contractor.value]}}</div>'+
              '<div class="col-md-4">Site (near end): {{site.value}}</div>'+
          '</div>'+
          '<div class="row">'+
              '<div class="col-md-12">Supplementary files:</div>'+
              '<iframe id="fileDownloadIframe" style="display:none;"></iframe>'+
              '<div class="col-md-12"><a ng-if="supplementaryFile1" href="{{supplementaryFile1.contentUrl}}">{{supplementaryFile1.valueInfo.filename}}</a></div>'+
              '<div class="col-md-12"><a ng-if="supplementaryFile2" href="{{supplementaryFile1.contentUrl}}">{{supplementaryFile2.valueInfo.filename}}</a></div>'+
              '<div class="col-md-12"><a ng-if="supplementaryFile3" href="{{supplementaryFile1.contentUrl}}">{{supplementaryFile3.valueInfo.filename}}</a></div>'+
              '<div class="col-md-12"><a ng-if="supplementaryFile4" href="{{supplementaryFile1.contentUrl}}">{{supplementaryFile4.valueInfo.filename}}</a></div>'+
              '<div class="col-md-12"><a ng-if="supplementaryFile5" href="{{supplementaryFile1.contentUrl}}">{{supplementaryFile5.valueInfo.filename}}</a></div>'+
          '</div>'+
          '<div class="row">'+
              '<div class="col-md-12">Works:</div>'+
              '<div class="col-md-12" ng-repeat="work in works">'+
                  '<a href="javascript:void(0)">{{$index+1}}. {{worksTitle[work.sapServiceNumber]}}, works qty: {{work.quantity}}, materials qty: {{work.materialQuantity}} {{unitsTitle[work.unit]}}, on sites: {{work.sites}} (Near end)</a>'+
              '</div>'+
          '</div>'+
          '<div class="row">'+
              '<div class="col-md-12">Explanation of works:</div>'+
              '<div class="col-md-12">{{explanation.value}}</div>'+
          '</div>'+
      '</div>'
    };
  });
  return customModule;
});