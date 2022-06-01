define(['./../module'], function(module){
    'use strict';
    module.directive('aftersalesRevolvingNumbersTsSipProtocol', function ($rootScope, $http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '=',
                showNewFields: '=',
                modifyConnection: '=',
                pbxData: "=",
                hiddenFields: "="
            },
            link: function(scope, element, attrs) {
                scope.$watch('pbxData', function (value) {
                    if (!scope.pbxData) scope.pbxData = {};
                });
                scope.$on("aftersalesPBXBINCheck", function(e, result) {
                    if (!result || result.aftersales) return;
                    if (result.techSpecs.connectionType == 'SIP over internet' && result.sip) parseFromPBX(JSON.parse(result.sip));
                });
                console.log(scope.data)

                  scope.$on('tab-selected', function(e, tabName) {
                    if (tabName === 'techSpec') {
                      var tmp = scope.data.description;
                      scope.data.description = 'this is because of tabset';
                      $timeout(function () {
                        scope.data.description = tmp;
                      });
                    }
                  });

                function parseFromPBX(sip) {
                    if (!sip) return;
                    if (sip.authorizationType) scope.data.authorizationType = sip.authorizationType;
                    if (sip.ipVoiceTraffic) scope.data.curPublicVoiceIP = sip.ipVoiceTraffic;
                    if (sip.ipSignaling) scope.data.curSignalingIP = sip.ipSignaling;
                    if (sip.transportLayerProtocol) scope.data.transProtocol = sip.transportLayerProtocol;
                    if (sip.signalingPort) scope.data.signalingPort = sip.signalingPort;
                    if (sip.sessionCount) scope.data.sessionsCount = sip.sessionCount;
                    if (sip.voiceTrafficPortStart) scope.data.voicePortStart = sip.voiceTrafficPortStart;
                    if (sip.voiceTrafficPortEnd) scope.data.voicePortEnd = sip.voiceTrafficPortEnd;
                    if (sip.preferredCoding) scope.data.coding = sip.preferredCoding;

                    scope.pbxData = JSON.parse(JSON.stringify(scope.data));
                    scope.pbxData.fetched = true;
                }
            },
            templateUrl: './js/directives/aftersalesRevolvingNumbers/tsSipProtocol.html'
        };
    });
});