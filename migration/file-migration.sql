CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- for history
select 'insert into act_ge_bytearray(id_, rev_, name_, bytes_) VALUES ('''||buuid||''', 1, ''trFilesName'', '''||json||''')',
      'insert into act_hi_varinst(id_, proc_def_key_, proc_def_id_, proc_inst_id_, execution_id_, act_inst_id_, name_, var_type_, rev_, bytearray_id_)'||
      ' VALUES ('''||vuuid||''', '''||proc_def_key_||''', '''||proc_def_id_||''', '''||proc_inst_id_||''', '''||execution_id_||''','''||act_inst_id_||''', ''trFilesName'', ''json'', 0, '''||buuid||''')'
from (
  select uuid_generate_v4() as buuid, uuid_generate_v4() as vuuid, json_agg(json_build_object('name', name, 'type', type, 'value', json_build_object('path', path, 'description', id, 'name', filename))) as json,
          t.proc_def_key_, t.proc_def_id_, t.proc_inst_id_ , t.execution_id_ , t.act_inst_id_ , t.case_def_key_ , t.case_def_id_ , t.case_inst_id_, t.case_execution_id_, t.task_id_
  from (
    select hhvi.proc_inst_id_ as processId, hhvi.name_ as name, 'Json' as type, hvi.proc_inst_id_ || '/' || replace(hhvi.text_,'''','''''') as path, el.js ->> 'id' as id,
      replace(hhvi.text_,'''','''''') as filename,
            hvi.proc_def_key_, hvi.proc_def_id_, hvi.proc_inst_id_ , hvi.execution_id_ , hvi.act_inst_id_ , hvi.case_def_key_ , hvi.case_def_id_ , hvi.case_inst_id_, hvi.case_execution_id_, hvi.task_id_
    from act_hi_varinst hvi
      join act_ge_bytearray ba on (hvi.var_type_ = 'json' and hvi.bytearray_id_ = ba.id_)
      join LATERAL json_array_elements(convert_from(ba.bytes_, 'UTF8')::json) WITH ORDINALITY as el(js, idx) on true
      join act_hi_varinst hhvi on hhvi.var_type_ = 'file' and hhvi.name_ = ('trFile' || el.idx - 1) and hhvi.proc_inst_id_ = hvi.proc_inst_id_
      join act_ge_bytearray bba on bba.id_ = hhvi.bytearray_id_
    where hvi.name_  = 'trFiles'
  ) t
  group by processId, t.proc_def_key_, t.proc_def_id_, t.proc_inst_id_ , t.execution_id_ , t.act_inst_id_ , t.case_def_key_ , t.case_def_id_ , t.case_inst_id_, t.case_execution_id_, t.task_id_
) l;

select 'insert into act_ge_bytearray(id_, rev_, name_, bytes_) VALUES ('''||buuid||''', 1, '''||l.name||''', '''||json||''')',
      'insert into act_hi_varinst(id_, proc_def_key_, proc_def_id_, proc_inst_id_, execution_id_, act_inst_id_, name_, var_type_, rev_, bytearray_id_)'||
      ' VALUES ('''||vuuid||''', '''||proc_def_key_||''', '''||proc_def_id_||''', '''||proc_inst_id_||''', '''||execution_id_||''','''||act_inst_id_||''', '''||l.name||''', ''json'', 0, '''||buuid||''')'
from (
  SELECT
    t.name AS name,
    uuid_generate_v4()                                                   AS buuid,
    uuid_generate_v4()                                                   AS vuuid,
    json_build_object('path', path, 'name', filename)                    AS json,
    t.proc_def_key_, t.proc_def_id_, t.proc_inst_id_ , t.execution_id_ , t.act_inst_id_ , t.case_def_key_ , t.case_def_id_ , t.case_inst_id_, t.case_execution_id_, t.task_id_
  FROM (
         SELECT
           hvi.name_ || 'Name'                   AS name,
           'json'                                AS type,
           hvi.proc_inst_id_ || '/' || replace(hvi.text_,'''','''''') AS path,
           replace(hvi.text_,'''','''''')                            AS filename,
           hvi.proc_def_key_, hvi.proc_def_id_, hvi.proc_inst_id_ , hvi.execution_id_ , hvi.act_inst_id_ , hvi.case_def_key_ , hvi.case_def_id_ , hvi.case_inst_id_, hvi.case_execution_id_, hvi.task_id_
         FROM act_hi_varinst hvi
           JOIN act_ge_bytearray ba ON (hvi.bytearray_id_ = ba.id_)
         WHERE hvi.var_type_ = 'file'
               AND hvi.name_ IN
                   ('actOfMaterialsDispatchingFile', 'tssrssidFile', 'eLicenseResolutionFile', 'sapPRFileXLS', 'kcellWarehouseMaterialsList', 'contractorZIPWarehouseMaterialsList',
                   'supplementaryFile1','supplementaryFile2','supplementaryFile3','supplementaryFile4','supplementaryFile5')
       ) t
) l;

select 'insert into act_ge_bytearray(id_, rev_, name_, bytes_) VALUES ('''||buuid||''', '''||1||''', ''siteWorksFiles'', '''||json||''')',
      'insert into act_hi_varinst(id_, proc_def_key_, proc_def_id_, proc_inst_id_, execution_id_, act_inst_id_, name_, var_type_, rev_, bytearray_id_)'||
      ' VALUES ('''||vuuid||''', '''||proc_def_key_||''', '''||proc_def_id_||''', '''||proc_inst_id_||''', '''||execution_id_||''','''||act_inst_id_||''', ''siteWorksFiles'', ''json'', 0, '''||buuid||''')'
from (
  SELECT
      uuid_generate_v4()                                                   AS buuid,
      uuid_generate_v4()                                                   AS vuuid,
      json_agg(json_build_object('name', t.oldName, 'type', type, 'value',
                                 json_build_object('path', path, 'name', filename))) AS json,
      t.proc_def_key_, t.proc_def_id_, t.proc_inst_id_ , t.execution_id_ , t.act_inst_id_
  FROM (
       SELECT
         hvi.name_ || 'Name'                   AS name,
         hvi.name_                             AS oldName,
         'json'                                AS type,
         hvi.proc_inst_id_ || '/' || replace(hvi.text_,'''','''''') AS path,
         replace(hvi.text_,'''','''''')                             AS filename,
         hvi.proc_def_key_, hvi.proc_def_id_, hvi.proc_inst_id_ , hvi.execution_id_ , hvi.act_inst_id_
       FROM act_hi_varinst hvi
         JOIN act_ge_bytearray ba ON (hvi.bytearray_id_ = ba.id_)
       WHERE hvi.var_type_ = 'file'
             AND hvi.name_ ILIKE 'work_%'
  ) t
  group by t.proc_def_key_, t.proc_def_id_, t.proc_inst_id_ , t.execution_id_ , t.act_inst_id_
) l;

/*
update act_hi_varinst set name_ = name_||'Old' where id_ in (
    select hvi.id_
    from act_hi_varinst hvi
    where hvi.name_  = 'trFiles' or (hvi.name_  = 'trFile%' and type_ = 'file')
    or hvi.name_ IN
                   ('actOfMaterialsDispatchingFile', 'tssrssidFile', 'eLicenseResolutionFile', 'sapPRFileXLS', 'kcellWarehouseMaterialsList', 'contractorZIPWarehouseMaterialsList',
                   'supplementaryFile1','supplementaryFile2','supplementaryFile3','supplementaryFile4','supplementaryFile5')
    or (hvi.var_type_ = 'file' AND hvi.name_ ILIKE 'work_%')
);
*/


-- for active

-- trFiles
select 'insert into act_ge_bytearray(id_, rev_, name_, bytes_) VALUES ('''||buuid||''', '''||1||''', ''trFilesName'', '''||json||''')',
       'insert into act_ru_variable(id_, rev_, type_, name_, execution_id_, proc_inst_id_, bytearray_id_, var_scope_, sequence_counter_, is_concurrent_local_) values '||
       '('''||vuuid||''', 1, ''json'', ''trFilesName'', '''||execution_id_||''', '''||proc_inst_id_||''', '''||buuid||''', '''||proc_inst_id_||''', 1, null)'
from (
  SELECT
    uuid_generate_v4() AS buuid,
    uuid_generate_v4() AS vuuid,
    json_agg(json_build_object('name', name, 'type', type, 'value',
                               json_build_object('path', path, 'description', id, 'name', filename))) AS json,
    t.execution_id_, t.proc_inst_id_ , t.var_scope_
  FROM (
         SELECT
           aarv.proc_inst_id_                     AS processId,
           aarv.name_                             AS name,
           'json'                                 AS type,
           coalesce(are.parent_id_,arv.proc_inst_id_) || '/' || replace(aarv.text_,'''','''''') AS path,
           el.js ->> 'id'                         AS id,
           replace(aarv.text_,'''','''''')        AS filename,
           arv.execution_id_, coalesce(are.parent_id_,arv.proc_inst_id_) as proc_inst_id_ , arv.var_scope_ , arv.sequence_counter_
         FROM act_ru_variable arv
           JOIN act_ge_bytearray ba ON (arv.bytearray_id_ = ba.id_)
           JOIN LATERAL json_array_elements(convert_from(ba.bytes_, 'UTF8') :: JSON) WITH ORDINALITY AS el(js, idx)
             ON TRUE
           JOIN act_ru_variable aarv
             ON aarv.type_ = 'file' AND aarv.name_ = ('trFile' || el.idx - 1) AND aarv.proc_inst_id_ = arv.proc_inst_id_
           JOIN act_ge_bytearray bba ON bba.id_ = aarv.bytearray_id_
           left OUTER JOIN act_ru_execution are ON (arv.proc_inst_id_ = are.id_)
         WHERE arv.type_ = 'json'
            AND arv.name_ ILIKE '%file%'
            AND arv.type_ <> 'file'
            AND ba.bytes_ is not null
       ) t
  GROUP BY processId, t.execution_id_, t.proc_inst_id_ , t.var_scope_
) l;

select 'insert into act_ge_bytearray(id_, rev_, name_, bytes_) VALUES ('''||buuid||''', 1, '''||l.name||''', '''||json||''')',
       'insert into act_ru_variable(id_, rev_, type_, name_, execution_id_, proc_inst_id_, bytearray_id_, var_scope_, sequence_counter_, is_concurrent_local_) values '||
       '('''||vuuid||''', 1, ''json'', '''||l.name||''', '''||execution_id_||''', '''||proc_inst_id_||''', '''||buuid||''', '''||proc_inst_id_||''', 1, null)'
from (
  SELECT
    t.name AS name,
    uuid_generate_v4()                                                   AS buuid,
    uuid_generate_v4()                                                   AS vuuid,
    json_build_object('path', path, 'name', filename)                    AS json,
    t.execution_id_,
    t.proc_inst_id_,
    t.var_scope_
  FROM (
         SELECT
           arv.name_ || 'Name'                   AS name,
           'json'                                AS type,
           coalesce(are.parent_id_,arv.proc_inst_id_) || '/' || replace(arv.text_,'''','''''') AS path,
           replace(arv.text_,'''','''''')                            AS filename,
           arv.execution_id_,
           coalesce(are.parent_id_,arv.proc_inst_id_) AS proc_inst_id_,
           arv.var_scope_
         FROM act_ru_variable arv
           JOIN act_ge_bytearray ba ON (arv.bytearray_id_ = ba.id_)
           left OUTER JOIN act_ru_execution are ON (arv.proc_inst_id_ = are.id_)
         WHERE arv.type_ = 'file'
              AND arv.text_ = 'RE 03283YRYSBAKHYT упала антенна.msg'
               AND arv.name_ IN
                   ('actOfMaterialsDispatchingFile', 'tssrssidFile', 'eLicenseResolutionFile', 'sapPRFileXLS', 'kcellWarehouseMaterialsList', 'contractorZIPWarehouseMaterialsList')
       ) t
) l;


--workFiles
select 'insert into act_ge_bytearray(id_, rev_, name_, bytes_) VALUES ('''||buuid||''', '''||1||''', ''siteWorksFiles'', '''||json||''')',
       'insert into act_ru_variable(id_, rev_, type_, name_, execution_id_, proc_inst_id_, bytearray_id_, var_scope_, sequence_counter_, is_concurrent_local_) values '||
       '('''||vuuid||''', 1, ''json'', ''siteWorksFiles'', '''||execution_id_||''', '''||proc_inst_id_||''', '''||buuid||''', '''||proc_inst_id_||''', 1, null)'
from (
  SELECT
      uuid_generate_v4()                                                   AS buuid,
      uuid_generate_v4()                                                   AS vuuid,
      t.proc_inst_id_,
      json_agg(json_build_object('name', name, 'type', type, 'value',
                                 json_build_object('path', path, 'name', filename))) AS json,
      t.execution_id_,
     t.var_scope_
  FROM (
     SELECT
       arv.name_                             AS name,
       'json'                                 AS type,
       coalesce(are.parent_id_,arv.proc_inst_id_) || '/' || replace(arv.text_,'''','''''') AS path,
       replace(arv.text_,'''','''''')                             AS filename,
       arv.execution_id_, coalesce(are.parent_id_,arv.proc_inst_id_) AS proc_inst_id_, arv.var_scope_
     FROM act_ru_variable arv
           left OUTER JOIN act_ru_execution are ON (arv.proc_inst_id_ = are.id_)
     WHERE arv.type_ = 'file'
        AND arv.name_ ILIKE 'work_%'
  ) t
  group by t.proc_inst_id_,
      t.execution_id_,
      t.proc_inst_id_,
      t.var_scope_
    order by t.proc_inst_id_
) l


--supplementaryFiles
select 'insert into act_ge_bytearray(id_, rev_, name_, bytes_) VALUES ('''||buuid||''', '''||1||''', ''supplementaryFiles'', '''||json||''')',
       'insert into act_ru_variable(id_, rev_, type_, name_, execution_id_, proc_inst_id_, bytearray_id_, var_scope_, sequence_counter_, is_concurrent_local_) values '||
       '('''||vuuid||''', 1, ''json'', ''supplementaryFiles'', '''||execution_id_||''', '''||proc_inst_id_||''', '''||buuid||''', '''||proc_inst_id_||''', 1, null)'
from (
  SELECT
      uuid_generate_v4()                                                   AS buuid,
      uuid_generate_v4()                                                   AS vuuid,
      t.proc_inst_id_,
      json_build_object('files', json_agg(json_build_object('name', text_, 'value', json_build_object('path', path)))) AS json,
      t.execution_id_,
     t.var_scope_
  FROM (
     SELECT
       arv.name_ AS name,
       replace(arv.text_,'''','''''') AS text_,
       'json' AS type,
       coalesce(are.parent_id_,arv.proc_inst_id_) || '/'|| replace(arv.text_,'''','''''') AS path,
       replace(arv.text_,'''','''''')                             AS filename,
       arv.execution_id_, coalesce(are.parent_id_,arv.proc_inst_id_) AS proc_inst_id_, arv.var_scope_
     FROM act_ru_variable arv
           left OUTER JOIN act_ru_execution are ON (arv.proc_inst_id_ = are.id_)
     WHERE arv.type_ = 'file'
        AND arv.name_ ILIKE 'supplementaryFile%'
  ) t
  group by t.proc_inst_id_,
      t.execution_id_,
      t.proc_inst_id_,
      t.var_scope_
    order by t.proc_inst_id_
) l

--priority variable creation

select 'insert into act_ru_variable(id_, rev_, type_, name_, execution_id_, proc_inst_id_, text_, var_scope_, sequence_counter_, is_concurrent_local_) values '||
       '('''||vuuid||''', 1, ''string'', ''priority'', '''||execution_id_||''', '''||proc_inst_id_||''', ''regular'', '''||proc_inst_id_||''', 1, null)'
from (
  select uuid_generate_v4() as vuuid,
        arv.execution_id_,
        arv.proc_inst_id_
  FROM act_ru_variable arv
  where arv.name_ = 'jrNumber'
  and arv.proc_inst_id_ not in (
    select arv.proc_inst_id_
    FROM act_ru_variable arv
    where arv.name_ = 'priority'
  )
  GROUP BY arv.execution_id_, arv.proc_inst_id_
) l
