package tnuTsd

import groovy.json.JsonSlurper
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

/*

"Name";"Name2";"Longitude";"Longitude2";"Latitude";"Latitude2";"Elevation";"Elevation2";"Site A";"Site B";"Azimuth1";"Azimuth2"; "TowerHeight1";"TowerHeight2"
"61146MATERIK-61159SAMALHOT(2661-181219)";"61146MATERIK-61159SAMALHOT(2661-181219)";"51,90006";"51,9181972222222";"47,0982800027778";"47,0950138916667";"-23,6";"-26,3";"61146MATERIK";"61159SAMALHOT";"105";"258";"9";"9"

def tnuTsdNumber = '61146MATERIK-61159SAMALHOT(2661-181219)'
def ne_longitude = '55.545545'
def fe_longitude = '70.545545'
def ne_latitude = '40.545545'
def fe_latitude = '60.545545'
def ne_altitude = '-23.1'
def fe_altitude = '-23.1'
def ne_sitename = '61146MATERIK'
def fe_sitename = '61159SAMALHOT'
def ne_azimuth = '234'
def fe_azimuth = '178'
def ne_construction_height = '9'
def fe_construction_height = '9'
*/

def binding = ["tnuTsdNumber":tnuTsdNumber,"ne_longitude":ne_longitude.replace(".",","),"fe_longitude":fe_longitude.replace(".",","),
               "ne_latitude":ne_latitude.replace(".",","),"fe_latitude":fe_latitude.replace(".",","),
               "ne_altitude":"","fe_altitude":"",
               "ne_sitename":ne_sitename,"fe_sitename":fe_sitename,"ne_azimuth":ne_azimuth.toString().replace(".",","),"fe_azimuth":fe_azimuth.toString().replace(".",","),
               "ne_suspension_height_antennas":ne_suspension_height_antennas, "fe_suspension_height":fe_suspension_height]

def template = ''' yieldUnescaped '"Name";"Name2";"Longitude";"Longitude2";"Latitude";"Latitude2";"Elevation";"Elevation2";"Site A";"Site B";"Azimuth1";"Azimuth2";"TowerHeight1";"TowerHeight2";"TXPower";"RadioProfile";"Tilt";"Feeder";"Parity";"Antenna_profile"'
newLine()
yieldUnescaped '"' + tnuTsdNumber + '";"' + tnuTsdNumber + '";"' + ne_longitude + '";"' + fe_longitude + '";"' + ne_latitude + '";"' + fe_latitude + '";"' + ne_altitude + '";"' + fe_altitude + '";"' + ne_sitename + '";"' + fe_sitename + '";"' + ne_azimuth + '";"' + fe_azimuth + '";"' + ne_suspension_height_antennas + '";"' + fe_suspension_height + '";"' + '"15";"Default_Radio";"0";"Default_Feeder";"Low";"Default_Antenna"' 
'''

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

print result

result
