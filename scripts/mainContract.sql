INSERT INTO public.act_hi_varinst (id_, proc_def_key_, proc_def_id_, proc_inst_id_, execution_id_, act_inst_id_, name_, var_type_, rev_, text_, state_)
select s.vuuid, 'Revision', s.proc_def_id_, s.proc_ins_id_, s.execution_id_, s.act_inst_id_, 'mainContract', 'string', 0, 'Revision', 'CREATED'
from (
    select uuid_generate_v4() AS vuuid,
          p.id_ as proc_ins_id_,
          v.proc_def_id_ as proc_def_id_,
          v.execution_id_ as execution_id_,
          v.act_inst_id_ as act_inst_id_
    from act_hi_procinst p
        inner join act_hi_varinst v on v.proc_inst_id_ = p.proc_inst_id_ and v.name_ = 'reason'
    where p.proc_def_key_ = 'Revision'
    and p.proc_inst_id_ not in (
      select v.proc_inst_id_
      from act_hi_varinst
      where name_ = 'mainContract'
    )
) as s;

INSERT INTO public.act_ru_variable (id_, rev_, type_, name_, execution_id_, proc_inst_id_, text_, var_scope_, sequence_counter_, is_concurrent_local_)
select s.id_, 1, 'string', 'mainContract', s.execution_id_, s.proc_inst_id_, 'Revision', s.proc_inst_id_, 1, false
from (
    select
        v.id_ as id_,
        arv.execution_id_ as execution_id_,
        arv.proc_inst_id_ as proc_inst_id_
    from act_hi_procinst p
        inner join act_ru_execution e on p.proc_inst_id_ = e.proc_inst_id_ and e.parent_id_ is null
        inner join act_hi_varinst v on v.proc_inst_id_ = p.proc_inst_id_ and v.name_ = 'mainContract'
        inner join act_ru_variable arv on e.proc_inst_id_ = arv.proc_inst_id_ and arv.name_ = 'reason'
    where p.proc_def_key_ = 'Revision'
    and p.proc_inst_id_ not in (
        select proc_inst_id_
        from act_ru_variable
        where name_ = 'mainContract'
    )
) as s;


--  для месячного акта
INSERT INTO public.act_hi_varinst (id_, proc_def_key_, proc_def_id_, proc_inst_id_, execution_id_, act_inst_id_, name_, var_type_, rev_, text_, state_)
select s.vuuid, 'Invoice', s.proc_def_id_, s.proc_ins_id_, s.execution_id_, s.act_inst_id_, 'mainContract', 'string', 0, 'Revision', 'CREATED'
from (
    select uuid_generate_v4() AS vuuid,
          p.id_ as proc_ins_id_,
          v.proc_def_id_ as proc_def_id_,
          v.execution_id_ as execution_id_,
          v.act_inst_id_ as act_inst_id_
    from act_hi_procinst p
        inner join act_hi_varinst v on v.proc_inst_id_ = p.proc_inst_id_ and v.name_ = 'workType'
    where p.proc_def_key_ = 'Invoice'
    and p.proc_inst_id_ not in (
      select proc_inst_id_
      from act_hi_varinst
      where name_ = 'mainContract'
    )
) as s;

INSERT INTO public.act_ru_variable (id_, rev_, type_, name_, execution_id_, proc_inst_id_, text_, var_scope_, sequence_counter_, is_concurrent_local_)
select s.id_, 1, 'string', 'mainContract', s.execution_id_, s.proc_inst_id_, 'Revision', s.proc_inst_id_, 1, false
from (
    select
        v.id_ as id_,
        arv.execution_id_ as execution_id_,
        arv.proc_inst_id_ as proc_inst_id_
    from act_hi_procinst p
        inner join act_ru_execution e on p.proc_inst_id_ = e.proc_inst_id_ and e.parent_id_ is null
        inner join act_hi_varinst v on v.proc_inst_id_ = p.proc_inst_id_ and v.name_ = 'mainContract'
        inner join act_ru_variable arv on e.proc_inst_id_ = arv.proc_inst_id_ and arv.name_ = 'workType'
    where p.proc_def_key_ = 'Invoice'
    and p.proc_inst_id_ not in (
        select proc_inst_id_
        from act_ru_variable
        where name_ = 'mainContract'
    )
) as s;


-----------------------------------------------------------------------------------------------------------------------------------------------------------

INSERT INTO public.act_hi_varinst (id_, proc_def_key_, proc_def_id_, proc_inst_id_, execution_id_, act_inst_id_, name_, var_type_, rev_, text_, state_)
select s.vuuid, 'Revision', s.proc_def_id_, s.proc_ins_id_, s.execution_id_, s.act_inst_id_, 'rolloutRO1', 'string', 0, 'false', 'CREATED'
from (
    select uuid_generate_v4() AS vuuid,
          p.id_ as proc_ins_id_,
          v.proc_def_id_ as proc_def_id_,
          v.execution_id_ as execution_id_,
          v.act_inst_id_ as act_inst_id_
    from act_hi_procinst p
        inner join act_hi_varinst v on v.proc_inst_id_ = p.proc_inst_id_ and v.name_ = 'mainContract' and v.text_ = 'Roll-out'
    where p.proc_def_key_ = 'Revision'
    and p.proc_inst_id_ not in (
      select v.proc_inst_id_
      from act_hi_varinst
      where name_ = 'rolloutRO1'
    )
) as s;

INSERT INTO public.act_ru_variable (id_, rev_, type_, name_, execution_id_, proc_inst_id_, text_, var_scope_, sequence_counter_, is_concurrent_local_)
select s.id_, 1, 'string', 'rolloutRO1', s.execution_id_, s.proc_inst_id_, 'false', s.proc_inst_id_, 1, false
from (
    select
        v.id_ as id_,
        arv.execution_id_ as execution_id_,
        arv.proc_inst_id_ as proc_inst_id_
    from act_hi_procinst p
        inner join act_ru_execution e on p.proc_inst_id_ = e.proc_inst_id_ and e.parent_id_ is null
        inner join act_hi_varinst v on v.proc_inst_id_ = p.proc_inst_id_ and v.name_ = 'rolloutRO1'
        inner join act_ru_variable arv on e.proc_inst_id_ = arv.proc_inst_id_ and arv.name_ = 'mainContract' and arv.text_ = 'Roll-out'
    where p.proc_def_key_ = 'Revision'
    and p.proc_inst_id_ not in (
        select proc_inst_id_
        from act_ru_variable
        where name_ = 'rolloutRO1'
    )
) as s;

INSERT INTO public.act_hi_varinst (id_, proc_def_key_, proc_def_id_, proc_inst_id_, execution_id_, act_inst_id_, name_, var_type_, rev_, text_, state_)
select s.vuuid, 'Revision', s.proc_def_id_, s.proc_ins_id_, s.execution_id_, s.act_inst_id_, 'rolloutRO2', 'string', 0, 'false', 'CREATED'
from (
    select uuid_generate_v4() AS vuuid,
          p.id_ as proc_ins_id_,
          v.proc_def_id_ as proc_def_id_,
          v.execution_id_ as execution_id_,
          v.act_inst_id_ as act_inst_id_
    from act_hi_procinst p
        inner join act_hi_varinst v on v.proc_inst_id_ = p.proc_inst_id_ and v.name_ = 'mainContract' and v.text_ = 'Roll-out'
    where p.proc_def_key_ = 'Revision'
    and p.proc_inst_id_ not in (
      select v.proc_inst_id_
      from act_hi_varinst
      where name_ = 'rolloutRO2'
    )
) as s;

INSERT INTO public.act_ru_variable (id_, rev_, type_, name_, execution_id_, proc_inst_id_, text_, var_scope_, sequence_counter_, is_concurrent_local_)
select s.id_, 1, 'string', 'rolloutRO2', s.execution_id_, s.proc_inst_id_, 'false', s.proc_inst_id_, 1, false
from (
    select
        v.id_ as id_,
        arv.execution_id_ as execution_id_,
        arv.proc_inst_id_ as proc_inst_id_
    from act_hi_procinst p
        inner join act_ru_execution e on p.proc_inst_id_ = e.proc_inst_id_ and e.parent_id_ is null
        inner join act_hi_varinst v on v.proc_inst_id_ = p.proc_inst_id_ and v.name_ = 'rolloutRO2'
        inner join act_ru_variable arv on e.proc_inst_id_ = arv.proc_inst_id_ and arv.name_ = 'mainContract' and arv.text_ = 'Roll-out'
    where p.proc_def_key_ = 'Revision'
    and p.proc_inst_id_ not in (
        select proc_inst_id_
        from act_ru_variable
        where name_ = 'rolloutRO2'
    )
) as s;


INSERT INTO public.act_hi_varinst (id_, proc_def_key_, proc_def_id_, proc_inst_id_, execution_id_, act_inst_id_, name_, var_type_, rev_, text_, state_)
select s.vuuid, 'Revision', s.proc_def_id_, s.proc_ins_id_, s.execution_id_, s.act_inst_id_, 'rolloutRO3', 'string', 0, 'false', 'CREATED'
from (
    select uuid_generate_v4() AS vuuid,
          p.id_ as proc_ins_id_,
          v.proc_def_id_ as proc_def_id_,
          v.execution_id_ as execution_id_,
          v.act_inst_id_ as act_inst_id_
    from act_hi_procinst p
        inner join act_hi_varinst v on v.proc_inst_id_ = p.proc_inst_id_ and v.name_ = 'mainContract' and v.text_ = 'Roll-out'
    where p.proc_def_key_ = 'Revision'
    and p.proc_inst_id_ not in (
      select v.proc_inst_id_
      from act_hi_varinst
      where name_ = 'rolloutRO3'
    )
) as s;

INSERT INTO public.act_ru_variable (id_, rev_, type_, name_, execution_id_, proc_inst_id_, text_, var_scope_, sequence_counter_, is_concurrent_local_)
select s.id_, 1, 'string', 'rolloutRO3', s.execution_id_, s.proc_inst_id_, 'false', s.proc_inst_id_, 1, false
from (
    select
        v.id_ as id_,
        arv.execution_id_ as execution_id_,
        arv.proc_inst_id_ as proc_inst_id_
    from act_hi_procinst p
        inner join act_ru_execution e on p.proc_inst_id_ = e.proc_inst_id_ and e.parent_id_ is null
        inner join act_hi_varinst v on v.proc_inst_id_ = p.proc_inst_id_ and v.name_ = 'rolloutRO3'
        inner join act_ru_variable arv on e.proc_inst_id_ = arv.proc_inst_id_ and arv.name_ = 'mainContract' and arv.text_ = 'Roll-out'
    where p.proc_def_key_ = 'Revision'
    and p.proc_inst_id_ not in (
        select proc_inst_id_
        from act_ru_variable
        where name_ = 'rolloutRO3'
    )
) as s;

INSERT INTO public.act_hi_varinst (id_, proc_def_key_, proc_def_id_, proc_inst_id_, execution_id_, act_inst_id_, name_, var_type_, rev_, text_, state_)
select s.vuuid, 'Revision', s.proc_def_id_, s.proc_ins_id_, s.execution_id_, s.act_inst_id_, 'rolloutRO4', 'string', 0, 'false', 'CREATED'
from (
    select uuid_generate_v4() AS vuuid,
          p.id_ as proc_ins_id_,
          v.proc_def_id_ as proc_def_id_,
          v.execution_id_ as execution_id_,
          v.act_inst_id_ as act_inst_id_
    from act_hi_procinst p
        inner join act_hi_varinst v on v.proc_inst_id_ = p.proc_inst_id_ and v.name_ = 'mainContract' and v.text_ = 'Roll-out'
    where p.proc_def_key_ = 'Revision'
    and p.proc_inst_id_ not in (
      select v.proc_inst_id_
      from act_hi_varinst
      where name_ = 'rolloutRO4'
    )
) as s;

INSERT INTO public.act_ru_variable (id_, rev_, type_, name_, execution_id_, proc_inst_id_, text_, var_scope_, sequence_counter_, is_concurrent_local_)
select s.id_, 1, 'string', 'rolloutRO4', s.execution_id_, s.proc_inst_id_, 'false', s.proc_inst_id_, 1, false
from (
    select
        v.id_ as id_,
        arv.execution_id_ as execution_id_,
        arv.proc_inst_id_ as proc_inst_id_
    from act_hi_procinst p
        inner join act_ru_execution e on p.proc_inst_id_ = e.proc_inst_id_ and e.parent_id_ is null
        inner join act_hi_varinst v on v.proc_inst_id_ = p.proc_inst_id_ and v.name_ = 'rolloutRO4'
        inner join act_ru_variable arv on e.proc_inst_id_ = arv.proc_inst_id_ and arv.name_ = 'mainContract' and arv.text_ = 'Roll-out'
    where p.proc_def_key_ = 'Revision'
    and p.proc_inst_id_ not in (
        select proc_inst_id_
        from act_ru_variable
        where name_ = 'rolloutRO4'
    )
) as s;

-- = {true,false}
INSERT INTO public.act_hi_varinst (id_, proc_def_key_, proc_def_id_, proc_inst_id_, execution_id_, act_inst_id_, name_, var_type_, rev_, long_, state_)
select s.vuuid, 'Revision', s.proc_def_id_, s.proc_ins_id_, s.execution_id_, s.act_inst_id_, 'contains13Work', 'boolean', 0, 1, 'CREATED'
from (
    select uuid_generate_v4() AS vuuid,
          p.id_ as proc_ins_id_,
          v.proc_def_id_ as proc_def_id_,
          v.execution_id_ as execution_id_,
          v.act_inst_id_ as act_inst_id_
    from act_hi_procinst p
        inner join act_hi_varinst v on v.proc_inst_id_ = p.proc_inst_id_ and v.name_ = 'mainContract' and v.text_ = 'Roll-out'
        inner join act_hi_varinst jw on jw.proc_inst_id_ = p.proc_inst_id_ and jw.name_ = 'workTitlesForSearch'
    where p.proc_def_key_ = 'Revision'
    and jw.text_ like '%13.SITE project Macro3G SiteCo locInd/Outd%'
    and p.proc_inst_id_ not in (
      select v.proc_inst_id_
      from act_hi_varinst
      where name_ = 'contains13Work'
    )
) as s;

INSERT INTO public.act_hi_varinst (id_, proc_def_key_, proc_def_id_, proc_inst_id_, execution_id_, act_inst_id_, name_, var_type_, rev_, long_, state_)
select s.vuuid, 'Revision', s.proc_def_id_, s.proc_ins_id_, s.execution_id_, s.act_inst_id_, 'contains13Work', 'boolean', 0, 0, 'CREATED'
from (
    select uuid_generate_v4() AS vuuid,
          p.id_ as proc_ins_id_,
          v.proc_def_id_ as proc_def_id_,
          v.execution_id_ as execution_id_,
          v.act_inst_id_ as act_inst_id_
    from act_hi_procinst p
        inner join act_hi_varinst v on v.proc_inst_id_ = p.proc_inst_id_ and v.name_ = 'mainContract' and v.text_ = 'Roll-out'
        inner join act_hi_varinst jw on jw.proc_inst_id_ = p.proc_inst_id_ and jw.name_ = 'workTitlesForSearch'
    where p.proc_def_key_ = 'Revision'
    and jw.text_ not like '%13.SITE project Macro3G SiteCo locInd/Outd%'
    and p.proc_inst_id_ not in (
      select v.proc_inst_id_
      from act_hi_varinst
      where name_ = 'contains13Work'
    )
) as s;

INSERT INTO public.act_ru_variable (id_, rev_, type_, name_, execution_id_, proc_inst_id_, long_, var_scope_, sequence_counter_, is_concurrent_local_)
select s.id_, 1, 'boolean', 'contains13Work', s.execution_id_, s.proc_inst_id_, s.long_, s.proc_inst_id_, 1, false
from (
    select
        v.id_ as id_,
        arv.execution_id_ as execution_id_,
        arv.proc_inst_id_ as proc_inst_id_,
        arv.long_ as long_
    from act_hi_procinst p
        inner join act_ru_execution e on p.proc_inst_id_ = e.proc_inst_id_ and e.parent_id_ is null
        inner join act_hi_varinst v on v.proc_inst_id_ = p.proc_inst_id_ and v.name_ = 'contains13Work'
        inner join act_ru_variable arv on e.proc_inst_id_ = arv.proc_inst_id_ and arv.name_ = 'mainContract' and arv.text_ = 'Roll-out'
    where p.proc_def_key_ = 'Revision'
    and p.proc_inst_id_ not in (
        select proc_inst_id_
        from act_ru_variable
        where name_ = 'contains13Work'
    )
) as s;