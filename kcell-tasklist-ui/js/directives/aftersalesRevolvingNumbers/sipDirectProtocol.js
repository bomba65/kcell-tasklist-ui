define(['./../module'], function(module){
    'use strict';
    module.directive('aftersalesRevolvingNumbersSipDirectProtocol', function ($rootScope, $http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '=',
                modifyConnection: '=',
                pbxData: "=",
                hiddenFields: "="
            },
            link: function(scope, element, attrs) {
                scope.$watch('pbxData', function (value) {
                    if (!scope.pbxData) scope.pbxData = {};
                });

                scope.calculateCapacity = function () {
                  if (scope.data.dirCoding.length > 0 && scope.data.dirSessionsCount != undefined) {
                    if (scope.data.dirCoding === 'g711') {
                      scope.data.dirMinChannelCapacity = Math.round((87.2 * scope.data.dirSessionsCount / 1024) * 100) / 100
                    } else if (scope.data.dirCoding === 'g729') {
                      scope.data.dirMinChannelCapacity = Math.round((32.2 * scope.data.dirSessionsCount / 1024) * 100) / 100
                    }
                    
                  }
                }

                scope.$on("aftersalesPBXBINCheck", function(e, result) {
                    if (!result || result.aftersales) return;
                    if (result.techSpecs.connectionType == 'SIP cable direct' && result.sip) parseFromPBX(JSON.parse(result.sip));
                });

                  // scope.$on('tab-selected', function(e, tabName) {
                  //   if (tabName === 'techSpec') {
                  //     var tmp = scope.data.description;
                  //     scope.data.description = 'this is because of tabset';
                  //     $timeout(function () {
                  //       scope.data.description = tmp;
                  //     });
                  //   }
                  // });

                function parseFromPBX(sip) {

                  
                    if (!sip) return;

                    if (sip.transportLayerProtocol) scope.data.transLayerProtocol = sip.transportLayerProtocol;
                    if (sip.publicStaticIp) scope.data.sipDirectPublicIP = sip.publicStaticIp;
                    if (sip.signalingPort) scope.data.dirSignalingPort = sip.signalingPort;
                    if (sip.voiceTrafficPort) scope.data.dirVoicePort = sip.voiceTrafficPort;
                    if (sip.sessionCount) scope.data.dirSessionsCount = sip.sessionCount;
                    if (sip.preferredCoding) scope.data.dirCoding = sip.preferredCoding;
                    if (sip.minChannelCapacity) scope.data.dirMinChannelCapacity = sip.minChannelCapacity;

                    scope.pbxData = JSON.parse(JSON.stringify(scope.data));
                    scope.pbxData.fetched = true;
                }
            },
            templateUrl: './js/directives/aftersalesRevolvingNumbers/sipDirectProtocol.html'
        };
    });
});