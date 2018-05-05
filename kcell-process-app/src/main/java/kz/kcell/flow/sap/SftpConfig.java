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
    private String sftpPasword;

    @Value("${sftp.remote.directory.to.jojr:/home/KWMS/JR_JO_Creation/Sap JO File}")
    private String sftpRemoteDirectoryToJoJr;

    @Value("${sftp.remote.directory.to.pr:/home/KWMS/CIP_PR_Creation/PR_Waiting}")
    private String sftpRemoteDirectoryToPr;

    @Value("${sftp.remote.directory.jojr:/home/KWMS/JR_JO_Creation/Sap JO File}")
    private String sftpRemoteDirectoryJoJr;

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
    @ServiceActivator(inputChannel = "toJoJrChannel")
    public MessageHandler toJoJrSftpMessageHandler() {
        SftpMessageHandler handler = new SftpMessageHandler(sftpSessionFactory());
        handler.setRemoteDirectoryExpression(new LiteralExpression(sftpRemoteDirectoryJoJr));
        handler.setFileNameGenerator(new FileNameGenerator() {
            @Override
            public String generateFileName(Message<?> message) {
                if (message.getPayload() instanceof File) {

                    SftpRemoteFileTemplate template = (SftpRemoteFileTemplate) template();
                    String successFilePath = sftpRemoteDirectoryToJoJr + "/" + ((File) message.getPayload()).getName();
                    Boolean successResult = template.exists(successFilePath);
                    if(successResult){
                        template.remove(successFilePath);
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
    }
}
