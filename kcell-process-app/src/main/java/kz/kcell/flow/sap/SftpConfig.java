package kz.kcell.flow.sap;

import com.jcraft.jsch.ChannelSftp;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.file.FileNameGenerator;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.outbound.SftpMessageHandler;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;

import javax.annotation.Resource;
import java.io.File;

@Configuration
@Log
public class SftpConfig {

    @Value("${sftp.host:195.47.255.121}")
    private String sftpHost;

    @Value("${sftp.port:22}")
    private int sftpPort;

    @Value("${sftp.user:KWMS}")
    private String sftpUser;

    @Value("${sftp.password:#{null}}")
    private String sftpPasword;

    @Value("${sftp.remote.directory.jojr:/home/KWMS/JR_JO_Creation/Sap JO File}")
    private String sftpRemoteDirectoryJoJr;

    @Value("${sftp.remote.directory.pr:/home/KWMS/CIP_PR_Creation/PR_Waiting}")
    private String sftpRemoteDirectoryPr;

    @Bean
    public SessionFactory<ChannelSftp.LsEntry> sftpSessionFactory() {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
        factory.setHost(sftpHost);
        factory.setPort(sftpPort);
        factory.setUser(sftpUser);
        factory.setPassword(sftpPasword);
        factory.setAllowUnknownKeys(true);
        return new CachingSessionFactory<>(factory);
    }

    @Bean
    @ServiceActivator(inputChannel = "toJoJrSftpChannel")
    public MessageHandler joJrHandler() {
        SftpMessageHandler handler = new SftpMessageHandler(sftpSessionFactory());
        handler.setRemoteDirectoryExpression(new LiteralExpression(sftpRemoteDirectoryJoJr));
        handler.setFileNameGenerator(new FileNameGenerator() {
            @Override
            public String generateFileName(Message<?> message) {
                if (message.getPayload() instanceof File) {
                    return ((File) message.getPayload()).getName();
                } else {
                    throw new IllegalArgumentException("joJr file expected as payload.");
                }
            }
        });
        return handler;
    }

    @Bean
    @ServiceActivator(inputChannel = "toPrSftpChannel")
    public MessageHandler prHandler() {
        SftpMessageHandler handler = new SftpMessageHandler(sftpSessionFactory());
        handler.setRemoteDirectoryExpression(new LiteralExpression(sftpRemoteDirectoryPr));
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

    @MessagingGateway
    public interface UploadGateway {

        @Gateway(requestChannel = "toJoJrSftpChannel")
        void uploadJrJo(File file);

        @Gateway(requestChannel = "toPrSftpChannel")
        void uploadPr(File file);
    }
}
