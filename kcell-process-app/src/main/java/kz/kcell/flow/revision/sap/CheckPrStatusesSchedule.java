package kz.kcell.flow.revision.sap;

import kz.kcell.flow.revision.sap.S3Config;
import kz.kcell.flow.revision.sap.SftpConfig;
import lombok.extern.java.Log;
import org.apache.commons.io.IOUtils;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@EnableScheduling
@Log
public class CheckPrStatusesSchedule {

    @Autowired
    private RemoteFileTemplate template;

    @Autowired
    private Environment environment;

    @Autowired
    private RuntimeService runtimeService;

    @Value("${sftp.remote.directory.status:/uploads/test/CIP_PR_Creation/PR_Status}")
    private String sftpRemoteDirectoryPrStatus;

    @Value("${s3.bucket.pr:prfiles}")
    private String prBucketName;

    @Autowired
    private SftpConfig.UploadGateway gateway;

    @Autowired
    private S3Config.UploadGateway s3gateway;

    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

    @Scheduled(cron = "0 0 0-23 * * 1-5")
//    @Scheduled(cron = "0 0 0-23 * * 2")
    public void checkPrStatuses() {

        List<String> usedBussinessKeyList = new ArrayList<>();

        Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));

        if(isSftp){
            log.info("Cron Task :: Execution Time - " + timeFormatter.format(LocalDateTime.now()));

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

            String filepath = "WeeklyMonday"
                + calendar.get(Calendar.YEAR)
                + ((calendar.get(Calendar.MONTH)+1) < 9 ? "0":"") + (calendar.get(Calendar.MONTH)+1)
                + (calendar.get(Calendar.DAY_OF_MONTH) < 9 ? "0":"") + calendar.get(Calendar.DAY_OF_MONTH)
                + ".txt";

            String prStatusFilePath = sftpRemoteDirectoryPrStatus + "/" + filepath;

            log.info("filepath: " + prStatusFilePath);

            Boolean statusesResult = template.exists(prStatusFilePath);

            if(statusesResult){
                log.info(filepath + " file exists");

                template.get(prStatusFilePath,
                    inputStream -> {
                        Scanner s = new Scanner(inputStream).useDelimiter("\\A");
                        String result = s.hasNext() ? s.next() : "";

                        StringTokenizer byNewLine = new StringTokenizer(result, "\n");

                        while(byNewLine.hasMoreTokens()){
                            String status = byNewLine.nextToken();
                            log.info("fetching " + status);

                            StringTokenizer byTab = new StringTokenizer(status, "\t");

                            String key = byTab.nextToken();

                            String k = key.substring(0, key.indexOf("-"));
                            switch (k) {
                                case "ALM":
                                    key = "Alm" + key.substring(key.indexOf("-"), key.length());
                                    break;
                                case "ASTANA":
                                    key = "Astana" + key.substring(key.indexOf("-"), key.length());
                                    break;
                                case "SOUTH":
                                    key = "South" + key.substring(key.indexOf("-"), key.length());
                                    break;
                                case "WEST":
                                    key = "West" + key.substring(key.indexOf("-"), key.length());
                                    break;
                                case "EAST":
                                    key = "East" + key.substring(key.indexOf("-"), key.length());
                                    break;
                                default:
                                    break;
                            }

                            if(!usedBussinessKeyList.contains(key)){
                                List<ProcessInstance> processInstances = runtimeService
                                    .createProcessInstanceQuery()
                                    .processDefinitionKey("Revision")
                                    .processInstanceBusinessKey(key)
                                    .list();

                                if(processInstances.size() > 0){
                                    log.info("ProcessInstance with bussiness key " + key + " found");

                                    runtimeService.setVariable(processInstances.get(0).getId(),"sapPRNo", byTab.nextToken());
                                    runtimeService.setVariable(processInstances.get(0).getId(),"sapPRTotalValue", Double.valueOf(byTab.nextToken()));
                                    runtimeService.setVariable(processInstances.get(0).getId(),"sapPRStatus", byTab.nextToken());

                                    String sapPRApproveDate = byTab.nextToken();
                                    try {
                                        runtimeService.setVariable(processInstances.get(0).getId(),"sapPRApproveDate", simpleDateFormat.parse(sapPRApproveDate));
                                    } catch (ParseException e){
                                        log.warning("Date " + sapPRApproveDate + " parse exception");
                                    }

                                    usedBussinessKeyList.add(key);
                                } else {
                                    log.info("ProcessInstance with bussiness key " + key + " is null");
                                }
                                log.info("----------------------Line Separator------------------------------------");
                            }
                        }

                        String tmpDir = System.getProperty("java.io.tmpdir");
                        File file = new File(tmpDir+ "/" + filepath);

                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(IOUtils.toByteArray(inputStream));
                        fos.close();

                        if(isSftp){
                            gateway.uploadPrStatusProcessed(file);
                        } else {
                            s3gateway.uploadPrStatusProcessed(file);
                        }
                    }
                );

                template.remove(prStatusFilePath);
            } else {
                log.info(filepath + " pr file not exists, waiting...");
            }
        }
    }
}
