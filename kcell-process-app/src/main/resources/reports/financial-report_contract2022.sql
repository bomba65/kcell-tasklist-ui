select
    case mainContract.text_
        when 'Roll-outRevision2020' then 'Roll-out and Revision 2020'
        else mainContract.text_
        end as mainContract,
    to_char(pi.start_time_ + interval '6 hour', 'YYYY') as "Year",
    to_char(pi.start_time_ + interval '6 hour', 'month') as "Month",
    substring(pi.business_key_ from '^[^-]+') as region,
    oblastName.text_ as "Oblast",
    case
        when relatedSites.site_names is not null then
            case
                when position(sitename.text_ in relatedSites.site_names) > 0 then relatedSites.site_names
                else sitename.text_ || ', ' || relatedSites.site_names
                end
        else
            sitename.text_
        end as sitename,
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
    to_timestamp(requestedDate.long_/1000) + interval '6 hour' as "Requested Date",
    pi.start_user_id_ as "Requested By",
    to_timestamp(validityDate.long_/1000) + interval '6 hour' as "Validity Date",
    to_timestamp(workStartDate.long_/1000) + interval '6 hour' as "workStartDate",
    to_timestamp(integrationRunDate.long_/1000) + interval '6 hour' as "integrationRunDate",
    to_timestamp(workCompletionDate.long_/1000) + interval '6 hour' as "workCompletionDate",
    relatedTo.text_ as "Related to the",
    project.text_ as "Project",
    mtListSignDate.value_ + interval '6 hour' as "Material List Signing Date",
    acceptanceByInitiatorDate.value_ + interval '6 hour' as "Accept by Initiator",
    acceptMaint.value_ + interval '6 hour' as "Accept by Work Maintenance",
    acceptPlan.value_ + interval '6 hour' as "Accept by Work Planning",
    acceptanceDate.value_ + interval '6 hour' as "Acceptance Date",
    -- сюда еще нужно состав работ разбитый на строки
    coalesce(title.value_, worksJson.value ->>'displayServiceName') as "Job Description",
    worksJson.value ->>'quantity' as "Quantity",
    worksJson.value ->> 'materialsProvidedBy' as "Materials from",
    jobReason.text_ as "Job reason",
    typeOfExpenses.text_ as "Type of expenses",
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
    totalWorkPrice.unitWorkPrice as "Price without transport",
    totalWorkPrice.unitWorkPricePlusTx as "Price with transport",
    totalWorkPrice.totalWithDiscount as "Price discount",
    monthlyAct.text_ as "Monthly act #",
    jrNumber.text_ as "JO#",
    sapPRNo.text_ as "PR#",
    sapPRTotalValue.text_ as "PR Total Value",
    sapPRStatus.text_ as "PR Status",
    to_timestamp(sapPRApproveDate.long_/1000) + interval '6 hour' as "PR Approval date",
    sapPONo.text_ as "PO#",
    invoiceNumber.text_ as "Invoice #",
    to_timestamp(invoiceDate.long_/1000) + interval '6 hour' as "Invoice date"
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
         left join act_hi_varinst relatedTo
                   on pi.id_ = relatedTo.proc_inst_id_ and relatedTo.name_ = 'relatedTo'
         left join act_hi_varinst project
                   on pi.id_ = project.proc_inst_id_ and project.name_ = 'project'
         left join act_hi_varinst jobReason
                   on pi.id_ = jobReason.proc_inst_id_ and jobReason.name_ = 'jobReason'
         left join act_hi_varinst typeOfExpenses
                   on pi.id_ = typeOfExpenses.proc_inst_id_ and typeOfExpenses.name_ = 'typeOfExpenses'
         left join act_hi_varinst monthlyAct
                   on pi.id_ = monthlyAct.proc_inst_id_ and monthlyAct.name_ = 'monthActNumber'
         left join act_hi_varinst jrNumber
                   on pi.id_ = jrNumber.proc_inst_id_ and jrNumber.name_ = 'jrNumber'
         left join act_hi_varinst discount
                   on pi.id_ = discount.proc_inst_id_ and discount.name_ = 'discount'
         left join act_hi_varinst oblastName
                   on pi.id_ = oblastName.proc_inst_id_ and oblastName.name_ = 'oblastName'
         left join act_hi_varinst sapPRNo
                   on pi.id_ = sapPRNo.proc_inst_id_ and sapPRNo.name_ = 'sapPRNo'
         left join act_hi_varinst sapPRTotalValue
                   on pi.id_ = sapPRTotalValue.proc_inst_id_ and sapPRTotalValue.name_ = 'sapPRTotalValue'
         left join act_hi_varinst sapPRStatus
                   on pi.id_ = sapPRStatus.proc_inst_id_ and sapPRStatus.name_ = 'sapPRStatus'
         left join act_hi_varinst sapPRApproveDate
                   on pi.id_ = sapPRApproveDate.proc_inst_id_ and sapPRApproveDate.name_ = 'sapPRApproveDate'
         left join act_hi_varinst sapPONo
                   on pi.id_ = sapPONo.proc_inst_id_ and sapPONo.name_ = 'sapPONo'
         left join act_hi_varinst invoiceNumber
                   on pi.id_ = invoiceNumber.proc_inst_id_ and invoiceNumber.name_ = 'invoiceNumber'
         left join act_hi_varinst invoiceDate
                   on pi.id_ = invoiceDate.proc_inst_id_ and invoiceDate.name_ = 'invoiceDate'
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

    -- canceled, accepted, in progress, количество работ
         left join act_hi_varinst jobWorks
                   on pi.id_ = jobWorks.proc_inst_id_ and jobWorks.name_ = 'jobWorks'
         left join act_ge_bytearray jobWorksBytes
                   on jobWorks.bytearray_id_ = jobWorksBytes.id_
         left join json_array_elements(CAST(convert_from(jobWorksBytes.bytes_, 'UTF8') AS json)) as worksJson
                   on true
         left join lateral (
    select distinct workPricesJson.value->>'sapServiceNumber' as sapServiceNumber,
                    workPricesJson.value->>'quantity' as quantity,
                    cast(workPricesJson.value->>'totalWithDiscount' as numeric) as totalWithDiscount,
                    --workPricesJson.value->>'basePriceByQuantity' as basePriceByQuantity,
                    --workPricesJson.value->>'netWorkPricePerSite' as netWorkPricePerSite
                    cast(workPricesJson.value ->>'unitWorkPrice' as numeric) * cast(worksJson.value ->>'quantity' as double precision) as unitWorkPrice,            --"Price without transport",
                    cast(workPricesJson.value ->>'unitWorkPricePlusTx' as numeric) * cast(worksJson.value ->>'quantity' as double precision) as unitWorkPricePlusTx --"Price with transport"
    from act_hi_varinst workPrices
             left join act_ge_bytearray workPricesBytes
                       on workPrices.bytearray_id_ = workPricesBytes.id_
             left join json_array_elements(CAST(convert_from(workPricesBytes.bytes_, 'UTF8') AS json)) as workPricesJson
                       on true
    where pi.id_ = workPrices.proc_inst_id_ and workPrices.name_ = 'workPrices'
    ) as totalWorkPrice
                   on worksJson.value->>'sapServiceNumber' = totalWorkPrice.sapServiceNumber
                       and worksJson.value->>'quantity' = totalWorkPrice.quantity
         left join lateral (
    select distinct worksPriceListJson.value->>'title' as value_
    from act_hi_varinst worksPriceList
             inner join act_ge_bytearray worksPriceListBytes
                        on worksPriceList.bytearray_id_ = worksPriceListBytes.id_
             inner join json_array_elements(CAST(convert_from(worksPriceListBytes.bytes_, 'UTF8') AS json)) as worksPriceListJson
                        on true and worksJson.value->>'sapServiceNumber' = worksPriceListJson.value->>'sapServiceNumber'
    where pi.id_ = worksPriceList.proc_inst_id_ and worksPriceList.name_ = 'worksPriceList'
    ) as title
                   on true
    -------------------------------------------------------------
    -- relatedSites
         left join LATERAL (
    select string_agg(distinct sites.value->>'site_name',', ') as site_names,
            worksJson.value->>'sapServiceNumber' as sapServiceNumber
    from act_hi_varinst jobWorks
             left join act_ge_bytearray jobWorksBytes
                       on jobWorks.bytearray_id_ = jobWorksBytes.id_
             left join json_array_elements(CAST(convert_from(jobWorksBytes.bytes_, 'UTF8') AS json)) as worksJson
                       on true
             left join json_array_elements(worksJson.value->'relatedSites') as sites
                       on true
    where jobWorks.proc_inst_id_ = pi.id_
      and jobWorks.name_ = 'jobWorks'
    GROUP BY
	 sapServiceNumber
    ) relatedSites on worksJson.value->>'sapServiceNumber'=relatedSites.sapServiceNumber

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

where pi.proc_def_key_ = 'Revision' and pi.state_ <> 'EXTERNALLY_TERMINATED' and mainContract.text_ = '2022Work-agreement'
order by "Requested Date", "Job Description"
