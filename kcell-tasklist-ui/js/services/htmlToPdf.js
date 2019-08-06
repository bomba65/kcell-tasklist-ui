define(['./module', 'html2canvas', 'pdfMake'], function(module, html2canvas, pdfMake){
  'use strict';
  return module.service('htmlToPdf', ['$http', function($http) {
    return function(domElement, options) {
      return new Promise(function(resolve, reject) {

        if (!domElement) {
          reject("No dom element provided");
          return;
        }

        options = options || {};

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
          var fileName = 'generated_file';
          if (options.fileName) fileName = options.fileName;
          fileName += '.pdf';

          if (options.download) {
            pdfMake.createPdf(docDefinition).download(fileName);
          }

          if (options.upload) {
            pdfMake.createPdf(docDefinition).getBlob(function(blob) {

              var uploadPath = 'generatedPDFs/' + fileName;
              if (options.uploadPath) uploadPath = options.uploadPath;

              var userName = 'htmlToPdfService';
              if (options.userName) userName = options.userName;

              var fileToUpload = {
                name: fileName,
                path: uploadPath,
                created: new Date(),
                createdBy: userName,
              };
              $http({method: 'GET', url: '/camunda/uploads/put/' + fileToUpload.path, transformResponse: [] }).then(function(response) {
                $http.put(response.data, blob, {headers: {'Content-Type': undefined}}).then(function () {
                  resolve();
                }).catch(function (error) {
                  reject(error);
                });
              }).catch(function(error) {
                reject(error);
              });

              resolve();
            });
          } else resolve();
        });
      });
    };
  }]);
});
