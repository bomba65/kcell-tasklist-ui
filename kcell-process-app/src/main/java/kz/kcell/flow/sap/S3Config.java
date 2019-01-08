package kz.kcell.flow.sap;

import com.amazonaws.services.s3.AmazonS3;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.aws.outbound.S3MessageHandler;
import org.springframework.integration.aws.support.S3RemoteFileTemplate;
import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.messaging.MessageHandler;

import java.io.File;
import java.util.Arrays;

@Configuration
@Profile("!sftp")
@Log
public class S3Config {

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${s3.bucket.jojr:jojr}")
    private String jojrBucketName;

    @Value("${sftp.bucket.pr:prfiles}")
    private String prBucketName;

    @Value("${s3.bucket.fa:fafiles}")
    private String faBucketName;

    @EventListener
    protected void makeBucket(ApplicationReadyEvent event) {
        if(!amazonS3.doesBucketExistV2(jojrBucketName)){
            amazonS3.createBucket(jojrBucketName);
        }
        if(!amazonS3.doesBucketExistV2(prBucketName)){
            amazonS3.createBucket(prBucketName);
        }
        if(!amazonS3.doesBucketExistV2(faBucketName)){
            amazonS3.createBucket(faBucketName);
        }
    }

    @Bean
    @ServiceActivator(inputChannel = "toJoJrChannel")
    public MessageHandler toJoJrS3MessageHandler() {
        return new S3MessageHandler(amazonS3, jojrBucketName);
    }

    @Bean
    @ServiceActivator(inputChannel = "toPrChannel")
    public MessageHandler toPrS3MessageHandler() {
        return new S3MessageHandler(amazonS3, prBucketName);
    }

    @Bean
    @ServiceActivator(inputChannel = "toFaChannel")
    public MessageHandler toFaS3MessageHandler() {
        return new S3MessageHandler(amazonS3, faBucketName);
    }

    @Bean
    @ServiceActivator(inputChannel = "toPrStatusProcessedChannel")
    public MessageHandler toPrStatusProcessedS3MessageHandler() {
        return new S3MessageHandler(amazonS3, prBucketName + "/" + "PR_status_processed");
    }

    @Bean
    public RemoteFileTemplate template() {
        return new S3RemoteFileTemplate(amazonS3);
    }

    @MessagingGateway
    public interface UploadGateway {

        @Gateway(requestChannel = "toJoJrChannel")
        void uploadJrJo(File file);

        @Gateway(requestChannel = "toPrChannel")
        void uploadPr(File file);

        @Gateway(requestChannel = "toFaChannel")
        void uploadFa(File file);

        @Gateway(requestChannel = "toPrStatusProcessedChannel")
        void uploadPrStatusProcessed(File file);
    }
}
