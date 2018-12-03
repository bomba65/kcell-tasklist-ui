create view bc_report as
	select t.proc_id as proc_id,
			(t.j->'businessKey'->>'val')::text as business_key,
			(t.j->'demandName'->>'val')::text as demand_name,
			(t.j->'BCData'->'val'->'data'->>'cashFlow')::json as cash_flow,
			(t.j->'BCData'->'val'->'data'->>'accurals')::json as accurals,
			(t.j->'BCData'->'val'->'data'->>'firstYear')::json as first_year,
			json_build_object(
			    'networkEconomics', (t.j->'BCData'->'val'->'data'->>'networkEconomics'),
			    'ipmDecision', (t.j->'BCData'->'val'->'data'->>'ipmDecision')
			)::json as general,
			json_build_object(
			    'benchmark', (t.j->'BCData'->'val'->'data'->>'benchmark')::json,
			    'npv', (t.j->'BCData'->'val'->'data'->>'npv'),
			    'roi', (t.j->'BCData'->'val'->'data'->>'roi'),
			    'paybackPeriod', (t.j->'BCData'->'val'->'data'->>'paybackPeriod'),
			    'plEffect', (t.j->'BCData'->'val'->'data'->>'plEffect'),
			    'cfEffect', (t.j->'BCData'->'val'->'data'->>'cfEffect')
			)::json as quantitative,
			json_build_object(
			    'strategicGoal', (t.j->'BCData'->'val'->'data'->>'strategicGoal'),
			    'strategyFit', (t.j->'BCData'->'val'->'data'->>'strategyFit'),
			    'businessPriority', (t.j->'BCData'->'val'->'data'->>'businessPriority'),
			    'opActivitiesImpact', (t.j->'BCData'->'val'->'data'->>'opActivitiesImpact')
			)::json as qualitative,
			json_build_object(
			    'quantitativeScore', (t.j->'BCData'->'val'->'data'->>'quantitativeScore'),
			    'qualitativeScore', (t.j->'BCData'->'val'->'data'->>'qualitativeScore'),
			    'strategyFitScore', (t.j->'BCData'->'val'->'data'->>'strategyFitScore'),
			    'businessPriorityScore', (t.j->'BCData'->'val'->'data'->>'businessPriorityScore'),
			    'score', (t.j->'BCData'->'val'->'data'->>'score')
			)::json as scoring,
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