package kz.kcell.flow.sap;

import com.jcraft.jsch.ChannelSftp;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.FileNameGenerator;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.filters.SftpSimplePatternFileListFilter;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizer;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizingMessageSource;
import org.springframework.integration.sftp.outbound.SftpMessageHandler;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

import javax.annotation.Resource;
import java.io.File;

@Configuration
@Profile("sftp")
@Log
public class SftpConfig {

    @Value("${sftp.host:195.47.255.121}")
    private String sftpHost;

    @Value("${sftp.port:22}")
    private int sftpPort;

    @Value("${sftp.user:KWMS}")
    private String sftpUser;

    @Value("${sftp.password:#{null}}")
    private String sftpPassword;

    @Value("${sftp.remote.directory.to.fa:/home/KWMS/FA_Geting/Get_Fixed_Asset}")
    private String sftpRemoteDirectoryToFa;

    @Value("${sftp.remote.directory.to.pr:/home/KWMS/CIP_PR_Creation/PR_Waiting}")
    private String sftpRemoteDirectoryToPr;

    @Value("${sftp.remote.directory.pr.error:/home/KWMS/CIP_PR_Creation/PR_Didnt_Created}")
    private String sftpRemoteDirectoryPrError;

    @Value("${sftp.remote.directory.jojr:/home/KWMS/JR_JO_Creation/Sap JO File}")
    private String sftpRemoteDirectoryJoJr;

    @Value("${sftp.remote.directory.jojr.error:/home/KWMS/JR_JO_Creation/JO Creation Errors}")
    private String sftpRemoteDirectoryJoJrError;

    @Value("${sftp.remote.directory.pr.status:/home/KWMS/CIP_PR_Creation/PR_Status}")
    private String sftpRemoteDirectoryPrStatus;

    @Value("${sftp.remote.directory.pr.status.processed:/home/KWMS/CIP_PR_Creation/PR_status_processed}")
    private String sftpRemoteDirectoryPrStatusProcessed;

    public SessionFactory<ChannelSftp.LsEntry> sftpSessionFactory() {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
        factory.setHost(sftpHost);
        factory.setPort(sftpPort);
        factory.setUser(sftpUser);
        factory.setPassword(sftpPassword);
        factory.setAllowUnknownKeys(true);
        factory.setTimeout(60000);
        return new CachingSessionFactory<>(factory);
    }

    @Bean
    @ServiceActivator(inputChannel = "toJoJrChannel")
    public MessageHandler toJoJrSftpMessageHandler() {
        SftpMessageHandler handler = new SftpMessageHandler(sftpSessionFactory());
        handler.setRemoteDirectoryExpression(new LiteralExpression(sftpRemoteDirectoryJoJr));
        handler.setFileNameGenerator(new FileNameGenerator() {
            @Override
            public String generateFileName(Message<?> message) {
                if (message.getPayload() instanceof File) {

                    SftpRemoteFileTemplate template = (SftpRemoteFileTemplate) template();
                    String successFilePath = sftpRemoteDirectoryJoJr + "/" + ((File) message.getPayload()).getName();
                    Boolean successResult = template.exists(successFilePath);
                    if(successResult){
                        template.remove(successFilePath);
                    }

                    String errorFilePath = sftpRemoteDirectoryJoJrError + "/" + ((File) message.getPayload()).getName();
                    Boolean errorResult = template.exists(errorFilePath);
                    if(errorResult){
                        template.remove(errorFilePath);
                    }

                    return ((File) message.getPayload()).getName();
                } else {
                    throw new IllegalArgumentException("joJr file expected as payload.");
                }
            }
        });
        return handler;
    }

    @Bean
    @ServiceActivator(inputChannel = "toPrChannel")
    public MessageHandler toPrSftpMessageHandler() {
        SftpMessageHandler handler = new SftpMessageHandler(sftpSessionFactory());
        handler.setRemoteDirectoryExpression(new LiteralExpression(sftpRemoteDirectoryToPr));
        handler.setFileNameGenerator(new FileNameGenerator() {
            @Override
            public String generateFileName(Message<?> message) {
                if (message.getPayload() instanceof File) {

                    SftpRemoteFileTemplate template = (SftpRemoteFileTemplate) template();
                    String successFilePath = sftpRemoteDirectoryToPr + "/" + ((File) message.getPayload()).getName();
                    Boolean successResult = template.exists(successFilePath);
                    if(successResult){
                        template.remove(successFilePath);
                    }

                    String errorFilePath = sftpRemoteDirectoryPrError + "/" + ((File) message.getPayload()).getName();
                    Boolean errorResult = template.exists(errorFilePath);
                    if(errorResult){
                        template.remove(errorFilePath);
                    }

                    return ((File) message.getPayload()).getName();
                } else {
                    throw new IllegalArgumentException("pr file expected as payload.");
                }
            }
        });
        return handler;
    }

    @Bean
    @ServiceActivator(inputChannel = "toFaChannel")
    public MessageHandler toFaSftpMessageHandler() {
        SftpMessageHandler handler = new SftpMessageHandler(sftpSessionFactory());
        handler.setRemoteDirectoryExpression(new LiteralExpression(sftpRemoteDirectoryToFa));
        handler.setFileNameGenerator(new FileNameGenerator() {
            @Override
            public String generateFileName(Message<?> message) {
                if (message.getPayload() instanceof File) {

                    SftpRemoteFileTemplate template = (SftpRemoteFileTemplate) template();
                    String successFilePath = sftpRemoteDirectoryToFa + "/" + ((File) message.getPayload()).getName();
                    Boolean successResult = template.exists(successFilePath);
                    if(successResult){
                        template.remove(successFilePath);
                    }

                    return ((File) message.getPayload()).getName();
                } else {
                    throw new IllegalArgumentException("fa file expected as payload.");
                }
            }
        });
        return handler;
    }

    @Bean
    @ServiceActivator(inputChannel = "toPrStatusProcessedChannel")
    public MessageHandler toPrStatusSftpMessageHandler() {
        SftpMessageHandler handler = new SftpMessageHandler(sftpSessionFactory());
        handler.setRemoteDirectoryExpression(new LiteralExpression(sftpRemoteDirectoryPrStatusProcessed));
        handler.setFileNameGenerator(new FileNameGenerator() {
            @Override
            public String generateFileName(Message<?> message) {
                if (message.getPayload() instanceof File) {
                    return ((File) message.getPayload()).getName();
                } else {
                    throw new IllegalArgumentException("pr file expected as payload.");
                }
            }
        });
        return handler;
    }

    @Bean
    public RemoteFileTemplate template() {
        return new SftpRemoteFileTemplate(sftpSessionFactory());
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
