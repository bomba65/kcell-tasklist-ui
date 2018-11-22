define(['./../module'], function(module){
    'use strict';
    module.directive('aftersalesSipProtocol', function ($rootScope, $http) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '=',
                showNewFields: '=',
                modifyConnection: '=',
                pbxData: "="
            },
            link: function(scope, element, attrs) {

                scope.$watch('pbxData', function (value) {
                    if (!scope.pbxData) scope.pbxData = {};
                });


                scope.$on("aftersalesPBXBINCheck", function(e, result) {
                    if (!result) return;
                    if (result.sip) parseFromPBX(JSON.parse(result.sip));
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
            templateUrl: './js/directives/aftersalesPBX/sipProtocol.html'
        };
    });
});