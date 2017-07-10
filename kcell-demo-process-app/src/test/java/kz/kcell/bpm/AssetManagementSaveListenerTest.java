package kz.kcell.bpm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import org.junit.Test;

import java.util.logging.Level;

/**
 * Created by admin on 4/10/17.
 */
public class AssetManagementSaveListenerTest {

    @Test
    public void testSave() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AssetManagementSaveListenerNew assetManagementSaveListenerNew = new AssetManagementSaveListenerNew("http://localhost/");
        /*JsonNode node = mapper.readTree("[{\n" +
                "  \"command\": \"MODIFY_CONNECTION\",\n" +
                "  \"id\": \"2\",\n" +
                "  \"payload\": {\n" +
                "    \"id\": \"2\",\n" +
                "    \"definition\": \"TRANSMISSION_RADIO_RELAY\",\n" +
                "    \"equipments\": [\n" +
                "      8,\n" +
                "      9,\n" +
                "      10\n" +
                "    ],\n" +
                "    \"_links\": {\n" +
                "      \"self\": {\n" +
                "        \"href\": \"http://localhost/asset-management/api/connectionInstances/2\"\n" +
                "      },\n" +
                "      \"connectionInstance\": {\n" +
                "        \"href\": \"http://localhost/asset-management/api/connectionInstances/2{?projection}\",\n" +
                "        \"templated\": true\n" +
                "      },\n" +
                "      \"equipments\": {\n" +
                "        \"href\": \"http://localhost/asset-management/api/connectionInstances/2/equipments\"\n" +
                "      },\n" +
                "      \"definition\": {\n" +
                "        \"href\": \"http://localhost/asset-management/api/connectionInstances/2/definition\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"_command\": \"MODIFY_CONNECTION\",\n" +
                "    \"params\": {\n" +
                "      \"type\": \"TDM\",\n" +
                "      \"channel_bandwidth\": \"14\",\n" +
                "      \"pass_distance\": 15,\n" +
                "      \"mode\": \"1+0\"\n" +
                "    }\n" +
                "  }\n" +
                "}]");*/
        //JsonNode node = mapper.readTree("[{\"command\":\"INSTALL_NEW_EQUIPMENT\",\"payload\":{\"id\":\"_NEW:9f1f3cfb-03e9-0778-337c-cb198810cb42\",\"params\":{\"capacity_ethernet\":4124214,\"link_capacity_nxe1\":4124214,\"protection_type\":\"1+0\"},\"equipment\":{\"params\":{\"type\":\"TN 20p\",\"serial_number\":\"4124214\",\"terminal_id\":\"4124214\"},\"definition\":\"IU\",\"id\":\"_NEW:31517474-ade7-4ef6-97c5-e965b476e77f\"},\"definition\":\"IU\",\"_command\":\"INSTALL_NEW_EQUIPMENT\",\"facility\":\"1\"}},{\"command\":\"INSTALL_NEW_EQUIPMENT\",\"payload\":{\"id\":\"_NEW:66ff6ac7-9c1c-85a9-3c45-81a5cbb3b5e7\",\"params\":{\"if_cable_length\":4214,\"tx_rx_frequencies\":412412,\"power_lvl_Tx\":412,\"power_lvl_Rx\":412421},\"equipment\":{\"params\":{\"frequency\":412,\"type\":\"RAU2 N\",\"modulation_type\":\"421421\",\"serial_number\":\"4214\",\"rau_subband\":\"4124\"},\"definition\":\"OU\",\"id\":\"_NEW:4cf9deaf-3c91-0b7c-01a2-5e9f89b484e7\"},\"definition\":\"OU\",\"_command\":\"INSTALL_NEW_EQUIPMENT\",\"facility\":\"1\"}},{\"command\":\"INSTALL_NEW_EQUIPMENT\",\"payload\":{\"id\":\"_NEW:6795a5a5-f495-b7df-5ecd-0638232418e5\",\"params\":{\"height_of_suspention_of_ant\":214214,\"min_horizontal_distance_from_roof_edge\":4124,\"placement_type\":\"on pole\",\"azimuth\":41244},\"equipment\":{\"params\":{\"polarization\":\"Horizontal\",\"diameter\":\"0.6\",\"type\":\"Type1\",\"serial_number\":\"4214214\",\"weight\":214},\"definition\":\"AU\",\"id\":\"_NEW:c62bf7b2-80c4-72a9-3b43-28ed9e47d61b\"},\"definition\":\"AU\",\"_command\":\"INSTALL_NEW_EQUIPMENT\",\"facility\":\"1\"}},{\"command\":\"CONNECT_EQUIPMENT\",\"payload\":{\"definition\":\"TRANSMISSION_RADIO_RELAY\",\"params\":{\"sites\":[\"SITE1\",\"SITE2\"],\"type\":\"TDM\",\"channel_bandwidth\":\"3.5\",\"pass_distance\":4,\"mode\":\"1+0\"},\"_command\":\"CONNECT_EQUIPMENT\",\"equipments\":[\"_NEW:31517474-ade7-4ef6-97c5-e965b476e77f\",\"_NEW:4cf9deaf-3c91-0b7c-01a2-5e9f89b484e7\",\"_NEW:c62bf7b2-80c4-72a9-3b43-28ed9e47d61b\"]}}]");
        /*JsonNode node = mapper.readTree("[{\n" +
                "  \"command\": \"INSTALL_NEW_EQUIPMENT\",\n" +
                "  \"payload\": {\n" +
                "    \"id\": \"_NEW:b68c71e2-bf7d-9318-a31b-a298b366a495\",\n" +
                "    \"params\": {},\n" +
                "    \"equipment\": {\n" +
                "      \"definition\": \"CABINET\",\n" +
                "      \"params\": {\n" +
                "        \"bsc\": \"AlmBSC8\",\n" +
                "        \"rnc\": \"AlmRNC5\",\n" +
                "        \"company_vendor_id\": \"Ericsson\",\n" +
                "        \"cabinet_type\": \"RBS 2216\",\n" +
                "        \"serial_number\": \"АОВДЛАОВЛЫДАОВДЛЫ ЛДАОЫВЛДВОАЛД\"\n" +
                "      },\n" +
                "      \"id\": \"_NEW:a8789fe8-3e39-4515-ae55-96b4be344826\"\n" +
                "    },\n" +
                "    \"_command\": \"INSTALL_NEW_EQUIPMENT\",\n" +
                "    \"definition\": \"CABINET\",\n" +
                "    \"facility\": \"1\"\n" +
                "  }\n" +
                "}]");
        assetManagementSaveListenerNew.saveCommandsToAssetManagement(node, "1");*/
        //StartRevisionProcess s = new StartRevisionProcess();
        //System.out.println(s.getNextSiteCounter("1"));
        Email email = new SimpleEmail();
        email.setCharset("utf-8");
        email.setHostName("localhost");
        email.setSmtpPort(1025);

        try {
            email.setFrom("test_flow@kcell.kz");
            email.setSubject("Task assigned: ");

            final String baseUrl = "http://test_flow.kcell.kz";

            email.setMsg("Добрый день.\n" +
                    "\n" +
                    "В рамках процесса одобрения заявок на проведение работ, в системе Kcell Workflow создана заявка, ожидающая вашего одобрения. Для просмотра заявки необходимо пройти по следующей ссылке: " + baseUrl + "/camunda/app/tasklist/default/#/?task=" + 213+"" +
                    "\n" +
                    "\n" +
                    "Пройдя по следующей ссылке на страницу в HUB.Kcell.kz, вы можете оставить в поле комментариев свои замечания и/или пожелания относительно функционала и интерфейса системы: https://hub.kcell.kz/x/kYNoAg");

            email.addTo("nurlan@muldashev.kz");

            email.send();

        } catch (Exception e) {
            System.out.println("Could not send email to assignee");
        }
    }
}
