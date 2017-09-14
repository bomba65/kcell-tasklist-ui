import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

import java.text.SimpleDateFormat
import groovy.json.JsonSlurper

def reasonsTitle = ["1":"Optimization works", "2":"Transmission works","3":"Infrastructure works","4":"Operation works"]
def servicesTitle = [1:"Revision",2:"Rollout",3:"Power",4:"Field Maintainance"]
def resolutionsTitle = ["createJr": "Create JR", "approved": "Approved", "rejected": "Reject", "cantFix": "Can't fix", "accepted": "Accepted", "modify": "Modify works", "attached": "Materials attached", "notAvailable": "Materials not Available", "match": "TR match with material list", "doesNotMatch": "TR doesn't match with material list", "generate.accepted": "Not required submit to leasing", "generate.submitToLeasing": "Required submit to leasing", "dispatched": "Dispatched Successfully", "permit.approved": "Permits received", "permit.notApproved": "Permits rejected", "prCreated": "PR Created", "saveChanges": "Save changes", "cannotPerform": "Cannot perform works", "done": "Done"]
def contractorsTitle = [1:"ТОО Аврора Сервис",2:"ТОО AICOM",3:"ТОО Spectr energy group",4:"TOO Line System Engineering",5:"JSC Kcell"]
def worksTitle = ["1": "1.BTS Macro/BTS Ultra installation (cabinet only) with on-air / Установка BTS Macro/BTS Ultra (только  кабинет) с активацией","2": "2.BTS Micro/BTS Midi installation (cabinet only) with on-air / Установка BTS Micro/BTS Midi (только кабинет) с активацией","3": "3.RBS/BTS Extension Installation(cabinet+battery backup) with on-air/ Установка RBS/BTS Extension (кабинет +система питания) с активацией","4": "4.RBS/BTS type change (cabinet+battery backup)with on-air / Замена RBS/BTS (кабинет+ запасная батарея) с активацией","5": "5.RBS/BTS cell addition(BBS+cabinet+antennas+feeders+poles) with on-air / Добавление сотового сегмента к  RBS/BTS (BBS+кабинет+антенны+фидер+  трубостойкb) с активацией","6": "6.BTS Macro removal (including packaging according Kcell required) / Демонтаж кабинета BTS Macro (включая упаковку согласно стандартам Kcell)","7": "7.BTS Micro/BTS Midi removal (including packaging according KCELL required) / Демонтаж BTS Micro/BTS Midi (включая упаковку согласно стандартам Kcell)","8": "8.Site change/replacement (All equipments, RF &TR& Infrastructure including packaging according Kcell required) / Замена/перемещение  сайта (все оборудование, RF &TR& Infrastructure, включая упаковку согласно стандартам Kcell)","9": "9.Site removal - all equipment including FE deinstallation(including packaging according Kcell required) / Демонтаж сайта - все оборудование включая демонтаж трансмиссионного оборудования на удаленном конце(включая упаковку согласно стандартам Kcell)","10": "10.BTS Swap - between Vendors (including packaging according Kcell required) / Замена BTS  - между вендорами (включая упаковку согласно стандартам Kcell)","11": "11.Battery Backup System Installation with BBS  (up to 2 sets) on-air / Установка системы резервного питания включая стойку BBS  (до 2 комплектов)  с активацией","12": "12.Battery Backup System change/replacement with on-air if required ( up to 2 sets) / Замена/перемещение системы резервных батарей с активацией, если необходимо (до 2 комплектов).","13": "13.Battery Backup System Removal (including packaging according Kcell required) / Демонтаж системы резервных батарей (включая упаковку согласно стандартам Kcell)","14": "14.Battery Change (deinstallation&Installation) per set/ замена аккумуляторной батареи (монтаж/демонтаж) за комплект","15": "15.Battery installation/deinstallation per set/ Монтаж/демонтаж аккумуляторной батареи за комплект","16": "16.Container installation / Установка контейнера","17": "17.Container change/replacement / Замена/перемещение контейнера","18": "18.Container removal / Демонтаж контейнера","19": "19.Site Grounding System Installation / Установка системы заземления на сайте.","20": "20.Site Grounding System сhange/replacement / Замена системы заземления","21": "21.Site Grounding System removal / Демонтаж системы заземления","22": "22.Site Lightning Protection installation / Установка системы защиты от молнии","23": "23.Lighting rod change/ replacement. / Замена/перемещение молниеотвода.","24": "24.Lighting rod remove / демонтаж молниеотвода","25": "25.Air Conditioning installation / Установка кондиционера","26": "26.Air Conditioning change/replacement/ Замена/перемещение кондиционера.","27": "27.Air Conditioning removal(including of packing according Kcell require)/ Демонтаж кондиционера (включая упаковку согласно стандартам Kcell)","28": "28.TMA installation and testing / Установка и тестирование ТМА","29": "29.TMA change/replacement / Замена/перемещение ТМА","30": "30.TMA remove and package / демонтаж ТМА","31": "31.TRU/TRX addition installation and testing / Дополнительная установка и тестирование TRU/TRX.","32": "32.TRU/TRX change/replacement. / Замена/перемещение TRU/TRX.","33": "33.TRU/TRX removal. / демонтаж TRU/TRX.","34": "34.EDGE DTRU/TSGB Upgrade. / Усовершенствование EDGE DTRU/TSGB","35": "35.Repeater installation and testing / Установка и тестирование репитера","36": "36.Repeater change/replacement. / Замена/перемещение репитера.","37": "37.Repeater removal. / Демонтаж репитера.","38": "38.GSM Antenna installation / Установка GSM антенны","39": "39.GSM Antenna location change/replacement (including poles) / Замена/Смена месторасположения GSM антенны (включая трубостойки)","40": "40.GSM Antenna direction change / Cмена направленности GSM антенны","41": "41.GSM Antenna tilting / Смена угла наклона GSM антенны","42": "42.GSM Antenna change (with pole) / Замена GSM антенны (включая трубостойки)","43": "43.GSM Antenna height change (up/down) / Изменение высоты GSM антенны (выше/ниже)","44": "44.GSM Antenna removal / Демонтаж GSM антенны","45": "45.Feeder installation 0-75m (per sector, if there is no feeder before) / Установка фидера 0-75м (за сектор, если ранее фидера не было)","46": "46.Feeder installation 76-175m (per sector, if there is no feeder before) / Установка фидера  76-175 м (за сектор, если ранее фидера не было)","47": "47.Feeder installation 176- up m (per sector, if there is no feeder before) / Установка фидера от 176 м и более (за сектор, если ранее фидера не было)","48": "48.Feeder change/replacement 0-75m (per sector, if it is needed to change feeder) / Замена/перемещение фидера 0-75 м (за сектор, если необходимо заменить фидер)","49": "49.Feeder change/replacement 76-175m (per sector, if it is needed to change feeder) / Замена/перемещение фидера 76-175 м (за сектор, если необходимо заменить фидер)","50": "50.Feeder change/replacement 176-up m (per sector, if it is needed to change feeder) / Замена/перемещение фидера от 176м и более (за сектор, если необходимо заменить фидер)","51": "51.Feeder removal 0-75m (per sector, if it's needed for dismantle) / демонтаж фидера 0-75 м (за сектор, если необходим демонтаж)","52": "52.Feeder removal 76-175m (per sector, if it's needed for dismantle) / демонтаж фидера 76-175 м (за сектор, если необходим демонтаж)","53": "53.Feeder removal 176-up m (per sector, if it's needed for dismantle) / демонтаж фидера 176 м и более (за сектор, если необходим демонтаж)","54": "54.Isolation& installation and implementation on ground/ on shelter / on roof for all type of Poles from 2m till 6m / изоляция, установка, ввод в эксплуатацию на земле/ на навесе/ на крыше всех типов трубостоек от 2 м до 6 м ","55": "55.Isolation& installation and implementation on ground/ on shelter / on roof for all type of Poles from 7m till 10m/ изоляция, установка, ввод в эксплуатацию на земле/ на навесе/ на крыше всех типов трубостоек от 7 м до 10 м ","56": "56.Change/Replacement on ground/ on shelter / on roof for all type of Poles from 2m till 6m / Замена/Перемещение на земле/ на навесе/ на крыше всех типов трубостоек от 2 м до 6 м ","57": "57.Change/Replacement on ground/ on shelter / on roof for all type of Poles from 7m till 10m /Замена/Перемещение на земле/ на навесе/ на крыше всех типов трубостоек от 7 м до 10 м ","58": "58.Removal on ground/ on shelter / on roof for all type of Poles from 2m till 6m / Демонтаж на земле/ на навесе/ на крыше всех типов трубостоек от 2 м до 6 м ","59": "59.Removal on ground/ on shelter / on roof for all type of Poles from 7m till 10m /Демонтаж на земле/ на навесе/ на крыше всех типов трубостоек от 7 м до 10 м","60": "60.2Mb, connection of PCM cable to DDF / Подключение  PCM кабеля к DDF, 2Mb","61": "61.Change/replacement  2Mb connection of PCM cable to DDF. / Замена/перемещение 2Mb потока PCM кабеля в DDF.","62": "62.HDSL Modem/router cisco, d-link/SIU installation and testing/ Инсталляция, тестирование HDSL Modem/router cisco, d-link/SIU","63": "63.HDSL Modem/router cisco, d-link/SIU Change/replacement/ Замена/перемещение HDSL Modem/router cisco, d-link/SIU","64": "64.HDSL Modem/router cisco, d-link/SIU Removal/ демонтаж HDSL Modem/router cisco, d-link/SIU","65": "65.MW HOP Installation and testing including indoor, outdoor, activation, adjustment (0,3-1,2m)/ инсталляция и тестирование Радиорелейного пролета, включая indoor, outdoor, активацию, настройку (0,3-1,2 м) ","66": "66.MW HOP Installation and testing including indoor, outdoor, activation, adjustment (1,6-3,3m)/ инсталляция и тестирование Радиорелейного пролета, включая indoor, outdoor, активацию, настройку (1,6-3,3 м) ","67": "67.MW HOP change/replacement including indoor, outdoor, activation, adjustment (0,3-1,2m)/ Замена/перемещение Радиорелейного пролета, включая indoor, outdoor, активацию, настройку (0,3-1,2 м) ","68": "68.MW HOP change/replacement including indoor, outdoor, activation, adjustment (1,6-3,3m)/  Замена/перемещение Радиорелейного пролета, включая indoor, outdoor, активацию, настройку (1,6-3,3 м) ","69": "69.MW HOP removal including indoor, outdoor (0,3-1,2m)/ демонтаж Радиорелейного пролета, включая indoor, outdoor (0,3-1,2м) ","70": "70.MW HOP removal including indoor, outdoor (1,6-3,3m)/ демонтаж Радиорелейного пролета, включая indoor, outdoor (1,6-3,3м) ","71": "71.MW Antenna installation and testing (0,3-1,2m)/ Инсталляция и тестирование радиорелейной  антенны (0,3-1,2м)","72": "72.MW Antenna installation and testing (1,6-3,3m)/ Инсталляция и тестирование радиорелейной  антенны (1,6-3,3м)","73": "73.MW Antenna change/replacement (0,3-1,2m)/ Замена/перемещение радиорелейной  антенны (0,3-1,2м)","74": "74.MW Antenna change/replacement (1,6-3,3m)/ Замена/перемещение радиорелейной  антенны (1,6-3,3м)","75": "75.MW Antenna removal (0,3-1,2m)/ демонтаж радиорелейной  антенны (0,3-1,2м)","76": "76.MW Antenna removal (1,6-3,3m)/ демонтаж радиорелейной  антенны (1,6-3,3м)","77": "77.MW Antenna direction change including adjustment (0,3-1,2m)/ изменение направления радиорелейной  антенны, включая настройку (0,3-1,2м)","78": "78.MW Antenna direction change including adjustment (1,6-3,3m)/ изменение направления радиорелейной  антенны, включая настройку (1,6-3,3м)","79": "79.Power Splitter change/ замена Power Splitter","80": "80.Additional fastening by metal pipe (per one pipe)/ дополнительное укрепление металлической трубой (за 1 трубу)","81": "81.LTU/ETU/PFU installation cabling and testing (any type)/ инсталляция, подключение кабеля и тестирование LTU/ETU/PFU (любого типа)","82": "82.LTU/ETU/PFU change/replacement (any type)/ замена/перемещение LTU/ETU/PFU (любого типа)","83": "83.LTU/ETU/PFU removal (any type)/ демонтаж LTU/ETU/PFU (любого типа)","84": "84.NPU installation/change/replacement/ инсталляция, замена/перемещение NPU","85": "85.FAU installation/change/removal/  инсталляция, замена/перемещение FAU","86": "86.upgrade changing configuration of MW equipment (capacity, protection type, software, modulation etc.) / модернизация, изменение конфигурации радиорелейного оборудования (емкость, тип резервирования, ПО, модуляция, т.п.)","87": "87.Traffic Node 2p/6p/FIU19 installation and testing/ Инсталляция и тестирование Traffic Node 2p/6p/FIU19 ","88": "88.Traffic Node 20p/Metro HUB installation and testing/ Инсталляция и тестирование Traffic Node 20p/Metro HUB","89": "89.Traffic Node/ FIU19/ Metro HUBchange/replacement (all types)/ замена/перемещение Traffic Node/ FIU19/ Metro HUB (всех типов)","90": "90.Traffic Node all type/MetroHUB/FIU19 removal/ Демонтаж Traffic Node всех типов/MetroHUB/FIU19","91": "91.MMU/SMU installation and testing/ Инсталляция и тестирование MMU/SMU","92": "92.MMU/SMU change/replacement /  замена/ перемещение MMU/SMU","93": "93.MMU/SMU removal / демонтаж MMU/SMU","94": "94.RAU installation with power splitter (any type)/ инсталляция RAU с power splitter (любой тип)","95": "95.RAU change (any type)/ замена RAU (любого типа)","96": "96.RAU removal with power splitter (any type)/ демонтаж RAU с power splitter (любого типа)","97": "97.AMM 2U/4U/14U installation (including grounding)/ инсталляция AMM 2U/4U/14U (включая заземление)","98": "98.AMM 2U/4U/14U change/replacement (including grounding)/ замена/перемещение AMM 2U/4U/14U (включая заземление)","99": "99.AMM 2U/4U/14U removal (including grounding)/ демонтаж AMM 2U/4U/14U (включая заземление)","100": "100.Transmission rack 19\" installation (including grounding)/ инсталляция трансмиссионной стойки 19\"(включая заземление)","101": "101.Transmission rack 19\" change/replacement (including grounding) / замена/перемещение трансмиссионной стойки 19\"(включая заземление)","102": "102.Transmission rack 19\" removal (including grounding)/ демонтаж трансмиссионной стойки 19\"(включая заземление)","103": "103.MW Feeder/cable installation 0-75m/ Инсталляция MW Feeder/cable 0-75 м","104": "104.MW Feeder/cable installation 76-175m / Инсталляция MW Feeder/cable 76-175 м","105": "105.MW Feeder/cable installation 176- up m /  Инсталляция MW Feeder/cable 176 м и больше","106": "106.MW Feeder/cable change/replacement 0-75m / Замена/ перемещение MW Feeder/cable 0-75 м","107": "107.MW Feeder/cable change/replacement 76-175m / Замена/ перемещение MW Feeder/cable 76-175 м","108": "108.MW Feeder/cable change/replacement 176- up m / Замена/ перемещение MW Feeder/cable 176 м и больше","109": "109.MW Feeder/cable removal 0-75m / демонтаж MW Feeder/cable 0-75 м","110": "110.MW Feeder/cable removal 75-175m/ демонтаж MW Feeder/cable 75-175 м","111": "111.MW Feeder/cable removal 176-up m/ демонтаж MW Feeder/cable 176 м и больше","112": "112.Additional fastering with installing pipes of ML feeder cable (any length)/ Дополнительное усиление с инсталляцией труб ML feeder cable любой длины","113": "113.PCM/UTP/KVSM/optical patchcord Cable installation 0-75m / Инсталляция пачкорда PCM/UTP/KVSM/optical 0-75 м","114": "114.PCM/UTP/KVSM/optical patchcord Cable installation 76-175m / Инсталляция пачкорда PCM/UTP/KVSM/optical 76-175 м","115": "115.PCM/UTP/KVSM/optical patchcord Cable installation 176-up m/ Инсталляция пачкорда PCM/UTP/KVSM/optical 176 м и больше","116": "116.PCM/UTP/KVSM/optical patchcord Cable change/replacement 0-75m / замена/перемещение пачкорда PCM/UTP/KVSM/optical 0-75м","117": "117.PCM/UTP/KVSM/optical patchcord Cable change/replacement  76-175m/ замена/перемещение пачкорда PCM/UTP/KVSM/optical 76-175м","118": "118.PCM/UTP/KVSM/optical patchcord Cable change/replacement 176-up m/ замена/перемещение пачкорда PCM/UTP/KVSM/optical 176м и больше","119": "119.PCM/UTP/KVSM/optical patchcord Cable removal 0-75m/ демонтаж пачкорда PCM/UTP/KVSM/optical 0-75м","120": "120.PCM/UTP/KVSM/optical patchcord Cable removal 76-175m/ демонтаж пачкорда PCM/UTP/KVSM/optical 76-175м","121": "121.PCM/UTP/KVSM/optical patchcord Cable removal 176-up m/ демонтаж пачкорда PCM/UTP/KVSM/optical 176м и больше","122": "122.DDF/plinth installation+ cabiling (for pair)/ инсталляция DDF/плинт + расшивка кабеля (за пару)","123": "123.DDF/plinth change/replacement (for pair)/ замена/перемещение DDF/плинт (за пару)","124": "124.DDF/plinth removal (for pair) / демонтаж DDF/плинт (за пару)","125": "125.Indoor Cable tracking way installation/change/replacement/removal 0-50m (Except new site concept if it is needed installation for up to 10 meters, it will be the same price)/ инсталляция, замена, перемещение, демонтаж   кабеля и кабелегона внутри помещений 0-50 м (за искл. Новых сайтов, если необходима установка до 10 метров, цена будет та же самая)","126": "126.Indoor Cable tracking way installation/change/replacement/removal 51-100m/ инсталляция, замена, перемещение, демонтаж   кабеля и кабелегона внутри помещений 51-100 м ","127": "127.Indoor Cable tracking way installation/change/replacement/removal 101 - up m/ инсталляция, замена, перемещение, демонтаж   кабеля и кабелегона внутри помещений 101м  и более","128": "128.outdoor Cable tracking way installation/change/replacement/removal 0-50 m (Except new site concept if it is needed installation for up to 10 meters, it will be the same price) / инсталляция, замена, перемещение, демонтаж   кабеля  вне помещений 0-50 м (за искл. Новых сайтов, если необходима установка до 10 метров, цена будет та же самая)","129": "129.outdoor Cable tracking way installation/change/replacement/removal 101 - up m/ инсталляция, замена, перемещение, демонтаж   кабеля вне помещений 101м  и более","130": "130.outdoor Cable tracking way installation/change/replacement/removal 51-100 m/ инсталляция, замена, перемещение, демонтаж   кабеля  вне помещений 51-100м  ","131": "131.outdoor TR rack installation / инсталляция вне помещения кабинета для оборудования систем передачи данных ","132": "132.outdoor TR rack change/replacement/ замена/перемещение вне помещения кабинета для оборудования систем передачи данных ","133": "133.outdoor TR rack removal/ демонтаж вне помещения кабинета для оборудования систем передачи данных ","134": "134.DC/DC converter installation, cabling connection/ инсталляция, подключение кабеля DC/DC converter","135": "135.DC/DC converter change/replacement/ замена/перемещение DC/DC converter","136": "136.DC/DC converter removal/ демонтаж DC/DC converter","137": "137.DDU installation/change/removal/ инсталляция/ замена/демонтаж DDU","138": "138.Power cable installation (4x10mm2, 4x16mm2), per m (excluding material) / Проведение электрического кабеля(4x10mm2,    4x16mm2), за  м (не включая расходы на оборудование)","139": "139.Power cable change/replacement (4x10mm2, 4x16mm2), per m    /  замена/перемещение электрического кабеля(4x10mm2,    4x16mm2), за  м","140": "140.Power Cable Removal (4x10mm2, 4x16mm2), per m / демонтаж электрического кабеля(4x10mm2,    4x16mm2), за  м    ","141": "141.Power cable installation (4x25mm2), per m / Проведение электрического кабеля (4x25mm2), за м   (не включая расходы на оборудование)","142": "142.Power cable change/replacement (4x25mm2), per m / замена/перемещение кабеля питания (4x25mm2), за м ","143": "143.Power cable removal (4x25mm2), per m / демонтаж  кабеля питания (4x25mm2), за м ","144": "144.Actura/Rectifier Installation (any type) / Монтаж и установка Actura (любая модель)","145": "145.Actura/Rectifier Change (any type)/ замена Actura/Rectifier (любой тип)","146": "146.Actura/Rectifier Removal (any type)/ демонтаж Actura/Rectifier (любой тип)","147": "147.Power counter Installation/ установка электрического счетчика","148": "148.Power counter change/replacement / Замена и перемещение электрического счетчика.","149": "149.Power counter Removal/ демонтаж электрического счетчика.","150": "150.Circuit breaker installation per breaker / Установка автоматического выключателя","151": "151.Circuit breaker change/ replacement per breaker / Замена и перемещение автоматического выключателя","152": "152.Cross Connection Checking / Проверка Cross Connection","153": "153.Lock installation/ Установка замка на сайт.","154": "154.Lock change/replacement/ замена замка на сайте.","155": "155.Mobile BTS installation (TR,Power,ON-AIR) / Установка мобильного BTS (TR,Power,ON-AIR)","156": "156.Installation and On-air Collocation of BTS on site. (including required GSM Sector cells , Battery Backup System, rectifiers, antennas, feeders,poles)/ установка и активация дополнительной (Collocation) BTS на сайте (включая необходимые GSM  сектора, системы резервирования аккумуляторных батарей, выпрямители, антенны, фидера, трубостойки)","157": "157.Installation survey per building site (Including all design &installation& power drawings in Auto CAD)/ Проект установки и монтажа сайта (включая все чертежи дизайна, инсталляции, питания в Auto CAD)","158": "158.Implementation per indoor antenna (if total antenna amount will be 1-20 pcs per site)/ Ввод в эксплуатацию за антенну внутри помещения (если общее кол-во антенн составляет 1-20штук на 1 сайте)","159": "159.Implementation per indoor antenna (if total antenna amount will be 21-50 pcs per site)/ Ввод в эксплуатацию за антенну внутри помещения (если общее кол-во антенн составляет 21-50штук на 1 сайте)","160": "160.Implementation per indoor antenna (if total antenna amount will be 51 and up per site)/ Ввод в эксплуатацию за антенну внутри помещения (если общее кол-во антенн составляет 51 штуку и более)"]
def i18n = ["task.resolution.filesModified": "Files modified", "task.resolution.createJr": "Create JR", "task.resolution.approved": "Approved", "task.resolution.rejected": "Reject", "task.resolution.cantFix": "Can't fix", "task.resolution.accepted": "Accepted", "task.resolution.modify": "Modify works", "task.resolution.attached": "Materials attached", "task.resolution.notAvailable": "Materials not Available", "task.resolution.match": "TR match with material list", "task.resolution.doesNotMatch": "TR doesn't match with material list", "task.resolution.generate.accepted": "Not required submit to leasing", "task.resolution.generate.submitToLeasing": "Required submit to leasing", "task.resolution.dispatched": "Dispatched Successfully", "task.resolution.permit.approved": "Permits received", "task.resolution.permit.notApproved": "Permits rejected", "task.resolution.prCreated": "PR Created", "task.resolution.saveChanges": "Save changes", "task.resolution.cannotPerform": "Cannot perform works", "task.resolution.done": "Done", "task.resolution.Completed": "Completed", "task.resolution.completed": "Completed", "task.resolution.needSSID": "Need SSID", "task.resolution.projectCompleted": "Project Completed"]

def formatDateTime = new SimpleDateFormat("dd.MM.yyyy HH:mm")
def formatDate = new SimpleDateFormat("dd.MM.yyyy")
def jrNumberObj = (jrNumber != null ? jrNumber : '########')
def reasonsObj = reasonsTitle[reason]
def requestedDateObj = formatDateTime.format(requestedDate)
def validityDateObj = formatDate.format(validityDate)
def serviceObj = servicesTitle[contract]
def contractorObj = contractorsTitle[contractor]
def jobWorksTotalObj = jobWorksTotal != null ? jobWorksTotal : 'N/A'
def initiatorFullObj = new JsonSlurper().parseText(initiatorFull.toString())
def jobWorksObj = new JsonSlurper().parseText(workPrices.toString())
def resolutionsObj = new JsonSlurper().parseText(resolutions.toString())

def binding = ["initiatorFull": initiatorFullObj, "jrNumber": jrNumberObj, "requestedDate": requestedDateObj, "reasons": reasonsObj, "materialsRequired" : materialsRequired, "validityDate": validityDateObj, "service": serviceObj, "leasingRequired": leasingRequired, "soaComplaintId": soaComplaintId, "contractor": contractorObj, "siteName": siteName, "powerRequired": powerRequired, "project": project, "site_name": site_name, "jobWorks": jobWorksObj, "jobWorksTotal": jobWorksTotalObj, "explanation": explanation, "resolutions": resolutionsObj, "worksTitle": worksTitle, "formatDateTime": formatDateTime, "history": history, "i18n": i18n]


def template = '''\
yieldUnescaped '<!DOCTYPE html>'

html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
        title('My page')
    }
    newLine()
    style ('td { padding: 10px; }')
    newLine()
    body {
        p('Hi,')
        newLine()
        p('Create PR for Job Request ' + jrNumber)
        newLine()
        table {
            tr {
                td {
                    b('Initiator')
                    yield ': ' + initiatorFull.firstName + ' ' + initiatorFull.lastName
                }
                td {
                    b('JR Number')
                    yield ': ' + jrNumber
                }
                td()
            }
            tr {
                td {
                    b('Requested date')
                    yield ': ' + requestedDate
                }
                td {
                    b('Reason')
                    yield ': ' + reasons
                }
                td{
                    b('Materials required')
                    yield ': ' + materialsRequired
                }
            }
            tr {
                td {
                    b('Validity date')
                    yield ': ' + validityDate
                }
                td {
                    b('Contract')
                    yield ': ' + service
                }
                td {
                    b('Leasing required')
                    yield ': ' + leasingRequired
                }
            }
            tr {
                td {
                    b('SA&O Complaint Id')
                    yield ': ' + soaComplaintId
                }
                td {
                    b('Contractor')
                    yield ': ' + contractor
                }
                td {
                    b('Site (near end)')
                    yield ': ' + siteName
                }
            }            
            tr {
                td {
                    b('Power engineering required')
                    yield ': ' + powerRequired
                }
                td {
                    b('Project')
                    yield ': ' + project
                }
                td {
                    b('Site Name')
                    yield ': ' + site_name
                }
            }
            tr {
                td {
                    b('Works')
                    yield ': '
                }
                td('')
                td('')
            }   
            tr {
                td(colspan: '3') {
                    table (class : 'table table-condensed') {
                        thead {
                            tr { 
                                th(width: '30%', 'Works')
                                th('Works Qty')
                                th('Materials Qty')
                                th('Sites')
                                th('Base Price')
                                th('Base + Transportation +8%')
                                th('1 work price per site Sum/site q-ty')
                                th('Sum * works q-ty')
                                th('Total')                            
                            }
                        }
                        tbody {
                            jobWorks.each { work ->
                                tr {
                                    td(worksTitle[work.sapServiceNumber])
                                    td(work.quantity!=null?work.quantity:'')
                                    td(work.materialQuantity!=null?work.materialQuantity:'')
                                    td {
                                        work.relatedSites.each { rs -> 
                                            yield '' + (rs.site_name == site_name? 'Main Site: ':'') + rs.site_name + ','
                                        }
                                    }
                                    td((work.unitWorkPrice!=null?work.unitWorkPrice:'N/A') + '&nbsp;&#8376;\')
                                    td((work.unitWorkPricePlusTx!=null?work.unitWorkPricePlusTx:'N/A') + '&nbsp;&#8376;\')
                                    td((work.unitWorkPricePerSite!=null?work.unitWorkPricePerSite:'N/A') + '&nbsp;&#8376;\')
                                    td((work.netWorkPricePerSite!=null?work.netWorkPricePerSite:'N/A') + '&nbsp;&#8376;')
                                    td(work.total!=null?work.total:'N/A')                                
                                }
                            }
                            tr {
                                th(colspan : '7', style : 'text-align: left;', 'Total')
                                th()
                                th(jobWorksTotal)
                            }
                        }
                    }
                }
            }
            tr {
               td(colspan : '3'){
                    b('Explanation of works')
                    yield ': ' + explanation                    
               } 
            }   
            tr {
               td(colspan : '3'){
                    h3('History')
                    table {
                        thead {
                            th('Activity')
                            th('Assignee')
                            th('Resolution')
                            th('Comment')                            
                        }
                        tbody {
                            resolutions.each { resolution ->
                                tr {
                                    td(history["tasksMap"]?.(resolution.taskId)?.name!=null?history["tasksMap"].(resolution.taskId).name:'N/A')
                                    td(history["assigneesMap"][resolution.assignee]!=null? history["assigneesMap"][resolution.assignee]:'N/A')
                                    td(i18n["task.resolution."+resolution.resolution]!=null?i18n["task.resolution."+resolution.resolution]: 'N/A')
                                    td(history["tasksMap"][resolution.taskId]?.endTime!=null?formatDateTime.format(history["tasksMap"][resolution.taskId].endTime):'N/A')
                                    td(resolution.comment!=null?resolution.comment:'N/A')
                                }
                            }
                        }
                    }
               }                
            }
        }
        newLine()
        p {
            yield 'Пройдя по следующей ссылке на страницу в HUB.Kcell.kz, вы можете оставить в поле комментариев свои замечания и/или пожелания относительно функционала и интерфейса системы:'
            a(href : 'https://hub.kcell.kz/x/kYNoAg', 'https://hub.kcell.kz/x/kYNoAg')
        }
        p ('Greetings,<br>Kcell Flow')
    }
}
'''

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

result