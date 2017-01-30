'use strict';
define('job-request-module', ['angular'], function (angular) {
  var customModule = angular.module('kcell.custom.module', []);
  customModule.directive('jobRequest', function () {
    return {
      restrict: 'E',
      scope: {
        jobModel: '='
      },
      template: 
        '<div class="well">'+
          '<div class="row">'+
              '<div class="col-md-4"><b>Requested date</b>: {{jobModel.requestedDate.value | date: \'dd.MM.yyyy HH:mm\'}}</div>'+
              '<div class="col-md-4"><b>Reason</b>: {{jobModel.reasonsTitle[jobModel.reason.value]}}</div>'+
              '<div class="col-md-4"><b>Materials required</b>: {{jobModel.materialsRequired.value}}</div>'+
          '</div>'+
          '<div class="row">'+
              '<div class="col-md-4"><b>Validity date</b>: {{jobModel.validityDate.value | date: \'dd.MM.yyyy\'}}</div>'+
              '<div class="col-md-4"><b>Contract</b>: {{jobModel.servicesTitle[jobModel.contract.value]}}</div>'+
              '<div class="col-md-4"><b>Leasing required</b>: {{jobModel.leasingRequired.value}}</div>'+
          '</div>'+
          '<div class="row">'+
              '<div class="col-md-4"><b>SA&O Complaint Id</b>: {{jobModel.soaComplaintId.value}}</div>'+
              '<div class="col-md-4"><b>Contractor</b>: {{jobModel.contractorsTitle[jobModel.contractor.value]}}</div>'+
              '<div class="col-md-4"><b>Site (near end)</b>: {{jobModel.site.value}}</div>'+
          '</div>'+
          '<br/>'+
          '<div class="row">'+
              '<div class="col-md-12"><b>Supplementary files</b>:</div>'+
              '<iframe id="fileDownloadIframe" style="display:none;"></iframe>'+
              '<div class="col-md-12" ng-if="jobModel.supplementaryFile1.contentUrl">&nbsp;&nbsp;&nbsp;&nbsp;<a href="{{jobModel.supplementaryFile1.contentUrl}}">{{jobModel.supplementaryFile1.valueInfo.filename}}</a></div>'+
              '<div class="col-md-12" ng-if="jobModel.supplementaryFile2.contentUrl">&nbsp;&nbsp;&nbsp;&nbsp;<a href="{{jobModel.supplementaryFile2.contentUrl}}">{{jobModel.supplementaryFile2.valueInfo.filename}}</a></div>'+
              '<div class="col-md-12" ng-if="jobModel.supplementaryFile3.contentUrl">&nbsp;&nbsp;&nbsp;&nbsp;<a href="{{jobModel.supplementaryFile3.contentUrl}}">{{jobModel.supplementaryFile3.valueInfo.filename}}</a></div>'+
              '<div class="col-md-12" ng-if="jobModel.supplementaryFile4.contentUrl">&nbsp;&nbsp;&nbsp;&nbsp;<a href="{{jobModel.supplementaryFile4.contentUrl}}">{{jobModel.supplementaryFile4.valueInfo.filename}}</a></div>'+
              '<div class="col-md-12" ng-if="jobModel.supplementaryFile5.contentUrl">&nbsp;&nbsp;&nbsp;&nbsp;<a href="{{jobModel.supplementaryFile5.contentUrl}}">{{jobModel.supplementaryFile5.valueInfo.filename}}</a></div>'+
          '</div>'+
          '<br/>'+
          '<div class="row" ng-if="!jobModel.showPrice">'+
              '<div class="col-md-12"><b>Works</b>:</div>'+
              '<div class="col-md-12" ng-repeat="work in jobModel.jobWorks.value">'+
                  '&nbsp;&nbsp;&nbsp;&nbsp;{{$index+1}}. {{jobModel.worksTitle[work.sapServiceNumber]}}, works qty: {{work.quantity}}, materials qty: {{work.materialQuantity}} {{jobModel.unitsTitle[work.unit]}}, on sites: {{work.sites}} (Near end)'+
              '</div>'+
          '</div>'+
          '<div class="row" ng-if="jobModel.showPrice">'+
              '<div class="col-md-6"><b>Works</b>:</div>'+
              '<div class="col-md-2"><b>Price</b>:</div>'+
              '<div class="col-md-2"><b>Transportation 8%</b></div>'+
              '<div class="col-md-2"><b>Total</b>:</div>'+
              '<div ng-repeat="work in jobModel.jobWorks">'+
                '<div class="col-md-6">'+
                    '&nbsp;&nbsp;&nbsp;&nbsp;{{$index+1}}. {{jobModel.worksTitle[work.sapServiceNumber]}}, works qty: {{work.quantity}}, materials qty: {{work.materialQuantity}} {{jobModel.unitsTitle[work.unit]}}, on sites: {{work.sites}} (Near end)'+
                '</div>'+
                '<div class="col-md-2"><b>{{work.price}} тенге</b></div>'+
                '<div class="col-md-2"><b>{{work.transportationPrice}} тенге</b></div>'+
                '<div class="col-md-2"><b>{{work.totalPrice}} тенге</b></div>'+
              '</div>'+
          '</div>'+
          '<hr/>'+
          '<div class="row">'+
              '<div class="col-md-6"><b>Total:</b></div>'+
              '<div class="col-md-2"><b>{{jobModel.jobWorksPriceTotal}} тенге</b></div>'+
              '<div class="col-md-2"><b>{{jobModel.jobWorksTransportationTotal}} тенге</b></div>'+
              '<div class="col-md-2"><b>{{jobModel.jobWorksTotal}} тенге</b></div>'+
          '</div>'+
          '<br/>'+
          '<div class="row">'+
              '<div class="col-md-12"><b>Explanation of works</b>: {{jobModel.explanation.value}}</div>'+
          '</div>'+
          '<div class="row" ng-if="jobModel.sapPRNo.value">'+
              '<div class="col-md-12"><b>SAP Purchase Request No</b>: {{jobModel.sapPRNo.value}} <a href="{{jobModel.sapPRFileXLS.contentUrl}}">(Download PR)</a></div>'+
          '</div>'+
          '<div class="row" ng-if="jobModel.kcellWarehouseMaterialsList.contentUrl">'+
              '<div class="col-md-12"><a href="{{jobModel.kcellWarehouseMaterialsList.contentUrl}}">Kcell Warehouse Materials List (xls)</a></div>'+
          '</div>'+
          '<div class="row" ng-if="jobModel.contractorZIPWarehouseMaterialsList.contentUrl">'+
              '<div class="col-md-12"><a href="{{jobModel.contractorZIPWarehouseMaterialsList.contentUrl}}">Contractor ZIP Warehouse Materials List (xls)</a></div>'+
          '</div>'+
          '<div class="row" ng-if="jobModel.sapTransferRequestFile.contentUrl">'+
              '<div class="col-md-12"><a href="{{jobModel.sapTransferRequestFile.contentUrl}}">SAP Transport Request File (xls)</a></div>'+
          '</div>'+
          '<div class="row" ng-if="jobModel.eLicenseResolutionFile.contentUrl">'+
              '<div class="col-md-12"><a href="{{jobModel.eLicenseResolutionFile.contentUrl}}">E-License resolution file</a></div>'+
          '</div>'+
      '</div>'
    };
  });
  return customModule;
});