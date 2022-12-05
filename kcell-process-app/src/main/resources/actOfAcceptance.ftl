<#assign sharingPlanObj = sharingPlan?eval>
<#assign colspan = sharingPlanObj.shared_sectors?size>

<html>
<head>
    <style>
        html {
            font-size: 12pt;
            font-family: "Times New Roman", Georgia, Serif;
        }
        table{
            font-size: 10pt;
            border-collapse: collapse;
            width: 100%;
        }
        .table table, .table th, .table td{
            border: 1px solid #000;
        }
    </style>
</head>
<body>
<p style="text-align: right;">Дополнение № 1</p>
<p style="text-align: center;"><b>Акт приёмки в эксплуатацию БС Стандарта LTE БС Атс74</b></p>
<p style="text-align: center;">(Заполняется Ведущим оператором)</p>
<table class="table">
    <tr>
        <td width="10%">Ведущий оператор:</td>
        <td width="30%">
            <#if sharingPlanObj.host == "Beeline">ТОО «КаР-Тел»
                <#elseif sharingPlanObj.host == "Kcell">АО «Кселл»
            </#if>
        </td>
        <td width="50%" colspan="${colspan}"></td>
    </tr>
    <tr>
        <td>Владелец Сайта:</td>
        <td>
            <#if sharingPlanObj.site_owner == "Beeline">ТОО «КаР-Тел»
                <#elseif sharingPlanObj.site_owner == "Kcell">АО «Кселл»
            </#if>
        </td>
        <td colspan="${colspan}"></td>
    </tr>
    <tr>
        <td>Идентификатор Площадки в сети Ведущего/Ведомого:</td>
        <td>${sharingPlanObj.site_id}</td>
        <td colspan="${colspan}"></td>
    </tr>
    <tr>
        <td>Адрес Площадки:</td>
        <td>${sharingPlanObj.address}</td>
        <td colspan="${colspan}"></td>
    </tr>
    <tr>
        <td>Частотный диапазон БС:</td>
        <td>${sharingPlanObj.shared_sectors[0].enodeb_range!""} Мгц</td>
        <td colspan="${colspan}"></td>
    </tr>
    <tr>
        <td>Конфигурация БС:</td>
        <td>
            <#list sharingPlanObj.shared_sectors as ss>
                        1<#if ss_index < (sharingPlanObj.shared_sectors?size -1) >/</#if>
            </#list>
        </td>
        <td colspan="${colspan}"></td>
    </tr>
    <tr>
        <td>Идентификатор БС в сети Ведущего/Ведомого</td>
        <td>${sharingPlanObj.site_name}</td>
        <td colspan="${colspan}"></td>
    </tr>
    <tr>
        <td>Характер использования БС:</td>
        <td>${sharingPlanObj.site_usage_type}</td>
        <td colspan="${colspan}"></td>
    </tr>
    <tr>
        <td>CellID:</td>
        <td></td>
        <#list sharingPlanObj.shared_sectors as ss>
            <td>${ss.sector_name}</td>
        </#list>
    </tr>
    <tr>
        <td>Ширина спектра (MГц):</td>
        <td></td>
        <#list sharingPlanObj.shared_sectors as ss>
            <td>
                <#if sharingPlanObj.site_owner == "Beeline">${ss.beeline_band}Мгц
                    <#elseif sharingPlanObj.site_owner == "Kcell">${ss.kcell_band}Мгц
                </#if>
            </td>
        </#list>
    </tr>
    <tr>
        <td>Владелец транспорта</td>
        <td>
            <#if sharingPlanObj.transmission_owner == "Beeline">ТОО «КаР-Тел»
                <#elseif sharingPlanObj.transmission_owner == "Kcell">АО «Кселл»
            </#if>
        </td>
        <td colspan="${colspan}"></td>
    </tr>
    <tr>
        <td>Емкость арендуемого ресурса транспортной сети у Владельца транспорта</td>
        <td>${sharingPlanObj.transmission_channel_volume_granted_to_partner} Мбит</td>
        <td colspan="${colspan}"></td>
    </tr>
    <tr>
        <td>Наименование Стыка</td>
        <td></td>
        <td colspan="${colspan}"></td>
    </tr>
    <tr>
        <td>Ответственный представитель Ведущего оператора (ФИО, Должность):</td>
        <td></td>
        <td colspan="${colspan}"></td>
    </tr>
    <#--<tr>-->
        <#--<td>Дата включения БС:</td>-->
        <#--<td><#if technical_launch_date??>${technical_launch_date?string['dd.MM.yyyy']}</#if></td>-->
        <#--<td colspan="${colspan}"></td>-->
    <#--</tr>-->
</table>
<br/>
<table class="table">
    <tr>
        <th width="50%" style="background-color: #D8D8D8;">Наименование параметра</th>
        <th width="50%" style="background-color: #D8D8D8;">Статус</th>
    </tr>
    <tr>
        <td>Выполнение требований к Инфраструктуре сайта, качеству монтажа Основного Оборудования</td>
        <td>В норме</td>
    </tr>
    <tr>
        <td>Выполнение требований к ресурсу транспортной сети</td>
        <td>В норме</td>
    </tr>
    <tr>
        <td>Обеспечение работоспособности сервисов</td>
        <td>В норме</td>
    </tr>
    <tr>
        <td>На площадке ведомого работы выполнены согласно утвержденного проекта</td>
        <td>В норме</td>
    </tr>
</table>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<table width="80%">
    <tr>
        <td><b>№</b></td>
        <td><b>Замечания</b><br/> (после устранения недостатка, его вычеркивает проверяющий)</td>
        <td><b>дата</b></td>
        <td><b>подпись</b></td>
        <td><b>ФИО</b></td>
    </tr>
    <tr>
        <td>____</td>
        <td>______________________________________________________</td>
        <td>________</td>
        <td>__________</td>
        <td>_______________________</td>
    </tr>
    <tr>
        <td>____</td>
        <td>______________________________________________________</td>
        <td>________</td>
        <td>__________</td>
        <td>_______________________</td>
    </tr>
    <tr>
        <td>____</td>
        <td>______________________________________________________</td>
        <td>________</td>
        <td>__________</td>
        <td>_______________________</td>
    </tr>
    <tr>
        <td>____</td>
        <td>______________________________________________________</td>
        <td>________</td>
        <td>__________</td>
        <td>_______________________</td>
    </tr>
    <tr>
        <td>____</td>
        <td>______________________________________________________</td>
        <td>________</td>
        <td>__________</td>
        <td>_______________________</td>
    </tr>
    <tr>
        <td>____</td>
        <td>______________________________________________________</td>
        <td>________</td>
        <td>__________</td>
        <td>_______________________</td>
    </tr>
    <tr>
        <td>____</td>
        <td>______________________________________________________</td>
        <td>________</td>
        <td>__________</td>
        <td>_______________________</td>
    </tr>
    <tr>
        <td>____</td>
        <td>______________________________________________________</td>
        <td>________</td>
        <td>__________</td>
        <td>_______________________</td>
    </tr>
    <tr>
        <td>____</td>
        <td>______________________________________________________</td>
        <td>________</td>
        <td>__________</td>
        <td>_______________________</td>
    </tr>
    <tr>
        <td>____</td>
        <td>______________________________________________________</td>
        <td>________</td>
        <td>__________</td>
        <td>_______________________</td>
    </tr>
    <tr>
        <td>____</td>
        <td>______________________________________________________</td>
        <td>________</td>
        <td>__________</td>
        <td>_______________________</td>
    </tr>
    <tr>
        <td>____</td>
        <td>______________________________________________________</td>
        <td>________</td>
        <td>__________</td>
        <td>_______________________</td>
    </tr>
    <tr>
        <td>____</td>
        <td>______________________________________________________</td>
        <td>________</td>
        <td>__________</td>
        <td>_______________________</td>
    </tr>
    <tr>
        <td>____</td>
        <td>______________________________________________________</td>
        <td>________</td>
        <td>__________</td>
        <td>_______________________</td>
    </tr>
    <tr>
        <td>____</td>
        <td>______________________________________________________</td>
        <td>________</td>
        <td>__________</td>
        <td>_______________________</td>
    </tr>
    <tr>
        <td>____</td>
        <td>______________________________________________________</td>
        <td>________</td>
        <td>__________</td>
        <td>_______________________</td>
    </tr>
</table>
<p><i>Базовую станцию _____________<b>(принять в эксплуатацию/выключить)</b>, недостатки устранить до «__ » ___ 201__ г.</i></p>
<p>Ответственный за приёмку БС в эксплуатацию со стороны Ведущего</p>
<p><u>Начальник технического отдела КФ ТОО «КаР-Тел»     Имашев Б. Г</u>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<u>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</u></p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(подпись)</p>
<p style="text-align: right;"><#if taskSubmitDate?? && checkingByGuestResult?? &&  checkingByGuestResult == 'approved' >${taskSubmitDate?string['dd.MM.yyyy']} г.<#else>« ____ » ____________ 20___ г.</#if> </p>

<br/>
<p>Ответственный за приёмку БС в эксплуатацию со стороны Ведомого</p>
<p>«__________ » ________________ «Ф.И.О.»&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;___________</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(подпись)</p>
<br/>
<br/>
<br/>

<span><br clear=all style='mso-special-character:line-break;page-break-before:always'></span>

<p style="text-align: right;">Дополнение № 2</p>
<p style="text-align: center;"><b>Протокол проверки выполнении требований к инфраструктуре сайта, АФУ и качеству монтажа основного оборудования БС Атс74</b></p>
<p style="text-align: center;">(Заполняется Ведущим оператором)</p>
<table class="table">
    <tr>
        <td width="40%">Ведущий оператор:</td>
        <td width="60%" colspan="2">ТОО «КаР-Тел»</td>
    </tr>
    <tr>
        <td>Владелец Сайта</td>
        <td colspan="2">ТОО «КаР-Тел»</td>
    </tr>
    <tr>
        <td>Идентификатор Площадки в сети Ведущего/Ведомого</td>
        <td colspan="2">83100478</td>
    </tr>
    <tr>
        <td>Адрес Площадки:</td>
        <td colspan="2">Карагандинская область, г. Караганды, ул. Строителей 4</td>
    </tr>
    <tr>
        <td>ФИО ответственного представителя Владельца Сайта, выполнившего проверку</td>
        <td colspan="2">Койшыбаев А.А.</td>
    </tr>
    <tr>
        <td>Требования</td>
        <td width="20%">Отметка о выполнении (ДА/НЕТ)</td>
        <td width="80%">Комментарий (В случае невыполнения, указать срок устранения)</td>
    </tr>
    <tr>
        <td>1.&nbsp;&nbsp;КСВ АФТ БС при любой длине АФТ (с антеннами) не превышает значения 1,2во всём диапазоне рабочих частот, предусмотренных стандартом/ми БС.</td>
        <td>В норме</td>
        <td></td>
    </tr>
    <tr>
        <td>2.&nbsp;&nbsp;Любая неоднородность на любой длине АФТ имеет  значение КСВН не более 1,105 при условии, если в АФТ отсутствуют элементы с иными значениями КСВ. Погрешность измерения КСВ не превышает 6%.</td>
        <td>В норме</td>
        <td></td>
    </tr>
    <tr>
        <td>3.&nbsp;&nbsp;Электропитание и источник заземления для eNodeB, места размещения и условия функционирования (в В нормельном режиме) оборудования в аппаратных / телекоммуникационных климатических шкафах (включая размещение в действующих стойках, шкафах, телекоммуникационных климатических шкафах) выполнены  в соответствии с требованиями фирм-производителей оборудования.</td>
        <td>В норме</td>
        <td></td>
    </tr>
    <tr>
        <td>4.&nbsp;&nbsp;Размещение на Площадке металлоконструкций для монтажа антенно-фидерного тракта выполнено с учетом:<br/>
            <ul>
                <li>обеспечения минимальной (с учетом выполнения требований по п.1 - обеспечения КСВ ≤1,2) длины фидеров / кабелей соединяющих оборудование;</li>
                <li>исключения искажения диаграммы направленности устанавливаемых на металлоконструкции антенн (в горизонтальной и вертикальной плоскости) стенами, углами, кровлей зданий / сооружений, металлоконструкциями или оснащением опоры, прочими препятствиями и обеспечения возможности юстировки антенн по азимуту не менее чем на 30 градусов в каждую сторону</li></td>
        <td>В норме</td>
        <td></td>
    </tr>
    <tr>
        <td>5.&nbsp;&nbsp;Для монтажа фидеров, кабелей, соединяющих оборудование распределенной (feederless) архитектуры, на металлоконструкциях, крышах, парапетах, стенах зданий / сооружений предусмотрены кабель-росты с учётом типов предполагаемых к использованию фидеров и кабелей, рекомендаций по монтажу фирм-производителей фидеров и кабелей</td>
        <td>В норме</td>
        <td></td>
    </tr>
    <tr>
        <td>6.&nbsp;&nbsp;Предусмотрены мероприятия по обеспечению безопасной эксплуатации объекта в соответствии с распорядительными и В нормативными государственными и (или) отраслевыми документами.</td>
        <td>В норме</td>
        <td></td>
    </tr>
</table>
<p>*Значение будут уточнено в ходе тестового периода</p>
<p>Ответственный за проверку выполнения требований со стороны Ведущего</p>
<p><u>Начальник технического отдела КФ ТОО «КаР-Тел»     Имашев Б. Г</u>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<u>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</u></p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(подпись)</p>
<p style="text-align: right;"><#if taskSubmitDate?? && checkingByGuestResult?? && checkingByGuestResult == 'approved' >${taskSubmitDate?string['dd.MM.yyyy']} г.<#else>« ____ » ____________ 20___ г.</#if> </p>
</body>
</html>
