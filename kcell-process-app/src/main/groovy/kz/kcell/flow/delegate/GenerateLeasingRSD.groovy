package kz.kcell.flow.delegate

import com.fasterxml.jackson.databind.JsonSerializable
import groovy.json.JsonSlurper
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import kz.kcell.flow.files.Minio
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.delegate.TaskListener
import org.camunda.bpm.engine.impl.util.json.JSONArray
import org.camunda.bpm.engine.impl.util.json.JSONObject
import org.camunda.bpm.engine.variable.Variables
import org.camunda.spin.plugin.variable.SpinValues
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service("GenerateLeasingRSD")
class GenerateLeasingRSD implements TaskListener {
    private Minio minioClient;

    @Autowired
    public GenerateLeasingRSD(Minio minioClient) {
        this.minioClient = minioClient;
    }

//    public static void main(String[] args) {
//
//        def jsonSlurper = new JsonSlurper()
//        def object = jsonSlurper.parseText('{"cn_addr_city": "cellAntennaJson.address.cn_addr_city", "cn_sitename": "candidateJson.siteName","cn_bsc": "candidateJson.bsc.name"}')
////                '               \'                "cn_latitude": "candidateJson.latitude",\\n\' +\n' +
////                '               \'                "cn_longitude": "candidateJson.longitude",\\n\' +\n' +
////                '               \'                "cn_altidude":"cn_altidude",\\n\' +\n' +
////                '               \'                "cn_height_constr": "cellAntennaJson.cn_height_gsm",\\n\' +\n' +
////                '               \'                "sysdate": "new Date()",\\n\' +\n' +
////                '               \'                "cn_date_visit": "candidateJson.dateOfVisit",\\n\' +\n' +
////                '               \'                "ncp_band":"ncp_band",\\n\' +\n' +
////                '               \'                "ncp_rbs_type": "candidateJson.rbsType",\\n\' +\n' +
////                '               \'                "cn_radio_unit": "cn_radio_unit",\\n\' +\n' +
////                '               \'                "cn_wcdma_carrier": "cellAntennaJson.cn_wcdma_carrier",\\n\' +\n' +
////                '               \'                "cn_trx": "cellAntennaJson.cn_trx",\\n\' +\n' +
////                '               \'                "cn_du":"cn_du",\\n\' +\n' +
////                '               \'                "sector_cell_antenna":"sector_cell_antenna",\\n\' +\n' +
////                '               \'                "cn_antenna_loc": "cn_antenna_loc",\\n\' +\n' +
////                '               \'                "cn_tilt_mech_gsm": "cn_tilt_mech_gsm",\\n\' +\n' +
////                '               \'                "cn_tilt_electr_gsm": "cellAntennaJson.cn_tilt_electr_gsmv",\\n\' +\n' +
////                '               \'                "cn_tilt_mech_lte": "cellAntennaJson.cn_tilt_mech_lte",\\n\' +\n' +
////                '               \'                "cn_tilt_electr_lte": "cellAntennaJson.cn_tilt_electr_lte",\\n\' +\n' +
////                '               \'                "cn_direction_gsm": "cellAntennaJson.cn_direction_gsm",\\n\' +\n' +
////                '               \'                "cn_height_gsm": "cellAntennaJson.cn_height_gsm",\\n\' +\n' +
////                '               \'                "cn_height_lte": "cellAntennaJson.cn_height_lte",\\n\' +\n' +
////                '               \'                "cn_duplex_gsm": "cellAntennaJson.cn_duplex_gsm",\\n\' +\n' +
////                '               \'                "cn_diversity": "cellAntennaJson.cn_diversity",\\n\' +\n' +
////                '               \'                "cn_power_splitter": "cellAntennaJson.cn_power_splitter",\\n\' +\n' +
////                '               \'                "cn_hcu": "cellAntennaJson.cn_hcu",\\n\' +\n' +
////                '               \'                "cn_ret": "cellAntennaJson.cn_ret",\\n\' +\n' +
////                '               \'                "cn_asc": "cellAntennaJson.cn_asc",\\n\' +\n' +
////                '               \'                "cn_tma_gsm": "cellAntennaJson.cn_tma_gsm",\\n\' +\n' +
////                '               \'                "cn_tcc": "cellAntennaJson.cn_tcc",\\n\' +\n' +
////                '               \'                "cn_gsm_range": "cellAntennaJson.cn_gsm_range",\\n\' +\n' +
////                '               \'                "cn_name_contact_person": "renterCompanyJson.contactName",\\n\' +\n' +
////                '               \'                "cn_lastname_contact_person": "renterCompanyJson.firstLeaderName",\\n\' +\n' +
////                '               \'                "cn_position_contact_person": "renterCompanyJson.contactPosition",\\n\' +\n' +
////                '               \'                "cn_contact_information": "renterCompanyJson.contactInfo",\\n\' +\n' +
////                '               \'                "cn_comments": "candidateJson.comments",\\n\' +\n' +
////                '               \'                "cn_addr_district": "candidateJson.address.cn_addr_district",\\n\' +\n' +
////                '               \'                "cn_addr_oblast": "candidateJson.address.cn_addr_oblast",\\n\' +\n' +
////                '               \'                "cn_addr_city": "candidateJson.address.cn_addr_city",\\n\' +\n' +
////                '               \'                "cn_addr_street": "candidateJson.address.cn_addr_street",\\n\' +\n' +
////                '               \'                "cn_addr_building": "candidateJson.address.cn_addr_building",\\n\' +\n' +
////                '               \'                "cn_addr_cadastral_number": "candidateJson.address.cn_addr_cadastral_number",\\n\' +\n' +
////                '               \'                "cn_addr_note": "candidateJson.address.cn_addr_note"}')
//
//
//
//
//        def result = gest(object)
////        println(result)
//    }

    static String render (Map binding ) {
        println(binding)
        def template = '''\
            
            yieldUnescaped '<!DOCTYPE html>\'
            html(lang:'en') {
                head {
                    meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
                    title('My page')
                }
                newLine()
                style ('td { padding: 10px; }')
                newLine()
                body {
                    table(class:"table", border:"1") {
                        tr {
                            td (width:"15%", style:"text-align: center;", rowspan: "5","KCELL")
                            td (width:"85%", style:"text-align: center;", colspan:"4", "ICT Department, K’Cell")
                        }
                        tr {
                            td (width:"85%", style:"text-align: center; font-weight: bold;", colspan:"4", "RADIO  SURVEY  DATA")
                        }
                        tr {
                            td (width:"25%", style:"font-weight: bold;","No.:")
                            td (width:"20%", style:"font-weight: bold;","Rev.No.:")
                            td (width:"20%", style:"font-weight: bold;","Rev.Date:")
                            td (width:"20%", style:"font-weight: bold;","Page:")
                        }
                        tr {
                            td (width:"25%","ICTD-RND-OCU-STSG-Fm-14")
                            td (width:"20%","02")
                            td (width:"20%","01/01/20")
                            td (width:"20%","1 of 1")
                        }
                        tr {
                            td (width:"25%","In Attention of:")
                            td (width:"20%","Approved By:")
                            td (width:"20%","Reviewed By:")
                            td (width:"20%","Prepared By:")
                        }
                        tr {
                            td (width:"25%", style:"font-weight: bold;" ,"External & Internal Use")
                            td (width:"20%","All Division Employees, Contractors")
                            td (width:"20%","Department Director")
                            td (width:"20%","Manager")
                            td (width:"20%","A. Medvedev")
                        }
                    }
                    newLine()
                    p('&nbsp;')
                    newLine()
                   table(class:"table") {

                        tr {
                            td (width:"20%", style:"font-weight: bold; border:1px solid", "City Name:")
                            td (width:"23%", colspan:"2", style:"border:1px solid", cn_addr_city)
                            td (width:"7%", style:"border-bottom:1;border-top:none; vertical-align:top;")
                            td (width:"7%", style:"border-bottom:1;border-top:none; border-left:none; vertical-align:top;")
                            td (width:"20%", style:"font-weight: bold;border:1px solid", "Date:")
                            td (width:"23%", style:"border:1px solid", new Date())
                        }
                        tr {
                            td (width:"20%",style:"font-weight: bold;border:1px solid", "Site Name:")
                            td (width:"23%",colspan:"2", style:"border:1px solid", cn_sitename)
                            td (width:"7%",style:"vertical-align:top;")
                            td (width:"7%", style:"vertical-align:top;")
                            td (width:"20%",style:"font-weight: bold;border:1px solid", "Visiting Date:")
                            td (width:"23%",style:"border:1px solid",cn_date_visit)
                        }
                        tr {
                            td (width:"20%",style:"font-weight: bold;border:1px solid", "BSC/RNC Name:")
                            td (width:"23%",colspan:"2", style:"border:1px solid", cn_bsc)
                            td (width:"7%",style:"vertical-align:top;")
                            td (width:"7%", style:"vertical-align:top;")
                            td (width:"20%",style:"font-weight: bold;border:1px solid", "Planning Engineer:")
                            td (width:"23%",style:"border:1px solid","Regional planning engineer")
                        }
                        tr {
                            td (width:"20%",style:"font-weight: bold;border:1px solid", "Coordinates:")
                            td (width:"11.5%", style:"border:1px solid", "N "+cn_long)
                            td (width:"11.5%", style:"border:1px solid", "E "+cn_lat)
                            td (width:"7%",style:"vertical-align:top;")
                            td (width:"7%", style:"vertical-align:top;")
                            td (width:"20%", style:"font-weight: bold; border:1px solid", "GSM-WCDMA BandGSM-WCDMA Band:")
                            td (width:"23%", style:"border:1px solid", ncp_band)
                        }
                        tr {
                            td (width:"20%", style:"font-weight: bold;border:1px solid", "Altitude, m.:")
                            td (width:"23%", colspan:"2", style:"border:1px solid",cn_altidude)
                            td (width:"7%", style:"vertical-align:top;")
                            td (width:"7%", style:"vertical-align:top;" )
                            td (width:"20%", style:"vertical-align:top;" )
                            td (width:"23%", style:"vertical-align:top;" )
                        }
                        tr {
                            td (width:"20%", style:"font-weight: bold;border:1px solid", "Height Of Construction, m:")
                            td (width:"23%", colspan:"2", style:"border:1px solid", cn_height_constr)
                            td (width:"7%", style:" vertical-align:top;")
                            td (width:"7%", style:"border-top:none; vertical-align:top;")
                            td (width:"20%", style:"border-top:none; vertical-align:top;")
                            td (width:"23%", style:"border-top:none; vertical-align:top;")
                        }
                        tr (style:"border-bottom:none"){
                            td (width:"20%", style:"font-weight: bold;border:1px solid", "Total # Of Cabinets:")
                            td (width:"23%", colspan:"2", style:"border:1px solid", "1")
                            td (width:"7%", style:"border-bottom:none; border-right:none solid black; border-top:none; vertical-align:top;")
                            td (width:"7%", style:"border-top:none; vertical-align:top;" )
                            td (width:"20%", style:"border-top:none; vertical-align:top;" )
                            td (width:"23%", style:"border:none vertical-align:top;" )
                        }
                    }
                    p('&nbsp;')
                def cycles = 3;
                def a = cycles % 3
                def cycleCount = 1
                if (a > 0) {
                    cycleCount = (int) (cycles/3)+1;
                } else {
                     cycleCount = (int) cycles/3;
                }
                for(int i = 1;i <= cycleCount;i++){
                        p('&nbsp;')
                        table(class:"table", border:"1") {
                            tr {
                                td (width:"25%",style:"background-color:#8b8e94;font-weight: bold",colspan:"2", "Sector №")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%",style:"background-color:#8b8e94", i*j)

                                    } else {
                                        td (width:"25%",style:"background-color:#8b8e94", i*j)
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Site type")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", " " )

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "RBS №")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", " " )

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "RBS Type")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", data[i*j-1].ncp_rbs_type)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "GSM-WCDMA Band")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", data[i*j-1].ncp_band)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Radio Unit type (RU)")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", data[i*j-1].ncp_band)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Carrier")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", data[i*j-1].cn_wcdma_carrier)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Required TRX")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", data[i*j-1].cn_trx)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "DU type")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", data[i*j-1].cn_du)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"15%", rowspan:"8", "Antenna Type")
                                td (style:"font-weight: bold;",width:"10%", "G900")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", data[i*j-1].antennaGSM900 )

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                
                                td (style:"font-weight: bold;",width:"10%", "G1800")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", data[i*j-1].antennaGSM1800 )

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"10%", "U900")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", data[i*j-1].antennaU900 )

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"10%", "U2100")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", data[i*j-1].antennaU2100 )

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"10%", "L800")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", data[i*j-1].antennaLTE800 )

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"10%", "L1800")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", data[i*j-1].antennaLTE1800 )

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"10%", "L2100")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", data[i*j-1].antennaLTE2100 )

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"10%", "L2600")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", data[i*j-1].antennaLTE2600 )

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Antenna Quantity")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", " " )

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Antenna Location")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", data[i*j-1].cn_antenna_loc)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Tilt [deg] mechanical 900/1800/WCDMA")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", data[i*j-1].cn_tilt_mech_gsm)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Tilt [deg] electrical 900/1800/WCDMA ")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", data[i*j-1].cn_tilt_electr_gsm)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Tilt [deg] mechanical LTE 800/1800/2100")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", data[i*j-1].cn_tilt_mech_lte)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Tilt [deg] electrical LTE 800/1800/2100")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", data[i*j-1].cn_tilt_electr_lte)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Direction [deg]")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", data[i*j-1].cn_direction_gsm)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Height (top of ant) [m]")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", "GSM "+data[i*j-1].cn_height_gsm +"/ LTE "+data[i*j-1].cn_height_lte)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Duplex Filter")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", data[i*j-1].cn_duplex_gsm)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Diversity")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", data[i*j-1].cn_diversity)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Power Splitter")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", data[i*j-1].cn_power_splitter)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "HCU")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", data[i*j-1].cn_hcu)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "RET")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", data[i*j-1].cn_ret)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "ASC")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", data[i*j-1].cn_asc)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "TMA")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", data[i*j-1].cn_tma_gsm)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%", colspan:"2", "TCC")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", data[i*j-1].cn_tcc)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Extended range")
                                for(int j =1;j<=3;j++){
                                    if(i*j-1<=cycles){
                                        td (width:"25%", data[i*j-1].cn_gsm_range)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                        }            
                    }
            p('&nbsp;')
            p('&nbsp;')
    
        table(class:"table", border:"1") {
            tr {
                td (width:"20%", style:"font-weigth:bold", "Address of Site:")
                td (width:"80%",  ca_addr)
            }
            tr {
                td (width:"20%", style:"font-weigth:bold", "Contact Person:")
                td (width:"80%",  cn_name_contact_person +", " +cn_lastname_contact_person  +", " + cn_position_contact_person  +", " + cn_contact_information)
            }
            tr {
                td (width:"20%", style:"font-weigth:bold", "Comments:")
                td (width:"80%",  cn_comments)
            }
            tr {
                td (width:"20%", style:"font-weigth:bold", "Planning Target")
                td (width:"80%",  " ")
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
        def pid = delegateTask.getProcessInstanceId()
        def tid = delegateTask.id
        def tAssignee = delegateTask.assignee

        def candidateJson = new JsonSlurper().parseText(execution.getVariable('candidate').toString());
        def cellAntennaJson = new JsonSlurper().parseText(execution.getVariable('cellAntenna').toString());
        def renterCompanyJson = new JsonSlurper().parseText(execution.getVariable('renterCompany').toString());
        def address = new JsonSlurper().parseText(execution.getVariable('address').toString());
        def data = [];
        def sectorsArr = cellAntennaJson.sectors
        def cycle = sectorsArr.size()
//        def String[] antennaNames = ['G900', 'G1800', 'U900', 'U2100', 'L800', 'L1800','L2100', 'L2600']
        ArrayList<String> antennaNames = new ArrayList<String>();
        antennaNames.add("GSM900");
        antennaNames.add("GSM1800");
        antennaNames.add("U900");
        antennaNames.add("U2100");
        antennaNames.add("LTE800");
        antennaNames.add("LTE1800");
        antennaNames.add("LTE2100");
        antennaNames.add("LTE2600");


        def antennaGSM900;
        def antennaGSM1800;
        def antennaU900;
        def antennaU2100;
        def antennaLTE800;
        def antennaLTE1800;
        def antennaLTE2100;
        def antennaLTE2600;
        println(candidateJson);
        println(cellAntennaJson);
        println(renterCompanyJson);
        for (int j =0;j<cycle;j++){
            def antennaItem = sectorsArr[j].antennas[0].antennaType
            for(String item : antennaNames){
                if(antennaItem.containsKey("GSM900")){
                    antennaGSM900 = antennaItem['GSM900']
                } else {
                    antennaGSM900 = ""
                }
                if(antennaItem.containsKey("GSM1800")){
                    antennaGSM1800 = antennaItem['GSM1800']
                } else {
                    antennaGSM1800 = ""
                }
                if(antennaItem.containsKey("U900")){
                    antennaU900 = antennaItem['U900']
                } else {
                    antennaU900 = ""
                }
                if(antennaItem.containsKey("U2100")){
                    antennaU2100 = antennaItem['U2100']
                } else {
                    antennaU2100 = ""
                }
                if(antennaItem.containsKey("LTE800")){
                    antennaLTE800 = antennaItem['LTE800']
                } else {
                    antennaLTE800 = ""
                }
                if(antennaItem.containsKey("LTE1800")){
                    antennaLTE1800 = antennaItem['LTE1800']
                } else {
                    antennaLTE1800 = ""
                }
                if(antennaItem.containsKey("LTE2100")){
                    antennaLTE2100 = antennaItem['LTE2100']
                } else {
                    antennaLTE2100 = ""
                }
                if(antennaItem.containsKey("LTE2600")){
                    antennaLTE2600 = antennaItem['LTE2600']
                } else {
                    antennaLTE2600 = ""
                }

            }
            // }
//        println(sectorsArr)
            def test = [
                    "cn_addr_city": cellAntennaJson.address.cn_addr_city,
                    "cn_sitename": candidateJson.siteName,
                    "cn_bsc": candidateJson.bsc.name,
                    "cn_latitude": candidateJson.latitude,
                    "cn_longitude": candidateJson.longitude,
                    "cn_altidude":"",
                    "cn_height_constr": cellAntennaJson.cn_height_gsm,
                    "sysdate": new Date(),
                    "cn_date_visit": candidateJson.dateOfVisit,
                    "ncp_band":"ncp_band",
                    "ncp_rbs_type": candidateJson.rbsType,
                    "cn_radio_unit": "cn_radio_unit",
                    "cn_wcdma_carrier": cellAntennaJson.cn_wcdma_carrier,
                    "cn_trx": cellAntennaJson.cn_trx,
                    "cn_du":"cn_du",
                    "sector_cell_antenna":"sector_cell_antenna",
                    "cn_antenna_loc": "cn_antenna_loc",
                    "cn_tilt_mech_gsm": "cn_tilt_mech_gsm",
                    "cn_tilt_electr_gsm": cellAntennaJson.cn_tilt_electr_gsmv,
                    "cn_tilt_mech_lte": cellAntennaJson.cn_tilt_mech_lte,
                    "cn_tilt_electr_lte": cellAntennaJson.cn_tilt_electr_lte,
                    "cn_direction_gsm": cellAntennaJson.cn_direction_gsm,
                    "cn_height_gsm": cellAntennaJson.cn_height_gsm,
                    "cn_height_lte": cellAntennaJson.cn_height_lte,
                    "cn_duplex_gsm": cellAntennaJson.cn_duplex_gsm,
                    "cn_diversity": cellAntennaJson.cn_diversity,
                    "cn_power_splitter": cellAntennaJson.cn_power_splitter,
                    "cn_hcu": cellAntennaJson.cn_hcu,
                    "cn_ret": cellAntennaJson.cn_ret,
                    "cn_asc": cellAntennaJson.cn_asc,
                    "cn_tma_gsm": cellAntennaJson.cn_tma_gsm,
                    "cn_tcc": cellAntennaJson.cn_tcc,
                    "cn_gsm_range": cellAntennaJson.cn_gsm_range,
                    "cn_name_contact_person": renterCompanyJson.contactName,
                    "cn_lastname_contact_person": renterCompanyJson.firstLeaderName,
                    "cn_position_contact_person": renterCompanyJson.contactPosition,
                    "cn_contact_information": renterCompanyJson.contactInfo,
                    "cn_comments": candidateJson.comments,
                    "cn_addr_district": address.cn_addr_district,
                    "cn_addr_oblast": address.cn_addr_oblast,
                    "cn_addr_city": address.cn_addr_city,
                    "cn_addr_street": address.cn_addr_street,
                    "cn_addr_building": address.cn_addr_building,
                    "cn_addr_cadastral_number": address.cn_addr_cadastral_number,
                    "cn_addr_note": address.cn_addr_note,
                    "antennaGSM900": antennaGSM900,
                    "antennaGSM1800": antennaGSM1800,
                    "antennaU900": antennaU900,
                    "antennaU2100": antennaU2100,
                    "antennaLTE800": antennaLTE800,
                    "antennaLTE1800": antennaLTE1800,
                    "antennaLTE2100": antennaLTE2100,
                    "antennaLTE2600": antennaLTE2600,
            ]
            data.add(test)
        }

        def binding = [
                "data":data,
                "cycles":cycle,
        ]

        def result = render(binding)
        // if (checkingByGuestResult && checkingByGuestResult == "approved") {
        //     execution.setVariable("acceptanceDate", taskSubmitDate)
        // }
        print (result);

        InputStream is = new ByteArrayInputStream(result.getBytes());
        def path = pid + "/" + tid + "/createdRSDFile.doc"
        minioClient.saveFile(path, is, "application/msword");

        JSONArray createdRSDFiles = new JSONArray();
        JSONObject createdRSDFile = new JSONObject();
        createdRSDFile.put("date", new Date().getTime());
        createdRSDFile.put("author", tAssignee);
        createdRSDFile.put("name", "createdRSDFile.doc");
        createdRSDFile.put("path", path);
        createdRSDFiles.put(createdRSDFile);
        execution.setVariable("createdRSDFile", SpinValues.jsonValue(createdRSDFiles.toString()))
    }
}
