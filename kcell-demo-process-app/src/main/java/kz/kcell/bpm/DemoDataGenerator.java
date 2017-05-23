package kz.kcell.bpm;

import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.authorization.*;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationEntity;
import org.camunda.bpm.engine.task.TaskQuery;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.camunda.bpm.engine.authorization.Permissions.*;
import static org.camunda.bpm.engine.authorization.Resources.*;

/**
 * Creates demo credentials to be used in the invoice showcase.
 *
 * @author drobisch
 */
public class DemoDataGenerator {

    private final static Logger LOGGER = Logger.getLogger(DemoDataGenerator.class.getName());

    private final static List<String> emails = Arrays.asList("Yernaz.Kalingarayev@kcell.kz", "Askar.Slambekov@kcell.kz", "Anzor.Israilov@kcell.kz", "Abai.Shapagatin@kcell.kz", "Adilet.Baishalov@kcell.kz", "Aidos.Kenzhebayev@kcell.kz", "Aigerim.Iskakova@kcell.kz", "Aigerim.Satybekova@kcell.kz", "Aigerim.Segizbayeva@kcell.kz", "Ainur.Beknazarova@kcell.kz", "Aleksandr.Kobelev@kcell.kz", "Alexander.Shanygin@kcell.kz", "Alexey.Khudaev@kcell.kz", "Alexey.Kolesnikov@kcell.kz", "Alexey.Kolyagin@kcell.kz", "Alibek.Nurkassymov@kcell.kz", "Amanbek.Suimenbayev@kcell.kz", "Anastassiya.Shenojak@kcell.kz", "Andrei.Lugovoy@kcell.kz", "Andrey.Medvedev@kcell.kz", "Anna.Martynova@kcell.kz", "Arman.Utepov@kcell.kz", "Askar.Bekmurzayev@kcell.kz", "Askar.Pernebekov@kcell.kz", "Askhat.Tatepbaev@kcell.kz", "Aslan.Shalov@kcell.kz", "Asset.Rashitov@kcell.kz", "Bahytzhan.Sandybayev@kcell.kz", "Begaly.Kokin@kcell.kz", "Beibit.Bitenov@kcell.kz", "Bella.Mamatova@kcell.kz", "Bogembay.Tleuzhanov@kcell.kz", "Bolat.Idirissov@kcell.kz", "Daniyar.Uzibayev@kcell.kz", "Daniyar.Yespayev@kcell.kz", "Dauren.Beispaev@kcell.kz", "Dmitriy.Saidashev@kcell.kz", "Evgeniy.Semenov@kcell.kz", "Farida.Jumanazarova@kcell.kz", "Galym.Tulenbayev@kcell.kz", "Gaukhar.Kaisarova@kcell.kz", "Georgiy.Kan@kcell.kz", "Gulzhan.Imandosova@kcell.kz", "Kairat.Parmanov@kcell.kz", "Kali.Esimbekov@kcell.kz", "Kanat.Kulmukhambetov@kcell.kz", "Kerey.Zatilda@kcell.kz", "Kirill.Kassatkin@kcell.kz", "Kuanysh.Khozhamuratov@kcell.kz", "Kuanysh.Kussainbekov@kcell.kz", "Lazizbek.Kurbantayev@kcell.kz", "Leila.Raisova@kcell.kz", "Lyudmila.Jabrailova@kcell.kz", "Lyudmila.Vilkova@kcell.kz", "Makhabbat.Sasanova@kcell.kz", "Maral.Amantay@kcell.kz", "Margarita.Kim@kcell.kz", "Marina.Paramonova@kcell.kz", "Maulen.Kempirbayev@kcell.kz", "Maxim.Goikolov@kcell.kz", "Nurzhan.Kochshigulov@kcell.kz", "Nurzhan.Mynbayev@kcell.kz", "Rinat.Kurbangaliyev@kcell.kz", "Ruslan.Tubekbayev@kcell.kz", "Samat.Akhmetov@kcell.kz", "Sara.Turabayeva@kcell.kz", "Saule.Beisembekova@kcell.kz", "Sergey.Chekh@kcell.kz", "Sergey.Chumachenko@kcell.kz", "Sergey.Grigor@kcell.kz", "Sergey.Lee@kcell.kz", "Sergey.Michshenko@kcell.kz", "Shyngys.Kassabekov@kcell.kz", "Tatyana.Solovyova@kcell.kz", "Temirlan.Kaliyev@kcell.kz", "Valeriy.Pogorelov@kcell.kz", "Vassiliy.Gopkalo@kcell.kz", "Vladimir.Grachyov@kcell.kz", "Vladimir.Yefanov@kcell.kz", "Vladislav.Posashkov@kcell.kz", "Yerkebulan.Dauletbayev@kcell.kz", "Yermek.Tanabekov@kcell.kz", "Yevgeniy.Elunin@kcell.kz", "Zhanat.Seitkanov@kcell.kz", "Zhandos.Bolatov@kcell.kz");
//            .stream().map(String::toLowerCase).collect(Collectors.toList());
    private final static List<String> createJREmails = Arrays.asList("Yernaz.Kalingarayev@kcell.kz", "Askar.Slambekov@kcell.kz", "Anzor.Israilov@kcell.kz", "Alexey.Kolyagin@kcell.kz", "Anastassiya.Shenojak@kcell.kz", "Dmitriy.Saidashev@kcell.kz", "Lazizbek.Kurbantayev@kcell.kz", "Nurzhan.Kochshigulov@kcell.kz", "Sergey.Michshenko@kcell.kz", "Kanat.Kulmukhambetov@kcell.kz", "Kerey.Zatilda@kcell.kz", "Maulen.Kempirbayev@kcell.kz", "Nurzhan.Mynbayev@kcell.kz", "Asset.Rashitov@kcell.kz", "Vladimir.Yefanov@kcell.kz", "Aslan.Shalov@kcell.kz", "Shyngys.Kassabekov@kcell.kz", "Alexey.Khudaev@kcell.kz", "Evgeniy.Semenov@kcell.kz", "Sergey.Chekh@kcell.kz", "Sergey.Lee@kcell.kz", "Yevgeniy.Elunin@kcell.kz", "Yermek.Tanabekov@kcell.kz", "Andrei.Lugovoy@kcell.kz", "Alexey.Kolesnikov@kcell.kz", "Askar.Bekmurzayev@kcell.kz", "Temirlan.Kaliyev@kcell.kz");
//            .stream().map(String::toLowerCase).collect(Collectors.toList());
    private final static List<String> adminEmails = Arrays.asList("Yernaz.Kalingarayev@kcell.kz", "Askar.Slambekov@kcell.kz", "Anzor.Israilov@kcell.kz");
//            .stream().map(String::toLowerCase).collect(Collectors.toList());

    public void createUsers(ProcessEngine engine) {

        final IdentityService identityService = engine.getIdentityService();
        LOGGER.warning("========================================================================");

        if (identityService.isReadOnly()) {
            LOGGER.info("Identity service provider is Read Only, not creating any demo users.");
            return;
        }

        User singleResult = identityService.createUserQuery().userId("demo").singleResult();
        if (singleResult != null) {
            LOGGER.warning("DEMO USER EXISTS");
            LOGGER.warning("========================================================================");
            return;
        }

        LOGGER.warning("Generating demo data for revision process");
        LOGGER.warning("========================================================================");

        User user = identityService.newUser("demo");
        user.setFirstName("Demo");
        user.setLastName("Demo");
        user.setPassword("demo");
        user.setEmail("demo@camunda.org");
        identityService.saveUser(user);

        User userDemo = identityService.newUser("test_flow@kcell.kz");
        userDemo.setFirstName("Kcell");
        userDemo.setLastName("Test");
        userDemo.setPassword("demo");
        userDemo.setEmail("test_flow@kcell.kz");
        identityService.saveUser(userDemo);

        for (String email : emails) {
            StringTokenizer st = new StringTokenizer(email);
            String firstName = st.nextToken(".");
            String lastName = st.nextToken(".@");

            User kcellUser = identityService.newUser(email);
            kcellUser.setFirstName(firstName);
            kcellUser.setLastName(lastName);
            kcellUser.setPassword("demo");
            kcellUser.setEmail(email);
            identityService.saveUser(kcellUser);
        }
        LOGGER.warning("ALL USERS CREATED");
        LOGGER.warning("========================================================================");


        Group kcellUsers = identityService.newGroup("kcellUsers");
        kcellUsers.setName("Kcell Users");
        kcellUsers.setType("WORKFLOW");
        identityService.saveGroup(kcellUsers);

        Group createJR = identityService.newGroup("createJR");
        createJR.setName("Create JR Users");
        createJR.setType("WORKFLOW");
        identityService.saveGroup(createJR);
        LOGGER.warning("Create JR Users GROUP CREATED");
        LOGGER.warning("========================================================================");

        final AuthorizationService authorizationService = engine.getAuthorizationService();

        // create group
        if (identityService.createGroupQuery().groupId(Groups.CAMUNDA_ADMIN).count() == 0) {
            Group camundaAdminGroup = identityService.newGroup(Groups.CAMUNDA_ADMIN);
            camundaAdminGroup.setName("camunda BPM Administrators");
            camundaAdminGroup.setType(Groups.GROUP_TYPE_SYSTEM);
            identityService.saveGroup(camundaAdminGroup);
        }

        // create ADMIN authorizations on all built-in resources
        for (Resource resource : Resources.values()) {
            if (authorizationService.createAuthorizationQuery().groupIdIn(Groups.CAMUNDA_ADMIN).resourceType(resource).resourceId(ANY).count() == 0) {
                AuthorizationEntity userAdminAuth = new AuthorizationEntity(AUTH_TYPE_GRANT);
                userAdminAuth.setGroupId(Groups.CAMUNDA_ADMIN);
                userAdminAuth.setResource(resource);
                userAdminAuth.setResourceId(ANY);
                userAdminAuth.addPermission(ALL);
                authorizationService.saveAuthorization(userAdminAuth);
            }
        }
        LOGGER.warning("CREATING MEMBERSHIPS DEMO");
        LOGGER.warning("========================================================================");

        identityService.createMembership("demo", "kcellUsers");
        identityService.createMembership("demo", "camunda-admin");
        identityService.createMembership("test_flow@kcell.kz", "kcellUsers");
        identityService.createMembership("test_flow@kcell.kz", "camunda-admin");

        for (String adminEmail : adminEmails) {
            identityService.createMembership(adminEmail, "camunda-admin");
        }

        LOGGER.warning("CREATING MEMBERSHIPS ALL");
        LOGGER.warning("========================================================================");

        for (String email : emails) {
            identityService.createMembership(email, "kcellUsers");
        }
        for (String email : createJREmails) {
            identityService.createMembership(email, "createJR");
        }

        LOGGER.warning("MEMBERSHIPS CREATED");
        LOGGER.warning("========================================================================");


        // authorize groups for tasklist only:

        Authorization kcellUsersTasklistAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUsersTasklistAuth.setGroupId("kcellUsers");
        kcellUsersTasklistAuth.addPermission(ACCESS);
        kcellUsersTasklistAuth.setResourceId("tasklist");
        kcellUsersTasklistAuth.setResource(APPLICATION);
        authorizationService.saveAuthorization(kcellUsersTasklistAuth);

        Authorization createJRReadProcessDefinition = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        createJRReadProcessDefinition.setGroupId("createJR");
        createJRReadProcessDefinition.addPermission(Permissions.ALL);
        createJRReadProcessDefinition.setResource(Resources.PROCESS_DEFINITION);
        createJRReadProcessDefinition.setResourceId("Revision");
        authorizationService.saveAuthorization(createJRReadProcessDefinition);

        Authorization createJRReadProcessInstance = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        createJRReadProcessInstance.setGroupId("createJR");
        createJRReadProcessInstance.addPermission(Permissions.ALL);
        createJRReadProcessInstance.setResource(Resources.PROCESS_INSTANCE);
        createJRReadProcessInstance.setResourceId("*");
        authorizationService.saveAuthorization(createJRReadProcessInstance);

        Authorization kcellUsersReadProcessInstance = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUsersReadProcessInstance.setGroupId("kcellUsers");
        kcellUsersReadProcessInstance.addPermission(Permissions.ALL);
        kcellUsersReadProcessInstance.setResource(Resources.PROCESS_INSTANCE);
        kcellUsersReadProcessInstance.setResourceId("*");
        authorizationService.saveAuthorization(kcellUsersReadProcessInstance);

        Authorization kcellUsersAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUsersAuth.setGroupId("kcellUsers");
        kcellUsersAuth.setResource(USER);
        kcellUsersAuth.setResourceId("demo");
        kcellUsersAuth.addPermission(READ);
        authorizationService.saveAuthorization(kcellUsersAuth);

        Authorization salesTestDemoAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        salesTestDemoAuth.setGroupId("kcellUsers");
        salesTestDemoAuth.setResource(USER);
        salesTestDemoAuth.setResourceId("test_flow@kcell.kz");
        salesTestDemoAuth.addPermission(READ);
        authorizationService.saveAuthorization(salesTestDemoAuth);


        for (String email : emails) {
            Authorization kcellUserAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
            kcellUserAuth.setGroupId("kcellUsers");
            kcellUserAuth.setResource(USER);
            kcellUserAuth.setResourceId(email);
            kcellUserAuth.addPermission(READ);
            authorizationService.saveAuthorization(kcellUserAuth);
        }

        for (String email : createJREmails) {
            Authorization kcellUserAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
            kcellUserAuth.setGroupId("createJR");
            kcellUserAuth.setResource(USER);
            kcellUserAuth.setResourceId(email);
            kcellUserAuth.addPermission(READ);
            authorizationService.saveAuthorization(kcellUserAuth);
        }

        LOGGER.warning("AUTHORIZATION CREATED");
        LOGGER.warning("========================================================================");

        Map<String, Object> filterProperties = new HashMap<String, Object>();
        FilterService filterService = engine.getFilterService();

        TaskQuery query;

        TaskService taskService = engine.getTaskService();

        filterProperties.clear();
        filterProperties.put("description", "All Tasks - Not recommended to be used in production :)");
        filterProperties.put("priority", 10);
        Map<String, String> variablesMap = new HashMap<>();
        variablesMap.put("name", "jrNumber");
        variablesMap.put("label", "JR Number");

        List<Map<String, String>> filterVariables = Arrays.asList(variablesMap);
        filterProperties.put("variables", filterVariables);
        //addVariables(filterProperties);
        query = taskService.createTaskQuery();
        Filter allTasksFilter = filterService.newTaskFilter().setName("All Tasks").setProperties(filterProperties).setOwner("demo").setQuery(query);
        filterService.saveFilter(allTasksFilter);


        //My Claimed Tasks
        filterProperties.clear();
        filterProperties.put("description", "My Claimed Tasks");
        filterProperties.put("priority", -10);
        filterProperties.put("variables", filterVariables);
        //addVariables(filterProperties);
        query = taskService.createTaskQuery().taskAssigneeExpression("${ currentUser() }");
        Filter myClaimedTasks = filterService.newTaskFilter().setName("My Claimed Tasks").setProperties(filterProperties).setOwner("kcellUsers").setQuery(query);
        filterService.saveFilter(myClaimedTasks);

        Authorization myClaimedTasksFilterRead = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
        myClaimedTasksFilterRead.setResource(FILTER);
        myClaimedTasksFilterRead.setResourceId(myClaimedTasks.getId());
        myClaimedTasksFilterRead.addPermission(READ);
        myClaimedTasksFilterRead.setGroupId("kcellUsers");
        authorizationService.saveAuthorization(myClaimedTasksFilterRead);

        //My Unclaimed Tasks
        filterProperties.clear();
        filterProperties.put("description", "My Unclaimed Tasks");
        filterProperties.put("priority", -10);
        filterProperties.put("variables", filterVariables);
        //addVariables(filterProperties);
        query = taskService.createTaskQuery().taskCandidateUserExpression("${currentUser()}");
        Filter myUnclaimedTasksFilter = filterService.newTaskFilter().setName("My Unclaimed Tasks").setProperties(filterProperties).setOwner("kcellUsers").setQuery(query);
        filterService.saveFilter(myUnclaimedTasksFilter);

        Authorization myUnclaimedTasksFilterRead = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
        myUnclaimedTasksFilterRead.setResource(FILTER);
        myUnclaimedTasksFilterRead.setResourceId(myUnclaimedTasksFilter.getId());
        myUnclaimedTasksFilterRead.addPermission(READ);
        myUnclaimedTasksFilterRead.setGroupId("kcellUsers");
        authorizationService.saveAuthorization(myUnclaimedTasksFilterRead);

        //My Group Tasks
        filterProperties.clear();
        filterProperties.put("description", "My Group Tasks");
        filterProperties.put("priority", -10);
        filterProperties.put("variables", filterVariables);
        //addVariables(filterProperties);
        query = taskService.createTaskQuery().taskCandidateGroupInExpression("${ currentUserGroups() }");
        Filter myGroupTasks = filterService.newTaskFilter().setName("My Group Tasks").setProperties(filterProperties).setOwner("kcellUsers").setQuery(query);
        filterService.saveFilter(myGroupTasks);

        Authorization myGroupTasksFilterRead = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
        myGroupTasksFilterRead.setResource(FILTER);
        myGroupTasksFilterRead.setResourceId(myGroupTasks.getId());
        myGroupTasksFilterRead.addPermission(READ);
        myGroupTasksFilterRead.setGroupId("kcellUsers");
        authorizationService.saveAuthorization(myGroupTasksFilterRead);

        LOGGER.warning("FILTERS CREATED");
        LOGGER.warning("========================================================================");

    }
}