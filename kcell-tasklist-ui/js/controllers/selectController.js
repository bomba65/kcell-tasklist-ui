define(['./module','jquery'], function(app) {
    "use strict";
    app.controller('selectpicker', [function () {
        $('.selectpicker').selectpicker();
    }]);
});