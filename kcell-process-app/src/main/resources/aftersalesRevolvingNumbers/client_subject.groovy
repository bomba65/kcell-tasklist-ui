import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def legalName = legalInfo.unwrap().get('legalName').asText()
def bin = clientBIN

def result = legalName + " BIN" + bin
result
