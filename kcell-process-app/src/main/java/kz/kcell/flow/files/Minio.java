package kz.kcell.flow.files;

import io.minio.MinioClient;
import io.minio.Result;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.extern.apachecommons.CommonsLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public Minio(@Value("${minio.url:http://localhost:9000}") String minioUrl,
                 @Value("${minio.access.key:AKIAIOSFODNN7EXAMPLE}") String minioAccessKey,
                 @Value("${minio.secret.key:wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY}") String minioSecretKey)
        throws InvalidPortException, InvalidEndpointException, IOException, InvalidKeyException, NoSuchAlgorithmException,
        InsufficientDataException, NoResponseException, InvalidBucketNameException, XmlPullParserException, InternalException,
        RegionConflictException, ErrorResponseException {

        minioClient = new MinioClient(minioUrl, minioAccessKey, minioSecretKey);
    }

    @EventListener
    protected void makeBucket(ApplicationReadyEvent event)
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
}
