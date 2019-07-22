define(['./module', 'html2canvas', 'pdfMake'], function(module, html2canvas, pdfMake){
  'use strict';
  return module.service('htmlToPdf', ['$translate', '$q', function($translate, $q) {
    return function(domElement, pdfName) {
      return new Promise(function(resolve, reject) {

        if (!domElement) {
          reject("No dom element provided");
          return;
        }

        var PAGE_HEIGHT = 815;
        var PAGE_WIDTH = 580;

        html2canvas(domElement).then(function(canvas) {
          var content = [];
          var fullHeight = canvas.height;

          var canvass = document.createElement('canvas');
          canvass.width = canvas.width;
          canvass.height = canvas.height;
          var context = canvass.getContext('2d');

          if (canvas.width > PAGE_WIDTH) {
            var ratio = canvas.height / canvas.width;
            fullHeight = canvas.height - ((canvas.width - PAGE_WIDTH) * ratio);
          }

          var totalPages = Math.ceil(fullHeight / PAGE_HEIGHT);
          var height = canvas.height;
          if (fullHeight > PAGE_HEIGHT) {
            height = height / (fullHeight / PAGE_HEIGHT);
          }

          for (var page = 0; page < totalPages; page++) {
            context.clearRect(0, 0, canvass.width, canvass.height);
            context.drawImage(canvas, 0, page * height, canvas.width, height, 0, 0, canvas.width, height);

            content.push({
              image: canvass.toDataURL(),
              width: PAGE_WIDTH
            });
          }
          var docDefinition = {
            pageSize: 'A4',
            pageMargins: [10, 10],
            content: content
          };
          if (!pdfName) pdfName = 'generated_pdf';
          pdfMake.createPdf(docDefinition).download(pdfName + '.pdf');

          resolve();
        });
      });
    };
  }]);
});