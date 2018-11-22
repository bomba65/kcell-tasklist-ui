select
  to_char(pi.start_time_, 'YYYY') as "Year",
  to_char(pi.start_time_, 'month') as "Month",
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
  title.value_ as "Job Description",
  worksJson.value ->>'quantity' as "Quantity",
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
  totalWorkPrice.basePriceByQuantity as "Price without transport",
  totalWorkPrice.netWorkPricePerSite as "Price with transport",
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
                                    workPricesJson.value->>'basePriceByQuantity' as basePriceByQuantity,
                                    workPricesJson.value->>'netWorkPricePerSite' as netWorkPricePerSite
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

    ------------------------------------------------------------------
    -- Reject details and counts
        -- Reject details and counts
    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'|| coalesce(resolutionsJson.value->>'assigneeName', resolutionsJson.value->>'assignee') ,', ') as value_
                          from act_hi_taskinst ti
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                            left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                          where ti.task_def_key_ in ('approve_jr_regions','UserTask_11b2osi')
      )
    as rejectedByRegionHead
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_taskinst ti
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                            left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                          where ti.task_def_key_ in ('approve_jr_regions','UserTask_11b2osi')
      )
    as rejectedByRegionHeadCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||coalesce(resolutionsJson.value->>'assigneeName', resolutionsJson.value->>'assignee'),', ') as value_
                          from act_hi_taskinst ti
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                            left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                          where ti.task_def_key_ in ('check_power','Task_1xhzfxw')
      )
    as rejectedByPowerEngineerHead
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_taskinst ti
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                            left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                          where ti.task_def_key_ in ('check_power','Task_1xhzfxw')
      )
    as rejectedByPowerEngineerHeadCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||coalesce(resolutionsJson.value->>'assigneeName', resolutionsJson.value->>'assignee'),', ') as value_
                      from act_hi_taskinst ti
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where ti.task_def_key_ in ('approve_jr','UserTask_1qf7rmc')
      )
    as rejectedByCenterHead
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_taskinst ti
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                            left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                          where ti.task_def_key_ in ('approve_jr','UserTask_1qf7rmc')
      )
    as rejectedByCenterHeadCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||coalesce(resolutionsJson.value->>'assigneeName', resolutionsJson.value->>'assignee'),', ') as value_
                      from act_hi_taskinst ti
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where ti.task_def_key_ in ('update_leasing_status_general','update_leasing_status_special','UserTask_1uw9qzb','Task_0euindd')
      )
    as rejectedByLeasingGroup
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_taskinst ti
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                            left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                          where ti.task_def_key_ in ('update_leasing_status_general','update_leasing_status_special','UserTask_1uw9qzb','Task_0euindd')
      )
    as rejectedByLeasingGroupCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||coalesce(resolutionsJson.value->>'assigneeName', resolutionsJson.value->>'assignee'),', ') as value_
                      from act_hi_taskinst ti
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where ti.task_def_key_ in ('approve_material_list_region','UserTask_12n8eyi')
      )
    as materialListRejectedByRegion
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_taskinst ti
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                            left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                          where ti.task_def_key_ in ('approve_material_list_region','UserTask_12n8eyi')
      )
    as materialListRejectedByRegionCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||coalesce(resolutionsJson.value->>'assigneeName', resolutionsJson.value->>'assignee'),', ') as value_
                      from act_hi_taskinst ti
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where ti.name_ = 'Approve Material List by Center'
      )
    as materialListRejectedByCenter
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_taskinst ti
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                            left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                          where ti.name_ = 'Approve Material List by Center'
      )
    as materialListRejectedByCenterCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||coalesce(resolutionsJson.value->>'assigneeName', resolutionsJson.value->>'assignee'),', ') as value_
                      from act_hi_taskinst ti
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where ti.task_def_key_ = 'approve_material_list_tnu_region'
      )
    as materialListRejectedByRegionTNU
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_taskinst ti
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                            left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                          where ti.task_def_key_ = 'approve_material_list_tnu_region'
      )
    as materialListRejectedByRegionTNUCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||coalesce(resolutionsJson.value->>'assigneeName', resolutionsJson.value->>'assignee'),', ') as value_
                      from act_hi_taskinst ti
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'

                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where ti.name_ = 'Approve Material List by "P&O"'
      )
    as materialListRejectedByCenterPO
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_taskinst ti
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                            left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                          where ti.name_ = 'Approve Material List by "P&O"'
      )
    as materialListRejectedByCenterPOCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||coalesce(resolutionsJson.value->>'assigneeName', resolutionsJson.value->>'assignee'),', ') as value_
                      from act_hi_taskinst ti
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where ti.name_ = 'Approve Material List by "Operation"'
      )
    as materialListRejectedByCenterSAO
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_taskinst ti
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                            left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                          where ti.name_ = 'Approve Material List by "Operation"'
      )
    as materialListRejectedByCenterSAOCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||coalesce(resolutionsJson.value->>'assigneeName', resolutionsJson.value->>'assignee'),', ') as value_
                      from act_hi_taskinst ti
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where ti.name_ = 'Approve Material List by "Transmission"'
      )
    as materialListRejectedByCenterTNU
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_taskinst ti
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                            left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                          where ti.name_ = 'Approve Material List by "Transmission"'
      )
    as materialListRejectedByCenterTNUCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||coalesce(resolutionsJson.value->>'assigneeName', resolutionsJson.value->>'assignee'),', ') as value_
                      from act_hi_taskinst ti
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where ti.name_ = 'Approve Material List by "S&FM"'
      )
    as materialListRejectedByCenterSFM
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_taskinst ti
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                            left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                          where ti.name_ = 'Approve Material List by "S&FM"'
      )
    as materialListRejectedByCenterSFMCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||coalesce(resolutionsJson.value->>'assigneeName', resolutionsJson.value->>'assignee'),', ') as value_
                      from act_hi_taskinst ti
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where ti.task_def_key_ in ('validate_tr','Task_0jxwgbt')
      )
    as trRejectedByRegion
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_taskinst ti
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                            left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                          where ti.task_def_key_ in ('validate_tr','Task_0jxwgbt')
      )
    as trRejectedByRegionCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||coalesce(resolutionsJson.value->>'assigneeName', resolutionsJson.value->>'assignee'),', ') as value_
                      from act_hi_taskinst ti
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where ti.name_ = 'Validate TR by Center by "P&O"'
      )
    as trRejectedByCenterGroupPO
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_taskinst ti
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                            left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                          where ti.name_ = 'Validate TR by Center by "P&O"'
      )
    as trRejectedByCenterGroupPOCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||coalesce(resolutionsJson.value->>'assigneeName', resolutionsJson.value->>'assignee'),', ') as value_
                      from act_hi_taskinst ti
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where ti.name_ = 'Validate TR by Center by "Operation"'
      )
    as trRejectedByCenterGroupSAO
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_taskinst ti
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                            left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                          where ti.name_ = 'Validate TR by Center by "Operation"'
      )
    as trRejectedByCenterGroupSAOCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||coalesce(resolutionsJson.value->>'assigneeName', resolutionsJson.value->>'assignee'),', ') as value_
                      from act_hi_taskinst ti
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where ti.name_ = 'Validate TR by Center by "Transmission"'
      )
    as trRejectedByCenterGroupTNU
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_taskinst ti
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                            left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                          where ti.name_ = 'Validate TR by Center by "Transmission"'
      )
    as trRejectedByCenterGroupTNUCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||coalesce(resolutionsJson.value->>'assigneeName', resolutionsJson.value->>'assignee'),', ') as value_
                      from act_hi_taskinst ti
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where ti.name_ = 'Validate TR by Center by "S&FM"'
      )
    as trRejectedByCenterGroupSFM
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_taskinst ti
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                            left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                          where ti.name_ = 'Validate TR by Center by "S&FM"'
      )
    as trRejectedByCenterGroupSFMCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||coalesce(resolutionsJson.value->>'assigneeName', resolutionsJson.value->>'assignee'),', ') as value_
                      from act_hi_taskinst ti
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where ti.task_def_key_ in ('verify_works','UserTask_0ib18ut')
      )
    as acceptanceRejectedByPermitTeam
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_taskinst ti
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                            left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                          where ti.task_def_key_ in ('verify_works','UserTask_0ib18ut')
      )
    as acceptanceRejectedByPermitTeamCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||coalesce(resolutionsJson.value->>'assigneeName', resolutionsJson.value->>'assignee'),', ') as value_
                      from act_hi_taskinst ti
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where ti.task_def_key_ = 'accept_work_initiator'
      )
    as acceptanceRejectedByInitiator
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_taskinst ti
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                            left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                          where ti.task_def_key_ = 'accept_work_initiator'
      )
    as acceptanceRejectedByInitiatorCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||coalesce(resolutionsJson.value->>'assigneeName', resolutionsJson.value->>'assignee'),', ') as value_
                      from act_hi_taskinst ti
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where ti.task_def_key_ = 'accept_work_maintenance_group'
      )
    as acceptanceRejectedByMaintenanceGroup
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_taskinst ti
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                            left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                          where ti.task_def_key_ = 'accept_work_maintenance_group'
      )
    as acceptanceRejectedByMaintenanceGroupCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||coalesce(resolutionsJson.value->>'assigneeName', resolutionsJson.value->>'assignee'),', ') as value_
                      from act_hi_taskinst ti
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where ti.task_def_key_ = 'accept_work_planning_group'
      )
    as acceptanceRejectedByPlanningGroup
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_taskinst ti
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                            left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                          where ti.task_def_key_ = 'accept_work_planning_group'
      )
    as acceptanceRejectedByPlanningGroupCount
    on true

    left join lateral (select string_agg( coalesce(il.group_id_, 'without group')||':'||coalesce(resolutionsJson.value->>'assigneeName', resolutionsJson.value->>'assignee'),', ') as value_
                      from act_hi_taskinst ti
                        inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                        on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                        left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                      where ti.task_def_key_ = 'sign_region_head'
      )
    as acceptanceRejectedByRegionHead
    on true
    left join lateral (select count(resolutionsJson.value->>'taskId') as value_
                          from act_hi_taskinst ti
                            inner join json_array_elements(CAST(convert_from(ba.bytes_, 'UTF8') AS json)) as resolutionsJson
                            on true and resolutionsJson.value->>'resolution' = 'rejected' and ti.id_ = resolutionsJson.value->>'taskId'
                            left outer join act_hi_identitylink il on il.task_id_ = ti.id_ and il.group_id_ is not null and type_ = 'candidate'
                          where ti.task_def_key_ = 'sign_region_head'
      )
    as acceptanceRejectedByRegionHeadCount
    on true

where pi.proc_def_key_ = 'Revision' and pi.state_ <> 'EXTERNALLY_TERMINATED'
order by "Requested Date", "Job Description"
