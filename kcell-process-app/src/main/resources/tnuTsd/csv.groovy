package tnuTsd

import groovy.json.JsonSlurper
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

/*

"Name";"Name2";"Longitude";"Longitude2";"Latitude";"Latitude2";"Elevation";"Elevation2";"Site A";"Site B";"Azimuth1";"Azimuth2";"TowerHeight1";"TowerHeight2"
"61146MATERIK-61159SAMALHOT(2661-181219)";"61146MATERIK-61159SAMALHOT(2661-181219)";"51,90006";"51,9181972222222";"47,0982800027778";"47,0950138916667";"-23,6";"-26,3";"61146MATERIK";"61159SAMALHOT";"105";"258";"9";"9"

 */

def tnuTsdNumber = "61146MATERIK-61159SAMALHOT(2661-181219)"

def binding = ["tnuTsdNumber":tnuTsdNumber]

def template = '''\
yieldUnescaped '' + tnuTsdNumber + ';' + tnuTsdNumber + ';'
'''

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

print result

result
