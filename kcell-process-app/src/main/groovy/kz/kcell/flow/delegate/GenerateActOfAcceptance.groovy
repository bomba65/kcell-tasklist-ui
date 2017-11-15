package kz.kcell.flow.delegate

import groovy.json.JsonSlurper
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import kz.kcell.flow.files.Minio
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.delegate.TaskListener
import org.camunda.bpm.engine.variable.Variables
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service("generateActOfAcceptance")
class GenerateActOfAcceptance implements TaskListener {
    @Autowired
    private Minio minio;

    public static void main(String[] args) {
        def sharingPlanObj = [
                "shared_bs_id": 103,
                "status": 1,
                "accepted_date": 42935,
                "city": "Almaty",
                "oblast": "Almatinskiya",
                "address": "Almaty city, Dostyk-Druzhba ave., 148/1 Общежитие № 2, КСК",
                "host": "Beeline",
                "site_owner": "Kcell",
                "site_name": "BeelineHost-KcellSite_81007ALMSKUSTAZ",
                "site_id": 81007,
                "cluster_name": "Almaty_CL01",
                "current_rbs_type": "RBS8800",
                "current_vendor": "ZTE",
                "new_rbs_type": "",
                "rbs_location": "Indoor",
                "existing_bands_with_section_number": "3*2100+3*900+2*1800",
                "existing_bands_with_section_number_rounded": "3*2100+3*900+3*1800",
                "po_configuration": "SWAP RBS6601 3x4 WCDMA + 3x4 GSM1800 + 3x4 GSM900/Extension of Existing Site with L8+L18 Indoor",
                "in_plan": "yes",
                "in_plan_date": 42694,
                "technology2g": "",
                "technology3g": "",
                "lte_band": "SWAP+LTE800",
                "band900": 742223,
                "band1800": "",
                "band2100": 80010865,
                "band800": 80010865,
                "swap_antennas_po": 80010865,
                "total_qty_of_antennas_per_sector": 2,
                "jumper": "4.3-10(m)-7-16(m)",
                "transport_type": "FIber",
                "possibility_to_provide_100_mbps_in_2017": "yes",
                "possibility_to_increase_transmission": "yes",
                "ran_po_name": "1)PO 68025_348LTE; 2)PO 68790_ZTE SWAP 1",
                "po_item_number": "1)2.1.1 - 2.1.3; 2.1.5 - 2.1.6; 2)3.7 - 3.8; 4.1",
                "dispatched_hw_for_sites": "",
                "site_usage_type": "совместного",
                "infrastructure_owner": "Kcell",
                "transmission_owner": "Kcell",
                "transmission_channel_volume_granted_to_partner": "",
                "integration_city": "Уральск",
                "isSpecialSite": "true",
                "shared_sectors": [
                        [
                                "sector_name": "581007-100",
                                "lac": 46,
                                "enodeb_id": 581007,
                                "shared_bs_id": 14214,
                                "beeline_position_number": "",
                                "kcell_position_number": "",
                                "position_type": "покрытие",
                                "location_type": "",
                                "placement_type": "",
                                "longitude": 61.3765,
                                "latitude": 61.19119,
                                "antenna_type": 80010865,
                                "height": 33,
                                "azimuth": 40,
                                "tilt": "2/6",
                                "power_watt": 40,
                                "ems_limitations_info": "нет",
                                "enodeb_range": 800,
                                "beeline_band": 10,
                                "kcell_band": 10,
                                "status": ""
                        ],
                        [
                                "sector_name": "581007-102",
                                "lac": 46,
                                "enodeb_id": 581007,
                                "shared_bs_id": 15224,
                                "beeline_position_number": "",
                                "kcell_position_number": "",
                                "position_type": "покрытие",
                                "location_type": "",
                                "placement_type": "",
                                "longitude": 61.3765,
                                "latitude": 61.19119,
                                "antenna_type": 80010865,
                                "height": 33,
                                "azimuth": 160,
                                "tilt": "2/8",
                                "power_watt": 40,
                                "ems_limitations_info": "нет",
                                "enodeb_range": 800,
                                "beeline_band": 10,
                                "kcell_band": 10,
                                "status": ""
                        ],
                        [
                                "sector_name": "581007-104",
                                "lac": 46,
                                "enodeb_id": 581007,
                                "shared_bs_id": 131232,
                                "beeline_position_number": "",
                                "kcell_position_number": "",
                                "position_type": "покрытие",
                                "location_type": "",
                                "placement_type": "",
                                "longitude": 61.3765,
                                "latitude": 61.19119,
                                "antenna_type": 80010865,
                                "height": 33,
                                "azimuth": 290,
                                "tilt": "2/6",
                                "power_watt": 40,
                                "ems_limitations_info": "нет",
                                "enodeb_range": 800,
                                "beeline_band": 10,
                                "kcell_band": 10,
                                "status": ""
                        ]
                ]
        ]

        def result = render([
                "sharingPlanObj": sharingPlanObj,
                "colspan": sharingPlanObj.shared_sectors.size(),
                "checkingByGuestResult": "approved",
                "taskSubmitDate": new Date()
        ])
        println(result)
    }

    static String render (Map binding ) {
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
                    p( style:'text-align: right;', "Дополнение № 1")
                    newLine()
                    p(style:'text-align: center;'){
                        yield b("Акт приёмки в эксплуатацию БС Стандарта LTE БС Атс74")
                    }
                    newLine()
                    p(style:'text-align: center;',"(Заполняется Ведущим оператором)")
                    newLine()
                    //p('sharing plan:' + sharingPlanObj)
                    newLine()
                    table(class:"table", border:"1") {
                        tr {
                            td (width:"10%","Ведущий оператор:")
                            td (width:"30%", sharingPlanObj.host == "Beeline" ? 'ТОО «КаР-Тел»' : sharingPlanObj.host == "Kcell" ? 'АО «Кселл»':'' )
                            td(colspan:colspan, width:"50%")
                        }
                        tr {
                            td ("Владелец Сайта:")
                            td (sharingPlanObj.site_owner == "Beeline" ? 'ТОО «КаР-Тел»' : sharingPlanObj.site_owner == "Kcell" ? 'АО «Кселл»':'' )
                            td(colspan:colspan)
                        }
                        tr {
                            td ("Идентификатор Площадки в сети Ведущего/Ведомого:")
                            td (sharingPlanObj.site_id)
                            td(colspan:colspan)
                        }
                        tr {
                            td ("Адрес Площадки:")
                            td (sharingPlanObj.address)
                            td(colspan:colspan)
                        }
                        tr {
                            td ("Частотный диапазон БС:")
                            td (sharingPlanObj.shared_sectors[0].enodeb_range?sharingPlanObj.shared_sectors[0].enodeb_range + ' Мгц' : '' + ' Мгц')
                            td(colspan:colspan)
                        }
                        tr {
                            td ("Конфигурация БС:")
                            td {
                                //for ( ss in sharingPlanObj.shared_sectors) {
                                sharingPlanObj.shared_sectors.eachWithIndex { ss, index ->
                                    yield '1' 
                                    if (index < (sharingPlanObj.shared_sectors.size()-1)) { yield '/' }
                                }
                            }
                            td(colspan:colspan)
                        }
                        tr {
                            td ("Идентификатор БС в сети Ведущего/Ведомого:")
                            td (sharingPlanObj.site_name)
                            td(colspan:colspan)
                        }
                        tr {
                            td ("Характер использования БС:")
                            td (sharingPlanObj.site_usage_type)
                            td(colspan:colspan)
                        }
                        tr {
                            td ("CellID:")
                            for ( ss in sharingPlanObj.shared_sectors) {
                                td {
                                    yield ss.sector_name
                                }
                            }
                        }
                        tr {
                            td ("Ширина спектра (MГц):")
                            for ( ss in sharingPlanObj.shared_sectors) {
                                td {
                                    yield sharingPlanObj.site_owner == "Beeline" ? ss.beeline_band: sharingPlanObj.site_owner == "Kcell" ? ss.kcell_band:''
                                    yield 'MГц'
                                }
                            }
                        }
                        tr {
                            td ("Владелец транспорта:")
                            for ( ss in sharingPlanObj.shared_sectors) {
                                td {
                                    yield sharingPlanObj.site_owner == "Beeline" ? 'ТОО «КаР-Тел»': sharingPlanObj.site_owner == "Kcell" ? 'АО «Кселл»':''
                                }
                                
                            }
                        }
                        tr {
                            td ("Емкость арендуемого ресурса транспортной сети у Владельца транспорта:")
                            td {
                                yield sharingPlanObj.transmission_channel_volume_granted_to_partner
                                yield ' Мбит'
                            }
                            td(colspan:colspan)
                        }
                        tr {
                            td ("Наименование Стыка:")
                            td ()
                            td(colspan:colspan)
                        }
                        tr {
                            td ("Ответственный представитель Ведущего оператора (ФИО, Должность):")
                            td ()
                            td(colspan:colspan)
                        }
                    }
                    newLine()
                    newLine()
                    p('&nbsp;')
                    newLine()
                    table(class:"table", border:"1") {
                        tr {
                            th (width:"50%",style:"background-color: #D8D8D8;","Наименование параметра")
                            th (width:"50%",style:"background-color: #D8D8D8;","Статус")
                        }
                        tr {
                            td ("Выполнение требований к Инфраструктуре сайта, качеству монтажа Основного Оборудования")
                            td("В норме")
                        }
                        tr {
                            td ("Выполнение требований к ресурсу транспортной сети")
                            td("В норме")
                        }
                        tr {
                            td ("Обеспечение работоспособности сервисов")
                            td("В норме")
                        }
                        tr {
                            td ("На площадке ведомого работы выполнены согласно утвержденного проекта")
                            td("В норме")
                        }
                    }
                    newLine()
                    p('&nbsp;')
                    p('&nbsp;')
                    p('&nbsp;')
                    p('&nbsp;')
                    p('&nbsp;')
                    p('&nbsp;')
                    newLine()
                    table(width:"80%") {
                        tr {
                            td{
                                b('№')
                            }
                            td{
                                yield b('Замечания')
                                newLine()
                                yield p('(после устранения недостатка, его вычеркивает проверяющий)')
                            }
                            td{b('дата')}
                            td{b('подпись')}
                            td{b('ФИО')}
                        }
                        for(int i = 0;i<=16;i++) {
                            tr {
                                td{b("____")}
                                td{b("______________________________________________________")}
                                td{b("________")}
                                td{b("__________")}
                                td{b("_______________________")}
                            }
                        }
                        
                    }
                    p('&nbsp;')
                    p{
                        i{ 
                            yield 'Базовую станцию _____________'
                            yield b('(принять в эксплуатацию/выключить)')
                            yield ', недостатки устранить до «__ » ___ 201__ г.'
                        } 
                    }
                    p('Ответственный за приёмку БС в эксплуатацию со стороны Ведущего')
                    p('«__________ » ________________ «Ф.И.О.»&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;___________')
                    p('&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(подпись)')
                    
                    p(style:"text-align: right;"){
                        if (checkingByGuestResult && checkingByGuestResult == "approved") {
                            yield taskSubmitDate.format('dd/MM/yyyy') + ' г'
                        } else {
                            yield '« ____ » ____________ 20___ г.'
                        }
                    }
                    p('Ответственный за приёмку БС в эксплуатацию со стороны Ведомого')
                    p('«__________ » ________________ «Ф.И.О.»&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;___________')
                    p('&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(подпись)')
                    br()
                    span{
                        br(clear:all, style:'mso-special-character:line-break;page-break-before:always')
                    }
                    
                    p(style:"text-align: right;",'Дополнение № 2')
                    p(style:"text-align: center;") {
                        yield 'Протокол проверки выполнении требований к инфраструктуре сайта, АФУ и качеству монтажа основного оборудования БС Атс74'
                    }
                    p(style:"text-align: center;",'(Заполняется Ведущим оператором)')
                    table(class:"table", border:"1") {
                        tr {
                            td(width:"40%", "Ведущий оператор:")
                            td(width:"60%", colspan:"2", "&nbsp;")
                        }
                        tr {
                            td ("Владелец Сайта")
                            td(colspan:"2", "&nbsp;")
                        }
                        tr {
                            td ("Идентификатор Площадки в сети Ведущего/Ведомого")
                            td(colspan:"2", "&nbsp;")
                        }
                        tr {
                            td ("Адрес Площадки:")
                            td(colspan:"2", "&nbsp;")
                        }
                        tr {
                            td ("ФИО ответственного представителя Владельца Сайта, выполнившего проверку")
                            td(colspan:"2", "&nbsp;")
                        }
                        tr {
                            td ("Требования")
                            td(width:"20%", "Отметка о выполнении (ДА/НЕТ)")
                            td(width:"80%", "Комментарий (В случае невыполнения, указать срок устранения)")
                        }
                        tr {
                            td ("1.&nbsp;&nbsp;КСВ АФТ БС при любой длине АФТ (с антеннами) не превышает значения 1,2во всём диапазоне рабочих частот, предусмотренных стандартом/ми БС.")
                            td("В норме")
                            td()
                        }
                        tr {
                            td ("2.&nbsp;&nbsp;Любая неоднородность на любой длине АФТ имеет  значение КСВН не более 1,105 при условии, если в АФТ отсутствуют элементы с иными значениями КСВ. Погрешность измерения КСВ не превышает 6%.")
                            td("В норме")
                            td()
                        }
                        tr {
                            td ("3.&nbsp;&nbsp;Электропитание и источник заземления для eNodeB, места размещения и условия функционирования (в В нормельном режиме) оборудования в аппаратных / телекоммуникационных климатических шкафах (включая размещение в действующих стойках, шкафах, телекоммуникационных климатических шкафах) выполнены  в соответствии с требованиями фирм-производителей оборудования.")
                            td("В норме")
                            td()
                        }
                        tr {
                            td ("4.&nbsp;&nbsp;Размещение на Площадке металлоконструкций для монтажа антенно-фидерного тракта выполнено с учетом:")
                            td{
                                ul{
                                    li('обеспечения минимальной (с учетом выполнения требований по п.1 - обеспечения КСВ ≤1,2) длины фидеров / кабелей соединяющих оборудование;')
                                    li('исключения искажения диаграммы направленности устанавливаемых на металлоконструкции антенн (в горизонтальной и вертикальной плоскости) стенами, углами, кровлей зданий / сооружений, металлоконструкциями или оснащением опоры, прочими препятствиями и обеспечения возможности юстировки антенн по азимуту не менее чем на 30 градусов в каждую сторону')
                                }
                            }
                            td()
                        }
                        tr {
                            td ("5.&nbsp;&nbsp;Для монтажа фидеров, кабелей, соединяющих оборудование распределенной (feederless) архитектуры, на металлоконструкциях, крышах, парапетах, стенах зданий / сооружений предусмотрены кабель-росты с учётом типов предполагаемых к использованию фидеров и кабелей, рекомендаций по монтажу фирм-производителей фидеров и кабелей")
                            td("В норме")
                            td()
                        }
                        tr {
                            td ("6.&nbsp;&nbsp;Предусмотрены мероприятия по обеспечению безопасной эксплуатации объекта в соответствии с распорядительными и В нормативными государственными и (или) отраслевыми документами.")
                            td("В норме")
                            td()
                        }
                    }
                    p('*Значение будут уточнено в ходе тестового периода')
                    p('Ответственный за проверку выполнения требований со стороны Ведущего')
                    p('«__________ » ________________ «Ф.И.О.»&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;___________')
                    p('&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(подпись)')
                    p(style:"text-align: right;"){
                        if (checkingByGuestResult && checkingByGuestResult == "approved") {
                            yield taskSubmitDate.format('dd/MM/yyyy') + ' г'
                        } else {
                            yield '« ____ » ____________ 20___ г.'
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
        return result
    }
    @Override
    void notify(DelegateTask delegateTask) {

        def execution = delegateTask.execution
        def sharingPlan = execution.getVariable("sharingPlan")
        def checkingByGuestResult = execution.getVariable("checkingByGuestResult")
        def sharingPlanObj = new JsonSlurper().parseText(sharingPlan.toString())
        def colspan = sharingPlanObj.shared_sectors.size()
        def taskSubmitDate = new Date()

        def binding = [
                "checkingByGuestResult": checkingByGuestResult,
                "sharingPlanObj": sharingPlanObj,
                "colspan": colspan,
                "taskSubmitDate": taskSubmitDate

        ]


        def result = render(binding)
        execution.setVariable("actOfAcceptanceDocument", Variables.fileValue("actOfAcceptance.doc").file(result.getBytes("utf-8")).mimeType("application/msword").create())
        execution.setVariable("acceptanceDate", taskSubmitDate)
    }
}
