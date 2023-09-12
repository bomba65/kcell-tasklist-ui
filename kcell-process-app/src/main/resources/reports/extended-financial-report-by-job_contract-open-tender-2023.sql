select
    case mainContract.text_
        when 'Roll-outRevision2020' then 'Roll-out and Revision 2020'
        else mainContract.text_
        end as mainContract,
    to_char(pi.start_time_ + interval '6 hour', 'YYYY') as "Year",
    to_char(pi.start_time_ + interval '6 hour', 'month') as "Month",
    substring(pi.business_key_ from '^[^-]+') as region,
    oblastName.text_ as "Oblast",
    sitename.text_ as sitename,
    pi.business_key_ as "JR No",
    case contractor.text_
        when '1' then 'avrora'
        when '2' then 'aicom'
        when '3' then 'spectr'
        when '4' then 'lse'
        when '5' then 'kcell'
        when '6' then 'Алта Телеком'
        when '7' then 'Логиком'
        when '8' then 'Arlan SI'
        when '9' then 'TOO Inter Service'
        when '10' then 'Forester-Hes Group'
        when '11' then 'Транстелеком'
        when '12' then 'Востоктелеком'
        when '-1' then 'Не выбран'
        else null
        end as "JR To",
    case reason.text_
        when '1' then 'Оптимизация и планирование'
        when '2' then 'Трансмиссия'
        when '3' then 'Инфраструктура'
        when '4' then 'Эксплуатация'
        when '5' then 'Строительно-монтажные работы'
        when '6' then 'Подготовка проекта'
        else null
        end as "JR Reason",
    to_timestamp(requestedDate.long_/1000) as "Requested Date",
    pi.start_user_id_ as "Requested By",
    to_timestamp(validityDate.long_/1000) as "Validity Date",
    to_timestamp(workStartDate.long_/1000) as "workStartDate",
    to_timestamp(integrationRunDate.long_/1000) as "integrationRunDate",
    to_timestamp(workCompletionDate.long_/1000) as "workCompletionDate",
    project.text_ as "Project",
    mtListSignDate.value_ + interval '6 hour' as "Material List Signing Date",
    acceptanceByInitiatorDate.value_ + interval '6 hour' as "Accept by Initiator",
    acceptMaint.value_ + interval '6 hour' as "Accept by Work Maintenance",
    acceptPlan.value_ + interval '6 hour' as "Accept by Work Planning",
    acceptanceDate.value_ + interval '6 hour' as "Acceptance Date",
    -- сюда еще нужно состав работ разбитый на строки
    aggregatedWorks.title as "Job Description",
    aggregatedWorks.quantity as "Quantity",
    aggregatedWorks.materialFrom as "Materials from",
    explanation.text_ as "Comments",
    case materialsRequired.text_
        when 'Yes' then 'required'
        else 'not required'
        end as "Customer Material",
    case pi.state_
        when 'ACTIVE' then 'In progress'
        else 'Closed'
        end as "Process State",
    CAST(convert_from(statusBytes.bytes_, 'UTF8') AS json)->>'parentStatus' as "JR Status",
    CAST(convert_from(statusBytes.bytes_, 'UTF8') AS json)->>'statusName' as "Detailed status",
    CAST(convert_from(statusBytes.bytes_, 'UTF8') AS json)->>'comment' as "Return reason",
    aggregatedWorks.unitWorkPrice as "Price without transport",
    aggregatedWorks.unitWorkPricePlusTx as "Price with transport",
    discount.text_ as "Price discount",
    monthlyAct.text_ as "Monthly act #",
    jrNumber.text_ as "JO#",
    job_list,
    pr_number.text_ as "PR",
    po_number.text_ as "PO",
    cm_number.text_ as "CM",
    avr_number.text_ as "AVR",
    esf_number.text_ as "ESF",
    to_timestamp(avr_date.long_/1000) + interval '6 hour' as "AVR date",
    to_timestamp(esf_date.long_/1000) + interval '6 hour' as "ESF date",
    ir.text_ as "IR"
from act_hi_procinst pi
         left join act_hi_varinst sitename
                   on pi.id_ = sitename.proc_inst_id_ and sitename.name_ = 'site_name'
         left join act_hi_varinst contractor
                   on pi.id_ = contractor.proc_inst_id_ and contractor.name_ = 'contractor'
         left join act_hi_varinst reason
                   on pi.id_ = reason.proc_inst_id_ and reason.name_ = 'reason'
         left join act_hi_varinst validityDate
                   on pi.id_ = validityDate.proc_inst_id_ and validityDate.name_ = 'validityDate'
         left join act_hi_varinst workStartDate
                   on pi.id_ = workStartDate.proc_inst_id_ and workStartDate.name_ = 'workStartDate'
         left join act_hi_varinst integrationRunDate
                   on pi.id_ = integrationRunDate.proc_inst_id_ and integrationRunDate.name_ = 'integrationRunDate'
         left join act_hi_varinst workCompletionDate
                   on pi.id_ = workCompletionDate.proc_inst_id_ and workCompletionDate.name_ = 'workCompletionDate'
         left join act_hi_varinst project
                   on pi.id_ = project.proc_inst_id_ and project.name_ = 'project'
         left join act_hi_varinst monthlyAct
                   on pi.id_ = monthlyAct.proc_inst_id_ and monthlyAct.name_ = 'monthActNumber'
         left join act_hi_varinst jrNumber
                   on pi.id_ = jrNumber.proc_inst_id_ and jrNumber.name_ = 'jrNumber'
         left join act_hi_varinst discount
                   on pi.id_ = discount.proc_inst_id_ and discount.name_ = 'discount'
         left join act_hi_varinst oblastName
                   on pi.id_ = oblastName.proc_inst_id_ and oblastName.name_ = 'oblastName'
         left join act_hi_varinst pr_number
                   on pi.id_ = pr_number.proc_inst_id_ and pr_number.name_ = 'pr_number'
         left join act_hi_varinst po_number
                   on pi.id_ = po_number.proc_inst_id_ and po_number.name_ = 'po_number'
         left join act_hi_varinst cm_number
                   on pi.id_ = cm_number.proc_inst_id_ and cm_number.name_ = 'cm_number'
         left join act_hi_varinst avr_number
                   on pi.id_ = avr_number.proc_inst_id_ and avr_number.name_ = 'avr_number'
         left join act_hi_varinst esf_number
                   on pi.id_ = esf_number.proc_inst_id_ and esf_number.name_ = 'esf_number'
         left join act_hi_varinst avr_date
                   on pi.id_ = avr_date.proc_inst_id_ and avr_date.name_ = 'avr_date'
         left join act_hi_varinst esf_date
                   on pi.id_ = esf_date.proc_inst_id_ and esf_date.name_ = 'esf_date'
         left join act_hi_varinst ir
                   on pi.id_ = ir.proc_inst_id_ and ir.name_ = 'ir'
         left join act_hi_varinst initiatorAcceptanceDate
                   on pi.id_ = initiatorAcceptanceDate.proc_inst_id_ and initiatorAcceptanceDate.name_ = 'initiatorAcceptanceDate'
         left join act_hi_varinst requestedDate
                   on pi.id_ = requestedDate.proc_inst_id_ and requestedDate.name_ = 'requestedDate'
         left join lateral (select max(ti.start_time_) as value_
                            from act_hi_taskinst ti
                            where pi.id_ = ti.proc_inst_id_
                              and ti.task_def_key_ in ('UserTask_14yc5q6', 'upload_tr_contractor'))
    as mtListSignDate
                   on true

         left join lateral (select max(ai.end_time_) as value_
                            from act_hi_actinst ai
                            where pi.id_ = ai.proc_inst_id_
                              and ai.act_id_ = 'endevt_accept_return_for_correction')
    as acceptReturn
                   on true
         left join lateral (select max(ti.end_time_) as value_
                            from act_hi_taskinst ti
                            where pi.id_ = ti.proc_inst_id_
                              and ti.task_def_key_ in ('accept_work_maintenance_group'))
    as acceptMaint
                   on acceptMaint.value_ > acceptReturn.value_
         left join lateral (select max(ti.end_time_) as value_
                            from act_hi_taskinst ti
                            where pi.id_ = ti.proc_inst_id_
                              and ti.task_def_key_ in ('accept_work_planning_group'))
    as acceptPlan
                   on acceptPlan.value_ > acceptReturn.value_

         left join act_hi_varinst acceptance
                   on pi.id_ = acceptance.proc_inst_id_
                       and acceptance.name_ = 'acceptPerformedJob'
         left join act_hi_varinst acceptAndSignByRegionHeadTaskResult
                   on pi.id_ = acceptAndSignByRegionHeadTaskResult.proc_inst_id_
                       and acceptAndSignByRegionHeadTaskResult.name_ = 'acceptAndSignByRegionHeadTaskResult'
         left join lateral (select max(ai.end_time_) as value_
                            from act_hi_actinst ai
                            where pi.id_ = ai.proc_inst_id_
                              and ai.act_id_ = 'SubProcess_0v7hq1m')
    as acceptanceDate
                   on acceptance.text_ in ('accepted','scan attached','invoiced')
                       and acceptAndSignByRegionHeadTaskResult.text_ = 'approved'
    -------------------------------------------------------------
    -- canceled, accepted, in progress, количество работ
         left join LATERAL (
    select works.proc_inst_id_,
           string_agg( (case rownum when 1 then works.title end), ', ' order by works.sapServiceNumber)  as title,         --"Job Description",
           --string_agg( (case rownum when 1 then cast(works.totalQuantityPerWorkType as char) end), ', ' order by works.sapServiceNumber)  as quantity,
           --sum(cast(works.unitWorkPrice as numeric))       as unitWorkPrice, --"Price without transport",
           --sum(cast(works.unitWorkPricePlusTx as numeric)) as unitWorkPricePlusTx --"Price with transport"
           --------------------------------
           string_agg( (case rownum when 1 then cast(works.totalQuantityPerWorkType as text) end), ', ' order by works.sapServiceNumber)  as quantity,
           string_agg( (case rownum when 1 then cast(works.materialsProvidedBy as text) end), ', ')  as materialFrom,
           sum(works.unitWorkPrice)             as unitWorkPrice,                     --"Price without transport",
           sum(works.unitWorkPricePlusTx)       as unitWorkPricePlusTx                --"Price with transport"
           --------------------------------
    from (
             select jobWorks.proc_inst_id_ as proc_inst_id_,
                    coalesce(worksPriceListJson.value->>'title',worksJson.value->>'displayServiceName') as title, --"Job Description",
                    row_number() OVER (PARTITION BY worksPriceListJson.value->>'title' order by worksPriceListBytes.id_) as rownum,
                    cast(case
                             when worksJson.value->>'sapServiceNumber' like 'RO%' then replace(worksJson.value->>'sapServiceNumber','RO','100')
                             when worksJson.value->>'sapServiceNumber' like 'FO-%' then replace(worksJson.value->>'sapServiceNumber','FO-','200')
                             when worksJson.value->>'sapServiceNumber' like 'SERV-%' then replace(worksJson.value->>'sapServiceNumber','SERV-','300')
                             when worksJson.value->>'sapServiceNumber' like '%.%' then replace(worksJson.value->>'sapServiceNumber','.','000')
                             else worksJson.value->>'sapServiceNumber'
                        end as int) as sapServiceNumber,
                    sum(cast(worksJson.value ->>'quantity' as double precision)) OVER (PARTITION BY worksJson.value->>'sapServiceNumber') as totalQuantityPerWorkType,
                    worksJson.value ->>'materialsProvidedBy' as materialsProvidedBy,
                    --workPricesJson.value ->>'basePriceByQuantity' as unitWorkPrice, --"Price without transport",
                    --workPricesJson.value ->>'netWorkPricePerSite' as unitWorkPricePlusTx --"Price with transport"
                    --------------------------------
                    cast(workPricesJson.value ->>'unitWorkPrice' as numeric) * cast(worksJson.value ->>'quantity' as double precision) as unitWorkPrice,            --"Price without transport",
                    cast(workPricesJson.value ->>'unitWorkPricePlusTx' as numeric) * cast(worksJson.value ->>'quantity' as double precision) as unitWorkPricePlusTx --"Price with transport"
                    --------------------------------
             from act_hi_varinst jobWorks
                      left join act_ge_bytearray jobWorksBytes
                                on jobWorks.bytearray_id_ = jobWorksBytes.id_
                      left join lateral (select row_number() over () as row_number, * from json_array_elements(CAST(convert_from(jobWorksBytes.bytes_, 'UTF8') AS json))) as worksJson
                                on true
                 ---------------------------------
                      left join act_hi_varinst workPrices
                                on workPrices.proc_inst_id_ = pi.id_ and workPrices.name_ = 'workPrices'
                      left join act_ge_bytearray workPricesBytes
                                on workPrices.bytearray_id_ = workPricesBytes.id_
                      left join lateral (select row_number() over () as row_number, * from json_array_elements(CAST(convert_from(workPricesBytes.bytes_, 'UTF8') AS json))) as workPricesJson
                                on true and worksJson.value->>'sapServiceNumber' = workPricesJson.value->>'sapServiceNumber' and worksJson.row_number = workPricesJson.row_number
                 ---------------------------------
                      left join act_hi_varinst worksPriceList
                                on worksPriceList.proc_inst_id_ = pi.id_ and worksPriceList.name_ = 'worksPriceList'
                      left join act_ge_bytearray worksPriceListBytes
                                on worksPriceList.bytearray_id_ = worksPriceListBytes.id_
                      left join json_array_elements(CAST(convert_from(worksPriceListBytes.bytes_, 'UTF8') AS json)) as worksPriceListJson
                                on true and worksJson.value->>'sapServiceNumber' = worksPriceListJson.value->>'sapServiceNumber'
                  ---------------------------------
             where jobWorks.proc_inst_id_ = pi.id_
               and jobWorks.name_ = 'jobWorks'
         ) works
    group by works.proc_inst_id_
    ) aggregatedWorks on true
    -------------------------------------------------------------
    -- relatedSites
         left join LATERAL (
    select string_agg(distinct sites.value->>'site_name',', ') as site_names
    from act_hi_varinst jobWorks
             left join act_ge_bytearray jobWorksBytes
                       on jobWorks.bytearray_id_ = jobWorksBytes.id_
             left join json_array_elements(CAST(convert_from(jobWorksBytes.bytes_, 'UTF8') AS json)) as worksJson
                       on true
             left join json_array_elements(worksJson.value->'relatedSites') as sites
                       on true
    where jobWorks.proc_inst_id_ = pi.id_
      and jobWorks.name_ = 'jobWorks'
    ) relatedSites on true

         left join act_hi_varinst acceptAndSignByInitiatorTaskResult
                   on pi.id_ = acceptAndSignByInitiatorTaskResult.proc_inst_id_
                       and acceptAndSignByInitiatorTaskResult.name_ = 'acceptAndSignByInitiatorTaskResult'
         left join lateral (select max(ti.end_time_) as value_
                            from act_hi_taskinst ti
                            where pi.id_ = ti.proc_inst_id_
                              and ti.task_def_key_ in ('accept_work_initiator')
    ) as acceptanceByInitiatorDate
                   on acceptance.text_ in ('accepted','scan attached','invoiced')
                       and acceptAndSignByInitiatorTaskResult.text_ = 'approved'

         left join act_hi_varinst explanation
                   on pi.id_ = explanation.proc_inst_id_ and explanation.name_ = 'explanation'
         left join act_hi_varinst materialsRequired
                   on pi.id_ = materialsRequired.proc_inst_id_ and materialsRequired.name_ = 'materialsRequired'

         left join act_hi_varinst status
                   on pi.id_ = status.proc_inst_id_ and status.name_ = 'status'
         left join act_ge_bytearray statusBytes
                   on status.bytearray_id_ = statusBytes.id_
         left join act_hi_varinst vi
                   on pi.id_ = vi.proc_inst_id_ and vi.name_ = 'resolutions'
         left join act_ge_bytearray ba
                   on vi.bytearray_id_ = ba.id_

         left join act_hi_varinst mainContract
                   on pi.id_ = mainContract.proc_inst_id_ and mainContract.name_ = 'mainContract'

         left join LATERAL (
           select string_agg(
                concat(
                 worksPriceListJson ->> 'title',
                '; qty: ',
                worksJson ->> 'quantity',
                '; materials: ',
                worksJson ->> 'materialsProvidedBy'
                )
                , chr(10)) as job_list
            from act_hi_varinst jobWorks
                     left join act_ge_bytearray jobWorksBytes
                               on jobWorks.bytearray_id_ = jobWorksBytes.id_
                     left join json_array_elements(CAST(convert_from(jobWorksBytes.bytes_, 'UTF8') AS json)) as worksJson
                               on true
                    left join act_hi_varinst worksPriceList
                                    on worksPriceList.proc_inst_id_ = pi.id_ and worksPriceList.name_ = 'worksPriceList'
                      left join act_ge_bytearray worksPriceListBytes
                                on worksPriceList.bytearray_id_ = worksPriceListBytes.id_
                    left join json_array_elements(CAST(convert_from(worksPriceListBytes.bytes_, 'UTF8') AS json)) as worksPriceListJson
                                        on true and worksJson.value->>'sapServiceNumber' = worksPriceListJson.value->>'sapServiceNumber'
            where jobWorks.proc_inst_id_ = pi.id_
              and jobWorks.name_ = 'jobWorks'

            ) jobList on true

where pi.proc_def_key_ = 'Revision' and pi.state_ <> 'EXTERNALLY_TERMINATED' and mainContract.text_ = 'open-tender-2023'
order by "Requested Date", "Job Description"
