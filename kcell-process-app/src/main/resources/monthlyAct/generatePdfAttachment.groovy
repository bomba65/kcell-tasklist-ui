import groovy.json.JsonSlurper
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import org.camunda.bpm.engine.impl.util.json.JSONArray
import org.camunda.bpm.engine.impl.util.json.JSONObject

import java.text.SimpleDateFormat

def monthsTitleRu = [
        'January': 'Январь',
        'February': 'Февраль',
        'March': 'Март',
        'April': 'Апрель',
        'May': 'Май',
        'June': 'Июнь',
        'July': 'Июль',
        'August': 'Август',
        'September': 'Сентябрь',
        'October': 'Октябрь',
        'November': 'Ноябрь',
        'December': 'Декабрь'
]
def regionsMap = [
        'alm': 'Almaty',
        'astana': 'Astana',
        'nc': 'N&C',
        'east': 'East',
        'south': 'South',
        'west': 'West'
]
def mapSubcontractorsToNum = [
        'ALTA':'6',
        'ARLAN':'8',
        'LOGYCOM':'7',
        'LINE':'4'
]

def contractorsTitle = [
        "-1": "Subcontractor",
        "1": "ТОО Аврора Сервис",
        "2": "ТОО AICOM",
        "3": "ТОО Spectr energy group",
        "4": "TOO Line System Engineering",
        "5": "Kcell_region",
        "6": "Алта Телеком",
        "7": "Логиком",
        "8": "Arlan SI",
        "9": "ТОО Inter Service",
        "10": "Forester-Hes Group",
        "11": "Транстелеком"
]

def reasonsTitle = [
        "1": "Optimization works",
        "2": "Transmission works",
        "3": "Infrastructure works",
        "4": "Operation works",
        "5": "Roll-out works",
        "6": "Подготовка проекта"
]


def selectedRevisionsObject = new JSONObject(selectedRevisions.toString());
/*
def selectedRevisions = '{"ab359001-0de8-11eb-900c-0242ac120008":{"selected":true,"businessKey":"West-Line_Eng-SAO-20-7004","processInstanceId":"ab359001-0de8-11eb-900c-0242ac120008","$$hashKey":"object:224","priority":"emergency","siteRegion":"west","site_name":"61163XMUNAISHI","performDate":"2020-11-13T18:15:34.207+0600","contractorJobAssignedDate":"2020-10-15T16:15:45.871+0600","acceptanceDate":"2020-10-19T01:00:00.000+0600","reason":"4","acceptWorkStartTime":"2020-11-13T18:15:34.207+0600","acceptWorkEndTime":"2020-10-19T01:00:00.000+0600","works":[{"relatedSites":[{"name":"61163","id":"34518","site_name":"61163XMUNAISHI","params":{"site_name":"61163XMUNAISHI"},"$$hashKey":"object:216"}],"displayServiceName":"8.15 Замена электрического счётчика ;шт.","sapServiceNumber":"8.15","materialUnit":"шт..","quantity":1,"materialQuantity":1,"reason":"change_support","expenseType":"OPEX","reasonDetail":"Замена - восстановление вышедшего из строя оборудования/OC для поддержания их в рабочем состоянии  с целью их профилактики","$$hashKey":"object:180","files":[{"name":"work_0_file_0","type":"Json","value":{"name":"61163XMUNAISHI.doc","path":"ab359001-0de8-11eb-900c-0242ac120008/4918b69f-25a5-11eb-a45d-0242ac120007/61163XMUNAISHI.doc"},"$$hashKey":"object:549"},{"name":"work_0_file_1","type":"Json","value":{"name":"61163XMUNAISHI.xls","path":"ab359001-0de8-11eb-900c-0242ac120008/4918b69f-25a5-11eb-a45d-0242ac120007/61163XMUNAISHI.xls"},"$$hashKey":"object:550"},{"name":"work_0_file_2","type":"Json","value":{"name":"61163XMUNAISHI.pdf","path":"ab359001-0de8-11eb-900c-0242ac120008/4918b69f-25a5-11eb-a45d-0242ac120007/61163XMUNAISHI.pdf"},"$$hashKey":"object:551"}]}],"workPrices":[{"relatedSites":[{"name":"61163","id":"34518","site_name":"61163XMUNAISHI","params":{"site_name":"61163XMUNAISHI"},"$$hashKey":"object:216"}],"displayServiceName":"8.15 Замена электрического счётчика ;шт.","sapServiceNumber":"8.15","materialUnit":"шт..","quantity":1,"materialQuantity":1,"reason":"change_support","expenseType":"OPEX","reasonDetail":"Замена - восстановление вышедшего из строя оборудования/OC для поддержания их в рабочем состоянии  с целью их профилактики","$$hashKey":"object:180","unitWorkPricePerSite":"46452.00","netWorkPricePerSite":"46452.00","unitWorkPrice":"46452.00","unitWorkPricePlusTx":"46452.00","basePriceByQuantity":"46452.00","total":"46452.00","basePrice":"46452.00","materialsProvidedBy":"kcell"}],"dueDate":"2021-04-07T06:00:00.000Z","delay":12},"0c475fed-1a72-11eb-80ef-0242ac120008":{"selected":true,"businessKey":"N&C-Line_Eng-TNU-20-7007","processInstanceId":"0c475fed-1a72-11eb-80ef-0242ac120008","$$hashKey":"object:228","priority":"regular","siteRegion":"nc","site_name":"13065ZHAYLAYU","performDate":"2020-11-09T15:55:21.539+0600","contractorJobAssignedDate":"2020-10-30T15:12:47.752+0600","acceptanceDate":"2020-11-10T00:00:00.000+0600","reason":"2","acceptWorkStartTime":"2020-11-09T15:55:21.539+0600","acceptWorkEndTime":"2020-11-10T00:00:00.000+0600","works":[{"relatedSites":[{"name":"13065","id":"30630","site_name":"13065ZHAYLAYU","params":{"site_name":"13065ZHAYLAU,13065ZHAYLAYU"},"$$hashKey":"object:1121"},{"name":"13032","id":"35611","site_name":"13032HOTDOSTYK ","params":{"site_name":"13032HOTDOSTYK "},"$$hashKey":"object:1122"},{"name":"13041","id":"30610","site_name":"13041KOKMARKET","params":{"site_name":"13041KOKMARKET"},"$$hashKey":"object:1123"}],"displayServiceName":"9.6 Инсталляция и тестирование Радиорелейного пролета (1+0), включая indoor, outdoor, активацию, настройку (0,3-1,2 м) ;пролет","sapServiceNumber":"9.6","materialUnit":"пролет","quantity":2,"materialQuantity":2,"reason":"installation","expenseType":"CAPEX","reasonDetail":"Монтаж - установка нового оборудования","$$hashKey":"object:1088","files":[{"name":"work_0_file_0","type":"Json","value":{"name":"N&C-Line_Eng-TNU-20-7007_13065ZHAYLAYU.rar","path":"0c475fed-1a72-11eb-80ef-0242ac120008/13f729d0-1a90-11eb-80ef-0242ac120008/N&C-Line_Eng-TNU-20-7007_13065ZHAYLAYU.rar"},"$$hashKey":"object:142"}]}],"workPrices":[{"relatedSites":[{"name":"13065","id":"30630","site_name":"13065ZHAYLAYU","params":{"site_name":"13065ZHAYLAU,13065ZHAYLAYU"},"$$hashKey":"object:1121"},{"name":"13032","id":"35611","site_name":"13032HOTDOSTYK ","params":{"site_name":"13032HOTDOSTYK "},"$$hashKey":"object:1122"},{"name":"13041","id":"30610","site_name":"13041KOKMARKET","params":{"site_name":"13041KOKMARKET"},"$$hashKey":"object:1123"}],"displayServiceName":"9.6 Инсталляция и тестирование Радиорелейного пролета (1+0), включая indoor, outdoor, активацию, настройку (0,3-1,2 м) ;пролет","sapServiceNumber":"9.6","materialUnit":"пролет","quantity":2,"materialQuantity":2,"reason":"installation","expenseType":"CAPEX","reasonDetail":"Монтаж - установка нового оборудования","files":[{"name":"work_0_file_0","type":"Json","value":{"name":"N&C-Line_Eng-TNU-20-7007_13065ZHAYLAYU.rar","path":"0c475fed-1a72-11eb-80ef-0242ac120008/13f729d0-1a90-11eb-80ef-0242ac120008/N&C-Line_Eng-TNU-20-7007_13065ZHAYLAYU.rar"},"$$hashKey":"object:142"}],"unitWorkPricePerSite":"172800.00","netWorkPricePerSite":"345600.00","unitWorkPrice":"518400.00","unitWorkPricePlusTx":"518400.00","basePriceByQuantity":"1036800.00","total":"1036800.00","basePrice":"518400.00"}],"delay":0,"dueDate":"2021-04-06T18:00:00.000Z"},"35432d90-0dec-11eb-900c-0242ac120008":{"selected":true,"businessKey":"W-Line_Eng-TNU-20-7039","processInstanceId":"35432d90-0dec-11eb-900c-0242ac120008","$$hashKey":"object:242","priority":"regular","siteRegion":"west","site_name":"61108MEREI","performDate":"2020-11-03T16:13:56.928+0600","contractorJobAssignedDate":"2020-10-27T16:07:49.164+0600","acceptanceDate":"2020-11-09T00:00:00.000+0600","reason":"2","acceptWorkStartTime":"2020-11-03T16:13:56.928+0600","acceptWorkEndTime":"2020-11-09T00:00:00.000+0600","works":[{"relatedSites":[{"name":"61108","id":"34480","site_name":"61108MEREI","params":{"site_name":"61108MEREI"},"$$hashKey":"object:371"},{"name":"61024","id":"34421","site_name":"61024UATYRTCDINA","params":{"site_name":"61024UATYRTCDINA,61024XATYRTCDIN"},"$$hashKey":"object:372"}],"displayServiceName":"9.10 Демонтаж Радиорелейного пролета (1+0 /1+1/2+0), включая indoor, outdoor (1,3-1,8м) ;пролет","sapServiceNumber":"9.9","materialUnit":"пролет","reason":"removal","expenseType":"OPEX","reasonDetail":"Демонтаж - снятие оборудования","quantity":1,"materialQuantity":1,"files":[{"name":"work_0_file_0","type":"Json","value":{"name":"W-Line_Eng-TNU-20-7039 61108MEREI.xlsx","path":"35432d90-0dec-11eb-900c-0242ac120008/2c4f65fa-1dbc-11eb-80ef-0242ac120008/W-Line_Eng-TNU-20-7039 61108MEREI.xlsx"},"$$hashKey":"object:540"},{"name":"work_0_file_1","type":"Json","value":{"name":"W-Line_Eng-TNU-20-7039 61108MEREI ACT.xlsx","path":"35432d90-0dec-11eb-900c-0242ac120008/2c4f65fa-1dbc-11eb-80ef-0242ac120008/W-Line_Eng-TNU-20-7039 61108MEREI ACT.xlsx"},"$$hashKey":"object:541"},{"name":"work_0_file_2","type":"Json","value":{"name":"W-Line_Eng-TNU-20-7039 61108MEREI.docx","path":"35432d90-0dec-11eb-900c-0242ac120008/2c4f65fa-1dbc-11eb-80ef-0242ac120008/W-Line_Eng-TNU-20-7039 61108MEREI.docx"},"$$hashKey":"object:542"}],"unitWorkPricePerSite":"129600.00","netWorkPricePerSite":"129600.00","unitWorkPrice":"259200.00","unitWorkPricePlusTx":"259200.00","basePriceByQuantity":"259200.00","total":"259200.00","basePrice":"259200.00","$$hashKey":"object:293"}],"workPrices":[{"relatedSites":[{"name":"61108","id":"34480","site_name":"61108MEREI","params":{"site_name":"61108MEREI"},"$$hashKey":"object:371"},{"name":"61024","id":"34421","site_name":"61024UATYRTCDINA","params":{"site_name":"61024UATYRTCDINA,61024XATYRTCDIN"},"$$hashKey":"object:372"}],"displayServiceName":"9.10 Демонтаж Радиорелейного пролета (1+0 /1+1/2+0), включая indoor, outdoor (1,3-1,8м) ;пролет","sapServiceNumber":"9.9","materialUnit":"пролет","reason":"removal","expenseType":"OPEX","reasonDetail":"Демонтаж - снятие оборудования","quantity":1,"materialQuantity":1,"files":[{"name":"work_0_file_0","type":"Json","value":{"name":"W-Line_Eng-TNU-20-7039 61108MEREI.xlsx","path":"35432d90-0dec-11eb-900c-0242ac120008/2c4f65fa-1dbc-11eb-80ef-0242ac120008/W-Line_Eng-TNU-20-7039 61108MEREI.xlsx"},"$$hashKey":"object:540"},{"name":"work_0_file_1","type":"Json","value":{"name":"W-Line_Eng-TNU-20-7039 61108MEREI ACT.xlsx","path":"35432d90-0dec-11eb-900c-0242ac120008/2c4f65fa-1dbc-11eb-80ef-0242ac120008/W-Line_Eng-TNU-20-7039 61108MEREI ACT.xlsx"},"$$hashKey":"object:541"},{"name":"work_0_file_2","type":"Json","value":{"name":"W-Line_Eng-TNU-20-7039 61108MEREI.docx","path":"35432d90-0dec-11eb-900c-0242ac120008/2c4f65fa-1dbc-11eb-80ef-0242ac120008/W-Line_Eng-TNU-20-7039 61108MEREI.docx"},"$$hashKey":"object:542"}],"unitWorkPricePerSite":"129600.00","netWorkPricePerSite":"129600.00","unitWorkPrice":"259200.00","unitWorkPricePlusTx":"259200.00","basePriceByQuantity":"259200.00","total":"259200.00","basePrice":"259200.00"}],"delay":0,"dueDate":"2021-04-07T18:00:00.000Z"},"ad680c26-0ec7-11eb-b1d9-0242ac120008":{"selected":true,"businessKey":"W-Line_Eng-TNU-20-7038","processInstanceId":"ad680c26-0ec7-11eb-b1d9-0242ac120008","$$hashKey":"object:241","priority":"regular","siteRegion":"west","site_name":"61180ALGABAS","performDate":"2020-11-10T17:08:40.325+0600","contractorJobAssignedDate":"2020-10-23T23:24:24.549+0600","acceptanceDate":"2020-11-16T00:00:00.000+0600","reason":"2","acceptWorkStartTime":"2020-11-10T17:08:40.325+0600","acceptWorkEndTime":"2020-11-16T00:00:00.000+0600","works":[{"relatedSites":[{"name":"61180","id":"34532","site_name":"61180ALGABAS","params":{"site_name":"61180ALGABAS"},"$$hashKey":"object:570"},{"name":"61098","id":"34470","site_name":"61098XKURSAY","params":{"site_name":"61098XKURSAY"},"$$hashKey":"object:571"}],"displayServiceName":"9.6 Инсталляция и тестирование Радиорелейного пролета (1+0), включая indoor, outdoor, активацию, настройку (0,3-1,2 м) ;пролет","sapServiceNumber":"9.6","materialUnit":"пролет","quantity":1,"materialQuantity":1,"reason":"installation","expenseType":"CAPEX","reasonDetail":"Монтаж - установка нового оборудования","files":[{"name":"work_0_file_0","type":"Json","value":{"name":"W-Line_Eng-TNU-20-7038 61180ALGABAS.xlsx","path":"ad680c26-0ec7-11eb-b1d9-0242ac120008/ccf79d08-2344-11eb-a45d-0242ac120007/W-Line_Eng-TNU-20-7038 61180ALGABAS.xlsx"},"$$hashKey":"object:309"},{"name":"work_0_file_1","type":"Json","value":{"name":"W-Line_Eng-TNU-20-7038 61180ALGABAS.docx","path":"ad680c26-0ec7-11eb-b1d9-0242ac120008/ccf79d08-2344-11eb-a45d-0242ac120007/W-Line_Eng-TNU-20-7038 61180ALGABAS.docx"},"$$hashKey":"object:310"},{"name":"work_0_file_2","type":"Json","value":{"name":"W-Line_Eng-TNU-20-7038 61180ALGABAS ACT TRANSFER.xlsx","path":"ad680c26-0ec7-11eb-b1d9-0242ac120008/ccf79d08-2344-11eb-a45d-0242ac120007/W-Line_Eng-TNU-20-7038 61180ALGABAS ACT TRANSFER.xlsx"},"$$hashKey":"object:311"}],"materialsProvidedBy":"subcontractor","unitWorkPricePerSite":"259200.00","netWorkPricePerSite":"259200.00","unitWorkPrice":"518400.00","unitWorkPricePlusTx":"518400.00","basePriceByQuantity":"518400.00","total":"518400.00","basePrice":"518400.00","$$hashKey":"object:305"}],"workPrices":[{"relatedSites":[{"name":"61180","id":"34532","site_name":"61180ALGABAS","params":{"site_name":"61180ALGABAS"},"$$hashKey":"object:570"},{"name":"61098","id":"34470","site_name":"61098XKURSAY","params":{"site_name":"61098XKURSAY"},"$$hashKey":"object:571"}],"displayServiceName":"9.6 Инсталляция и тестирование Радиорелейного пролета (1+0), включая indoor, outdoor, активацию, настройку (0,3-1,2 м) ;пролет","sapServiceNumber":"9.6","materialUnit":"пролет","quantity":1,"materialQuantity":1,"reason":"installation","expenseType":"CAPEX","reasonDetail":"Монтаж - установка нового оборудования","files":[{"name":"work_0_file_0","type":"Json","value":{"name":"W-Line_Eng-TNU-20-7038 61180ALGABAS.xlsx","path":"ad680c26-0ec7-11eb-b1d9-0242ac120008/ccf79d08-2344-11eb-a45d-0242ac120007/W-Line_Eng-TNU-20-7038 61180ALGABAS.xlsx"},"$$hashKey":"object:309"},{"name":"work_0_file_1","type":"Json","value":{"name":"W-Line_Eng-TNU-20-7038 61180ALGABAS.docx","path":"ad680c26-0ec7-11eb-b1d9-0242ac120008/ccf79d08-2344-11eb-a45d-0242ac120007/W-Line_Eng-TNU-20-7038 61180ALGABAS.docx"},"$$hashKey":"object:310"},{"name":"work_0_file_2","type":"Json","value":{"name":"W-Line_Eng-TNU-20-7038 61180ALGABAS ACT TRANSFER.xlsx","path":"ad680c26-0ec7-11eb-b1d9-0242ac120008/ccf79d08-2344-11eb-a45d-0242ac120007/W-Line_Eng-TNU-20-7038 61180ALGABAS ACT TRANSFER.xlsx"},"$$hashKey":"object:311"}],"materialsProvidedBy":true,"unitWorkPricePerSite":"259200.00","netWorkPricePerSite":"259200.00","unitWorkPrice":"518400.00","unitWorkPricePlusTx":"518400.00","basePriceByQuantity":"518400.00","total":"518400.00","basePrice":"518400.00"}],"delay":0,"dueDate":"2021-04-06T18:00:00.000Z"},"a8ac8698-09ed-11eb-900c-0242ac120008":{"selected":true,"businessKey":"Astana-Line_Eng-SAO-20-7005","processInstanceId":"a8ac8698-09ed-11eb-900c-0242ac120008","$$hashKey":"object:226","priority":"regular","siteRegion":"astana","site_name":"11520DALINA","performDate":"2020-10-14T17:35:15.744+0600","contractorJobAssignedDate":"2020-10-13T15:23:52.379+0600","acceptanceDate":"2020-10-14T00:00:00.000+0600","reason":"4","acceptWorkStartTime":"2020-10-14T17:35:15.744+0600","acceptWorkEndTime":"2020-10-14T00:00:00.000+0600","works":[{"relatedSites":[{"name":"11520","id":"30036","site_name":"11520DALINA","params":{"site_name":"11520DALINA"},"$$hashKey":"object:2284"}],"displayServiceName":"2.47 Замена аккумуляторных батарей ;Комплект- 4шт","sapServiceNumber":"2.47","materialUnit":"Комплект-4шт","quantity":2,"materialQuantity":2,"reason":"change_improvement","expenseType":"CAPEX","reasonDetail":"Замена - восстановление вышедшего из строя оборудования/ОС для улучшения состояния объекта, повышающего его первоначально оцененные нормативные показатели: срок службы, производственную мощность и т. д","$$hashKey":"object:2248","files":[{"name":"work_0_file_0","type":"Json","value":{"name":"JR Astana-Line_Eng-SAO-20-7005_11520DALINA.zip","path":"a8ac8698-09ed-11eb-900c-0242ac120008/3966009b-0d46-11eb-900c-0242ac120008/JR Astana-Line_Eng-SAO-20-7005_11520DALINA.zip"},"$$hashKey":"object:2671"}]}],"workPrices":[{"relatedSites":[{"name":"11520","id":"30036","site_name":"11520DALINA","params":{"site_name":"11520DALINA"},"$$hashKey":"object:2284"}],"displayServiceName":"2.47 Замена аккумуляторных батарей ;Комплект- 4шт","sapServiceNumber":"2.47","materialUnit":"Комплект-4шт","quantity":2,"materialQuantity":2,"reason":"change_improvement","expenseType":"CAPEX","reasonDetail":"Замена - восстановление вышедшего из строя оборудования/ОС для улучшения состояния объекта, повышающего его первоначально оцененные нормативные показатели: срок службы, производственную мощность и т. д","$$hashKey":"object:2248","unitWorkPricePerSite":"44400.00","netWorkPricePerSite":"88800.00","unitWorkPrice":"44400.00","unitWorkPricePlusTx":"44400.00","basePriceByQuantity":"88800.00","total":"88800.00","basePrice":"44400.00","materialsProvidedBy":"kcell"}],"delay":1,"dueDate":"2021-04-11T18:00:00.000Z"},"651a42d0-0e04-11eb-900c-0242ac120008":{"selected":true,"businessKey":"W-Line_Eng-TNU-20-7036","processInstanceId":"651a42d0-0e04-11eb-900c-0242ac120008","$$hashKey":"object:240","priority":"regular","siteRegion":"west","site_name":"61037NALUPRAVL","performDate":"2020-11-03T16:15:31.388+0600","contractorJobAssignedDate":"2020-10-23T12:41:47.553+0600","acceptanceDate":"2020-11-09T00:00:00.000+0600","reason":"2","acceptWorkStartTime":"2020-11-03T16:15:31.388+0600","acceptWorkEndTime":"2020-11-09T00:00:00.000+0600","works":[{"relatedSites":[{"name":"61037","id":"34433","site_name":"61037NALUPRAVL","params":{"status":{"61037NALUPRAVL":"replaced"},"site_name":"61037NALUPRAVL"},"$$hashKey":"object:493"},{"name":"61015","id":"34413","site_name":"61015ATDOMBYTA","params":{"site_name":"61015ATDOMBYTA"},"$$hashKey":"object:494"}],"displayServiceName":"9.9 Демонтаж Радиорелейного пролета (1+0/ 1+1/2+0), включая indoor, outdoor (0,3-1,2м) ;пролет","sapServiceNumber":"9.9","materialUnit":"пролет","quantity":1,"materialQuantity":1,"reason":"removal","expenseType":"OPEX","reasonDetail":"Демонтаж - снятие оборудования","files":[{"name":"work_0_file_0","type":"Json","value":{"name":"W-Line_Eng-TNU-20-7036 61037NALUPRAVL.xlsx","path":"651a42d0-0e04-11eb-900c-0242ac120008/331f528d-1dbc-11eb-80ef-0242ac120008/W-Line_Eng-TNU-20-7036 61037NALUPRAVL.xlsx"},"$$hashKey":"object:361"},{"name":"work_0_file_1","type":"Json","value":{"name":"W-Line_Eng-TNU-20-7036 61037NALUPRAVL ACT.xlsx","path":"651a42d0-0e04-11eb-900c-0242ac120008/331f528d-1dbc-11eb-80ef-0242ac120008/W-Line_Eng-TNU-20-7036 61037NALUPRAVL ACT.xlsx"},"$$hashKey":"object:362"},{"name":"work_0_file_2","type":"Json","value":{"name":"W-Line_Eng-TNU-20-7036 61037NALUPRAVL.docx","path":"651a42d0-0e04-11eb-900c-0242ac120008/331f528d-1dbc-11eb-80ef-0242ac120008/W-Line_Eng-TNU-20-7036 61037NALUPRAVL.docx"},"$$hashKey":"object:363"}],"materialsProvidedBy":"subcontractor","unitWorkPricePerSite":"129600.00","netWorkPricePerSite":"129600.00","unitWorkPrice":"259200.00","unitWorkPricePlusTx":"259200.00","basePriceByQuantity":"259200.00","total":"259200.00","basePrice":"259200.00","$$hashKey":"object:328"}],"workPrices":[{"relatedSites":[{"name":"61037","id":"34433","site_name":"61037NALUPRAVL","params":{"status":{"61037NALUPRAVL":"replaced"},"site_name":"61037NALUPRAVL"},"$$hashKey":"object:493"},{"name":"61015","id":"34413","site_name":"61015ATDOMBYTA","params":{"site_name":"61015ATDOMBYTA"},"$$hashKey":"object:494"}],"displayServiceName":"9.9 Демонтаж Радиорелейного пролета (1+0/ 1+1/2+0), включая indoor, outdoor (0,3-1,2м) ;пролет","sapServiceNumber":"9.9","materialUnit":"пролет","quantity":1,"materialQuantity":1,"reason":"removal","expenseType":"OPEX","reasonDetail":"Демонтаж - снятие оборудования","files":[{"name":"work_0_file_0","type":"Json","value":{"name":"W-Line_Eng-TNU-20-7036 61037NALUPRAVL.xlsx","path":"651a42d0-0e04-11eb-900c-0242ac120008/331f528d-1dbc-11eb-80ef-0242ac120008/W-Line_Eng-TNU-20-7036 61037NALUPRAVL.xlsx"},"$$hashKey":"object:361"},{"name":"work_0_file_1","type":"Json","value":{"name":"W-Line_Eng-TNU-20-7036 61037NALUPRAVL ACT.xlsx","path":"651a42d0-0e04-11eb-900c-0242ac120008/331f528d-1dbc-11eb-80ef-0242ac120008/W-Line_Eng-TNU-20-7036 61037NALUPRAVL ACT.xlsx"},"$$hashKey":"object:362"},{"name":"work_0_file_2","type":"Json","value":{"name":"W-Line_Eng-TNU-20-7036 61037NALUPRAVL.docx","path":"651a42d0-0e04-11eb-900c-0242ac120008/331f528d-1dbc-11eb-80ef-0242ac120008/W-Line_Eng-TNU-20-7036 61037NALUPRAVL.docx"},"$$hashKey":"object:363"}],"materialsProvidedBy":true,"unitWorkPricePerSite":"129600.00","netWorkPricePerSite":"129600.00","unitWorkPrice":"259200.00","unitWorkPricePlusTx":"259200.00","basePriceByQuantity":"259200.00","total":"259200.00","basePrice":"259200.00"}],"delay":1,"dueDate":"2021-04-12T18:00:00.000Z"},"b2e1a12c-1a71-11eb-80ef-0242ac120008":{"selected":true,"businessKey":"N&C-Line_Eng-TNU-20-7006","processInstanceId":"b2e1a12c-1a71-11eb-80ef-0242ac120008","$$hashKey":"object:227","priority":"emergency","siteRegion":"nc","site_name":"17229LENINSKOE","performDate":"2020-11-09T15:08:22.329+0600","contractorJobAssignedDate":"2020-11-02T15:35:18.757+0600","acceptanceDate":"2020-11-11T00:00:00.000+0600","reason":"2","acceptWorkStartTime":"2020-11-09T15:08:22.329+0600","acceptWorkEndTime":"2020-11-11T00:00:00.000+0600","works":[{"relatedSites":[{"name":"17229","id":"31136","site_name":"17229LENINSKOE","params":{"site_name":"17229LENINSKOE"},"$$hashKey":"object:2759"}],"displayServiceName":"9.59 Монтаж кабеля/патчкорда UTP, KVSM, FO ;метр","sapServiceNumber":"9.76","materialUnit":"метр","quantity":35,"materialQuantity":35,"reason":"installation","expenseType":"CAPEX","reasonDetail":"Монтаж - установка нового оборудования","$$hashKey":"object:2688","files":[{"name":"work_0_file_0","type":"Json","value":{"name":"N&C-Line_Eng-TNU-20-7006_17229LENINSKOE.rar","path":"b2e1a12c-1a71-11eb-80ef-0242ac120008/1191d65c-226a-11eb-a45d-0242ac120007/N&C-Line_Eng-TNU-20-7006_17229LENINSKOE.rar"},"$$hashKey":"object:370"}]}],"workPrices":[{"relatedSites":[{"name":"17229","id":"31136","site_name":"17229LENINSKOE","params":{"site_name":"17229LENINSKOE"},"$$hashKey":"object:2759"}],"displayServiceName":"9.59 Монтаж кабеля/патчкорда UTP, KVSM, FO ;метр","sapServiceNumber":"9.76","materialUnit":"метр","quantity":35,"materialQuantity":35,"reason":"installation","expenseType":"CAPEX","reasonDetail":"Монтаж - установка нового оборудования","$$hashKey":"object:2688","unitWorkPricePerSite":"2263.54","netWorkPricePerSite":"79223.90","unitWorkPrice":"2263.54","unitWorkPricePlusTx":"2263.54","basePriceByQuantity":"79223.90","total":"79223.90","basePrice":"2263.54"}],"dueDate":"2021-04-16T06:00:00.000Z","delay":24},"f9ebee72-09ed-11eb-900c-0242ac120008":{"selected":true,"businessKey":"Astana-Line_Eng-SAO-20-7003","processInstanceId":"f9ebee72-09ed-11eb-900c-0242ac120008","$$hashKey":"object:222","priority":"regular","siteRegion":"astana","site_name":"11815ARKAAUTO","performDate":"2020-10-14T17:09:20.672+0600","contractorJobAssignedDate":"2020-10-13T15:23:33.685+0600","acceptanceDate":"2020-10-14T00:00:00.000+0600","reason":"4","acceptWorkStartTime":"2020-10-14T17:09:20.672+0600","acceptWorkEndTime":"2020-10-14T00:00:00.000+0600","works":[{"relatedSites":[{"name":"11815","id":"30236","site_name":"11815ARKAAUTO","params":{"site_name":"11815ARKAAUTO"},"$$hashKey":"object:3142"}],"displayServiceName":"2.47 Замена аккумуляторных батарей ;Комплект- 4шт","sapServiceNumber":"2.47","materialUnit":"Комплект-4шт","quantity":2,"materialQuantity":2,"reason":"change_improvement","expenseType":"CAPEX","reasonDetail":"Замена - восстановление вышедшего из строя оборудования/ОС для улучшения состояния объекта, повышающего его первоначально оцененные нормативные показатели: срок службы, производственную мощность и т. д","$$hashKey":"object:3106","files":[{"name":"work_0_file_0","type":"Json","value":{"name":"JR Astana-Line_Eng-SAO-20-7003_11815ARKAAUTO.zip","path":"f9ebee72-09ed-11eb-900c-0242ac120008/23fa9ad9-0d46-11eb-900c-0242ac120008/JR Astana-Line_Eng-SAO-20-7003_11815ARKAAUTO.zip"},"$$hashKey":"object:1767"}]}],"workPrices":[{"relatedSites":[{"name":"11815","id":"30236","site_name":"11815ARKAAUTO","params":{"site_name":"11815ARKAAUTO"},"$$hashKey":"object:3142"}],"displayServiceName":"2.47 Замена аккумуляторных батарей ;Комплект- 4шт","sapServiceNumber":"2.47","materialUnit":"Комплект-4шт","quantity":2,"materialQuantity":2,"reason":"change_improvement","expenseType":"CAPEX","reasonDetail":"Замена - восстановление вышедшего из строя оборудования/ОС для улучшения состояния объекта, повышающего его первоначально оцененные нормативные показатели: срок службы, производственную мощность и т. д","$$hashKey":"object:3106","unitWorkPricePerSite":"44400.00","netWorkPricePerSite":"88800.00","unitWorkPrice":"44400.00","unitWorkPricePlusTx":"44400.00","basePriceByQuantity":"88800.00","total":"88800.00","basePrice":"44400.00","materialsProvidedBy":"kcell"}],"delay":0,"dueDate":"2021-04-15T18:00:00.000Z"},"d7a4ed13-0ec8-11eb-b1d9-0242ac120008":{"selected":true,"businessKey":"W-Line_Eng-TNU-20-7035","processInstanceId":"d7a4ed13-0ec8-11eb-b1d9-0242ac120008","$$hashKey":"object:239","priority":"regular","siteRegion":"west","site_name":"61190RECHNIK","performDate":"2020-11-10T17:09:36.712+0600","contractorJobAssignedDate":"2020-10-23T23:26:47.929+0600","acceptanceDate":"2020-11-16T00:00:00.000+0600","reason":"2","acceptWorkStartTime":"2020-11-10T17:09:36.712+0600","acceptWorkEndTime":"2020-11-16T00:00:00.000+0600","works":[{"relatedSites":[{"name":"61190","id":"34538","site_name":"61190RECHNIK","params":{"site_name":"61190RECHNIK"},"$$hashKey":"object:329"},{"name":"61090","id":"34464","site_name":"61090ERKINKALA","params":{"site_name":"61090ERKINKALA"},"$$hashKey":"object:330"}],"displayServiceName":"9.6 Инсталляция и тестирование Радиорелейного пролета (1+0), включая indoor, outdoor, активацию, настройку (0,3-1,2 м) ;пролет","sapServiceNumber":"9.6","materialUnit":"пролет","reason":"installation","expenseType":"CAPEX","reasonDetail":"Монтаж - установка нового оборудования","quantity":1,"materialQuantity":1,"files":[{"name":"work_0_file_0","type":"Json","value":{"name":"W-Line_Eng-TNU-20-7035 61190RECHNIK.xlsx","path":"d7a4ed13-0ec8-11eb-b1d9-0242ac120008/e8ea7618-2344-11eb-a45d-0242ac120007/W-Line_Eng-TNU-20-7035 61190RECHNIK.xlsx"},"$$hashKey":"object:335"},{"name":"work_0_file_1","type":"Json","value":{"name":"W-Line_Eng-TNU-20-7035 61190RECHNIK.docx","path":"d7a4ed13-0ec8-11eb-b1d9-0242ac120008/e8ea7618-2344-11eb-a45d-0242ac120007/W-Line_Eng-TNU-20-7035 61190RECHNIK.docx"},"$$hashKey":"object:336"},{"name":"work_0_file_2","type":"Json","value":{"name":"W-Line_Eng-TNU-20-7035 61190RECHNIK ACT TRANSFER.xlsx","path":"d7a4ed13-0ec8-11eb-b1d9-0242ac120008/e8ea7618-2344-11eb-a45d-0242ac120007/W-Line_Eng-TNU-20-7035 61190RECHNIK ACT TRANSFER.xlsx"},"$$hashKey":"object:337"}],"materialsProvidedBy":"subcontractor","unitWorkPricePerSite":"259200.00","netWorkPricePerSite":"259200.00","unitWorkPrice":"518400.00","unitWorkPricePlusTx":"518400.00","basePriceByQuantity":"518400.00","total":"518400.00","basePrice":"518400.00","$$hashKey":"object:480"}],"workPrices":[{"relatedSites":[{"name":"61190","id":"34538","site_name":"61190RECHNIK","params":{"site_name":"61190RECHNIK"},"$$hashKey":"object:329"},{"name":"61090","id":"34464","site_name":"61090ERKINKALA","params":{"site_name":"61090ERKINKALA"},"$$hashKey":"object:330"}],"displayServiceName":"9.6 Инсталляция и тестирование Радиорелейного пролета (1+0), включая indoor, outdoor, активацию, настройку (0,3-1,2 м) ;пролет","sapServiceNumber":"9.6","materialUnit":"пролет","reason":"installation","expenseType":"CAPEX","reasonDetail":"Монтаж - установка нового оборудования","quantity":1,"materialQuantity":1,"files":[{"name":"work_0_file_0","type":"Json","value":{"name":"W-Line_Eng-TNU-20-7035 61190RECHNIK.xlsx","path":"d7a4ed13-0ec8-11eb-b1d9-0242ac120008/e8ea7618-2344-11eb-a45d-0242ac120007/W-Line_Eng-TNU-20-7035 61190RECHNIK.xlsx"},"$$hashKey":"object:335"},{"name":"work_0_file_1","type":"Json","value":{"name":"W-Line_Eng-TNU-20-7035 61190RECHNIK.docx","path":"d7a4ed13-0ec8-11eb-b1d9-0242ac120008/e8ea7618-2344-11eb-a45d-0242ac120007/W-Line_Eng-TNU-20-7035 61190RECHNIK.docx"},"$$hashKey":"object:336"},{"name":"work_0_file_2","type":"Json","value":{"name":"W-Line_Eng-TNU-20-7035 61190RECHNIK ACT TRANSFER.xlsx","path":"d7a4ed13-0ec8-11eb-b1d9-0242ac120008/e8ea7618-2344-11eb-a45d-0242ac120007/W-Line_Eng-TNU-20-7035 61190RECHNIK ACT TRANSFER.xlsx"},"$$hashKey":"object:337"}],"materialsProvidedBy":true,"unitWorkPricePerSite":"259200.00","netWorkPricePerSite":"259200.00","unitWorkPrice":"518400.00","unitWorkPricePlusTx":"518400.00","basePriceByQuantity":"518400.00","total":"518400.00","basePrice":"518400.00"}],"delay":2,"dueDate":"2021-04-14T18:00:00.000Z"}}';
def selectedRevisionsObject = new JSONObject(selectedRevisions);
def yearOfFormalPeriod = '2021'
def monthOfFormalPeriod = 'May'
def subcontractor = 'LINE'
def businessKey = 'LINE-2021-05'
 */

def regularWorks = new JSONArray();
def emergencyWorks = new JSONArray();

Iterator<String> keys = selectedRevisionsObject.keys();
while(keys.hasNext()) {
    String key = keys.next();
    JSONObject object = selectedRevisionsObject.getJSONObject(key);
    if(object.getString('priority') == 'emergency'){
        emergencyWorks.put(object);
    } else {
        regularWorks.put(object);
    }
}

def regularfd = new SimpleDateFormat("dd.MM.yyyy")
def emergencyfd = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss")
def format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXX")

for(int i= 0;i<regularWorks.length();i++){
    regularWorks.getJSONObject(i).put('contractorJobAssignedDateString', regularfd.format(format.parse(regularWorks.getJSONObject(i).getString('contractorJobAssignedDate'))));
    regularWorks.getJSONObject(i).put('performDateString', regularfd.format(format.parse(regularWorks.getJSONObject(i).getString('performDate'))));
    regularWorks.getJSONObject(i).put('dueDateString', regularfd.format(format.parse(regularWorks.getJSONObject(i).getString('dueDate'))));
}

for(int i= 0;i<emergencyWorks.length();i++){
    emergencyWorks.getJSONObject(i).put('contractorJobAssignedDateString', emergencyfd.format(format.parse(emergencyWorks.getJSONObject(i).getString('contractorJobAssignedDate'))));
    emergencyWorks.getJSONObject(i).put('performDateString', emergencyfd.format(format.parse(emergencyWorks.getJSONObject(i).getString('performDate'))));
    emergencyWorks.getJSONObject(i).put('dueDateString', emergencyfd.format(format.parse(emergencyWorks.getJSONObject(i).getString('dueDate'))));
}

def binding = [
        'monthsTitleRu':monthsTitleRu,
        'regionsMap':regionsMap,
        'mapSubcontractorsToNum':mapSubcontractorsToNum,
        'regularWorks':regularWorks,
        'emergencyWorks':emergencyWorks,
        'businessKey':businessKey,
        'monthOfFormalPeriod':monthOfFormalPeriod,
        'yearOfFormalPeriod':yearOfFormalPeriod,
        'subcontractor':subcontractor,
        'contractorsTitle':contractorsTitle,
        'reasonsTitle':reasonsTitle
]

def template = '''\

            yieldUnescaped '<!DOCTYPE html>'
            html(lang:'en') {
                head {
                    meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
                    title('My page')
                }
                newLine()
                style ('.table-bordered { border: 1px solid #ddd; } .table { width: 100%;max-width: 100%;margin-bottom: 20px; } table { background-color: transparent;} table { border-collapse: collapse; border-spacing: 0; } thead { display: table-header-group; vertical-align: middle; border-color: inherit; } tr { display: table-row; vertical-align: inherit; border-color: inherit; } .table>caption+thead>tr:first-child>td, .table>caption+thead>tr:first-child>th, .table>colgroup+thead>tr:first-child>td, .table>colgroup+thead>tr:first-child>th, .table>thead:first-child>tr:first-child>td, .table>thead:first-child>tr:first-child>th { border-top: 0; } .table-bordered>thead>tr>td, .table-bordered>thead>tr>th { border-bottom-width: 1px; } .table-bordered>thead>tr>td, .table-bordered>thead>tr>th { border-bottom-width: 2px; } .table-bordered>tbody>tr>td, .table-bordered>tbody>tr>th, .table-bordered>tfoot>tr>td, .table-bordered>tfoot>tr>th, .table-bordered>thead>tr>td, .table-bordered>thead>tr>th { border: 1px solid #ddd; } .table>thead>tr>th { vertical-align: bottom; border-bottom: 2px solid #ddd; } .table>tbody>tr>td, .table>tbody>tr>th, .table>tfoot>tr>td, .table>tfoot>tr>th, .table>thead>tr>td, .table>thead>tr>th { padding: 8px; line-height: 1.42857143; vertical-align: top; border-top: 1px solid #ddd; } th { text-align: left; } td, th { padding: 0; } tbody { display: table-row-group; vertical-align: middle; border-color: inherit; } tr { display: table-row; vertical-align: inherit; border-color: inherit; } .table-bordered>tbody>tr>td, .table-bordered>tbody>tr>th, .table-bordered>tfoot>tr>td, .table-bordered>tfoot>tr>th, .table-bordered>thead>tr>td, .table-bordered>thead>tr>th { border: 1px solid #ddd; }                  .table>tbody>tr>td, .table>tbody>tr>th, .table>tfoot>tr>td, .table>tfoot>tr>th, .table>thead>tr>td, .table>thead>tr>th { padding: 8px; line-height: 1.42857143; vertical-align: top; border-top: 1px solid #ddd; } td, th { padding: 0; } * { box-sizing: border-box; } * { -webkit-box-sizing: border-box; -moz-box-sizing: border-box; box-sizing: border-box; } td { display: table-cell; vertical-align: inherit; } body { font-family: "Open Sans","Helvetica Neue",sans-serif; }')
                newLine()
                body {
                    h4('Номер акта: ' + businessKey)
                    h4('Отчетный период: ' + monthsTitleRu[monthOfFormalPeriod] + ' ' + yearOfFormalPeriod)                   
                    
                    h3(style:'font-size:12px;text-align: center', 'Месячный технический акт выполненных работ TOO ' + contractorsTitle[mapSubcontractorsToNum[subcontractor]] + ' согласно')
                    h3(style:'font-size:12px;text-align: center', 'Договора Подряда на Выполнение Работ по Внедрению и Модификации No')
                    h3(style:'font-size:12px;text-align: center;', 'Плановые работы')
                
                    table(class:"table table-bordered") {
                        thead {
   tr {
    th('#')
    th('Заявка на выполнение работ')
    th('Сайт')
    th('Регион')
    th('Тип работы')
    th('Наименование Работ')
    th('Кол-во работ')
    th('Дата Заказа')
    th('Дата Выполнения')
    th('Согласованный срок выполнения работ (Дата)')
    th('Задержка, дней')
   } 
                        }
                        tbody {
    for(int i= 0;i<regularWorks.length();i++){    
    for(int j = 0;j<regularWorks.getJSONObject(i).getJSONArray('works').length();j++){
        tr { 
            if(j==0){
                td (rowspan: regularWorks.getJSONObject(i).getJSONArray('works').length(), ''+(i+1))
                td (rowspan: regularWorks.getJSONObject(i).getJSONArray('works').length(), ''+ regularWorks.getJSONObject(i).businessKey)
                td (rowspan: regularWorks.getJSONObject(i).getJSONArray('works').length(), ''+ regularWorks.getJSONObject(i).site_name)
                td (rowspan: regularWorks.getJSONObject(i).getJSONArray('works').length(), ''+ regionsMap[regularWorks.getJSONObject(i).siteRegion])
                td (rowspan: regularWorks.getJSONObject(i).getJSONArray('works').length(), ''+ reasonsTitle[regularWorks.getJSONObject(i).reason])
                td (''+ regularWorks.getJSONObject(i).getJSONArray('works').getJSONObject(j).displayServiceName)
                td (''+ regularWorks.getJSONObject(i).getJSONArray('works').getJSONObject(j).quantity)
                td (rowspan: regularWorks.getJSONObject(i).getJSONArray('works').length(), ''+ regularWorks.getJSONObject(i).contractorJobAssignedDateString)
                td (rowspan: regularWorks.getJSONObject(i).getJSONArray('works').length(), ''+ regularWorks.getJSONObject(i).performDateString)
                td (rowspan: regularWorks.getJSONObject(i).getJSONArray('works').length(), ''+ regularWorks.getJSONObject(i).dueDateString)
                td (rowspan: regularWorks.getJSONObject(i).getJSONArray('works').length(), ''+ regularWorks.getJSONObject(i).delay)
            } else {
                td (''+ regularWorks.getJSONObject(i).getJSONArray('works').getJSONObject(j).displayServiceName)
                td (''+ regularWorks.getJSONObject(i).getJSONArray('works').getJSONObject(j).quantity)                
            }
        }
    }
    }
                        
                        }
                    }
                    newLine()
                    h3(style:'font-size:12px;text-align: center', 'Внеплановые работы')
                
                    table(class:"table table-bordered") {
                        thead {
   tr {
    th('#')
    th('Заявка на выполнение работ')
    th('Сайт')
    th('Регион')
    th('Тип работы')
    th('Наименование Работ')
    th('Кол-во работ')
    th('Дата Заказа')
    th('Дата Выполнения')
    th('Согласованный срок выполнения работ (Дата)')
    th('Задержка, часы')
   } 
                        }
                        tbody {
    for(int i= 0;i<emergencyWorks.length();i++){    
    for(int j = 0;j<emergencyWorks.getJSONObject(i).getJSONArray('works').length();j++){
        tr { 
            if(j==0){
                td (rowspan: emergencyWorks.getJSONObject(i).getJSONArray('works').length(), ''+(i+1))
                td (rowspan: emergencyWorks.getJSONObject(i).getJSONArray('works').length(), ''+ emergencyWorks.getJSONObject(i).businessKey)
                td (rowspan: emergencyWorks.getJSONObject(i).getJSONArray('works').length(), ''+ emergencyWorks.getJSONObject(i).site_name)
                td (rowspan: emergencyWorks.getJSONObject(i).getJSONArray('works').length(), ''+ regionsMap[emergencyWorks.getJSONObject(i).siteRegion])
                td (rowspan: emergencyWorks.getJSONObject(i).getJSONArray('works').length(), ''+ reasonsTitle[emergencyWorks.getJSONObject(i).reason])
                td (''+ emergencyWorks.getJSONObject(i).getJSONArray('works').getJSONObject(j).displayServiceName)
                td (''+ emergencyWorks.getJSONObject(i).getJSONArray('works').getJSONObject(j).quantity)
                td (rowspan: emergencyWorks.getJSONObject(i).getJSONArray('works').length(), ''+ emergencyWorks.getJSONObject(i).contractorJobAssignedDateString)
                td (rowspan: emergencyWorks.getJSONObject(i).getJSONArray('works').length(), ''+ emergencyWorks.getJSONObject(i).performDateString)
                td (rowspan: emergencyWorks.getJSONObject(i).getJSONArray('works').length(), ''+ emergencyWorks.getJSONObject(i).dueDateString)
                td (rowspan: emergencyWorks.getJSONObject(i).getJSONArray('works').length(), ''+ emergencyWorks.getJSONObject(i).delay)
            } else {
                td (''+ emergencyWorks.getJSONObject(i).getJSONArray('works').getJSONObject(j).displayServiceName)
                td (''+ emergencyWorks.getJSONObject(i).getJSONArray('works').getJSONObject(j).quantity)                
            }
        }
    }
    }
                        }
                    }
                    
                    table(width:"100%", style:"border: 0px") {
                        tbody {
    tr {
    td(colspan:'2'){
        b('_________________________________________________________________________________________________________________________________________________________________________________________________')    
    }
    }
    tr {
    td(width:'60%', ''){
        '&nbsp;&nbsp;&nbsp;&nbsp;Представитель '
        b('' + contractorsTitle[mapSubcontractorsToNum[subcontractor]])
    }
    td{
        span('Подпись')
    }
    }
    tr {
    td(colspan:'2'){
        h4(style:"text-align: center", 'АО «Кселл»')
    }
    }
    tr {
    td(colspan:'2', '&nbsp;&nbsp;&nbsp;&nbsp;Ким Александр')
    }
    tr {
    td(colspan:'2'){
        b('_________________________________________________________________________________________________________________________________________________________________________________________________')    
    }
    }
    tr {
    td(width:'60%', '&nbsp;&nbsp;&nbsp;&nbsp;Сеть радиодоступа и инсталляция новых сайтов')
    td{
        span('Подпись')
    }
    }
    tr {
    td(colspan:'2', '&nbsp;')
    }
    tr {
    td(colspan:'2', '&nbsp;&nbsp;&nbsp;&nbsp;Серебряков Евгений')
    }
    tr {
    td(colspan:'2'){
        b('_________________________________________________________________________________________________________________________________________________________________________________________________')    
    }
    }
    tr {
    td(width:'60%', '&nbsp;&nbsp;&nbsp;&nbsp;Обеспечение по развитию объектов')
    td{
        span('Подпись')
    }
    }
    tr {
    td(colspan:'2', '&nbsp;')
    }
    tr {
    td(colspan:'2', '&nbsp;&nbsp;&nbsp;&nbsp;Парамонова Марина')
    }
    tr {
    td(colspan:'2'){
        b('_________________________________________________________________________________________________________________________________________________________________________________________________')    
    }
    }
    tr {
    td(width:'60%', '&nbsp;&nbsp;&nbsp;&nbsp;Сеть радиодоступа')
    td{
        span('Подпись')
    }
    }
    tr {
    td(colspan:'2', '&nbsp;')
    }
    tr {
    td(colspan:'2', '&nbsp;&nbsp;&nbsp;&nbsp;Туленбаев Галым')
    }
    tr {
    td(colspan:'2'){
        b('_________________________________________________________________________________________________________________________________________________________________________________________________')    
    }
    }
    tr {
    td(width:'60%', '&nbsp;&nbsp;&nbsp;&nbsp;Сеть передачи данных')
    td{
        span('Подпись')
    }
    }
    tr {
    td(colspan:'2', '&nbsp;')
    }
    tr {
    td(colspan:'2', '&nbsp;&nbsp;&nbsp;&nbsp;Парманов Кайрат')
    }
    tr {
    td(colspan:'2'){
        b('_________________________________________________________________________________________________________________________________________________________________________________________________')    
    }
    }
    tr {
    td(width:'60%', '&nbsp;&nbsp;&nbsp;&nbsp;Поддержка коммутационной сети')
    td{
        span('Подпись')
    }
    }
    tr {
    td(colspan:'2', '&nbsp;')
    }
    tr {
    td(colspan:'2', '&nbsp;&nbsp;&nbsp;&nbsp;Страшенко Кирилл')
    }
    tr {
    td(colspan:'2'){
        b('_________________________________________________________________________________________________________________________________________________________________________________________________')    
    }
    }
    tr {
    td(width:'60%', '&nbsp;&nbsp;&nbsp;&nbsp;Заместитель Директора по развитию и эксплуатации сети')
    td{
        span('Подпись')
    }
    }
    tr {
    td(colspan:'2', '&nbsp;')
    }    
    tr {
    td(colspan:'2', '&nbsp;&nbsp;&nbsp;&nbsp;Есеркегенов Аскар')
    }
    tr {
    td(colspan:'2'){
        b('_________________________________________________________________________________________________________________________________________________________________________________________________')    
    }
    }
    tr {
    td(width:'60%', '&nbsp;&nbsp;&nbsp;&nbsp;Главный Технический Директор, Член Правления АО "Кселл"')
    td{
        span('Подпись')
    }
    }                      
                        }
                    }
                }
            }

        '''

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

print result

result
