create view bc_report as
	select t.proc_id as proc_id,
			(t.j->'businessKey'->>'val')::text as business_key,
			(t.j->'demandName'->>'val')::text as demand_name,
			(t.j->'BCData'->>'val')::json as business_case,
			(t.j->'supportiveInputs'->>'val')::json as support_requests,
			(t.j->'riskData'->>'val')::json as risks
	from (
		select v.proc_inst_id_ as proc_id, json_object_agg(
			v.name_,
			case
			when (v.name_ = 'businessKey' or v.name_ = 'demandName') then json_build_object('val', v.text_)
			when (v.name_ = 'BCData' or v.name_ = 'supportiveInputs' or v.name_ = 'riskData') then json_build_object('val', convert_from(b.bytes_, 'utf-8')::json)
			end
		) as j
		from act_hi_varinst as v left join act_ge_bytearray as b on v.bytearray_id_ = b.id_
		where v.name_ = 'businessKey' or v.name_ = 'demandName' or v.name_ = 'BCData' or v.name_ = 'supportiveInputs' or v.name_ = 'riskData'
		group by v.proc_inst_id_
	) as t;


create user bc_report_user with encrypted password 'bc_user_2018_pass';
grant select on table bc_report to bc_report_user;