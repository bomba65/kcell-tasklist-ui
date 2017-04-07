package kz.kcell.bpm;

import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.authorization.*;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationEntity;
import org.camunda.bpm.engine.task.TaskQuery;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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

        User user_0 = identityService.newUser("Daniyar.Yespayev@kcell.kz");
        user_0.setFirstName("Daniyar");
        user_0.setLastName("Yespayev");
        user_0.setPassword("demo");
        user_0.setEmail("Daniyar.Yespayev@kcell.kz");
        identityService.saveUser(user_0);

        User user_1 = identityService.newUser("Yerkebulan.Dauletbayev@kcell.kz");
        user_1.setFirstName("Yerkebulan");
        user_1.setLastName("Dauletbayev");
        user_1.setPassword("demo");
        user_1.setEmail("Yerkebulan.Dauletbayev@kcell.kz");
        identityService.saveUser(user_1);

        User user_2 = identityService.newUser("Aigerim.Segizbayeva@kcell.kz");
        user_2.setFirstName("Aigerim");
        user_2.setLastName("Segizbayeva");
        user_2.setPassword("demo");
        user_2.setEmail("Aigerim.Segizbayeva@kcell.kz");
        identityService.saveUser(user_2);

        User user_3 = identityService.newUser("Evgeniy.Semenov@kcell.kz");
        user_3.setFirstName("Evgeniy");
        user_3.setLastName("Semenov");
        user_3.setPassword("demo");
        user_3.setEmail("Evgeniy.Semenov@kcell.kz");
        identityService.saveUser(user_3);

        User user_4 = identityService.newUser("Alexander.Shanygin@kcell.kz");
        user_4.setFirstName("Alexander");
        user_4.setLastName("Shanygin");
        user_4.setPassword("demo");
        user_4.setEmail("Alexander.Shanygin@kcell.kz");
        identityService.saveUser(user_4);

        User user_5 = identityService.newUser("Asset.Rashitov@kcell.kz");
        user_5.setFirstName("Asset");
        user_5.setLastName("Rashitov");
        user_5.setPassword("demo");
        user_5.setEmail("Asset.Rashitov@kcell.kz");
        identityService.saveUser(user_5);

        User user_6 = identityService.newUser("Anna.Martynova@kcell.kz");
        user_6.setFirstName("Anna");
        user_6.setLastName("Martynova");
        user_6.setPassword("demo");
        user_6.setEmail("Anna.Martynova@kcell.kz");
        identityService.saveUser(user_6);

        User user_7 = identityService.newUser("Makhabbat.Sasanova@kcell.kz");
        user_7.setFirstName("Makhabbat");
        user_7.setLastName("Sasanova");
        user_7.setPassword("demo");
        user_7.setEmail("Makhabbat.Sasanova@kcell.kz");
        identityService.saveUser(user_7);

        User user_8 = identityService.newUser("Amanbek.Suimenbayev@kcell.kz");
        user_8.setFirstName("Amanbek");
        user_8.setLastName("Suimenbayev");
        user_8.setPassword("demo");
        user_8.setEmail("Amanbek.Suimenbayev@kcell.kz");
        identityService.saveUser(user_8);

        User user_9 = identityService.newUser("Lazizbek.Kurbantayev@kcell.kz");
        user_9.setFirstName("Lazizbek");
        user_9.setLastName("Kurbantayev");
        user_9.setPassword("demo");
        user_9.setEmail("Lazizbek.Kurbantayev@kcell.kz");
        identityService.saveUser(user_9);

        User user_10 = identityService.newUser("Gaukhar.Kaisarova@kcell.kz");
        user_10.setFirstName("Gaukhar");
        user_10.setLastName("Kaisarova");
        user_10.setPassword("demo");
        user_10.setEmail("Gaukhar.Kaisarova@kcell.kz");
        identityService.saveUser(user_10);

        User user_11 = identityService.newUser("Bahytzhan.Sandybayev@kcell.kz");
        user_11.setFirstName("Bahytzhan");
        user_11.setLastName("Sandybayev");
        user_11.setPassword("demo");
        user_11.setEmail("Bahytzhan.Sandybayev@kcell.kz");
        identityService.saveUser(user_11);

        User user_12 = identityService.newUser("Sergey.Michshenko@kcell.kz");
        user_12.setFirstName("Sergey");
        user_12.setLastName("Michshenko");
        user_12.setPassword("demo");
        user_12.setEmail("Sergey.Michshenko@kcell.kz");
        identityService.saveUser(user_12);

        User user_13 = identityService.newUser("Aleksandr.Kobelev@kcell.kz");
        user_13.setFirstName("Aleksandr");
        user_13.setLastName("Kobelev");
        user_13.setPassword("demo");
        user_13.setEmail("Aleksandr.Kobelev@kcell.kz");
        identityService.saveUser(user_13);

        User user_14 = identityService.newUser("Alibek.Nurkassymov@kcell.kz");
        user_14.setFirstName("Alibek");
        user_14.setLastName("Nurkassymov");
        user_14.setPassword("demo");
        user_14.setEmail("Alibek.Nurkassymov@kcell.kz");
        identityService.saveUser(user_14);

        User user_15 = identityService.newUser("Margarita.Kim@kcell.kz");
        user_15.setFirstName("Margarita");
        user_15.setLastName("Kim");
        user_15.setPassword("demo");
        user_15.setEmail("Margarita.Kim@kcell.kz");
        identityService.saveUser(user_15);

        User user_16 = identityService.newUser("Daniyar.Uzibayev@kcell.kz");
        user_16.setFirstName("Daniyar");
        user_16.setLastName("Uzibayev");
        user_16.setPassword("demo");
        user_16.setEmail("Daniyar.Uzibayev@kcell.kz");
        identityService.saveUser(user_16);

        User user_17 = identityService.newUser("Marina.Paramonova@kcell.kz");
        user_17.setFirstName("Marina");
        user_17.setLastName("Paramonova");
        user_17.setPassword("demo");
        user_17.setEmail("Marina.Paramonova@kcell.kz");
        identityService.saveUser(user_17);

        User user_18 = identityService.newUser("Yermek.Tanabekov@kcell.kz");
        user_18.setFirstName("Yermek");
        user_18.setLastName("Tanabekov");
        user_18.setPassword("demo");
        user_18.setEmail("Yermek.Tanabekov@kcell.kz");
        identityService.saveUser(user_18);

        User user_19 = identityService.newUser("Vladislav.Posashkov@kcell.kz");
        user_19.setFirstName("Vladislav");
        user_19.setLastName("Posashkov");
        user_19.setPassword("demo");
        user_19.setEmail("Vladislav.Posashkov@kcell.kz");
        identityService.saveUser(user_19);

        User user_20 = identityService.newUser("Andrei.Lugovoy@kcell.kz");
        user_20.setFirstName("Andrei");
        user_20.setLastName("Lugovoy");
        user_20.setPassword("demo");
        user_20.setEmail("Andrei.Lugovoy@kcell.kz");
        identityService.saveUser(user_20);

        User user_21 = identityService.newUser("Nurzhan.Kochshigulov@kcell.kz");
        user_21.setFirstName("Nurzhan");
        user_21.setLastName("Kochshigulov");
        user_21.setPassword("demo");
        user_21.setEmail("Nurzhan.Kochshigulov@kcell.kz");
        identityService.saveUser(user_21);

        User user_22 = identityService.newUser("Vassiliy.Gopkalo@kcell.kz");
        user_22.setFirstName("Vassiliy");
        user_22.setLastName("Gopkalo");
        user_22.setPassword("demo");
        user_22.setEmail("Vassiliy.Gopkalo@kcell.kz");
        identityService.saveUser(user_22);

        User user_23 = identityService.newUser("Askar.Pernebekov@kcell.kz");
        user_23.setFirstName("Askar");
        user_23.setLastName("Pernebekov");
        user_23.setPassword("demo");
        user_23.setEmail("Askar.Pernebekov@kcell.kz");
        identityService.saveUser(user_23);

        User user_24 = identityService.newUser("Lyudmila.Jabrailova@kcell.kz");
        user_24.setFirstName("Lyudmila");
        user_24.setLastName("Jabrailova");
        user_24.setPassword("demo");
        user_24.setEmail("Lyudmila.Jabrailova@kcell.kz");
        identityService.saveUser(user_24);

        User user_25 = identityService.newUser("Sergey.Chekh@kcell.kz");
        user_25.setFirstName("Sergey");
        user_25.setLastName("Chekh");
        user_25.setPassword("demo");
        user_25.setEmail("Sergey.Chekh@kcell.kz");
        identityService.saveUser(user_25);

        User user_26 = identityService.newUser("Beibit.Bitenov@kcell.kz");
        user_26.setFirstName("Beibit");
        user_26.setLastName("Bitenov");
        user_26.setPassword("demo");
        user_26.setEmail("Beibit.Bitenov@kcell.kz");
        identityService.saveUser(user_26);

        User user_27 = identityService.newUser("Sergey.Lee@kcell.kz");
        user_27.setFirstName("Sergey");
        user_27.setLastName("Lee");
        user_27.setPassword("demo");
        user_27.setEmail("Sergey.Lee@kcell.kz");
        identityService.saveUser(user_27);

        User user_28 = identityService.newUser("Yevgeniy.Elunin@kcell.kz");
        user_28.setFirstName("Yevgeniy");
        user_28.setLastName("Elunin");
        user_28.setPassword("demo");
        user_28.setEmail("Yevgeniy.Elunin@kcell.kz");
        identityService.saveUser(user_28);

        User user_29 = identityService.newUser("Nurzhan.Mynbayev@kcell.kz");
        user_29.setFirstName("Nurzhan");
        user_29.setLastName("Mynbayev");
        user_29.setPassword("demo");
        user_29.setEmail("Nurzhan.Mynbayev@kcell.kz");
        identityService.saveUser(user_29);

        User user_30 = identityService.newUser("Aigerim.Satybekova@kcell.kz");
        user_30.setFirstName("Aigerim");
        user_30.setLastName("Satybekova");
        user_30.setPassword("demo");
        user_30.setEmail("Aigerim.Satybekova@kcell.kz");
        identityService.saveUser(user_30);

        User user_31 = identityService.newUser("Dauren.Beispaev@kcell.kz");
        user_31.setFirstName("Dauren");
        user_31.setLastName("Beispaev");
        user_31.setPassword("demo");
        user_31.setEmail("Dauren.Beispaev@kcell.kz");
        identityService.saveUser(user_31);

        User user_32 = identityService.newUser("Alexey.Khudaev@kcell.kz");
        user_32.setFirstName("Alexey");
        user_32.setLastName("Khudaev");
        user_32.setPassword("demo");
        user_32.setEmail("Alexey.Khudaev@kcell.kz");
        identityService.saveUser(user_32);

        User user_33 = identityService.newUser("Maulen.Kempirbayev@kcell.kz");
        user_33.setFirstName("Maulen");
        user_33.setLastName("Kempirbayev");
        user_33.setPassword("demo");
        user_33.setEmail("Maulen.Kempirbayev@kcell.kz");
        identityService.saveUser(user_33);

        User user_34 = identityService.newUser("Kuanysh.Kussainbekov@kcell.kz");
        user_34.setFirstName("Kuanysh");
        user_34.setLastName("Kussainbekov");
        user_34.setPassword("demo");
        user_34.setEmail("Kuanysh.Kussainbekov@kcell.kz");
        identityService.saveUser(user_34);

        User user_35 = identityService.newUser("Valeriy.Pogorelov@kcell.kz");
        user_35.setFirstName("Valeriy");
        user_35.setLastName("Pogorelov");
        user_35.setPassword("demo");
        user_35.setEmail("Valeriy.Pogorelov@kcell.kz");
        identityService.saveUser(user_35);

        User user_36 = identityService.newUser("Aidos.Kenzhebayev@kcell.kz");
        user_36.setFirstName("Aidos");
        user_36.setLastName("Kenzhebayev");
        user_36.setPassword("demo");
        user_36.setEmail("Aidos.Kenzhebayev@kcell.kz");
        identityService.saveUser(user_36);

        User user_37 = identityService.newUser("Abai.Shapagatin@kcell.kz");
        user_37.setFirstName("Abai");
        user_37.setLastName("Shapagatin");
        user_37.setPassword("demo");
        user_37.setEmail("Abai.Shapagatin@kcell.kz");
        identityService.saveUser(user_37);

        User user_38 = identityService.newUser("Vladimir.Yefanov@kcell.kz");
        user_38.setFirstName("Vladimir");
        user_38.setLastName("Yefanov");
        user_38.setPassword("demo");
        user_38.setEmail("Vladimir.Yefanov@kcell.kz");
        identityService.saveUser(user_38);

        User user_39 = identityService.newUser("Zhanat.Seitkanov@kcell.kz");
        user_39.setFirstName("Zhanat");
        user_39.setLastName("Seitkanov");
        user_39.setPassword("demo");
        user_39.setEmail("Zhanat.Seitkanov@kcell.kz");
        identityService.saveUser(user_39);

        User user_40 = identityService.newUser("Vladimir.Grachyov@kcell.kz");
        user_40.setFirstName("Vladimir");
        user_40.setLastName("Grachyov");
        user_40.setPassword("demo");
        user_40.setEmail("Vladimir.Grachyov@kcell.kz");
        identityService.saveUser(user_40);

        User user_41 = identityService.newUser("Sergey.Chumachenko@kcell.kz");
        user_41.setFirstName("Sergey");
        user_41.setLastName("Chumachenko");
        user_41.setPassword("demo");
        user_41.setEmail("Sergey.Chumachenko@kcell.kz");
        identityService.saveUser(user_41);

        User user_42 = identityService.newUser("Kuanysh.Khozhamuratov@kcell.kz");
        user_42.setFirstName("Kuanysh");
        user_42.setLastName("Khozhamuratov");
        user_42.setPassword("demo");
        user_42.setEmail("Kuanysh.Khozhamuratov@kcell.kz");
        identityService.saveUser(user_42);

        User user_43 = identityService.newUser("Kirill.Kassatkin@kcell.kz");
        user_43.setFirstName("Kirill");
        user_43.setLastName("Kassatkin");
        user_43.setPassword("demo");
        user_43.setEmail("Kirill.Kassatkin@kcell.kz");
        identityService.saveUser(user_43);

        User user_44 = identityService.newUser("Aslan.Shalov@kcell.kz");
        user_44.setFirstName("Aslan");
        user_44.setLastName("Shalov");
        user_44.setPassword("demo");
        user_44.setEmail("Aslan.Shalov@kcell.kz");
        identityService.saveUser(user_44);

        User user_45 = identityService.newUser("Galym.Tulenbayev@kcell.kz");
        user_45.setFirstName("Galym");
        user_45.setLastName("Tulenbayev");
        user_45.setPassword("demo");
        user_45.setEmail("Galym.Tulenbayev@kcell.kz");
        identityService.saveUser(user_45);

        User user_46 = identityService.newUser("Leila.Raisova@kcell.kz");
        user_46.setFirstName("Leila");
        user_46.setLastName("Raisova");
        user_46.setPassword("demo");
        user_46.setEmail("Leila.Raisova@kcell.kz");
        identityService.saveUser(user_46);

        User user_47 = identityService.newUser("Alexey.Kolyagin@kcell.kz");
        user_47.setFirstName("Alexey");
        user_47.setLastName("Kolyagin");
        user_47.setPassword("demo");
        user_47.setEmail("Alexey.Kolyagin@kcell.kz");
        identityService.saveUser(user_47);

        User user_48 = identityService.newUser("Ruslan.Tubekbayev@kcell.kz");
        user_48.setFirstName("Ruslan");
        user_48.setLastName("Tubekbayev");
        user_48.setPassword("demo");
        user_48.setEmail("Ruslan.Tubekbayev@kcell.kz");
        identityService.saveUser(user_48);

        User user_49 = identityService.newUser("Saule.Beisembekova@kcell.kz");
        user_49.setFirstName("Saule");
        user_49.setLastName("Beisembekova");
        user_49.setPassword("demo");
        user_49.setEmail("Saule.Beisembekova@kcell.kz");
        identityService.saveUser(user_49);

        User user_50 = identityService.newUser("Arman.Utepov@kcell.kz");
        user_50.setFirstName("Arman");
        user_50.setLastName("Utepov");
        user_50.setPassword("demo");
        user_50.setEmail("Arman.Utepov@kcell.kz");
        identityService.saveUser(user_50);

        User user_51 = identityService.newUser("Andrey.Medvedev@kcell.kz");
        user_51.setFirstName("Andrey");
        user_51.setLastName("Medvedev");
        user_51.setPassword("demo");
        user_51.setEmail("Andrey.Medvedev@kcell.kz");
        identityService.saveUser(user_51);

        User user_52 = identityService.newUser("Rinat.Kurbangaliyev@kcell.kz");
        user_52.setFirstName("Rinat");
        user_52.setLastName("Kurbangaliyev");
        user_52.setPassword("demo");
        user_52.setEmail("Rinat.Kurbangaliyev@kcell.kz");
        identityService.saveUser(user_52);

        User user_53 = identityService.newUser("Anastassiya.Shenojak@kcell.kz");
        user_53.setFirstName("Anastassiya");
        user_53.setLastName("Shenojak");
        user_53.setPassword("demo");
        user_53.setEmail("Anastassiya.Shenojak@kcell.kz");
        identityService.saveUser(user_53);

        User user_54 = identityService.newUser("Zhandos.Bolatov@kcell.kz");
        user_54.setFirstName("Zhandos");
        user_54.setLastName("Bolatov");
        user_54.setPassword("demo");
        user_54.setEmail("Zhandos.Bolatov@kcell.kz");
        identityService.saveUser(user_54);

        User user_55 = identityService.newUser("Kerey.Zatilda@kcell.kz");
        user_55.setFirstName("Kerey");
        user_55.setLastName("Zatilda");
        user_55.setPassword("demo");
        user_55.setEmail("Kerey.Zatilda@kcell.kz");
        identityService.saveUser(user_55);

        User user_56 = identityService.newUser("Aigerim.Iskakova@kcell.kz");
        user_56.setFirstName("Aigerim");
        user_56.setLastName("Iskakova");
        user_56.setPassword("demo");
        user_56.setEmail("Aigerim.Iskakova@kcell.kz");
        identityService.saveUser(user_56);

        User user_57 = identityService.newUser("Gulzhan.Imandosova@kcell.kz");
        user_57.setFirstName("Gulzhan");
        user_57.setLastName("Imandosova");
        user_57.setPassword("demo");
        user_57.setEmail("Gulzhan.Imandosova@kcell.kz");
        identityService.saveUser(user_57);

        User user_58 = identityService.newUser("Samat.Akhmetov@kcell.kz");
        user_58.setFirstName("Samat");
        user_58.setLastName("Akhmetov");
        user_58.setPassword("demo");
        user_58.setEmail("Samat.Akhmetov@kcell.kz");
        identityService.saveUser(user_58);

        User user_59 = identityService.newUser("Farida.Jumanazarova@kcell.kz");
        user_59.setFirstName("Farida");
        user_59.setLastName("Jumanazarova");
        user_59.setPassword("demo");
        user_59.setEmail("Farida.Jumanazarova@kcell.kz");
        identityService.saveUser(user_59);

        User user_60 = identityService.newUser("Kanat.Kulmukhambetov@kcell.kz");
        user_60.setFirstName("Kanat");
        user_60.setLastName("Kulmukhambetov");
        user_60.setPassword("demo");
        user_60.setEmail("Kanat.Kulmukhambetov@kcell.kz");
        identityService.saveUser(user_60);

        User user_61 = identityService.newUser("Kali.Esimbekov@kcell.kz");
        user_61.setFirstName("Kali");
        user_61.setLastName("Esimbekov");
        user_61.setPassword("demo");
        user_61.setEmail("Kali.Esimbekov@kcell.kz");
        identityService.saveUser(user_61);

        User user_62 = identityService.newUser("Dmitriy.Saidashev@kcell.kz");
        user_62.setFirstName("Dmitriy");
        user_62.setLastName("Saidashev");
        user_62.setPassword("demo");
        user_62.setEmail("Dmitriy.Saidashev@kcell.kz");
        identityService.saveUser(user_62);

        User user_63 = identityService.newUser("Sergey.Grigor@kcell.kz");
        user_63.setFirstName("Sergey");
        user_63.setLastName("Grigor");
        user_63.setPassword("demo");
        user_63.setEmail("Sergey.Grigor@kcell.kz");
        identityService.saveUser(user_63);

        User user_64 = identityService.newUser("Alexey.Kolesnikov@kcell.kz");
        user_64.setFirstName("Alexey");
        user_64.setLastName("Kolesnikov");
        user_64.setPassword("demo");
        user_64.setEmail("Alexey.Kolesnikov@kcell.kz");
        identityService.saveUser(user_64);

        User user_65 = identityService.newUser("Anzor.Israilov@kcell.kz");
        user_65.setFirstName("Anzor");
        user_65.setLastName("Israilov");
        user_65.setPassword("demo");
        user_65.setEmail("Anzor.Israilov@kcell.kz");
        identityService.saveUser(user_65);

        User user_66 = identityService.newUser("Yernaz.Kalingarayev@kcell.kz");
        user_66.setFirstName("Yernaz");
        user_66.setLastName("Kalingarayev");
        user_66.setPassword("demo");
        user_66.setEmail("Yernaz.Kalingarayev@kcell.kz");
        identityService.saveUser(user_66);

        User user_67 = identityService.newUser("Askar.Slambekov@kcell.kz");
        user_67.setFirstName("Askar");
        user_67.setLastName("Slambekov");
        user_67.setPassword("demo");
        user_67.setEmail("Askar.Slambekov@kcell.kz");
        identityService.saveUser(user_67);

        LOGGER.warning("ALL USERS CREATED");
        LOGGER.warning("========================================================================");


        Group salesGroup = identityService.newGroup("kcellUsers");
        salesGroup.setName("Kcell Users");
        salesGroup.setType("WORKFLOW");
        identityService.saveGroup(salesGroup);
        LOGGER.warning("KCELL USERS GROUP CREATED");
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

        LOGGER.warning("CREATING MEMBERSHIPS ALL");
        LOGGER.warning("========================================================================");

        identityService.createMembership("Daniyar.Yespayev@kcell.kz", "kcellUsers");
        identityService.createMembership("Yerkebulan.Dauletbayev@kcell.kz", "kcellUsers");
        identityService.createMembership("Aigerim.Segizbayeva@kcell.kz", "kcellUsers");
        identityService.createMembership("Evgeniy.Semenov@kcell.kz", "kcellUsers");
        identityService.createMembership("Alexander.Shanygin@kcell.kz", "kcellUsers");
        identityService.createMembership("Asset.Rashitov@kcell.kz", "kcellUsers");
        identityService.createMembership("Anna.Martynova@kcell.kz", "kcellUsers");
        identityService.createMembership("Makhabbat.Sasanova@kcell.kz", "kcellUsers");
        identityService.createMembership("Amanbek.Suimenbayev@kcell.kz", "kcellUsers");
        identityService.createMembership("Lazizbek.Kurbantayev@kcell.kz", "kcellUsers");
        identityService.createMembership("Gaukhar.Kaisarova@kcell.kz", "kcellUsers");
        identityService.createMembership("Bahytzhan.Sandybayev@kcell.kz", "kcellUsers");
        identityService.createMembership("Sergey.Michshenko@kcell.kz", "kcellUsers");
        identityService.createMembership("Aleksandr.Kobelev@kcell.kz", "kcellUsers");
        identityService.createMembership("Alibek.Nurkassymov@kcell.kz", "kcellUsers");
        identityService.createMembership("Margarita.Kim@kcell.kz", "kcellUsers");
        identityService.createMembership("Daniyar.Uzibayev@kcell.kz", "kcellUsers");
        identityService.createMembership("Marina.Paramonova@kcell.kz", "kcellUsers");
        identityService.createMembership("Yermek.Tanabekov@kcell.kz", "kcellUsers");
        identityService.createMembership("Vladislav.Posashkov@kcell.kz", "kcellUsers");
        identityService.createMembership("Andrei.Lugovoy@kcell.kz", "kcellUsers");
        identityService.createMembership("Nurzhan.Kochshigulov@kcell.kz", "kcellUsers");
        identityService.createMembership("Vassiliy.Gopkalo@kcell.kz", "kcellUsers");
        identityService.createMembership("Askar.Pernebekov@kcell.kz", "kcellUsers");
        identityService.createMembership("Lyudmila.Jabrailova@kcell.kz", "kcellUsers");
        identityService.createMembership("Sergey.Chekh@kcell.kz", "kcellUsers");
        identityService.createMembership("Beibit.Bitenov@kcell.kz", "kcellUsers");
        identityService.createMembership("Sergey.Lee@kcell.kz", "kcellUsers");
        identityService.createMembership("Yevgeniy.Elunin@kcell.kz", "kcellUsers");
        identityService.createMembership("Nurzhan.Mynbayev@kcell.kz", "kcellUsers");
        identityService.createMembership("Aigerim.Satybekova@kcell.kz", "kcellUsers");
        identityService.createMembership("Dauren.Beispaev@kcell.kz", "kcellUsers");
        identityService.createMembership("Alexey.Khudaev@kcell.kz", "kcellUsers");
        identityService.createMembership("Maulen.Kempirbayev@kcell.kz", "kcellUsers");
        identityService.createMembership("Kuanysh.Kussainbekov@kcell.kz", "kcellUsers");
        identityService.createMembership("Valeriy.Pogorelov@kcell.kz", "kcellUsers");
        identityService.createMembership("Aidos.Kenzhebayev@kcell.kz", "kcellUsers");
        identityService.createMembership("Abai.Shapagatin@kcell.kz", "kcellUsers");
        identityService.createMembership("Vladimir.Yefanov@kcell.kz", "kcellUsers");
        identityService.createMembership("Zhanat.Seitkanov@kcell.kz", "kcellUsers");
        identityService.createMembership("Vladimir.Grachyov@kcell.kz", "kcellUsers");
        identityService.createMembership("Sergey.Chumachenko@kcell.kz", "kcellUsers");
        identityService.createMembership("Kuanysh.Khozhamuratov@kcell.kz", "kcellUsers");
        identityService.createMembership("Kirill.Kassatkin@kcell.kz", "kcellUsers");
        identityService.createMembership("Aslan.Shalov@kcell.kz", "kcellUsers");
        identityService.createMembership("Galym.Tulenbayev@kcell.kz", "kcellUsers");
        identityService.createMembership("Leila.Raisova@kcell.kz", "kcellUsers");
        identityService.createMembership("Alexey.Kolyagin@kcell.kz", "kcellUsers");
        identityService.createMembership("Ruslan.Tubekbayev@kcell.kz", "kcellUsers");
        identityService.createMembership("Saule.Beisembekova@kcell.kz", "kcellUsers");
        identityService.createMembership("Arman.Utepov@kcell.kz", "kcellUsers");
        identityService.createMembership("Andrey.Medvedev@kcell.kz", "kcellUsers");
        identityService.createMembership("Rinat.Kurbangaliyev@kcell.kz", "kcellUsers");
        identityService.createMembership("Anastassiya.Shenojak@kcell.kz", "kcellUsers");
        identityService.createMembership("Zhandos.Bolatov@kcell.kz", "kcellUsers");
        identityService.createMembership("Kerey.Zatilda@kcell.kz", "kcellUsers");
        identityService.createMembership("Aigerim.Iskakova@kcell.kz", "kcellUsers");
        identityService.createMembership("Gulzhan.Imandosova@kcell.kz", "kcellUsers");
        identityService.createMembership("Samat.Akhmetov@kcell.kz", "kcellUsers");
        identityService.createMembership("Farida.Jumanazarova@kcell.kz", "kcellUsers");
        identityService.createMembership("Kanat.Kulmukhambetov@kcell.kz", "kcellUsers");
        identityService.createMembership("Kali.Esimbekov@kcell.kz", "kcellUsers");
        identityService.createMembership("Dmitriy.Saidashev@kcell.kz", "kcellUsers");
        identityService.createMembership("Sergey.Grigor@kcell.kz", "kcellUsers");
        identityService.createMembership("Alexey.Kolesnikov@kcell.kz", "kcellUsers");
        identityService.createMembership("Anzor.Israilov@kcell.kz", "kcellUsers");
        identityService.createMembership("Yernaz.Kalingarayev@kcell.kz", "kcellUsers");
        identityService.createMembership("Askar.Slambekov@kcell.kz", "kcellUsers");

        LOGGER.warning("MEMBERSHIPS CREATED");
        LOGGER.warning("========================================================================");


        // authorize groups for tasklist only:

        Authorization salesTasklistAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        salesTasklistAuth.setGroupId("kcellUsers");
        salesTasklistAuth.addPermission(ACCESS);
        salesTasklistAuth.setResourceId("tasklist");
        salesTasklistAuth.setResource(APPLICATION);
        authorizationService.saveAuthorization(salesTasklistAuth);

        Authorization salesReadProcessDefinition = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        salesReadProcessDefinition.setGroupId("kcellUsers");
        salesReadProcessDefinition.addPermission(Permissions.ALL);
        salesReadProcessDefinition.setResource(Resources.PROCESS_DEFINITION);
        salesReadProcessDefinition.setResourceId("Revision");
        authorizationService.saveAuthorization(salesReadProcessDefinition);

        Authorization salesReadProcessInstance = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        salesReadProcessInstance.setGroupId("kcellUsers");
        salesReadProcessInstance.addPermission(Permissions.ALL);
        salesReadProcessInstance.setResource(Resources.PROCESS_INSTANCE);
        salesReadProcessInstance.setResourceId("*");
        authorizationService.saveAuthorization(salesReadProcessInstance);

        Authorization salesDemoAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        salesDemoAuth.setGroupId("kcellUsers");
        salesDemoAuth.setResource(USER);
        salesDemoAuth.setResourceId("demo");
        salesDemoAuth.addPermission(READ);
        authorizationService.saveAuthorization(salesDemoAuth);

        Authorization salesTestDemoAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        salesTestDemoAuth.setGroupId("kcellUsers");
        salesTestDemoAuth.setResource(USER);
        salesTestDemoAuth.setResourceId("test_flow@kcell.kz");
        salesTestDemoAuth.addPermission(READ);
        authorizationService.saveAuthorization(salesTestDemoAuth);


        Authorization kcellUserAuth_0 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_0.setGroupId("kcellUsers");
        kcellUserAuth_0.setResource(USER);
        kcellUserAuth_0.setResourceId("Daniyar.Yespayev@kcell.kz");
        kcellUserAuth_0.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_0);

        Authorization kcellUserAuth_1 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_1.setGroupId("kcellUsers");
        kcellUserAuth_1.setResource(USER);
        kcellUserAuth_1.setResourceId("Yerkebulan.Dauletbayev@kcell.kz");
        kcellUserAuth_1.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_1);

        Authorization kcellUserAuth_2 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_2.setGroupId("kcellUsers");
        kcellUserAuth_2.setResource(USER);
        kcellUserAuth_2.setResourceId("Aigerim.Segizbayeva@kcell.kz");
        kcellUserAuth_2.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_2);

        Authorization kcellUserAuth_3 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_3.setGroupId("kcellUsers");
        kcellUserAuth_3.setResource(USER);
        kcellUserAuth_3.setResourceId("Evgeniy.Semenov@kcell.kz");
        kcellUserAuth_3.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_3);

        Authorization kcellUserAuth_4 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_4.setGroupId("kcellUsers");
        kcellUserAuth_4.setResource(USER);
        kcellUserAuth_4.setResourceId("Alexander.Shanygin@kcell.kz");
        kcellUserAuth_4.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_4);

        Authorization kcellUserAuth_5 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_5.setGroupId("kcellUsers");
        kcellUserAuth_5.setResource(USER);
        kcellUserAuth_5.setResourceId("Asset.Rashitov@kcell.kz");
        kcellUserAuth_5.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_5);

        Authorization kcellUserAuth_6 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_6.setGroupId("kcellUsers");
        kcellUserAuth_6.setResource(USER);
        kcellUserAuth_6.setResourceId("Anna.Martynova@kcell.kz");
        kcellUserAuth_6.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_6);

        Authorization kcellUserAuth_7 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_7.setGroupId("kcellUsers");
        kcellUserAuth_7.setResource(USER);
        kcellUserAuth_7.setResourceId("Makhabbat.Sasanova@kcell.kz");
        kcellUserAuth_7.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_7);

        Authorization kcellUserAuth_8 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_8.setGroupId("kcellUsers");
        kcellUserAuth_8.setResource(USER);
        kcellUserAuth_8.setResourceId("Amanbek.Suimenbayev@kcell.kz");
        kcellUserAuth_8.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_8);

        Authorization kcellUserAuth_9 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_9.setGroupId("kcellUsers");
        kcellUserAuth_9.setResource(USER);
        kcellUserAuth_9.setResourceId("Lazizbek.Kurbantayev@kcell.kz");
        kcellUserAuth_9.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_9);

        Authorization kcellUserAuth_10 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_10.setGroupId("kcellUsers");
        kcellUserAuth_10.setResource(USER);
        kcellUserAuth_10.setResourceId("Gaukhar.Kaisarova@kcell.kz");
        kcellUserAuth_10.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_10);

        Authorization kcellUserAuth_11 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_11.setGroupId("kcellUsers");
        kcellUserAuth_11.setResource(USER);
        kcellUserAuth_11.setResourceId("Bahytzhan.Sandybayev@kcell.kz");
        kcellUserAuth_11.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_11);

        Authorization kcellUserAuth_12 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_12.setGroupId("kcellUsers");
        kcellUserAuth_12.setResource(USER);
        kcellUserAuth_12.setResourceId("Sergey.Michshenko@kcell.kz");
        kcellUserAuth_12.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_12);

        Authorization kcellUserAuth_13 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_13.setGroupId("kcellUsers");
        kcellUserAuth_13.setResource(USER);
        kcellUserAuth_13.setResourceId("Aleksandr.Kobelev@kcell.kz");
        kcellUserAuth_13.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_13);

        Authorization kcellUserAuth_14 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_14.setGroupId("kcellUsers");
        kcellUserAuth_14.setResource(USER);
        kcellUserAuth_14.setResourceId("Alibek.Nurkassymov@kcell.kz");
        kcellUserAuth_14.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_14);

        Authorization kcellUserAuth_15 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_15.setGroupId("kcellUsers");
        kcellUserAuth_15.setResource(USER);
        kcellUserAuth_15.setResourceId("Margarita.Kim@kcell.kz");
        kcellUserAuth_15.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_15);

        Authorization kcellUserAuth_16 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_16.setGroupId("kcellUsers");
        kcellUserAuth_16.setResource(USER);
        kcellUserAuth_16.setResourceId("Daniyar.Uzibayev@kcell.kz");
        kcellUserAuth_16.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_16);

        Authorization kcellUserAuth_17 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_17.setGroupId("kcellUsers");
        kcellUserAuth_17.setResource(USER);
        kcellUserAuth_17.setResourceId("Marina.Paramonova@kcell.kz");
        kcellUserAuth_17.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_17);

        Authorization kcellUserAuth_18 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_18.setGroupId("kcellUsers");
        kcellUserAuth_18.setResource(USER);
        kcellUserAuth_18.setResourceId("Yermek.Tanabekov@kcell.kz");
        kcellUserAuth_18.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_18);

        Authorization kcellUserAuth_19 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_19.setGroupId("kcellUsers");
        kcellUserAuth_19.setResource(USER);
        kcellUserAuth_19.setResourceId("Vladislav.Posashkov@kcell.kz");
        kcellUserAuth_19.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_19);

        Authorization kcellUserAuth_20 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_20.setGroupId("kcellUsers");
        kcellUserAuth_20.setResource(USER);
        kcellUserAuth_20.setResourceId("Andrei.Lugovoy@kcell.kz");
        kcellUserAuth_20.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_20);

        Authorization kcellUserAuth_21 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_21.setGroupId("kcellUsers");
        kcellUserAuth_21.setResource(USER);
        kcellUserAuth_21.setResourceId("Nurzhan.Kochshigulov@kcell.kz");
        kcellUserAuth_21.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_21);

        Authorization kcellUserAuth_22 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_22.setGroupId("kcellUsers");
        kcellUserAuth_22.setResource(USER);
        kcellUserAuth_22.setResourceId("Vassiliy.Gopkalo@kcell.kz");
        kcellUserAuth_22.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_22);

        Authorization kcellUserAuth_23 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_23.setGroupId("kcellUsers");
        kcellUserAuth_23.setResource(USER);
        kcellUserAuth_23.setResourceId("Askar.Pernebekov@kcell.kz");
        kcellUserAuth_23.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_23);

        Authorization kcellUserAuth_24 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_24.setGroupId("kcellUsers");
        kcellUserAuth_24.setResource(USER);
        kcellUserAuth_24.setResourceId("Lyudmila.Jabrailova@kcell.kz");
        kcellUserAuth_24.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_24);

        Authorization kcellUserAuth_25 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_25.setGroupId("kcellUsers");
        kcellUserAuth_25.setResource(USER);
        kcellUserAuth_25.setResourceId("Sergey.Chekh@kcell.kz");
        kcellUserAuth_25.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_25);

        Authorization kcellUserAuth_26 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_26.setGroupId("kcellUsers");
        kcellUserAuth_26.setResource(USER);
        kcellUserAuth_26.setResourceId("Beibit.Bitenov@kcell.kz");
        kcellUserAuth_26.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_26);

        Authorization kcellUserAuth_27 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_27.setGroupId("kcellUsers");
        kcellUserAuth_27.setResource(USER);
        kcellUserAuth_27.setResourceId("Sergey.Lee@kcell.kz");
        kcellUserAuth_27.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_27);

        Authorization kcellUserAuth_28 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_28.setGroupId("kcellUsers");
        kcellUserAuth_28.setResource(USER);
        kcellUserAuth_28.setResourceId("Yevgeniy.Elunin@kcell.kz");
        kcellUserAuth_28.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_28);

        Authorization kcellUserAuth_29 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_29.setGroupId("kcellUsers");
        kcellUserAuth_29.setResource(USER);
        kcellUserAuth_29.setResourceId("Nurzhan.Mynbayev@kcell.kz");
        kcellUserAuth_29.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_29);

        Authorization kcellUserAuth_30 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_30.setGroupId("kcellUsers");
        kcellUserAuth_30.setResource(USER);
        kcellUserAuth_30.setResourceId("Aigerim.Satybekova@kcell.kz");
        kcellUserAuth_30.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_30);

        Authorization kcellUserAuth_31 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_31.setGroupId("kcellUsers");
        kcellUserAuth_31.setResource(USER);
        kcellUserAuth_31.setResourceId("Dauren.Beispaev@kcell.kz");
        kcellUserAuth_31.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_31);

        Authorization kcellUserAuth_32 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_32.setGroupId("kcellUsers");
        kcellUserAuth_32.setResource(USER);
        kcellUserAuth_32.setResourceId("Alexey.Khudaev@kcell.kz");
        kcellUserAuth_32.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_32);

        Authorization kcellUserAuth_33 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_33.setGroupId("kcellUsers");
        kcellUserAuth_33.setResource(USER);
        kcellUserAuth_33.setResourceId("Maulen.Kempirbayev@kcell.kz");
        kcellUserAuth_33.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_33);

        Authorization kcellUserAuth_34 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_34.setGroupId("kcellUsers");
        kcellUserAuth_34.setResource(USER);
        kcellUserAuth_34.setResourceId("Kuanysh.Kussainbekov@kcell.kz");
        kcellUserAuth_34.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_34);

        Authorization kcellUserAuth_35 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_35.setGroupId("kcellUsers");
        kcellUserAuth_35.setResource(USER);
        kcellUserAuth_35.setResourceId("Valeriy.Pogorelov@kcell.kz");
        kcellUserAuth_35.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_35);

        Authorization kcellUserAuth_36 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_36.setGroupId("kcellUsers");
        kcellUserAuth_36.setResource(USER);
        kcellUserAuth_36.setResourceId("Aidos.Kenzhebayev@kcell.kz");
        kcellUserAuth_36.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_36);

        Authorization kcellUserAuth_37 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_37.setGroupId("kcellUsers");
        kcellUserAuth_37.setResource(USER);
        kcellUserAuth_37.setResourceId("Abai.Shapagatin@kcell.kz");
        kcellUserAuth_37.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_37);

        Authorization kcellUserAuth_38 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_38.setGroupId("kcellUsers");
        kcellUserAuth_38.setResource(USER);
        kcellUserAuth_38.setResourceId("Vladimir.Yefanov@kcell.kz");
        kcellUserAuth_38.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_38);

        Authorization kcellUserAuth_39 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_39.setGroupId("kcellUsers");
        kcellUserAuth_39.setResource(USER);
        kcellUserAuth_39.setResourceId("Zhanat.Seitkanov@kcell.kz");
        kcellUserAuth_39.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_39);

        Authorization kcellUserAuth_40 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_40.setGroupId("kcellUsers");
        kcellUserAuth_40.setResource(USER);
        kcellUserAuth_40.setResourceId("Vladimir.Grachyov@kcell.kz");
        kcellUserAuth_40.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_40);

        Authorization kcellUserAuth_41 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_41.setGroupId("kcellUsers");
        kcellUserAuth_41.setResource(USER);
        kcellUserAuth_41.setResourceId("Sergey.Chumachenko@kcell.kz");
        kcellUserAuth_41.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_41);

        Authorization kcellUserAuth_42 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_42.setGroupId("kcellUsers");
        kcellUserAuth_42.setResource(USER);
        kcellUserAuth_42.setResourceId("Kuanysh.Khozhamuratov@kcell.kz");
        kcellUserAuth_42.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_42);

        Authorization kcellUserAuth_43 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_43.setGroupId("kcellUsers");
        kcellUserAuth_43.setResource(USER);
        kcellUserAuth_43.setResourceId("Kirill.Kassatkin@kcell.kz");
        kcellUserAuth_43.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_43);

        Authorization kcellUserAuth_44 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_44.setGroupId("kcellUsers");
        kcellUserAuth_44.setResource(USER);
        kcellUserAuth_44.setResourceId("Aslan.Shalov@kcell.kz");
        kcellUserAuth_44.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_44);

        Authorization kcellUserAuth_45 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_45.setGroupId("kcellUsers");
        kcellUserAuth_45.setResource(USER);
        kcellUserAuth_45.setResourceId("Galym.Tulenbayev@kcell.kz");
        kcellUserAuth_45.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_45);

        Authorization kcellUserAuth_46 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_46.setGroupId("kcellUsers");
        kcellUserAuth_46.setResource(USER);
        kcellUserAuth_46.setResourceId("Leila.Raisova@kcell.kz");
        kcellUserAuth_46.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_46);

        Authorization kcellUserAuth_47 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_47.setGroupId("kcellUsers");
        kcellUserAuth_47.setResource(USER);
        kcellUserAuth_47.setResourceId("Alexey.Kolyagin@kcell.kz");
        kcellUserAuth_47.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_47);

        Authorization kcellUserAuth_48 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_48.setGroupId("kcellUsers");
        kcellUserAuth_48.setResource(USER);
        kcellUserAuth_48.setResourceId("Ruslan.Tubekbayev@kcell.kz");
        kcellUserAuth_48.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_48);

        Authorization kcellUserAuth_49 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_49.setGroupId("kcellUsers");
        kcellUserAuth_49.setResource(USER);
        kcellUserAuth_49.setResourceId("Saule.Beisembekova@kcell.kz");
        kcellUserAuth_49.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_49);

        Authorization kcellUserAuth_50 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_50.setGroupId("kcellUsers");
        kcellUserAuth_50.setResource(USER);
        kcellUserAuth_50.setResourceId("Arman.Utepov@kcell.kz");
        kcellUserAuth_50.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_50);

        Authorization kcellUserAuth_51 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_51.setGroupId("kcellUsers");
        kcellUserAuth_51.setResource(USER);
        kcellUserAuth_51.setResourceId("Andrey.Medvedev@kcell.kz");
        kcellUserAuth_51.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_51);

        Authorization kcellUserAuth_52 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_52.setGroupId("kcellUsers");
        kcellUserAuth_52.setResource(USER);
        kcellUserAuth_52.setResourceId("Rinat.Kurbangaliyev@kcell.kz");
        kcellUserAuth_52.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_52);

        Authorization kcellUserAuth_53 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_53.setGroupId("kcellUsers");
        kcellUserAuth_53.setResource(USER);
        kcellUserAuth_53.setResourceId("Anastassiya.Shenojak@kcell.kz");
        kcellUserAuth_53.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_53);

        Authorization kcellUserAuth_54 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_54.setGroupId("kcellUsers");
        kcellUserAuth_54.setResource(USER);
        kcellUserAuth_54.setResourceId("Zhandos.Bolatov@kcell.kz");
        kcellUserAuth_54.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_54);

        Authorization kcellUserAuth_55 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_55.setGroupId("kcellUsers");
        kcellUserAuth_55.setResource(USER);
        kcellUserAuth_55.setResourceId("Kerey.Zatilda@kcell.kz");
        kcellUserAuth_55.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_55);

        Authorization kcellUserAuth_56 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_56.setGroupId("kcellUsers");
        kcellUserAuth_56.setResource(USER);
        kcellUserAuth_56.setResourceId("Aigerim.Iskakova@kcell.kz");
        kcellUserAuth_56.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_56);

        Authorization kcellUserAuth_57 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_57.setGroupId("kcellUsers");
        kcellUserAuth_57.setResource(USER);
        kcellUserAuth_57.setResourceId("Gulzhan.Imandosova@kcell.kz");
        kcellUserAuth_57.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_57);

        Authorization kcellUserAuth_58 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_58.setGroupId("kcellUsers");
        kcellUserAuth_58.setResource(USER);
        kcellUserAuth_58.setResourceId("Samat.Akhmetov@kcell.kz");
        kcellUserAuth_58.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_58);

        Authorization kcellUserAuth_59 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_59.setGroupId("kcellUsers");
        kcellUserAuth_59.setResource(USER);
        kcellUserAuth_59.setResourceId("Farida.Jumanazarova@kcell.kz");
        kcellUserAuth_59.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_59);

        Authorization kcellUserAuth_60 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_60.setGroupId("kcellUsers");
        kcellUserAuth_60.setResource(USER);
        kcellUserAuth_60.setResourceId("Kanat.Kulmukhambetov@kcell.kz");
        kcellUserAuth_60.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_60);

        Authorization kcellUserAuth_61 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_61.setGroupId("kcellUsers");
        kcellUserAuth_61.setResource(USER);
        kcellUserAuth_61.setResourceId("Kali.Esimbekov@kcell.kz");
        kcellUserAuth_61.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_61);

        Authorization kcellUserAuth_62 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_62.setGroupId("kcellUsers");
        kcellUserAuth_62.setResource(USER);
        kcellUserAuth_62.setResourceId("Dmitriy.Saidashev@kcell.kz");
        kcellUserAuth_62.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_62);

        Authorization kcellUserAuth_63 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_63.setGroupId("kcellUsers");
        kcellUserAuth_63.setResource(USER);
        kcellUserAuth_63.setResourceId("Sergey.Grigor@kcell.kz");
        kcellUserAuth_63.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_63);

        Authorization kcellUserAuth_64 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_64.setGroupId("kcellUsers");
        kcellUserAuth_64.setResource(USER);
        kcellUserAuth_64.setResourceId("Alexey.Kolesnikov@kcell.kz");
        kcellUserAuth_64.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_64);

        Authorization kcellUserAuth_65 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_65.setGroupId("kcellUsers");
        kcellUserAuth_65.setResource(USER);
        kcellUserAuth_65.setResourceId("Anzor.Israilov@kcell.kz");
        kcellUserAuth_65.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_65);

        Authorization kcellUserAuth_66 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_66.setGroupId("kcellUsers");
        kcellUserAuth_66.setResource(USER);
        kcellUserAuth_66.setResourceId("Yernaz.Kalingarayev@kcell.kz");
        kcellUserAuth_66.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_66);

        Authorization kcellUserAuth_67 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        kcellUserAuth_67.setGroupId("kcellUsers");
        kcellUserAuth_67.setResource(USER);
        kcellUserAuth_67.setResourceId("Askar.Slambekov@kcell.kz");
        kcellUserAuth_67.addPermission(READ);
        authorizationService.saveAuthorization(kcellUserAuth_67);

        LOGGER.warning("AUTHORIZATION CREATED");
        LOGGER.warning("========================================================================");

        // create default filters

//        FilterService filterService = engine.getFilterService();
//
//        Map<String, Object> filterProperties = new HashMap<String, Object>();
//        filterProperties.put("description", "Tasks assigned to me");
//        filterProperties.put("priority", -10);
//        addVariables(filterProperties);
//        TaskService taskService = engine.getTaskService();
//        TaskQuery query = taskService.createTaskQuery().taskAssigneeExpression("${currentUser()}");
//        Filter myTasksFilter = filterService.newTaskFilter().setName("My Tasks").setProperties(filterProperties).setOwner("demo").setQuery(query);
//        filterService.saveFilter(myTasksFilter);
//
//        filterProperties.clear();
//        filterProperties.put("description", "Tasks assigned to my Groups");
//        filterProperties.put("priority", -5);
//        addVariables(filterProperties);
//        query = taskService.createTaskQuery().taskCandidateGroupInExpression("${currentUserGroups()}").taskUnassigned();
//        Filter groupTasksFilter = filterService.newTaskFilter().setName("My Group Tasks").setProperties(filterProperties).setOwner("demo").setQuery(query);
//        filterService.saveFilter(groupTasksFilter);
//
//        // global read authorizations for these filters
//
//        Authorization globalMyTaskFilterRead = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GLOBAL);
//        globalMyTaskFilterRead.setResource(FILTER);
//        globalMyTaskFilterRead.setResourceId(myTasksFilter.getId());
//        globalMyTaskFilterRead.addPermission(READ);
//        authorizationService.saveAuthorization(globalMyTaskFilterRead);
//
//        Authorization globalGroupFilterRead = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GLOBAL);
//        globalGroupFilterRead.setResource(FILTER);
//        globalGroupFilterRead.setResourceId(groupTasksFilter.getId());
//        globalGroupFilterRead.addPermission(READ);
//        authorizationService.saveAuthorization(globalGroupFilterRead);
//
//        // management filter
//
//        filterProperties.clear();
//        filterProperties.put("description", "Tasks for Group Accounting");
//        filterProperties.put("priority", -3);
//        addVariables(filterProperties);
//        query = taskService.createTaskQuery().taskCandidateGroupIn(Arrays.asList("accounting")).taskUnassigned();
//        Filter candidateGroupTasksFilter = filterService.newTaskFilter().setName("Accounting").setProperties(filterProperties).setOwner("demo").setQuery(query);
//        filterService.saveFilter(candidateGroupTasksFilter);
//
//        Authorization managementGroupFilterRead = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
//        managementGroupFilterRead.setResource(FILTER);
//        managementGroupFilterRead.setResourceId(candidateGroupTasksFilter.getId());
//        managementGroupFilterRead.addPermission(READ);
//        managementGroupFilterRead.setGroupId("accounting");
//        authorizationService.saveAuthorization(managementGroupFilterRead);

//        // john's tasks
//
//        filterProperties.clear();
//        filterProperties.put("description", "Tasks assigned to John");
//        filterProperties.put("priority", -1);
//        addVariables(filterProperties);
//        query = taskService.createTaskQuery().taskAssignee("john");
//        Filter johnsTasksFilter = filterService.newTaskFilter().setName("John's Tasks").setProperties(filterProperties).setOwner("demo").setQuery(query);
//        filterService.saveFilter(johnsTasksFilter);
//
//        // mary's tasks
//
//        filterProperties.clear();
//        filterProperties.put("description", "Tasks assigned to Mary");
//        filterProperties.put("priority", -1);
//        addVariables(filterProperties);
//        query = taskService.createTaskQuery().taskAssignee("mary");
//        Filter marysTasksFilter = filterService.newTaskFilter().setName("Mary's Tasks").setProperties(filterProperties).setOwner("demo").setQuery(query);
//        filterService.saveFilter(marysTasksFilter);
//
//        // peter's tasks
//
//        filterProperties.clear();
//        filterProperties.put("description", "Tasks assigned to Peter");
//        filterProperties.put("priority", -1);
//        addVariables(filterProperties);
//        query = taskService.createTaskQuery().taskAssignee("peter");
//        Filter petersTasksFilter = filterService.newTaskFilter().setName("Peter's Tasks").setProperties(filterProperties).setOwner("demo").setQuery(query);
//        filterService.saveFilter(petersTasksFilter);
//
//        // all tasks
//
        Map<String, Object> filterProperties = new HashMap<String, Object>();
        FilterService filterService = engine.getFilterService();
        TaskService taskService = engine.getTaskService();
        TaskQuery query = taskService.createTaskQuery().taskAssigneeExpression("${currentUser()}");
        filterProperties.clear();
        filterProperties.put("description", "All Tasks - Not recommended to be used in production :)");
        filterProperties.put("priority", 10);
        //addVariables(filterProperties);
        query = taskService.createTaskQuery();
        Filter allTasksFilter = filterService.newTaskFilter().setName("All Tasks").setProperties(filterProperties).setOwner("demo").setQuery(query);
        filterService.saveFilter(allTasksFilter);


        //My Claimed Tasks
        filterProperties.clear();
        filterProperties.put("description", "My Claimed Tasks");
        filterProperties.put("priority", -10);
        //addVariables(filterProperties);
        query = taskService.createTaskQuery().taskAssigneeExpression("${ currentUser() }");
        Filter myClaimedTasks = filterService.newTaskFilter().setName("My Claimed Tasks").setProperties(filterProperties).setOwner("demo").setQuery(query);
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
        //addVariables(filterProperties);
        query = taskService.createTaskQuery().taskCandidateUserExpression("${currentUser()}");
        Filter myUnclaimedTasksFilter = filterService.newTaskFilter().setName("My Unclaimed Tasks").setProperties(filterProperties).setOwner("demo").setQuery(query);
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
        //addVariables(filterProperties);
        query = taskService.createTaskQuery().taskCandidateGroupInExpression("${ currentUserGroups() }");
        Filter myGroupTasks = filterService.newTaskFilter().setName("My Group Tasks").setProperties(filterProperties).setOwner("demo").setQuery(query);
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

//    protected void addVariables(Map<String, Object> filterProperties) {
//        List<Object> variables = new ArrayList<Object>();
//
//        addVariable(variables, "amount", "Invoice Amount");
//        addVariable(variables, "invoiceNumber", "Invoice Number");
//        addVariable(variables, "creditor", "Creditor");
//        addVariable(variables, "approver", "Approver");
//
//        filterProperties.put("variables", variables);
//    }
//
//    protected void addVariable(List<Object> variables, String name, String label) {
//        Map<String, String> variable = new HashMap<String, String>();
//        variable.put("name", name);
//        variable.put("label", label);
//        variables.add(variable);
//    }
}