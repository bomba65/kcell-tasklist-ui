package kz.kcell.flow.files;

import io.minio.MinioClient;
import io.minio.errors.*;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@CommonsLog
public class Minio {

    private final MinioClient minioClient;
    private final String bucketName = "uploads";
    private final String tempBucketName = "tempuploads";

    @Autowired
    public Minio(@Value("${minio.url:http://localhost:9002}") String minioUrl,
                 @Value("${minio.access.key:AKIAIOSFODNN7EXAMPLE}") String minioAccessKey,
                 @Value("${minio.secret.key:wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY}") String minioSecretKey)
        throws InvalidPortException, InvalidEndpointException {

        minioClient = new MinioClient(minioUrl, minioAccessKey, minioSecretKey);
    }

    @EventListener
    protected void makeBucket(ApplicationReadyEvent event) throws IOException, InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException, InternalException,
        NoResponseException, InvalidBucketNameException, XmlPullParserException, RegionConflictException, ErrorResponseException {
        makeBucket(bucketName);
        makeBucket(tempBucketName);
    }

    private void makeBucket(String bucketName) throws IOException, InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException, InternalException,
        NoResponseException, InvalidBucketNameException, XmlPullParserException, RegionConflictException, ErrorResponseException {
        try {
            minioClient.makeBucket(bucketName);
        } catch (ErrorResponseException e) {
            if ("BucketAlreadyOwnedByYou".equals(e.errorResponse().code())) {
                log.debug("Bucket " + bucketName + " is already there, noop");
            } else {
                throw e;
            }
        }
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getTempBucketName() {
        return tempBucketName;
    }

    public void moveToPermanentStorage(String tempPath, String path) throws IOException, InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException, InternalException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException, InvalidArgumentException {
        minioClient.copyObject(this.getTempBucketName(), tempPath, this.getBucketName(), path);
    }

    public void saveFile(String path, InputStream stream, String contentType) throws IOException, InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException, InternalException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException, InvalidArgumentException {
        minioClient.putObject(this.getBucketName(), path, stream, contentType);
    }

    public InputStream getObject(String path) throws IOException, InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException, InvalidArgumentException, InternalException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException {
        return minioClient.getObject(this.bucketName, path);
    }
}
