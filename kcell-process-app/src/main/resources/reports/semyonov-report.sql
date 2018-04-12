-- отчет для Семенова
select
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
  case pi.state_
  when 'ACTIVE' then 'Open/In progress JR'
  else case pi.end_act_id_
       when 'EndEvent_1fo49fj' then 'Rejected JR'
       when 'endevt_create_jr_rejected' then 'Rejected JR'
       when 'endevt_createjr_cancelled' then 'Cancelled JR'
       when 'endevt_revision' then 'Completed JR'
       else 'Closed JR (' || pi.end_act_id_ || ')'
       end
  end as "JR Status",
  pi.start_time_ as "Requested Date",
  pi.start_user_id_ as "Requested By",
  validityDate.text_ as "Validity Date",
  mtListSignDate.value_ as "Material List Signing Date",
  acceptMaint.value_ as "Accept by Work Maintenance",
  acceptPlan.value_ as "Accept by Work Planning",
  acceptanceDate.value_ as "Acceptance Date",
  -- сюда еще нужно состав работ разбитый на строки
  worksPriceListJson.value->>'title' as "Job Description",
  worksJson.value ->>'quantity' as "Quantity",
  explanation.text_ as "Comments",
  case materialsRequired.text_
  when 'Yes' then 'required'
  else 'not required'
  end as "Customer Material"
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
  left join lateral (select max(ai.end_time_) as value_
                     from act_hi_actinst ai
                     where pi.id_ = ai.proc_inst_id_
                           and ai.act_id_ = 'SubProcess_0v7hq1m')
    as acceptanceDate
    on acceptance.text_ = 'accepted'

  -- canceled, accepted, in progress, количество работ

  left join act_hi_varinst jobWorks
    on pi.id_ = jobWorks.proc_inst_id_ and jobWorks.name_ = 'jobWorks'
  left join act_ge_bytearray jobWorksBytes
    on jobWorks.bytearray_id_ = jobWorksBytes.id_
  left join json_array_elements(CAST(convert_from(jobWorksBytes.bytes_, 'UTF8') AS json)) as worksJson
    on true

  left join act_hi_varinst worksPriceList
    on pi.id_ = worksPriceList.proc_inst_id_ and worksPriceList.name_ = 'worksPriceList'
  left join act_ge_bytearray worksPriceListBytes
    on worksPriceList.bytearray_id_ = worksPriceListBytes.id_
  left join json_array_elements(CAST(convert_from(worksPriceListBytes.bytes_, 'UTF8') AS json)) as worksPriceListJson
    on true and worksJson.value->>'sapServiceNumber' = worksPriceListJson.value->>'sapServiceNumber'

  left join act_hi_varinst explanation
    on pi.id_ = explanation.proc_inst_id_ and explanation.name_ = 'explanation'
  left join act_hi_varinst materialsRequired
    on pi.id_ = materialsRequired.proc_inst_id_ and materialsRequired.name_ = 'materialsRequired'
where pi.proc_def_key_ = 'Revision' and pi.state_ <> 'EXTERNALLY_TERMINATED'
order by "Requested Date", "Job Description"
--limit 5