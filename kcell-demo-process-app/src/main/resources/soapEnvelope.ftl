<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:ws="http://ws.gismeteo.ru/">
    <soap:Header/>
    <soap:Body>
        <ws:GetHHObservation>
            <!--Optional:-->
            <ws:serial>9f3552ab-7cad-424e-8e8a-9fd03afe6313</ws:serial>
            <ws:location>${city[5..]}</ws:location>
        </ws:GetHHObservation>
    </soap:Body>
</soap:Envelope>