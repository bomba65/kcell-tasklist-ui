select
    case mainContract.text_
        when 'Roll-outRevision2020' then 'Roll-out and Revision 2020'
        else mainContract.text_
        end as mainContract,
    substring(pi.business_key_ from '^[^-]+') as region,
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
        when '-1' then 'Не выбран'
        else null
        end as "JR To",
    case reason.text_
        when '1' then 'Optimization works'
        when '2' then 'Transmission works'
        when '3' then 'Infrastructure works'
        when '4' then 'Operation works'
        when '5' then 'Roll-out works'
        else null
        end as "JR Reason",
    pi.start_time_ + interval '6 hour' as "Requested Date",
    pi.start_user_id_ as "Requested By",
    to_timestamp(validityDate.long_/1000) + interval '6 hour' as "Validity Date",
    mtListSignDate.value_ + interval '6 hour' as "Material List Signing Date",
    acceptanceByInitiatorDate.value_ + interval '6 hour' as "Accept by Initiator",
    acceptMaint.value_ + interval '6 hour' as "Accept by Work Maintenance",
    acceptPlan.value_ + interval '6 hour' as "Accept by Work Planning",
    acceptanceDate.value_ + interval '6 hour' as "Acceptance Date",
    aggregatedWorks.title as "Job Description",
    aggregatedWorks.quantity as "Quantity",
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
    CAST(convert_from(statusBytes.bytes_, 'UTF8') AS json)->>'comment' as "Return reason"
from act_hi_procinst pi
         left join act_hi_varinst sitename
                   on pi.id_ = sitename.proc_inst_id_ and sitename.name_ = 'site_name'
         left join act_hi_varinst contractor
                   on pi.id_ = contractor.proc_inst_id_ and contractor.name_ = 'contractor'
         left join act_hi_varinst reason
                   on pi.id_ = reason.proc_inst_id_ and reason.name_ = 'reason'
         left join act_hi_varinst validityDate
                   on pi.id_ = validityDate.proc_inst_id_ and validityDate.name_ = 'validityDate'
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
           --string_agg( (case rownum when 1 then cast(works.totalQuantityPerWorkType as char) end), ', ' order by works.sapServiceNumber)  as quantity
           string_agg( (case rownum when 1 then cast(works.totalQuantityPerWorkType as text) end), ', ' order by works.sapServiceNumber)  as quantity
    from (
             select jobWorks.proc_inst_id_ as proc_inst_id_,
                    coalesce(worksPriceListJson.value->>'title', worksJson.value->>'displayServiceName') as title, --"Job Description",
                    row_number() OVER (PARTITION BY worksPriceListJson.value->>'title' order by worksPriceListBytes.id_) as rownum,
                    cast(case
                             when worksJson.value->>'sapServiceNumber' like 'RO%' then replace(worksJson.value->>'sapServiceNumber','RO','100')
                             when worksJson.value->>'sapServiceNumber' like 'FO-%' then replace(worksJson.value->>'sapServiceNumber','FO-','200')
                             when worksJson.value->>'sapServiceNumber' like 'SERV-%' then replace(worksJson.value->>'sapServiceNumber','SERV-','300')
                             when worksJson.value->>'sapServiceNumber' like '%.%' then replace(worksJson.value->>'sapServiceNumber','.','000')
                             else worksJson.value->>'sapServiceNumber'
                        end as int) as sapServiceNumber,
                    sum(cast(worksJson.value ->>'quantity' as double precision)) OVER (PARTITION BY worksJson.value->>'sapServiceNumber') as totalQuantityPerWorkType
             from act_hi_varinst jobWorks
                      left join act_ge_bytearray jobWorksBytes
                                on jobWorks.bytearray_id_ = jobWorksBytes.id_
                      left join json_array_elements(CAST(convert_from(jobWorksBytes.bytes_, 'UTF8') AS json)) as worksJson
                                on true
                 ---------------------------------
                      left join act_hi_varinst workPrices
                                on workPrices.proc_inst_id_ = pi.id_ and workPrices.name_ = 'workPrices'
                      left join act_ge_bytearray workPricesBytes
                                on workPrices.bytearray_id_ = workPricesBytes.id_
                      left join json_array_elements(CAST(convert_from(workPricesBytes.bytes_, 'UTF8') AS json)) as workPricesJson
                                on true and worksJson.value->>'sapServiceNumber' = workPricesJson.value->>'sapServiceNumber'
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

         left join act_hi_varinst mainContract
                   on pi.id_ = mainContract.proc_inst_id_ and mainContract.name_ = 'mainContract'

where pi.proc_def_key_ = 'Revision' and pi.state_ <> 'EXTERNALLY_TERMINATED'
order by "Requested Date", "Job Description"
