create or replace function
  bc_report_json_concat(j json, nj json) returns jsonb
language plpgsql
as $$
declare
result jsonb;
begin
	select json_agg(t.rw)::jsonb
	from (
		select (value::jsonb || nj::jsonb)::jsonb as rw
		from json_array_elements(j)
	) as t into result;
	return result;
end
$$;

drop view if exists bc_report;
create view bc_report as
select * from (
    select bcdata.business_key,
        bcdata.demand_name,
        bcdata.network_economics,
        bcdata.ipm_decision,
        bcdata.min_pp,
        bcdata.max_roi,
        bcdata.max_npv,
        bcdata.max_pl,
        bcdata.max_cd,
        bcdata.wacc,
        bcdata.npv,
        bcdata.roi,
        bcdata.payback_period,
        bcdata.p_l_effect_first_year,
        bcdata.c_f_effect_first_year,
        bcdata.strategic_goal,
        bcdata.strategy_fit,
        bcdata.business_priority,
        bcdata.operational_activities_impact,
        bcdata.quantitative_score,
        bcdata.qualitative_score,
        bcdata.strategy_fit_score,
        bcdata.business_priority_score,
        bcdata.score,
        bcdata.first_year,
        d."table",
        d."section",
        d."rocName" as roc_name,
        d.wbs as wbs_line_name,
        d.cost as cost_executor,
        d.hyperion as hyperion_definition,
        d.investment1 as capex_investment_1,
        d.investment2 as capex_investment_2,
        d.purchasing as purchasing_group,
        d."vendorName" as vendor_name,
        d."vendorCode" as vendor_code,
        d.contract as cm_contract,
        d."contractAdd" as cm_contract_add,
        d."startDate" as agreement_start,
        d."endDate" as agreement_end,
        d.amount as total_contract_amount,
        d."contractAmount" as contract_amount_project,
        d.currency,
        d."depCode" as dep_code,
        y."1" as "year_1",
        y."2" as "year_2",
        y."3" as "year_3",
        y."4" as "year_4",
        y."5" as "year_5",
        m1."Jan" as "year_1_Jan",
        m1."Feb" as "year_1_Feb",
        m1."Mar" as "year_1_Mar",
        m1."Apr" as "year_1_Apr",
        m1."May" as "year_1_May",
        m1."Jun" as "year_1_Jun",
        m1."Jul" as "year_1_Jul",
        m1."Aug" as "year_1_Aug",
        m1."Sep" as "year_1_Sep",
        m1."Oct" as "year_1_Oct",
        m1."Nov" as "year_1_Nov",
        m1."Dec" as "year_1_Dec",
        m2."Jan" as "year_2_Jan",
        m2."Feb" as "year_2_Feb",
        m2."Mar" as "year_2_Mar",
        m2."Apr" as "year_2_Apr",
        m2."May" as "year_2_May",
        m2."Jun" as "year_2_Jun",
        m2."Jul" as "year_2_Jul",
        m2."Aug" as "year_2_Aug",
        m2."Sep" as "year_2_Sep",
        m2."Oct" as "year_2_Oct",
        m2."Nov" as "year_2_Nov",
        m2."Dec" as "year_2_Dec"
    from (
        select t.proc_id,
            (t.j->'businessKey'->>'val')::text as business_key,
            (t.j->'demandName'->>'val')::text as demand_name,
			(t.j->'BCData'->'val'->'data'->>'networkEconomics') as network_economics,
			(t.j->'BCData'->'val'->'data'->>'ipmDecision') as ipm_decision,
            (t.j->'BCData'->'val'->'data'->'benchmark'->>'minPP')::float as min_pp,
            (t.j->'BCData'->'val'->'data'->'benchmark'->>'maxROI')::float as max_roi,
            (t.j->'BCData'->'val'->'data'->'benchmark'->>'maxNPV')::float as max_npv,
            (t.j->'BCData'->'val'->'data'->'benchmark'->>'maxPL')::float as max_pl,
            (t.j->'BCData'->'val'->'data'->'benchmark'->>'maxCF')::float as max_cd,
            (t.j->'BCData'->'val'->'data'->'benchmark'->>'wacc')::float as wacc,
            (t.j->'BCData'->'val'->'data'->>'npv')::float as npv,
            (t.j->'BCData'->'val'->'data'->>'roi')::float as roi,
            (t.j->'BCData'->'val'->'data'->>'paybackPeriod')::float as payback_period,
            (t.j->'BCData'->'val'->'data'->>'plEffect')::float as p_l_effect_first_year,
            (t.j->'BCData'->'val'->'data'->>'cfEffect')::float as c_f_effect_first_year,
            (t.j->'BCData'->'val'->'data'->>'strategicGoal')::text as strategic_goal,
            (t.j->'BCData'->'val'->'data'->>'strategyFit')::text as strategy_fit,
            (t.j->'BCData'->'val'->'data'->>'businessPriority')::text as business_priority,
            (t.j->'BCData'->'val'->'data'->>'opActivitiesImpact')::text as operational_activities_impact,
            (t.j->'BCData'->'val'->'data'->>'quantitativeScore')::float as quantitative_score,
            (t.j->'BCData'->'val'->'data'->>'qualitativeScore')::float as qualitative_score,
            (t.j->'BCData'->'val'->'data'->>'strategyFitScore')::float as strategy_fit_score,
            (t.j->'BCData'->'val'->'data'->>'businessPriorityScore')::float as business_priority_score,
            (t.j->'BCData'->'val'->'data'->>'score')::float as score,
			(t.j->'BCData'->'val'->'data'->>'firstYear')::int as first_year,
            t.j->'BCData'->'val'->'data'->'cashFlow' as cashflow,
            t.j->'BCData'->'val'->'data'->'accurals' as accurals
        from (
            select v.proc_inst_id_ as proc_id, json_object_agg(
                v.name_,
                case
                when (v.name_ = 'businessKey' or v.name_ = 'demandName') then json_build_object('val', v.text_)
                when (v.name_ = 'BCData') then json_build_object('val', convert_from(b.bytes_, 'utf-8')::json)
                end
            ) as j
            from act_hi_varinst as v left join act_ge_bytearray as b on v.bytearray_id_ = b.id_
            where v.name_ = 'businessKey' or v.name_ = 'demandName' or v.name_ = 'BCData'
            group by v.proc_inst_id_
        ) as t
    ) as bcdata,
	jsonb_to_recordset(
	    bc_report_json_concat(bcdata.cashflow->'revenues', json_build_object('table', 'cashFlow', 'section', 'revenue'))
	    || bc_report_json_concat(bcdata.cashflow->'opexes', json_build_object('table', 'cashFlow', 'section', 'opex'))
	    || bc_report_json_concat(bcdata.cashflow->'capexes', json_build_object('table', 'cashFlow', 'section', 'capex'))
	    || bc_report_json_concat(bcdata.cashflow->'cogs', json_build_object('table', 'cashFlow', 'section', 'cogs'))
	    || bc_report_json_concat(bcdata.cashflow->'income', json_build_object('table', 'cashFlow', 'section', 'income'))
--	    || ((bcdata.cashflow->'revenuesTotal')::jsonb || jsonb_build_object('table', 'cashFlow', 'section', 'revenuesTotal'))
	    || bc_report_json_concat(bcdata.accurals->'revenues', json_build_object('table', 'accurals', 'section', 'revenue'))
	    || bc_report_json_concat(bcdata.accurals->'opexes', json_build_object('table', 'accurals', 'section', 'opex'))
	    || bc_report_json_concat(bcdata.accurals->'capexes', json_build_object('table', 'accurals', 'section', 'capex'))
	    || bc_report_json_concat(bcdata.accurals->'cogs', json_build_object('table', 'accurals', 'section', 'cogs'))
	    || bc_report_json_concat(bcdata.accurals->'income', json_build_object('table', 'accurals', 'section', 'income'))
	) as d (
        "table" text,
        "section" text,
        "rocName" text,
        "wbs" text,
        "cost" text,
        "hyperion" text,
        "investment1" text,
        "investment2" text,
        "purchasing" text,
        "vendorName" text,
        "vendorCode" text,
        "contract" text,
        "contractAdd" text,
        "startDate" text,
        "endDate" text,
        "amount" text,
        "contractAmount" text,
        "currency" text,
        "depCode" text,
        "year" json,
        "month" json
    ),
	json_to_record(d.year) as y (
	    "1" float,
        "2" float,
        "3" float,
        "4" float,
        "5" float
	),
	json_to_record(d.month->'1') as m1 (
	    "Jan" float,
        "Feb" float,
        "Mar" float,
        "Apr" float,
        "May" float,
        "Jun" float,
        "Jul" float,
        "Aug" float,
        "Sep" float,
        "Oct" float,
        "Nov" float,
        "Dec" float
	),
	json_to_record(d.month->'2') as m2 (
	    "Jan" float,
        "Feb" float,
        "Mar" float,
        "Apr" float,
        "May" float,
        "Jun" float,
        "Jul" float,
        "Aug" float,
        "Sep" float,
        "Oct" float,
        "Nov" float,
        "Dec" float
	)
) as r;

create user bc_report_user with encrypted password 'bc_user_2018_pass';
grant select on table bc_report to bc_report_user;


