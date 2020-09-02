package kz.kcell.flow.delegate

import groovy.json.JsonSlurper
import groovy.json.internal.LazyMap
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import kz.kcell.flow.files.Minio
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.bpm.engine.impl.util.json.JSONArray
import org.camunda.bpm.engine.impl.util.json.JSONObject
import org.camunda.spin.plugin.variable.SpinValues
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.text.DateFormat
import java.text.SimpleDateFormat

@Service("GenerateLeasingRSD")
class GenerateLeasingRSD implements ExecutionListener {
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
        // println(binding)
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
                    table(class:"table", style:"font-size:12px;", border:"1") {
                        tr {
                            td (width:"15%", style:"text-align: center;", rowspan: "5")
                                img(src: "data:image/png;base64,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")
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
                            td (width:"25%", style:"font-weight: bold;", "In Attention of:")
                            td (width:"20%", style:"font-weight: bold;", "Approved By:")
                            td (width:"20%", style:"font-weight: bold;", "Reviewed By:")
                            td (width:"20%", style:"font-weight: bold;", "Prepared By:")
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
                    table(class:"table", style:"font-size:12px;") {
                        tr {
                            td (width:"20%", style:"font-weight: bold; border:1px solid", "City Name:")
                            td (width:"23%", colspan:"2", style:"border:1px solid", data[0].cn_addr_city)
                            td (width:"7%", style:"border-bottom:1;border-top:none; vertical-align:top;")
                            td (width:"7%", style:"border-bottom:1;border-top:none; border-left:none; vertical-align:top;")
                            td (width:"20%", style:"font-weight: bold;border:1px solid", "Date:")
                            td (width:"23%", style:"border:1px solid", new Date())
                        }
                        tr {
                            td (width:"20%",style:"font-weight: bold;border:1px solid", "Site Name:")
                            td (width:"23%",colspan:"2", style:"border:1px solid", data[0].cn_sitename)
                            td (width:"7%",style:"vertical-align:top;")
                            td (width:"7%", style:"vertical-align:top;")
                            td (width:"20%",style:"font-weight: bold;border:1px solid", "Visiting Date:")
                            td (width:"23%",style:"border:1px solid",data[0].cn_date_visit)
                        }
                        tr {
                            td (width:"20%",style:"font-weight: bold;border:1px solid", "BSC/RNC Name:")
                            td (width:"23%",colspan:"2", style:"border:1px solid", data[0].cn_bsc)
                            td (width:"7%",style:"vertical-align:top;")
                            td (width:"7%", style:"vertical-align:top;")
                            td (width:"20%",style:"font-weight: bold;border:1px solid", "Planning Engineer:")
                            td (width:"23%",style:"border:1px solid","Regional planning engineer")
                        }
                        tr {
                            td (width:"20%",style:"font-weight: bold;border:1px solid", "Coordinates:")
                            td (width:"11.5%", style:"border:1px solid", "N "+data[0].cn_longitude)
                            td (width:"11.5%", style:"border:1px solid", "E "+data[0].cn_latitude)
                            td (width:"7%",style:"vertical-align:top;")
                            td (width:"7%", style:"vertical-align:top;")
                            td (width:"20%", style:"font-weight: bold; border:1px solid", "GSM-WCDMA Band:")
                            td (width:"23%", style:"border:1px solid", data[0].ncp_band)
                        }
                        tr {
                            td (width:"20%", style:"font-weight: bold;border:1px solid", "Altitude, m.:")
                            td (width:"23%", colspan:"2", style:"border:1px solid", data[0].cn_altidude)
                            td (width:"7%", style:"vertical-align:top;")
                            td (width:"7%", style:"vertical-align:top;" )
                            td (width:"20%", style:"vertical-align:top;" )
                            td (width:"23%", style:"vertical-align:top;" )
                        }
                        tr {
                            td (width:"20%", style:"font-weight: bold;border:1px solid", "Height Of Construction, m:")
                            td (width:"23%", colspan:"2", style:"border:1px solid", data[0].cn_height_constr)
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
                        tr (style:"border-bottom:none"){
                            td (width:"20%", style:"font-weight: bold;border:1px solid", "Site type:")
                            td (width:"23%", colspan:"2", style:"border:1px solid", data[0].siteType)
                            td (width:"7%", style:"border-bottom:none; border-right:none solid black; border-top:none; vertical-align:top;")
                            td (width:"7%", style:"border-top:none; vertical-align:top;" )
                            td (width:"20%", style:"border-top:none; vertical-align:top;" )
                            td (width:"23%", style:"border:none vertical-align:top;" )
                        }
                    }
                def cycles = cycles;
                def a = cycles % 3
                def cycleCount = 1
                if (a > 0) {
                    cycleCount = (int) (cycles/3)+1;
                } else {
                     cycleCount = (int) cycles/3;
                }
                for(int i = 1;i <= cycleCount;i++){
                        table(class:"table", style:"font-size:12px;", border:"1") {
                            tr {
                                td (width:"25%",style:"background-color:#8b8e94;font-weight: bold",colspan:"2", "Sector №")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%",style:"background-color:#8b8e94", indx + 1)

                                    } else {
                                        td (width:"25%",style:"background-color:#8b8e94", indx + 1)
                                    }
                                    }else {
                                        td (width:"25%",style:"background-color:#8b8e94", indx + 1)
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Site type")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", " " )

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "RBS №")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", " " )

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "RBS Type")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", data[indx].ncp_rbs_type)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "GSM-WCDMA Band")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", data[indx].ncp_band)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Radio Unit type (RU)")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", data[indx].cn_radio_unit)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Carrier")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", data[indx].cn_wcdma_carrier)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Required TRX")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", data[indx].cn_trx)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "DU type")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", data[indx].cn_du)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"15%", rowspan:"8", "Antenna Type")
                                td (style:"font-weight: bold;",width:"10%", "G900")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", data[indx].antennaGSM900 )

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                
                                td (style:"font-weight: bold;",width:"10%", "G1800")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", data[indx].antennaGSM1800 )

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"10%", "U900")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", data[indx].antennaU900 )

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"10%", "U2100")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", data[indx].antennaU2100 )

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"10%", "L800")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", data[indx].antennaLTE800 )

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"10%", "L1800")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", data[indx].antennaLTE1800 )

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"10%", "L2100")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", data[indx].antennaLTE2100 )

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"10%", "L2600")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", data[indx].antennaLTE2600 )

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Antenna Quantity")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", data[indx].antennaQuantity )

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Antenna Location")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", data[indx].cn_antenna_loc)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Tilt [deg] mechanical 900/1800/WCDMA")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", data[indx].cn_tilt_mech_gsm)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Tilt [deg] electrical 900/1800/WCDMA ")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", data[indx].cn_tilt_electr_gsm)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Tilt [deg] mechanical LTE 800/1800/2100")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", data[indx].cn_tilt_mech_lte)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Tilt [deg] electrical LTE 800/1800/2100")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", data[indx].cn_tilt_electr_lte)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Direction [deg]")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", data[indx].cn_direction_gsm)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Height (top of ant) [m]")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", "GSM "+data[indx].cn_height_gsm +"/ LTE "+data[indx].cn_height_lte)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Duplex Filter")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", data[indx].cn_duplex_gsm)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Diversity")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", data[indx].cn_diversity)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Power Splitter")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", data[indx].cn_power_splitter)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "HCU")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", data[indx].cn_hcu)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "RET")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", data[indx].cn_ret)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "ASC")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", data[indx].cn_asc)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "TMA")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", data[indx].cn_tma_gsm)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%", colspan:"2", "TCC")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", data[indx].cn_tcc)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                            tr {
                                td (style:"font-weight: bold;",width:"25%",colspan:"2", "Extended range")
                                for(int j =1;j<=3;j++){
                                    int indx = (i-1)*3+(j-1)
                                    if(indx<=data.size()){

                                    if(indx<cycles){
                                        td (width:"25%", data[indx].cn_gsm_range)

                                    } else {
                                        td (width:"25%", "")
                                    }
                                    }else {
                                        td (width:"25%", "")
                                    }
                                }
                            }
                        }  
                        newLine()
          
                    }
                newLine()
        table(class:"table", style:"font-size:12px;", border:"1") {
            tr {
                td (width:"20%", style:"font-weight:bold", "Address of Site:")
                td (width:"80%", data[0].cn_addr_district + ", " + data[0].cn_addr_oblast + ", " + data[0].cn_addr_city + ", " + data[0].cn_addr_street + ", " + data[0].cn_addr_building)
            }
            tr {
                td (width:"20%", style:"font-weight:bold", "Contact Person:")
                td (width:"80%", data[0].cn_name_contact_person +", " +data[0].cn_lastname_contact_person  +", " + data[0].cn_position_contact_person  +", " + data[0].cn_contact_information)
            }
            tr {
                td (width:"20%", style:"font-weight:bold", "Comments:")
                td (width:"80%", data[0].cn_comments)
            }
            tr {
                td (width:"20%", style:"font-weight:bold", "Planning Target")
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
    public void notify(DelegateExecution delegateExecution) {


        def execution = delegateExecution
        def pid = delegateExecution.getProcessInstanceId()

        def bandJson = new JsonSlurper().parseText(execution.getVariable('bands').toString());

        def bandList = "";
        for(int l=0;l<bandJson.size();l++){
            if(bandJson[l].value == true){
                bandList += bandJson[l].title + ", "
            }
        }

        def candidateJson = new JsonSlurper().parseText(execution.getVariable('candidate').toString());
        def siteTypeJson = new JsonSlurper().parseText(execution.getVariable('siteType').toString());
        def cellAntennaJson = new JsonSlurper().parseText(execution.getVariable('cellAntenna').toString());
        def renterCompanyJson = new JsonSlurper().parseText(execution.getVariable('renterCompany').toString());
        def address = new JsonSlurper().parseText(execution.getVariable('address').toString());
        def data = [];
        def sectorsArr = cellAntennaJson.sectors
        def cycle = sectorsArr.size()
//        def String[] antennaNames = ['G900', 'G1800', 'U900', 'U2100', 'L800', 'L1800','L2100', 'L2600']
        ArrayList<String> antennaNames = new ArrayList<String>();
        antennaNames.add("G900");
        antennaNames.add("G1800");
        antennaNames.add("U900");
        antennaNames.add("U2100");
        antennaNames.add("L800");
        antennaNames.add("L1800");
        antennaNames.add("L2100");
        antennaNames.add("L2600");

        for (int j =0;j<cycle;j++){
            def antennaLocation = sectorsArr[j].antennas[0].cn_antenna_loc
            def antennaQuantity = 0;
            def antennaGSM900 = "";
            def antennaGSM1800 = "";
            def antennaU900 = "";
            def antennaU2100 = "";
            def antennaLTE800 = "";
            def antennaLTE1800 = "";
            def antennaLTE2100 = "";
            def antennaLTE2600 = "";

            def cn_tilt_mech_gsm = "";
            def cn_tilt_electr_gsm = "";
            def cn_tilt_mech_lte = "";
            def cn_tilt_electr_lte = "";
            def cn_direction_gsm = "";
            def cn_height_gsm = "";
            def cn_height_lte = "";
            def cn_duplex_gsm = "";
            def cn_diversity = "";
            def cn_power_splitter = "";
            def cn_hcu = "";
            def cn_ret = "";
            def cn_asc = "";
            def cn_tma_gsm = "";
            def cn_tcc = "";
            def cn_gsm_range = "";
            
            for(int k=0;k<sectorsArr[j].antennas.size();k++){
            antennaQuantity += sectorsArr[j].antennas[k].quantity
            def antennaItem = sectorsArr[j].antennas[k].antennaType
            def antennaName = sectorsArr[j].antennas[k].antennaName



            cn_tilt_mech_gsm = sectorsArr[j].square ? sectorsArr[j].square : "";
            cn_tilt_electr_gsm = sectorsArr[j].cn_tilt_electr_gsm ? sectorsArr[j].cn_tilt_electr_gsm : "";
            cn_tilt_mech_lte = sectorsArr[j].cn_tilt_mech_lte ? sectorsArr[j].cn_tilt_mech_lte : "";
            cn_tilt_electr_lte = sectorsArr[j].cn_tilt_electr_lte ? sectorsArr[j].cn_tilt_electr_lte : "";
            cn_direction_gsm = sectorsArr[j].cn_direction_gsm ? sectorsArr[j].cn_direction_gsm : "";
            cn_height_gsm = sectorsArr[j].cn_height_gsm ? sectorsArr[j].cn_height_gsm : "";
            cn_height_lte = sectorsArr[j].cn_height_lte ? sectorsArr[j].cn_height_lte : "";
            cn_duplex_gsm = sectorsArr[j].cn_duplex_gsm ? sectorsArr[j].cn_duplex_gsm : "";
            cn_diversity = sectorsArr[j].cn_diversity ? sectorsArr[j].cn_diversity : "";
            cn_power_splitter = sectorsArr[j].cn_power_splitter ? sectorsArr[j].cn_power_splitter : "";
            cn_hcu = sectorsArr[j].cn_hcu ? sectorsArr[j].cn_hcu : "";
            cn_ret = sectorsArr[j].cn_ret ? sectorsArr[j].cn_ret : "";
            cn_asc = sectorsArr[j].cn_asc ? sectorsArr[j].cn_asc : "";
            cn_tma_gsm = sectorsArr[j].cn_tma_gsm ? sectorsArr[j].cn_tma_gsm : "";
            cn_tcc = sectorsArr[j].cn_tcc ? sectorsArr[j].cn_tcc : "";
            cn_gsm_range = sectorsArr[j].cn_gsm_range ? sectorsArr[j].cn_gsm_range : "";
//            def cn_diversity = sectorsArr[j].cn_diversity ? sectorsArr[j].cn_diversity : "";


            for(String item : antennaNames){
                if(antennaItem == "G900"){
                    antennaGSM900 = antennaName
                } 
                if(antennaItem == "G1800"){
                    antennaGSM1800 = antennaName
                } 
                if(antennaItem == "U900"){
                    antennaU900 = antennaName
                } 
                if(antennaItem == "U2100"){
                    antennaU2100 = antennaName
                } 
                if(antennaItem == "L800"){
                    antennaLTE800 = antennaName
                } 
                if(antennaItem == "L1800"){
                    antennaLTE1800 = antennaName
                } 
                if(antennaItem == "L2100"){
                    antennaLTE2100 = antennaName
                } 
                if(antennaItem == "L2600"){
                    antennaLTE2600 = antennaName
                }
            }
            }
            
            def test = [
                    "cn_addr_city": cellAntennaJson.address ? cellAntennaJson.address.cn_addr_city : '',
                    "cn_sitename": candidateJson.siteName ? candidateJson.siteName : '',
                    "cn_bsc": candidateJson.bsc ?  (candidateJson.bsc instanceof LazyMap? candidateJson.bsc.name : candidateJson.bsc) : '',
                    "cn_latitude": candidateJson.latitude ? candidateJson.latitude : '',
                    "cn_longitude": candidateJson.longitude ? candidateJson.longitude : '',
                    "cn_altidude": candidateJson.cn_altitude ? candidateJson.cn_altitude : '',
                    "cn_height_constr": candidateJson.cn_height_constr ? candidateJson.cn_height_constr : '',
                    "sysdate": new Date(),
                    "cn_date_visit": candidateJson.dateOfVisit ? candidateJson.dateOfVisit : '',
                    "ncp_band":bandList ? bandList : '',
                    "ncp_rbs_type": candidateJson.rbsType ? candidateJson.rbsType : '',
                    "cn_radio_unit": cellAntennaJson.cn_radio_unit ? cellAntennaJson.cn_radio_unit : '',
                    "cn_wcdma_carrier": cellAntennaJson.cn_wcdma_carrier ? cellAntennaJson.cn_wcdma_carrier : '',
                    "cn_trx": cellAntennaJson.cn_trx ? cellAntennaJson.cn_trx : '',
                    "cn_du": cellAntennaJson.cn_du ? cellAntennaJson.cn_du : '',
                    "sector_cell_antenna":"sector_cell_antenna",
                    "cn_antenna_loc": antennaLocation ? antennaLocation : '',
                    "cn_tilt_mech_gsm": cn_tilt_mech_gsm,
                    "cn_tilt_electr_gsm": cn_tilt_electr_gsm,
                    "cn_tilt_mech_lte": cn_tilt_mech_lte,
                    "cn_tilt_electr_lte": cn_tilt_electr_lte,
                    "cn_direction_gsm": cn_direction_gsm,
                    "cn_height_gsm": cn_height_gsm,
                    "cn_height_lte": cn_height_lte,
                    "cn_duplex_gsm": cn_duplex_gsm,
                    "cn_diversity": cn_diversity,
                    "siteType": siteTypeJson.name ? siteTypeJson.name : '',
                    "cn_power_splitter": cn_power_splitter,
                    "cn_hcu": cn_hcu,
                    "cn_ret": cn_ret,
                    "cn_asc": cn_asc,
                    "cn_tma_gsm": cn_tma_gsm,
                    "cn_tcc": cn_tcc,
                    "cn_gsm_range": cn_gsm_range,
                    "cn_name_contact_person": renterCompanyJson.contactName ? renterCompanyJson.contactName : '',
                    "cn_lastname_contact_person": renterCompanyJson.firstLeaderName ? renterCompanyJson.firstLeaderName : '',
                    "cn_position_contact_person": renterCompanyJson.contactPosition ? renterCompanyJson.contactPosition : '',
                    "cn_contact_information": renterCompanyJson.contactInfo ? renterCompanyJson.contactInfo : '',
                    "cn_comments": candidateJson.comments ? candidateJson.comments : '',
                    "cn_addr_district": address.cn_addr_district ? address.cn_addr_district : '',
                    "cn_addr_oblast": address.cn_addr_oblast ? address.cn_addr_oblast : '',
                    "cn_addr_city": address.cn_addr_city ? address.cn_addr_city : '',
                    "cn_addr_street": address.cn_addr_street ? address.cn_addr_street : '',
                    "cn_addr_building": address.cn_addr_building ? address.cn_addr_building : '',
                    "cn_addr_cadastral_number": address.cn_addr_cadastral_number ? address.cn_addr_cadastral_number : '',
                    "cn_addr_note": address.cn_addr_note ? address.cn_addr_note : '',
                    "antennaGSM900": antennaGSM900,
                    "antennaGSM1800": antennaGSM1800,
                    "antennaU900": antennaU900,
                    "antennaU2100": antennaU2100,
                    "antennaLTE800": antennaLTE800,
                    "antennaLTE1800": antennaLTE1800,
                    "antennaLTE2100": antennaLTE2100,
                    "antennaLTE2600": antennaLTE2600,
                    "antennaQuantity": antennaQuantity
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
        def path = pid + "/createdRSDFile.doc"
        minioClient.saveFile(path, is, "application/msword");

        JSONArray createdRSDFiles = new JSONArray();
        JSONObject createdRSDFile = new JSONObject();
        def regionCode = execution.getVariable('regionCode').toString();
        def regionStr = "";
        if (regionCode.equals("1")) {
            regionStr = "Alm";
        } else if (regionCode.equals("2")) {
            regionStr = "N&C";
        } else if (regionCode.equals("3")) {
            regionStr = "East";
        } else if (regionCode.equals("4")) {
            regionStr = "South";
        } else if (regionCode.equals("5")) {
            regionStr = "West";
        } else if (regionCode.equals("0")) {
            regionStr = "Ast";
        }

        def siteName = execution.getVariable('siteName').toString();
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        String s = df.format(date);
        String dateString = s;

        def fileName = "RSD_" + regionStr + "_" + siteName + "_" + dateString + ".doc";
        createdRSDFile.put("date", new Date().getTime());
        createdRSDFile.put("author", "");
        createdRSDFile.put("name", fileName);
        createdRSDFile.put("path", path);
        createdRSDFiles.put(createdRSDFile);
        execution.setVariable("createdRSDFile", SpinValues.jsonValue(createdRSDFiles.toString()))
    }
}
