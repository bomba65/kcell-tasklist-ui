// this script parses the temperature from the response

// remove prologue which cannot be parsed: '<?xml version="1.0" encoding="utf-16"?>'
var rawForecast = execution.getVariable('forecast');
var forecast = rawForecast.substring(0,(rawForecast.indexOf(".")< 0 ? rawForecast.length : rawForecast.indexOf(".")));
forecast;