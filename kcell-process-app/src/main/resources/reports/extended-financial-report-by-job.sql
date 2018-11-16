select
  to_char(pi.start_time_, 'YYYY') as "Year",
  to_char(pi.start_time_, 'month') as "Month",
  substring(pi.business_key_ from '^[^-]+') as region,
  sitename.text_ as sitename,
  pi.business_key_ as "JR No",
  case contractor.text_
  when '1' then 'avrora'
  when '2' then 'aicom'
  when '3' then 'spectr'
  when '4' then 'lse'
  when '5' then 'kcell'
  else null
  end as "JR To",
  case reason.text_
  when '1' then 'Optimization works'
  when '2' then 'Transmission works'
  when '3' then 'Infrastructure works'
  when '4' then 'Operation works'
  else null
  end as "JR Reason",
  pi.start_time_ as "Requested Date",
  pi.start_user_id_ as "Requested By",
  to_timestamp(validityDate.long_/1000) as "Validity Date",
  relatedTo.text_ as "Related to the",
  project.text_ as "Project",
  mtListSignDate.value_ as "Material List Signing Date",
  acceptanceByInitiatorDate.value_ as "Accept by Initiator",
  acceptMaint.value_ as "Accept by Work Maintenance",
  acceptPlan.value_ as "Accept by Work Planning",
  acceptanceDate.value_ as "Acceptance Date",
  -- сюда еще нужно состав работ разбитый на строки
  aggregatedWorks.title as "Job Description",
  aggregatedWorks.quantity as "Quantity",
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
  aggregatedWorks.unitWorkPrice as "Price without transport",
  aggregatedWorks.unitWorkPricePlusTx as "Price with transport",
  monthlyAct.text_ as "Monthly act #",
  jrNumber.text_ as "JO#",
  sapPRNo.text_ as "PR#",
  sapPRTotalValue.text_ as "PR Total Value",
  sapPRStatus.text_ as "PR Status",
  to_timestamp(sapPRApproveDate.long_/1000) as "PR Approval date",
  sapPONo.text_ as "PO#",
  invoiceNumber.text_ as "Invoice #",
  to_timestamp(invoiceDate.long_/1000) as "Invoice date",
  rejectedByRegionHead.value_ as "rejected by region head",
  rejectedByRegionHeadCount.value_ as "qty of rejects1",
  rejectedByPowerEngineerHead.value_ as "rejected by power engineer",
  rejectedByPowerEngineerHeadCount.value_ as "qty of rejects2",
  rejectedByCenterHead.value_ as "rejected by center",
  rejectedByCenterHeadCount.value_ as "qty of rejects3",
  rejectedByLeasingGroup.value_ as "rejected by leasing group",
  rejectedByLeasingGroupCount.value_ as "qty of rejects4",
  materialListRejectedByRegion.value_ as "Material list rejected by region",
  materialListRejectedByRegionCount.value_ as "qty of rejects5",
  materialListRejectedByCenter.value_ as "Material list rejected by center",
  materialListRejectedByCenterCount.value_ as "qty of rejects6",
  materialListRejectedByRegionTNU.value_ as "Material list rejected by region TNU",
  materialListRejectedByRegionTNUCount.value_ as "qty of rejects7",
  materialListRejectedByCenterPO.value_ as "Material list rejected by Center group P&O",
  materialListRejectedByCenterPOCount.value_ as "qty of rejects8",
  materialListRejectedByCenterSAO.value_ as "Material list rejected by Center group SAO",
  materialListRejectedByCenterSAOCount.value_ as "qty of rejects9",
  materialListRejectedByCenterTNU.value_ as "Material list rejected by Center group TNU",
  materialListRejectedByCenterTNUCount.value_ as "qty of rejects10",
  materialListRejectedByCenterSFM.value_ as "Material list rejected by Center group S&FM",
  materialListRejectedByCenterSFMCount.value_ as "qty of rejects11",
  trRejectedByRegion.value_ as "TR rejected by region",
  trRejectedByRegionCount.value_ as "qty of rejects12",
  trRejectedByCenterGroupPO.value_ as "TR rejected by Center group P&O",
  trRejectedByCenterGroupPOCount.value_ as "qty of rejects13",
  trRejectedByCenterGroupSAO.value_ as "TR rejected by Center group SAO",
  trRejectedByCenterGroupSAOCount.value_ as "qty of rejects14",
  trRejectedByCenterGroupTNU.value_ as "TR rejected by Center group TNU",
  trRejectedByCenterGroupTNUCount.value_ as "qty of rejects15",
  trRejectedByCenterGroupSFM.value_ as "TR rejected by Center group S&FM",
  trRejectedByCenterGroupSFMCount.value_ as "qty of rejects16",
  acceptanceRejectedByPermitTeam.value_ as "Acceptance rejected by Permit team",
  acceptanceRejectedByPermitTeamCount.value_ as "qty of rejects17",
  acceptanceRejectedByInitiator.value_ as "Acceptance rejected by Initiator",
  acceptanceRejectedByInitiatorCount.value_ as "qty of rejects18",
  acceptanceRejectedByMaintenanceGroup.value_ as "Acceptance rejected by maintenance group",
  acceptanceRejectedByMaintenanceGroupCount.value_ as "qty of rejects19",
  acceptanceRejectedByPlanningGroup.value_ as "Acceptance rejected by planning group",
  acceptanceRejectedByPlanningGroupCount.value_ as "qty of rejects20",
  acceptanceRejectedByRegionHead.value_ as "Acceptance rejected by region Head",
  acceptanceRejectedByRegionHeadCount.value_ as "qty of rejects21"
  from act_hi_procinst pi
  left join act_hi_varinst sitename
    on pi.id_ = sitename.proc_inst_id_ and sitename.name_ = 'site_name'
  left join act_hi_varinst contractor
    on pi.id_ = contractor.proc_inst_id_ and contractor.name_ = 'contractor'
  left join act_hi_varinst reason
    on pi.id_ = reason.proc_inst_id_ and reason.name_ = 'reason'
  left join act_hi_varinst validityDate
    on pi.id_ = validityDate.proc_inst_id_ and validityDate.name_ = 'validityDate'
  left join act_hi_varinst relatedTo
    on pi.id_ = relatedTo.proc_inst_id_ and relatedTo.name_ = 'relatedTo'
  left join act_hi_varinst project
    on pi.id_ = project.proc_inst_id_ and project.name_ = 'project'
  left join act_hi_varinst jobReason
    on pi.id_ = jobReason.proc_inst_id_ and jobReason.name_ = 'jobReason'
  left join act_hi_varinst typeOfExpenses
    on pi.id_ = typeOfExpenses.proc_inst_id_ and typeOfExpenses.name_ = 'typeOfExpenses'
  left join act_hi_varinst monthlyAct
    on pi.id_ = monthlyAct.proc_inst_id_ and monthlyAct.name_ = 'monthlyAct'
  left join act_hi_varinst jrNumber
    on pi.id_ = jrNumber.proc_inst_id_ and jrNumber.name_ = 'jrNumber'
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
             --string_agg(works.title, ', ')                   as title,         --"Job Description",
             --sum(cast(works.quantity as int))                as quantity,
             string_agg( (case rownum when 1 then works.title end), ', ' order by works.sapServiceNumber)  as title,         --"Job Description",
             string_agg( (case rownum when 1 then cast(works.totalQuantityPerWorkType as char) end), ', ' order by works.sapServiceNumber)  as quantity,
             sum(cast(works.unitWorkPrice as numeric))       as unitWorkPrice, --"Price without transport",
             sum(cast(works.unitWorkPricePlusTx as numeric)) as unitWorkPricePlusTx --"Price with transport"
      from (
              select --distinct
                     jobWorks.proc_inst_id_ as proc_inst_id_,
                     -- сюда еще нужно состав работ разбитый на строки
                     worksPriceListJson.value->>'title' as title, --"Job Description",
                     --worksJson.value ->>'quantity' as quantity, --"Quantity",
                     row_number() OVER (PARTITION BY worksPriceListJson.value->>'title' order by worksPriceListBytes.id_) as rownum,
                     cast(worksJson.value->>'sapServiceNumber' as int) as sapServiceNumber,
                     sum(cast(worksJson.value ->>'quantity' as int)) OVER (PARTITION BY worksJson.value->>'sapServiceNumber') as totalQuantityPerWorkType,
                     workPricesJson.value ->>'basePriceByQuantity' as unitWorkPrice, --"Price without transport",
                     workPricesJson.value ->>'netWorkPricePerSite' as unitWorkPricePlusTx --"Price with transport"
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

    ------------------------------------------------------------------
    -- Reject details and counts
    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'|| u.first_||' '||u.last_ ,', ') as value_
                      from act_hi_varinst vi
                        inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'rejected'
                        inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.task_def_key_ in ('approve_jr_regions','UserTask_11b2osi')
                        inner join act_id_user u on u.id_ = resolutionsJson.value->>'assignee'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where pi.id_ = vi.proc_inst_id_
                      and vi.name_ = 'resolutions'
      )
    as rejectedByRegionHead
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_varinst vi
                            inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected'
                            inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.task_def_key_ in ('approve_jr_regions','UserTask_11b2osi')
                          where pi.id_ = vi.proc_inst_id_
                          and vi.name_ = 'resolutions'
      )
    as rejectedByRegionHeadCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||u.first_||' '||u.last_,', ') as value_
                      from act_hi_varinst vi
                        inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'rejected'
                        inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.task_def_key_ in ('check_power','Task_1xhzfxw')
                        inner join act_id_user u on u.id_ = resolutionsJson.value->>'assignee'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where pi.id_ = vi.proc_inst_id_
                      and vi.name_ = 'resolutions'
      )
    as rejectedByPowerEngineerHead
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_varinst vi
                            inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected'
                            inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.task_def_key_ in ('check_power','Task_1xhzfxw')
                          where pi.id_ = vi.proc_inst_id_
                          and vi.name_ = 'resolutions'
      )
    as rejectedByPowerEngineerHeadCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||u.first_||' '||u.last_,', ') as value_
                      from act_hi_varinst vi
                        inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'rejected'
                        inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.task_def_key_ in ('approve_jr','UserTask_1qf7rmc')
                        inner join act_id_user u on u.id_ = resolutionsJson.value->>'assignee'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where pi.id_ = vi.proc_inst_id_
                      and vi.name_ = 'resolutions'
      )
    as rejectedByCenterHead
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_varinst vi
                            inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected'
                            inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.task_def_key_ in ('approve_jr','UserTask_1qf7rmc')
                          where pi.id_ = vi.proc_inst_id_
                          and vi.name_ = 'resolutions'
      )
    as rejectedByCenterHeadCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||u.first_||' '||u.last_,', ') as value_
                      from act_hi_varinst vi
                        inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'impossible'
                        inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.task_def_key_ in ('update_leasing_status_general','update_leasing_status_special','UserTask_1uw9qzb','Task_0euindd')
                        inner join act_id_user u on u.id_ = resolutionsJson.value->>'assignee'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where pi.id_ = vi.proc_inst_id_
                      and vi.name_ = 'resolutions'
      )
    as rejectedByLeasingGroup
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_varinst vi
                            inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'impossible'
                            inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.task_def_key_ in ('update_leasing_status_general','update_leasing_status_special','UserTask_1uw9qzb','Task_0euindd')
                          where pi.id_ = vi.proc_inst_id_
                          and vi.name_ = 'resolutions'
      )
    as rejectedByLeasingGroupCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||u.first_||' '||u.last_,', ') as value_
                      from act_hi_varinst vi
                        inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'rejected'
                        inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.task_def_key_ in ('approve_material_list_region','UserTask_12n8eyi')
                        inner join act_id_user u on u.id_ = resolutionsJson.value->>'assignee'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where pi.id_ = vi.proc_inst_id_
                      and vi.name_ = 'resolutions'
      )
    as materialListRejectedByRegion
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_varinst vi
                            inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected'
                            inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.task_def_key_ in ('approve_material_list_region','UserTask_12n8eyi')
                          where pi.id_ = vi.proc_inst_id_
                          and vi.name_ = 'resolutions'
      )
    as materialListRejectedByRegionCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||u.first_||' '||u.last_,', ') as value_
                      from act_hi_varinst vi
                        inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'return'
                        inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.name_ = 'Approve Material List by Center'
                        inner join act_id_user u on u.id_ = resolutionsJson.value->>'assignee'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where pi.id_ = vi.proc_inst_id_
                      and vi.name_ = 'resolutions'
      )
    as materialListRejectedByCenter
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_varinst vi
                            inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'return'
                            inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.name_ = 'Approve Material List by Center'
                          where pi.id_ = vi.proc_inst_id_
                          and vi.name_ = 'resolutions'
      )
    as materialListRejectedByCenterCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||u.first_||' '||u.last_,', ') as value_
                      from act_hi_varinst vi
                        inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'return'
                        inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.task_def_key_ = 'approve_material_list_tnu_region'
                        inner join act_id_user u on u.id_ = resolutionsJson.value->>'assignee'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where pi.id_ = vi.proc_inst_id_
                      and vi.name_ = 'resolutions'
      )
    as materialListRejectedByRegionTNU
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_varinst vi
                            inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'return'
                            inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.task_def_key_ = 'approve_material_list_tnu_region'
                          where pi.id_ = vi.proc_inst_id_
                          and vi.name_ = 'resolutions'
      )
    as materialListRejectedByRegionTNUCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||u.first_||' '||u.last_,', ') as value_
                      from act_hi_varinst vi
                        inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'return'
                        inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.name_ = 'Approve Material List by "P&O"'
                        inner join act_id_user u on u.id_ = resolutionsJson.value->>'assignee'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where pi.id_ = vi.proc_inst_id_
                      and vi.name_ = 'resolutions'
      )
    as materialListRejectedByCenterPO
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_varinst vi
                            inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'return'
                            inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.name_ = 'Approve Material List by "P&O"'
                          where pi.id_ = vi.proc_inst_id_
                          and vi.name_ = 'resolutions'
      )
    as materialListRejectedByCenterPOCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||u.first_||' '||u.last_,', ') as value_
                      from act_hi_varinst vi
                        inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'return'
                        inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.name_ = 'Approve Material List by "Operation"'
                        inner join act_id_user u on u.id_ = resolutionsJson.value->>'assignee'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where pi.id_ = vi.proc_inst_id_
                      and vi.name_ = 'resolutions'
      )
    as materialListRejectedByCenterSAO
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_varinst vi
                            inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'return'
                            inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.name_ = 'Approve Material List by "Operation"'
                          where pi.id_ = vi.proc_inst_id_
                          and vi.name_ = 'resolutions'
      )
    as materialListRejectedByCenterSAOCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||u.first_||' '||u.last_,', ') as value_
                      from act_hi_varinst vi
                        inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'return'
                        inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.name_ = 'Approve Material List by "Transmission"'
                        inner join act_id_user u on u.id_ = resolutionsJson.value->>'assignee'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where pi.id_ = vi.proc_inst_id_
                      and vi.name_ = 'resolutions'
      )
    as materialListRejectedByCenterTNU
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_varinst vi
                            inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'return'
                            inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.name_ = 'Approve Material List by "Transmission"'
                          where pi.id_ = vi.proc_inst_id_
                          and vi.name_ = 'resolutions'
      )
    as materialListRejectedByCenterTNUCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||u.first_||' '||u.last_,', ') as value_
                      from act_hi_varinst vi
                        inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'return'
                        inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.name_ = 'Approve Material List by "S&FM"'
                        inner join act_id_user u on u.id_ = resolutionsJson.value->>'assignee'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where pi.id_ = vi.proc_inst_id_
                      and vi.name_ = 'resolutions'
      )
    as materialListRejectedByCenterSFM
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_varinst vi
                            inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'return'
                            inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.name_ = 'Approve Material List by "S&FM"'
                          where pi.id_ = vi.proc_inst_id_
                          and vi.name_ = 'resolutions'
      )
    as materialListRejectedByCenterSFMCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||u.first_||' '||u.last_,', ') as value_
                      from act_hi_varinst vi
                        inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'return'
                        inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'doesNotMatch' and ti.task_def_key_ in ('validate_tr','Task_0jxwgbt')
                        inner join act_id_user u on u.id_ = resolutionsJson.value->>'assignee'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where pi.id_ = vi.proc_inst_id_
                      and vi.name_ = 'resolutions'
      )
    as trRejectedByRegion
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_varinst vi
                            inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'doesNotMatch'
                            inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.task_def_key_ in ('validate_tr','Task_0jxwgbt')
                          where pi.id_ = vi.proc_inst_id_
                          and vi.name_ = 'resolutions'
      )
    as trRejectedByRegionCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||u.first_||' '||u.last_,', ') as value_
                      from act_hi_varinst vi
                        inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'doesNotMatch'
                        inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.name_ = 'Validate TR by Center by "P&O"'
                        inner join act_id_user u on u.id_ = resolutionsJson.value->>'assignee'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where pi.id_ = vi.proc_inst_id_
                      and vi.name_ = 'resolutions'
      )
    as trRejectedByCenterGroupPO
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_varinst vi
                            inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'doesNotMatch'
                            inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.name_ = 'Validate TR by Center by "P&O"'
                          where pi.id_ = vi.proc_inst_id_
                          and vi.name_ = 'resolutions'
      )
    as trRejectedByCenterGroupPOCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||u.first_||' '||u.last_,', ') as value_
                      from act_hi_varinst vi
                        inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'doesNotMatch'
                        inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.name_ = 'Validate TR by Center by "Operation"'
                        inner join act_id_user u on u.id_ = resolutionsJson.value->>'assignee'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where pi.id_ = vi.proc_inst_id_
                      and vi.name_ = 'resolutions'
      )
    as trRejectedByCenterGroupSAO
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_varinst vi
                            inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'doesNotMatch'
                            inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.name_ = 'Validate TR by Center by "Operation"'
                          where pi.id_ = vi.proc_inst_id_
                          and vi.name_ = 'resolutions'
      )
    as trRejectedByCenterGroupSAOCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||u.first_||' '||u.last_,', ') as value_
                      from act_hi_varinst vi
                        inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'doesNotMatch'
                        inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.name_ = 'Validate TR by Center by "Transmission"'
                        inner join act_id_user u on u.id_ = resolutionsJson.value->>'assignee'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where pi.id_ = vi.proc_inst_id_
                      and vi.name_ = 'resolutions'
      )
    as trRejectedByCenterGroupTNU
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_varinst vi
                            inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'doesNotMatch'
                            inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.name_ = 'Validate TR by Center by "Transmission"'
                          where pi.id_ = vi.proc_inst_id_
                          and vi.name_ = 'resolutions'
      )
    as trRejectedByCenterGroupTNUCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||u.first_||' '||u.last_,', ') as value_
                      from act_hi_varinst vi
                        inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'doesNotMatch'
                        inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.name_ = 'Validate TR by Center by "S&FM"'
                        inner join act_id_user u on u.id_ = resolutionsJson.value->>'assignee'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where pi.id_ = vi.proc_inst_id_
                      and vi.name_ = 'resolutions'
      )
    as trRejectedByCenterGroupSFM
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_varinst vi
                            inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'doesNotMatch'
                            inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.name_ = 'Validate TR by Center by "S&FM"'
                          where pi.id_ = vi.proc_inst_id_
                          and vi.name_ = 'resolutions'
      )
    as trRejectedByCenterGroupSFMCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||u.first_||' '||u.last_,', ') as value_
                      from act_hi_varinst vi
                        inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'notApproved'
                        inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.task_def_key_ in ('verify_works','UserTask_0ib18ut')
                        inner join act_id_user u on u.id_ = resolutionsJson.value->>'assignee'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where pi.id_ = vi.proc_inst_id_
                      and vi.name_ = 'resolutions'
      )
    as acceptanceRejectedByPermitTeam
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_varinst vi
                            inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'notApproved'
                            inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.task_def_key_ in ('verify_works','UserTask_0ib18ut')
                          where pi.id_ = vi.proc_inst_id_
                          and vi.name_ = 'resolutions'
      )
    as acceptanceRejectedByPermitTeamCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||u.first_||' '||u.last_,', ') as value_
                      from act_hi_varinst vi
                        inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'rejected'
                        inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.task_def_key_ = 'accept_work_initiator'
                        inner join act_id_user u on u.id_ = resolutionsJson.value->>'assignee'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where pi.id_ = vi.proc_inst_id_
                      and vi.name_ = 'resolutions'
      )
    as acceptanceRejectedByInitiator
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_varinst vi
                            inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected'
                            inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.task_def_key_ = 'accept_work_initiator'
                          where pi.id_ = vi.proc_inst_id_
                          and vi.name_ = 'resolutions'
      )
    as acceptanceRejectedByInitiatorCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||u.first_||' '||u.last_,', ') as value_
                      from act_hi_varinst vi
                        inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'rejected'
                        inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.task_def_key_ = 'accept_work_maintenance_group'
                        inner join act_id_user u on u.id_ = resolutionsJson.value->>'assignee'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where pi.id_ = vi.proc_inst_id_
                      and vi.name_ = 'resolutions'
      )
    as acceptanceRejectedByMaintenanceGroup
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_varinst vi
                            inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected'
                            inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.task_def_key_ = 'accept_work_maintenance_group'
                          where pi.id_ = vi.proc_inst_id_
                          and vi.name_ = 'resolutions'
      )
    as acceptanceRejectedByMaintenanceGroupCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||u.first_||' '||u.last_,', ') as value_
                      from act_hi_varinst vi
                        inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'rejected'
                        inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.task_def_key_ = 'accept_work_planning_group'
                        inner join act_id_user u on u.id_ = resolutionsJson.value->>'assignee'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where pi.id_ = vi.proc_inst_id_
                      and vi.name_ = 'resolutions'
      )
    as acceptanceRejectedByPlanningGroup
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_varinst vi
                            inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected'
                            inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.task_def_key_ = 'accept_work_planning_group'
                          where pi.id_ = vi.proc_inst_id_
                          and vi.name_ = 'resolutions'
      )
    as acceptanceRejectedByPlanningGroupCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||u.first_||' '||u.last_,', ') as value_
                      from act_hi_varinst vi
                        inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'rejected'
                        inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.task_def_key_ = 'sign_region_head'
                        inner join act_id_user u on u.id_ = resolutionsJson.value->>'assignee'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where pi.id_ = vi.proc_inst_id_
                      and vi.name_ = 'resolutions'
      )
    as acceptanceRejectedByRegionHead
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_varinst vi
                            inner join act_ge_bytearray ba on vi.bytearray_id_ = ba.id_
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected'
                            inner join act_hi_taskinst ti on ti.id_ = resolutionsJson.value->>'taskId' and ti.task_def_key_ = 'sign_region_head'
                          where pi.id_ = vi.proc_inst_id_
                          and vi.name_ = 'resolutions'
      )
    as acceptanceRejectedByRegionHeadCount
    on true

where pi.proc_def_key_ = 'Revision' and pi.state_ <> 'EXTERNALLY_TERMINATED'
order by "Requested Date", "Job Description"
