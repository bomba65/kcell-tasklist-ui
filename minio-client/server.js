var Minio = require('minio')

// Instantiate the minio client with the endpoint
// and access keys as shown below.
var client = new Minio.Client({
    endPoint: 'minio',
    port: 9000,
    secure: false,
    accessKey: 'AKIAIOSFODNN7EXAMPLE',
    secretKey: 'wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY'
});

// express is a small HTTP server wrapper, but this works with any HTTP server
const server = require('express')();

server.set('trust proxy', true);

client.makeBucket('uploads', 'us-east-1', function(err) {
    if (err){
		if(err.code !== 'BucketAlreadyOwnedByYou'){
			return console.log(err);
		}
	}

	server.get('/minio-client/presignedUploadUrl', (req, res) => {
		console.log(req.protocol);
		console.log(req.hostname);
	    client.presignedPutObject('uploads', req.query.name, (err, url) => {
	        if (err) throw err
	        res.end(req.protocol+"://"+req.hostname+url.substring('http://minio:9000'.length));
	    })
	})

	server.get('/minio-client/presignedDownloadUrl', (req, res) => {
	    client.presignedGetObject('uploads', req.query.name, 24*60*60, (err, url) => {
	        if (err) throw err
	        res.end(req.protocol+"://"+req.hostname+url.substring('http://minio:9000'.length));
	    });
	})

	server.get('/minio-client/', (req, res) => {
	    res.sendFile(__dirname + '/index.html');
	})

	server.listen(8080);
});