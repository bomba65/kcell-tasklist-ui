import groovy.json.JsonSlurper
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

// For testing purpose
def ncpNumber = '00198'
def siteName = 'WGM00198BCAPPLE'
def infillDate = '02-05-2018'
def creator = 'NURZHAN.KOCHSHIGULOV'
def reason = 'Indoor design site'
def project = '1.Corporate Client'
def visitDate = '02-05-2018'
def address = ''
def client = ''
def contactInfo = ''
def latitude = ''
def longitude = ''
def constructionType = ''
def squareMeter = ''
def rbsType = ''
def bands = ''
def rbsLocation = ''
def antennasType = ''
def antennaType = ''
def antennasQuantity = ''
def antennaQuantity = ''
def antennasFrequencyBand = ''
def antennaFrequencyBand = ''
def antennasDimensions = ''
def antennaDiameter = ''
def antennasWeight = ''
def antennaWeight = ''
def antennasSuspensionHeight = ''
def antennaSuspensionHeight = ''
def antennasAzimuth = ''
def antennaAzimuth = ''
def antennaComments = ''
def legalAddress = ''
def legalTelFax = ''
def legalFirstLeaderName = ''
def legalFirstLeaderPosition = ''
def contactPersonName = ''
def contactPersonPosition = ''
def contactPersonContactInfo = ''

def binding = [
        "ncpNumber": ncpNumber,
        "siteName": siteName,
        "infillDate": infillDate,
        "creator": creator,
        "reason": reason,
        "project": project,
        "visitDate": visitDate,
        "address": address,
        "client":client,
        "contactInfo":contactInfo,
        "latitude":latitude,
        "longitude":longitude,
        "constructionType":constructionType,
        "squareMeter":squareMeter,
        "rbsType":rbsType,
        "bands":bands,
        "rbsLocation":rbsLocation,
        "antennasType":antennasType,
        "antennaType":antennaType,
        "antennasQuantity":antennasQuantity,
        "antennaQuantity":antennaQuantity,
        "antennasFrequencyBand":antennasFrequencyBand,
        "antennaFrequencyBand":antennaFrequencyBand,
        "antennasDimensions":antennasDimensions,
        "antennaDiameter":antennaDiameter,
        "antennasWeight":antennasWeight,
        "antennaWeight":antennaWeight,
        "antennasSuspensionHeight":antennasSuspensionHeight,
        "antennaSuspensionHeight":antennaSuspensionHeight,
        "antennasAzimuth":antennasAzimuth,
        "antennaAzimuth":antennaAzimuth,
        "antennaComments":antennaComments,
        "legalAddress":legalAddress,
        "legalTelFax":legalTelFax,
        "legalFirstLeaderName":legalFirstLeaderName,
        "legalFirstLeaderPosition":legalFirstLeaderPosition,
        "contactPersonName":contactPersonName,
        "contactPersonPosition":contactPersonPosition,
        "contactPersonContactInfo":contactPersonContactInfo
]


def template = '''\
yieldUnescaped '<!DOCTYPE html>'

html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
        meta('http-equiv':'"Content-Style-Type" content="text/css"')
        title('My page')
    }
    body {
        table(cellspacing: '0', cellpadding: '0', class: 't1') {
            tbody {
                tr {
                    td(valign: 'middle', class: 'td1') {
                        p(class: 'p1') {
                            b('NCP number:')
                        }
                    }
                    td(valign: 'middle', class: 'td2') {
                        p(class: 'p2') {
                            yield '' + ncpNumber
                        }
                    }                    
                    td(valign: 'middle', class: 'td3') {
                        p(class: 'p2') {
                            b {
                                yield 'Rent request number:'
                                span(class: 'Apple-converted-space'){
                                    yield ' '
                                }
                            }
                        }
                    }
                    td(valign: 'middle', class: 'td4') {
                        p(class: 'p3') {
                            br()
                        }
                    }                    
                }
                tr {
                   td(valign: 'middle', class: 'td1') {
                        p(class: 'p1') {
                            b('Site name:')
                        }
                   }
                   td(valign: 'middle', class: 'td2') {
                        p(class: 'p2') {
                            b {
                                yield '' + siteName
                            }
                        }
                   }
                   td(valign: 'middle', class: 'td3') {
                        p(class: 'p2') {
                            b('Infill data:')
                        }
                   }
                   td(valign: 'middle', class: 'td5') {
                        p(class: 'p2') {
                            b {
                                yield '' + infillDate
                            }
                        }
                   }
                }
                tr {
                   td(valign: 'middle', class: 'td6') {
                        p(class: 'p4') {
                            br()
                        }
                   }
                   td(valign: 'middle', class: 'td3') {
                        p(class: 'p2') {
                            b('Creator:')                            
                        }
                   }
                   td(colspan: '2', valign: 'middle', class: 'td5') {
                        p(class: 'p2') {
                            b {
                                yield '' + creator
                            }
                        }
                   }
                }
                tr {
                   td(valign: 'middle', class: 'td6') {
                        p(class: 'p5') {
                            span(class: 's1'){
                                b {
                                    yield 'Reason: '
                                    span(class: 'Apple-converted-space') {
                                        yield '   '
                                    }
                                }
                            }
                            yield '' + reason
                        }
                   }
                   td(valign: 'middle', class: 'td3') {
                        p(class: 'p2') {
                            b('Company:')                            
                        }
                   }
                   td(valign: 'middle', class: 'td5') {
                        p(class: 'p6') {
                            b()
                            br()
                        }
                   }
                }
                tr {
                   td(valign: 'middle', class: 'td6') {
                        p(class: 'p5') {
                            span(class: 's1'){
                                b {
                                    yield 'Project: '
                                    span(class: 'Apple-converted-space') {
                                        yield '   '
                                    }
                                }
                            }
                            yield '' + project
                        }
                   }
                   td(colspan: '3', valign: 'middle', class: 'td7') {
                        p(class: 'p3') {
                            br()
                        }
                   }                   
                }
                tr {
                   td(valign: 'middle', class: 'td6') {
                        p(class: 'p1') {
                            b {
                                yield 'Date of visit: '
                                span(class: 'Apple-converted-space') {
                                    yield ' '
                                }
                            }
                            span(class: 's2'){
                                yield '' + visitDate
                            }
                        }
                   }                
                   td(colspan: '3', valign: 'middle', class: 'td7') {
                        p(class: 'p3') {
                            br()
                        }
                   }                   
                }
            }
        }
        p(class: 'p7'){
            br()
        }
        ol(class: 'ol1'){
            li(class: 'li8'){
                span(class: 's3'){
                    b {
                        yield '1.'
                        span(class: 'Apple-tab-span'){
                            yield '\t'
                        }
                    }
                }
                b {
                    yield 'Object on setting of radiotelephone equipment:'                
                }
            }
        }
        table(cellspacing:'0', cellpadding:'0', class:'t1') {
            tbody {
                tr {
                    td(valign:'middle', class:'td8') {
                        p(class:'p1') {
                            b('1)')
                        }
                    }
                    td(valign:'middle', class:'td9') {
                        p(class:'p2') {
                            b('Address:')
                        }
                    }                    
                    td(colspan:'4', valign:'middle', class:'td10') {
                        p(class:'p1') {
                            b {
                                yield '' + address + ';'
                                span(class: 'Apple-converted-space'){
                                    yield '  '
                                }
                                yield 'B2B Client: ' + client
                            }
                        }
                        p(class:'p4') {
                            br()
                        }
                    }                    
                }
                tr {
                    td(valign:'middle', class:'td8') {
                        p(class:'p1') {
                            b('2)')
                        }
                    }
                    td(valign:'middle', class:'td9') {
                        p(class:'p2') {
                            b('Contact information:')
                        }
                    }
                    td(colspan:'4', valign:'middle', class:'td10') {
                        p(class:'p9') {
                            b {
                                span(class: 'Apple-converted-space'){
                                    yield '  '
                                }
                            }
                        }
                        p(class:'p1') {
                            b {
                                yield '' + contactInfo
                            }
                        }
                    }
                }
                tr {
                    td(valign:'middle', class:'td8') {
                        p(class:'p1') {
                            b('3)')
                        }
                    }
                    td(valign:'middle', class:'td9') {
                        p(class:'p2') {
                            b('Coordinates:')
                        }
                    }
                    td(valign:'middle', class:'td11') {
                        p(class:'p1') {
                            yield 'LATITUDE'
                        }
                    }
                    td(valign:'middle', class:'td12') {
                        p(class:'p10') {
                            yield '' + latitude
                        }
                    }
                    td(valign:'middle', class:'td13') {
                        p(class:'p2') {
                            yield 'LONGITUDE'
                            span(class: 'LONGITUDE') {
                                yield ' '
                            }
                        }
                    }
                    td(valign:'middle', class:'td14') {
                        p(class:'p11') {
                            yield '' + longitude
                        }
                    }                    
                }
                tr {
                    td(valign:'middle', class:'td8') {
                        p(class:'p1') {
                            b('4)')
                        }
                    }
                    td(valign:'middle', class:'td9') {
                        p(class:'p2') {
                            b('Construction type:')
                        }
                    }       
                    td(colspan:'4', valign:'middle', class:'td10') {
                        p(class:'p1') {
                            yield '' + constructionType
                        }
                    }
                }
                tr {
                    td(valign:'middle', class:'td8') {
                        p(class:'p1') {
                            b('5)')
                        }
                    }
                    td(valign:'middle', class:'td9') {
                        p(class:'p2') {
                            b('Square, m')
                            span(class: 's4') {
                                b('2')
                            }
                            b(':')
                        }
                    }
                    td(colspan:'4', valign:'middle', class:'td10') {
                        p(class:'p1') {
                            b {
                                yield '' + squareMeter
                            }
                        }
                    }
                }
                tr {
                    td(valign:'middle', class:'td8') {
                        p(class:'p1') {
                            b('6)')
                        }
                    }
                    td(valign:'middle', class:'td9') {
                        p(class:'p2') {
                            b('Tower type:')
                        }
                    }
                    td(colspan:'4', valign:'middle', class:'td10') {
                        p(class:'p9') {
                            b()
                            br()
                        }
                    }                    
                }
                tr {
                    td(valign:'middle', class:'td8') {
                        p(class:'p1') {
                            b('7)')
                        }
                    }
                    td(colspan:'5', valign:'middle', class:'td15') {
                        p(class:'p2') {
                            b('Parameters of the set equipment:')
                        }
                    }
                }
                tr {
                    td(valign:'middle', class:'td8') {
                        p(class:'p4') {
                            br()
                        }
                    }                    
                    td(valign:'middle', class:'td16') {
                        p(class:'p2') {
                            b('RBS type:')
                        }
                    }
                    td(colspan:'4', valign:'middle', class:'td17') {
                        p(class:'p12') {
                            b {
                                yield '' + rbsType                                
                            }
                        }
                    }                    
                }
                tr {
                    td(valign:'middle', class:'td8') {
                        p(class:'p4') {
                            br()
                        }
                    }
                    td(valign:'middle', class:'td16') {
                        p(class:'p2') {
                            b('BAND:')
                        }
                    }
                    td(colspan:'4', valign:'middle', class:'td17') {
                        p(class:'p12') {
                            yield '' + bands                                
                        }
                    }                    
                }
                tr {
                    td(valign:'middle', class:'td8') {
                        p(class:'p4') {
                            br()
                        }
                    }
                    td(valign:'middle', class:'td16') {
                        p(class:'p2') {
                            b('Total quantity of cabinets:')
                        }
                    }
                    td(colspan:'4', valign:'middle', class:'td17') {
                        p(class:'p12') {
                            b {
                                yield '' + cabinetsQuantity
                            }
                        }
                    }                    
                }
                tr {
                    td(valign:'middle', class:'td8') {
                        p(class:'p4') {
                            br()
                        }
                    }
                    td(valign:'middle', class:'td16') {
                        p(class:'p2') {
                            b('RBS location:')
                        }
                    }
                    td(colspan:'4', valign:'middle', class:'td17') {
                        p(class:'p12') {
                            yield '' + rbsLocation
                        }
                    }
                }
                tr {
                    td(valign:'middle', class:'td8') {
                        p(class:'p4') {
                            br()
                        }
                    }
                    td(valign:'middle', class:'td16') {
                        p(class:'p2') {
                            b('Cell antennas:')
                        }
                    }
                    td(valign:'middle', class:'td18') {
                        p(class:'p13') {
                            br()
                        }
                    }
                    td(valign:'middle', class:'td19') {
                        p(class:'p2') {
                            b('Transmission antenna:')
                        }
                    }    
                    td(colspan:'2', valign:'middle', class:'td20') {
                        p(class:'p14') {
                            br()
                        }
                    }
                }
                tr {
                    td(valign:'middle', class:'td8') {
                        p(class:'p4') {
                            br()
                        }
                    }      
                    td(valign:'middle', class:'td16') {
                        p(class:'p2') {
                            b('a) Antennas type:')
                        }
                    }
                    td(valign:'middle', class:'td18') {
                        p(class:'p12') {
                            b {
                                yield '' + antennasType
                            }
                        }
                    }
                    td(valign:'middle', class:'td19') {
                        p(class:'p2') {
                            yield 'a) Antenna type:'
                        }
                    }
                    td(colspan:'2', valign:'middle', class:'td20') {
                        p(class:'p11') {
                            b {
                                yield '' + antennaType
                            }
                        }
                    }
                }
                tr {
                    td(valign:'middle', class:'td8') {
                        p(class:'p4') {
                            br()
                        }
                    }      
                    td(valign:'middle', class:'td16') {
                        p(class:'p2') {
                            b('b) Antennas quantity:')
                        }
                    }
                    td(valign:'middle', class:'td18') {
                        p(class:'p12') {
                            b {
                                yield '' + antennasQuantity
                            }
                        }
                    }
                    td(valign:'middle', class:'td19') {
                        p(class:'p2') {
                            yield 'b) Antenna quantity:'
                        }
                    }
                    td(colspan:'2', valign:'middle', class:'td20') {
                        p(class:'p11') {
                            b {
                                yield '' + antennaQuantity
                            }
                        }
                    }                    
                }
                tr {
                    td(valign:'middle', class:'td8') {
                        p(class:'p4') {
                            br()
                        }
                    }      
                    td(valign:'middle', class:'td16') {
                        p(class:'p2') {
                            b('с) Frequency band:')
                        }
                    }
                    td(valign:'middle', class:'td18') {
                        p(class:'p12') {
                            b {
                                yield '' + antennasFrequencyBand
                            }
                        }
                    }
                    td(valign:'middle', class:'td19') {
                        p(class:'p2') {
                            yield 'с) Frequency band:'
                        }
                    }
                    td(colspan:'2', valign:'middle', class:'td20') {
                        p(class:'p11') {
                            b {
                                yield '' + antennaFrequencyBand
                            }
                        }
                    }                    
                }
                tr {
                    td(valign:'middle', class:'td8') {
                        p(class:'p4') {
                            br()
                        }
                    }      
                    td(valign:'middle', class:'td16') {
                        p(class:'p2') {
                            b('d) Dimensions, (LxWxH) mm:')
                        }
                    }
                    td(valign:'middle', class:'td18') {
                        p(class:'p12') {
                            b {
                                yield '' + antennasDimensions
                            }
                        }
                    }
                    td(valign:'middle', class:'td19') {
                        p(class:'p2') {
                            yield 'd) Diameter, mm:'
                        }
                    }
                    td(colspan:'2', valign:'middle', class:'td20') {
                        p(class:'p11') {
                            b {
                                yield '' + antennaDiameter
                            }
                        }
                    }                    
                }
                tr {
                    td(valign:'middle', class:'td8') {
                        p(class:'p4') {
                            br()
                        }
                    }      
                    td(valign:'middle', class:'td16') {
                        p(class:'p2') {
                            b('e) Weight, kg:')
                        }
                    }
                    td(valign:'middle', class:'td18') {
                        p(class:'p15') {
                            b {
                                yield '' + antennasWeight
                            }
                        }
                    }
                    td(valign:'middle', class:'td19') {
                        p(class:'p2') {
                            yield 'e) Weight, kg:'
                        }
                    }
                    td(colspan:'2', valign:'middle', class:'td20') {
                        p(class:'p11') {
                            b {
                                yield '' + antennaWeight
                            }
                        }
                    }                    
                }
                tr {
                    td(valign:'middle', class:'td8') {
                        p(class:'p4') {
                            br()
                        }
                    }      
                    td(valign:'middle', class:'td16') {
                        p(class:'p16') {
                            b('f) Suspension height of antennas:')
                        }
                    }
                    td(valign:'middle', class:'td18') {
                        p(class:'p12') {
                            b {
                                yield '' + antennasSuspensionHeight
                            }
                        }
                    }
                    td(valign:'middle', class:'td19') {
                        p(class:'p17') {
                            yield 'f) Suspension height of antennas:'
                        }
                    }
                    td(colspan:'2', valign:'middle', class:'td20') {
                        p(class:'p11') {
                            b {
                                yield '' + antennaSuspensionHeight
                            }
                        }
                    }                    
                }
                tr {
                    td(valign:'middle', class:'td8') {
                        p(class:'p4') {
                            br()
                        }
                    }      
                    td(valign:'middle', class:'td16') {
                        p(class:'p2') {
                            b('g) Azimuth:')
                        }
                    }
                    td(valign:'middle', class:'td18') {
                        p(class:'p15') {
                            b {
                                yield '' + antennasAzimuth
                            }
                        }
                    }
                    td(valign:'middle', class:'td19') {
                        p(class:'p2') {
                            yield 'g) Azimuth:'
                        }
                    }
                    td(colspan:'2', valign:'middle', class:'td20') {
                        p(class:'p11') {
                            b {
                                yield '' + antennaAzimuth
                            }
                        }
                    }                    
                }  
                tr {
                    td(valign:'middle', class:'td8') {
                        p(class:'p1') {
                            b('8)')
                        }
                    }
                    td(valign:'middle', class:'td16') {
                        p(class:'p2') {
                            b('Comments:')
                        }
                    }
                    td(colspan:'4', valign:'middle', class:'td17') {
                        p(class:'p12') {
                            b {
                                yield '' + antennaComments
                            }
                        }
                    }
                }                              
            }
        }
        p(class:'p18') {
            br()
        }
        ol(class:'ol1'){
            li(class:'li8'){
                span(class:'s3'){
                    b {
                        yield '2.'
                        span(class:'Apple-tab-span'){
                            yield ' '
                        }
                    }
                }
                b {
                    yield 'Renter company:'
                }
            }
        }
        table(cellspacing:'0', cellpadding:'0', class:'t1') {
            tbody {
                tr {
                    td(valign:'top', class:'td21') {
                        p(class:'p1'){
                            b {
                                yield '1) Legal name:'
                            }
                        }
                    }
                    td(valign:'top', class:'td22') {
                        p(class:'p19'){
                            b {
                                yield '' + legalName
                            }
                        }
                    }                    
                }
                tr {
                    td(valign:'top', class:'td21') {
                        p(class:'p1'){
                            b {
                                yield '2) Legal address:'
                            }
                        }
                    }
                    td(valign:'top', class:'td22') {
                        p(class:'p19'){
                            b {
                                yield '' + legalAddress
                            }
                        }
                    }                    
                }
                tr {
                    td(valign:'top', class:'td21') {
                        p(class:'p1'){
                            b {
                                yield '3) Tel./Fax'
                            }
                        }
                    }
                    td(valign:'top', class:'td22') {
                        p(class:'p19'){
                            b {
                                yield '' + legalTelFax
                            }
                        }
                    }
                }
                tr {
                    td(valign:'top', class:'td21') {
                        p(class:'p1'){
                            b {
                                yield '4) Name of the 1-st Leader:'
                            }
                        }
                    }
                    td(valign:'top', class:'td22') {
                        p(class:'p19'){
                            b {
                                yield '' + legalFirstLeaderName
                            }
                        }
                    }
                }
                tr {
                    td(valign:'top', class:'td21') {
                        p(class:'p20'){
                            b {
                                yield '4) Position of the 1-st Leader:'
                            }
                        }
                    }
                    td(valign:'top', class:'td22') {
                        p(class:'p19'){
                            b {
                                yield '' + legalFirstLeaderPosition
                            }
                        }
                    }
                }                
                tr {
                    td(valign:'top', class:'td21') {
                        p(class:'p20'){
                            b {
                                yield '4) E-mail'
                            }
                        }
                    }
                    td(valign:'top', class:'td22') {
                        p(class:'p19'){
                            b {
                                yield '' + legalFirstLeaderPosition
                            }
                        }
                    }
                }
                tr {
                    td(valign:'top', class:'td21') {
                        p(class:'p1'){
                            b {
                                yield '5) Contact person:'
                            }
                        }
                    }
                    td(valign:'top', class:'td22') {
                        p(class:'p21'){
                            b {
                                yield ''
                            }
                        }
                    }
                }
                tr {
                    td(valign:'top', class:'td21') {
                        p(class:'p22'){
                            b {
                                yield 'Name:'
                                span(class:'Apple-converted-space') {
                                    yield '   '
                                }
                            }
                        }
                    }
                    td(valign:'top', class:'td22') {
                        p(class:'p19'){
                            b {
                                yield '' + contactPersonName
                            }
                        }
                    }
                }
                tr {
                    td(valign:'top', class:'td21') {
                        p(class:'p22'){
                            b {
                                yield 'Position:'
                            }
                        }
                    }
                    td(valign:'top', class:'td22') {
                        p(class:'p19'){
                            b {
                                yield '' + contactPersonPosition
                            }
                        }
                    }
                }
                tr {
                    td(valign:'top', class:'td21') {
                        p(class:'p22'){
                            b {
                                yield 'Contact information:'
                            }
                        }
                    }
                    td(valign:'top', class:'td22') {
                        p(class:'p10'){
                            b {
                                yield '' + contactPersonContactInfo
                            }
                        }
                    }
                }
            }
        }
    }
}
'''

/*

<ol class="ol1">
  <li class="li8"><span class="s3"><b>3.<span class="Apple-tab-span">	</span></b></span><b>Power source:</b></li>
</ol>
<p class="p23"><b>1) From landlord:</b></p>
<ol class="ol1">
  <ol class="ol2">
    <li class="li8"><span class="s3">a.<span class="Apple-tab-span">	</span></span>Does renter agree to provide us 3 Phase/3.5kW of electrical energy?</li>
  </ol>
</ol>
<p class="p24"><span class="Apple-converted-space">  </span>Yes</p>
<p class="p25"><br></p>
<ol class="ol1">
<ol class="ol2">
<li class="li8"><span class="s3">b.<span class="Apple-tab-span">	</span></span>Cable length to connection point<span class="Apple-converted-space">  </span>m.</li>
<li class="li8"><span class="s3">c.<span class="Apple-tab-span">	</span></span>Cable laying type:</li>
  </ol>
</ol>
<p class="p26"><span class="Apple-converted-space">  </span>By the walls</p>
<p class="p27"><br></p>
<ol class="ol1">
<ol class="ol2">
<li class="li8"><span class="s3">d.<span class="Apple-tab-span">	</span></span>Does renter agree to receive monthly payment for consumed electrical energy according to our power counters displays buy the bank transactions and give us invoices?</li>
  </ol>
</ol>
<p class="p28"><span class="Apple-converted-space">  </span>Yes</p>
<p class="p29"><br></p>
<p class="p23"><b>2) From subscription from power provider:</b></p>
<ol class="ol1">
<ol class="ol2">
<li class="li8"><span class="s3">e.<span class="Apple-tab-span">	</span></span>Closest public (RES) electrical line:</li>
    <ol class="ol3">
      <li class="li8"><span class="s3">I.<span class="Apple-tab-span">	</span></span>0,4kV <span class="s5">1</span> m</li>
      <li class="li8"><span class="s3">II.<span class="Apple-tab-span">	</span></span>10kV<span class="Apple-converted-space">  </span>m</li>
    </ol>
</ol>
</ol>
<p class="p29"><br></p>
<p class="p29"><br></p>
<ol class="ol1">
<ol class="ol3">
<ol class="ol4">
<li class="li8"><span class="s3"><b>4.<span class="Apple-tab-span">	</span></b></span><span class="s5"><b>Photo of object:</b></span></li>
</ol>
  </ol>
</ol>
<p class="p29"><br></p>
<table cellspacing="0" cellpadding="0" class="t1">
<tbody>
<tr>
<td valign="middle" class="td23">
<p class="p1"><b>Request number:<span class="Apple-converted-space"> </span></b></p>
      </td>
<td valign="middle" class="td24">
<p class="p3"><br></p>
      </td>
<td valign="middle" class="td25">
<p class="p3"><br></p>
      </td>
<td valign="middle" class="td26">
<p class="p11"><b>Infill data:</b></p>
</td>
      <td valign="middle" class="td27">
        <p class="p11"><b>02-05-2018</b></p>
      </td>
</tr>
    <tr>
      <td valign="middle" class="td23">
        <p class="p4"><br></p>
</td>
      <td valign="middle" class="td28">
        <p class="p3"><br></p>
</td>
      <td valign="middle" class="td25">
        <p class="p3"><br></p>
</td>
      <td valign="middle" class="td26">
        <p class="p11"><a href="file:/bip/bi_11g/user_projects/domains/bifoundation_domain/javascript:popupFieldHelp('123373703895256178','3853423075830251')"><b>Creator</b></a><b>:</b></p>
</td>
      <td valign="middle" class="td27">
        <p class="p11"><b>NURZHAN.KOCHSHIGULOV</b></p>
      </td>
</tr>
  </tbody>
</table>
<p class="p29"><br></p>
<table cellspacing="0" cellpadding="0" class="t1">
<tbody>
<tr>
<td valign="top" class="td29">
<p class="p1"><b>Survey Date:</b></p>
</td>
      <td valign="top" class="td30">
        <p class="p6"><b></b><br></p>
      </td>
<td valign="top" class="td31">
<p class="p30"><br></p>
      </td>
<td valign="top" class="td32">
<p class="p31"><a href="%22f"><b>Date of visit</b></a><b>:</b></p>
</td>
      <td valign="top" class="td33">
        <p class="p32"><b>02-05-2018</b></p>
      </td>
</tr>
  </tbody>
</table>
<p class="p29"><br></p>
<table cellspacing="0" cellpadding="0" class="t1">
<tbody>
<tr>
<td valign="top" class="td34">
<p class="p1"><b>Far End Name:</b></p>
</td>
      <td valign="top" class="td35">
        <p class="p1"><b>01241KZNTU</b></p>
      </td>
</tr>
  </tbody>
</table>
<p class="p29"><br></p>
<ol class="ol1">
<ol class="ol3">
<li class="li8"><span class="s3"><b>I.<span class="Apple-tab-span">	</span></b></span><b>Object on setting of transmission equipment:</b></li>
  </ol>
</ol>
<table cellspacing="0" cellpadding="0" class="t1">
  <tbody>
    <tr>
      <td valign="top" class="td36">
        <p class="p1"><b>1)</b></p>
      </td>
<td valign="top" class="td37">
<p class="p2"><b>Far End Address:</b></p>
</td>
      <td valign="top" class="td38">
        <p class="p2"><b>г.Алматы , ул.Байтурсынова 140, Казахский национальный технический университет им. Сатпаева (КазНТУ)</b></p>
      </td>
</tr>
    <tr>
      <td valign="top" class="td36">
        <p class="p1"><b>2)</b></p>
      </td>
<td valign="top" class="td37">
<p class="p2"><b>Contact information:</b></p>
</td>
      <td valign="top" class="td38">
        <p class="p6"><b></b><br></p>
      </td>
</tr>
    <tr>
      <td valign="top" class="td36">
        <p class="p1"><b>3)</b></p>
      </td>
<td valign="top" class="td37">
<p class="p2"><b>Square, m</b><span class="s4"><b>2</b></span><b>:</b></p>
      </td>
<td valign="middle" class="td38">
<p class="p6"><br></p>
      </td>
</tr>
    <tr>
      <td valign="top" class="td36">
        <p class="p1"><b>4)</b></p>
      </td>
<td valign="top" class="td37">
<p class="p2"><b>Equipment Type:</b></p>
</td>
      <td valign="top" class="td38">
        <p class="p2"><b>FH</b></p>
      </td>
</tr>
    <tr>
      <td valign="top" class="td36">
        <p class="p1"><b>5)</b></p>
      </td>
<td valign="top" class="td37">
<p class="p2"><b>Antenna:</b></p>
</td>
      <td valign="top" class="td38">
        <p class="p3"><br></p>
</td>
    </tr>
<tr>
<td valign="top" class="td36">
<p class="p4"><br></p>
      </td>
<td valign="top" class="td37">
<p class="p2">a) Antenna Diameter, mm:</p>
      </td>
<td valign="top" class="td38">
<p class="p2"><b><span class="Apple-converted-space"> </span>0,3</b></p>
      </td>
</tr>
    <tr>
      <td valign="top" class="td36">
        <p class="p4"><br></p>
</td>
      <td valign="top" class="td37">
        <p class="p2">b) Antennas quantity:</p>
</td>
      <td valign="top" class="td38">
        <p class="p2"><b>1</b></p>
      </td>
</tr>
    <tr>
      <td valign="top" class="td36">
        <p class="p4"><br></p>
</td>
      <td valign="top" class="td37">
        <p class="p2">с) TX Frequency:</p>
</td>
      <td valign="top" class="td38">
        <p class="p6"><b></b><br></p>
      </td>
</tr>
    <tr>
      <td valign="top" class="td36">
        <p class="p4"><br></p>
</td>
      <td valign="top" class="td37">
        <p class="p2">d) RX Frequency</p>
</td>
      <td valign="top" class="td38">
        <p class="p6"><b></b><br></p>
      </td>
</tr>
    <tr>
      <td valign="top" class="td36">
        <p class="p4"><br></p>
</td>
      <td valign="top" class="td37">
        <p class="p2">e) Weight, kg:</p>
</td>
      <td valign="top" class="td38">
        <p class="p2"><b>13</b></p>
      </td>
</tr>
    <tr>
      <td valign="top" class="td36">
        <p class="p4"><br></p>
</td>
      <td valign="top" class="td37">
        <p class="p16">f) Suspension height of antennas:</p>
</td>
      <td valign="top" class="td38">
        <p class="p6"><b></b><br></p>
      </td>
</tr>
    <tr>
      <td valign="top" class="td36">
        <p class="p4"><br></p>
</td>
      <td valign="top" class="td37">
        <p class="p2">g) Azimuth:</p>
</td>
      <td valign="top" class="td38">
        <p class="p2"><b>180</b></p>
      </td>
</tr>
    <tr>
      <td valign="top" class="td36">
        <p class="p1"><b>6)</b></p>
      </td>
<td valign="top" class="td37">
<p class="p2"><b>Construction type:</b></p>
</td>
      <td valign="top" class="td38">
        <p class="p6"><b></b><br></p>
      </td>
</tr>
    <tr>
      <td valign="top" class="td36">
        <p class="p1"><b>7)</b></p>
      </td>
<td valign="top" class="td37">
<p class="p2"><b>Comments:</b></p>
</td>
      <td valign="top" class="td38">
        <p class="p6"><b></b><br></p>
      </td>
</tr>
  </tbody>
</table>
<p class="p29"><br></p>
<p class="p29"><br></p>
<ol class="ol1">
  <ol class="ol3">
    <li class="li8"><span class="s3"><b>II.<span class="Apple-tab-span">	</span></b></span><b>Renter company:</b></li>
</ol>
</ol>
<table cellspacing="0" cellpadding="0" class="t1">
<tbody>
<tr>
<td valign="top" class="td21">
<p class="p1"><b>1) Legal name:</b></p>
</td>
      <td valign="top" class="td39">
        <p class="p19"><b></b><br></p>
      </td>
</tr>
    <tr>
      <td valign="top" class="td21">
        <p class="p1"><b>2) Legal address:</b></p>
      </td>
<td valign="top" class="td39">
<p class="p19"><b></b><br></p>
</td>
    </tr>
<tr>
<td valign="top" class="td21">
<p class="p1"><b>3) Tel./Fax</b></p>
      </td>
<td valign="top" class="td39">
<p class="p19"><b></b><br></p>
</td>
    </tr>
<tr>
<td valign="top" class="td21">
<p class="p1"><b>4) Name of the 1-st Leader:</b></p>
</td>
      <td valign="top" class="td39">
        <p class="p19"><b></b><br></p>
      </td>
</tr>
    <tr>
      <td valign="top" class="td21">
        <p class="p20"><b>4) Position of the 1-st Leader:</b></p>
      </td>
<td valign="top" class="td39">
<p class="p19"><b></b><br></p>
</td>
    </tr>
<tr>
<td valign="top" class="td21">
<p class="p1"><b>4) E-mail</b></p>
</td>
      <td valign="top" class="td39">
        <p class="p19"><b></b><br></p>
      </td>
</tr>
    <tr>
      <td valign="top" class="td21">
        <p class="p1"><b>5) Contact person:</b></p>
      </td>
<td valign="top" class="td39">
<p class="p21"><br></p>
      </td>
</tr>
    <tr>
      <td valign="top" class="td21">
        <p class="p22"><b>Name:<span class="Apple-converted-space">   </span></b></p>
</td>
      <td valign="top" class="td39">
        <p class="p19"><b></b><br></p>
      </td>
</tr>
    <tr>
      <td valign="top" class="td21">
        <p class="p22"><b>Position:</b></p>
      </td>
<td valign="top" class="td39">
<p class="p19"><b></b><br></p>
</td>
    </tr>
<tr>
<td valign="top" class="td21">
<p class="p22"><b>Contact information:</b></p>
</td>
      <td valign="top" class="td39">
        <p class="p19"><b></b><br></p>
      </td>
</tr>
  </tbody>
</table>
<p class="p29"><br></p>
<p class="p29"><br></p>
<p class="p33"><b>III. Results of visit of objects:</b><span class="s2"><span class="Apple-converted-space"> </span></span></p>
<p class="p34"><br></p>
*/

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

print result

result
