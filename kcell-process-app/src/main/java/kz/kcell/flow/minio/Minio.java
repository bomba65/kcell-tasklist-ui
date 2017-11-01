package kz.kcell.flow.minio;

import io.minio.MinioClient;
import io.minio.errors.*;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@Log
public class Minio {

    private static final Logger log = LoggerFactory.getLogger(Minio.class);

    private final MinioClient minioClient;
    private final String bucketName = "uploads";
    private final String tempBucketName = "tempuploads";

    @Autowired
    public Minio(@Value("${minio.url:http://localhost:9000}") String minioUrl,
                 @Value("${minio.access.key:AKIAIOSFODNN7EXAMPLE}") String minioAccessKey,
                 @Value("${minio.secret.key:wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY}") String minioSecretKey)
        throws InvalidPortException, InvalidEndpointException, IOException, InvalidKeyException, NoSuchAlgorithmException,
        InsufficientDataException, NoResponseException, InvalidBucketNameException, XmlPullParserException, InternalException,
        RegionConflictException, ErrorResponseException {

        minioClient = new MinioClient(minioUrl, minioAccessKey, minioSecretKey);
    }

    @EventListener
    public void makeBucket(ApplicationReadyEvent event)
        throws IOException, InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException, InternalException,
        NoResponseException, InvalidBucketNameException, XmlPullParserException, RegionConflictException, ErrorResponseException {
        try {
            minioClient.makeBucket(bucketName);
        } catch (ErrorResponseException e) {
            if ("BucketAlreadyOwnedByYou".equals(e.errorResponse().code())) {
                log.debug("Bucket is already there, noop");
            } else {
                throw e;
            }
        }
        try {
            minioClient.makeBucket(tempBucketName);
        } catch (ErrorResponseException e) {
            if ("BucketAlreadyOwnedByYou".equals(e.errorResponse().code())) {
                log.debug("Temp Bucket is already there, noop");
            } else {
                throw e;
            }
        }
    }


    public void copyObject(String bucketName, String objectName, String destBucketName, String destObjectName)
        throws InvalidKeyException, InvalidBucketNameException, NoSuchAlgorithmException, InsufficientDataException,
        NoResponseException, ErrorResponseException, InternalException, IOException, XmlPullParserException,
        InvalidArgumentException {
        minioClient.copyObject(bucketName, objectName, destBucketName, destObjectName);
    }

    public void removeObject(String bucketName, String objectName)
        throws InvalidBucketNameException, NoSuchAlgorithmException, InsufficientDataException, IOException,
        InvalidKeyException, NoResponseException, XmlPullParserException, ErrorResponseException,
        InternalException {
        minioClient.removeObject(bucketName, objectName);
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getTempBucketName() {
        return tempBucketName;
    }
}
