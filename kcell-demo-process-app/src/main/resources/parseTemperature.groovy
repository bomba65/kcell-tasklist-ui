// this script parses the temperature from the response

// remove prologue which cannot be parsed: '<?xml version="1.0" encoding="utf-16"?>'
rawForecast = execution.getVariable('forecast')
println rawForecast
forecast = rawForecast.substring(0,rawForecast.indexOf("."))
forecast