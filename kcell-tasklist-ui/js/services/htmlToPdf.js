define(['./module', 'html2canvas', 'pdfMake'], function(module, html2canvas, pdfMake){
  'use strict';
  return module.service('htmlToPdf', ['$translate', '$q', function($translate, $q) {
    return function(domElement, pdfName) {
      if (!domElement) return;

      const PAGE_HEIGHT = 700;
      const PAGE_WIDTH = 500;

      const content = [];

      function getPngDimensions (base64) {
        const header = atob(base64.slice(22, 70)).slice(16, 24);
        const uint8 = Uint8Array.from(header, c => c.charCodeAt(0));
        const dataView = new DataView(uint8.buffer);

        return {
          width: dataView.getInt32(0),
          height: dataView.getInt32(4)
        };
      }

      const splitImage = (img, content, callback) => () => {

        const canvas = document.createElement('canvas');
        const ctx    = canvas.getContext('2d');
        const printHeight = img.height * PAGE_WIDTH / img.width;

        canvas.width = PAGE_WIDTH;

        for (let pages = 0; printHeight > pages * PAGE_HEIGHT; pages++) {
          /* Don't use full height for the last image */
          canvas.height = Math.min(PAGE_HEIGHT, printHeight - pages * PAGE_HEIGHT);
          ctx.drawImage(img, 0, -pages * PAGE_HEIGHT, canvas.width, printHeight);
          content.push({ image: canvas.toDataURL(), margin: [0, 5], width: PAGE_WIDTH });
        }

        callback();
      };

      function next () {
        /* add other content here, can call addImage() again for example */
        pdfMake.createPdf({ content }).download();
      };

      html2canvas(domElement).then(function(canvas) {
        const image = canvas.toDataURL();
        // const { width, height } = getPngDimensions(image);
        // const printHeight = height * PAGE_WIDTH / width;

        /*if (printHeight > PAGE_HEIGHT) {
          const img = new Image();
          img.onload = splitImage(img, content, next);
          img.src = image;
          return;
        }

        content.push({ image, margin: [0, 5], width: PAGE_WIDTH });
        next();*/

        var docDefinition = {
          pageSize: 'A4',
          content: [{
            image: image,
            width: 500
          }]
        };
        if (!pdfName) pdfName = 'generated_pdf';
        pdfMake.createPdf(docDefinition).download(pdfName + '.pdf');
      });
    };
  }]);
});