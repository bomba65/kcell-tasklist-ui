CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

/*actOfMaterialsDispatchingFile - actOfMaterialsDispatchingFileName
tssrssidFile - tssrssidFileName
eLicenseResolutionFile - eLicenseResolutionFileName
sapPRFileXLS - sapPRFileXLSName
kcellWarehouseMaterialsList - kcellWarehouseMaterialsListName
contractorZIPWarehouseMaterialsList - contractorZIPWarehouseMaterialsListName
works_{{$index}}_file_{{work.files.length}} - worksFiles
trFile{X} -

jrBlank - jrBlank
sapTransferRequestFile -
*/
-- for active

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
              AND arv.name_ IN
                   ('actOfMaterialsDispatchingFile', 'tssrssidFile', 'eLicenseResolutionFile', 'sapPRFileXLS',
                    'kcellWarehouseMaterialsList', 'contractorZIPWarehouseMaterialsList')
              AND arv.proc_inst_id_ not in (
                  select alr.proc_inst_id_
                  from act_ru_variable alr
                  where alr.name_ = 'siteWorksFiles'
                  and alr.type_ = 'json'
              )
       ) t
) l;


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
        AND arv.proc_inst_id_ not in (
            select alr.proc_inst_id_
            from act_ru_variable alr
            where alr.name_ = 'siteWorksFiles'
            and alr.type_ = 'json'
        )
  ) t
  group by t.proc_inst_id_,
      t.execution_id_,
      t.proc_inst_id_,
      t.var_scope_
    order by t.proc_inst_id_

) l;


--[{"description":"01766ALMATYLAKE ALM-LSE-SAO-17-5153_TR239952","name":"01766ALMATYLAKE ALM-LSE-SAO-17-5153_TR239952.pdf","path":"1807eda4-bdf1-11e7-9a13-0242ac130008/cd75477e-066c-11e8-a817-0242ac120008/01766ALMATYLAKE ALM-LSE-SAO-17-5153_TR239952.pdf","$$hashKey":"object:882"}]

select 'insert into act_ge_bytearray(id_, rev_, name_, bytes_) VALUES ('''||buuid||''', '''||1||''', ''trFilesName'', '''||json||''')',
       'insert into act_ru_variable(id_, rev_, type_, name_, execution_id_, proc_inst_id_, bytearray_id_, var_scope_, sequence_counter_, is_concurrent_local_) values '||
       '('''||vuuid||''', 1, ''json'', ''trFilesName'', '''||execution_id_||''', '''||proc_inst_id_||''', '''||buuid||''', '''||proc_inst_id_||''', 1, null)'
from (
  SELECT
    uuid_generate_v4() AS buuid,
    uuid_generate_v4() AS vuuid,
    json_agg(json_build_object('path', path, 'description', id, 'name', filename)) AS json,
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
            AND arv.name_ = 'trFiles'
            AND arv.type_ <> 'file'
            AND ba.bytes_ is not null
            AND arv.proc_inst_id_ not in (
                select alr.proc_inst_id_
                from act_ru_variable alr
                where alr.name_ like 'trFile%'
                and alr.type_ = 'json'
                and alr.name_ != 'trFiles'
            )
       ) t
  GROUP BY processId, t.execution_id_, t.proc_inst_id_ , t.var_scope_
) l;

--for history

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
                   ('actOfMaterialsDispatchingFile', 'tssrssidFile', 'eLicenseResolutionFile', 'sapPRFileXLS',
                    'kcellWarehouseMaterialsList', 'contractorZIPWarehouseMaterialsList')
              AND hvi.proc_inst_id_ not in (
                  select alr.proc_inst_id_
                  from act_hi_varinst alr
                  where alr.name_ in ('actOfMaterialsDispatchingFileName', 'tssrssidFileName', 'eLicenseResolutionFileName', 'sapPRFileXLSName',
                    'kcellWarehouseMaterialsListName', 'contractorZIPWarehouseMaterialsListName')
                  and alr.var_type_ = 'json'
              )
       ) t
) l;

select 'insert into act_ge_bytearray(id_, rev_, name_, bytes_) VALUES ('''||buuid||''', '''||1||''', ''siteWorksFiles'', '''||json||''')',
      'insert into act_hi_varinst(id_, proc_def_key_, proc_def_id_, proc_inst_id_, execution_id_, act_inst_id_, name_, var_type_, rev_, bytearray_id_) VALUES ('''||vuuid||''', '''||proc_def_key_||''', '''||proc_def_id_||''', '''||proc_inst_id_||''', '''||execution_id_||''','''||act_inst_id_||''', ''siteWorksFiles'', ''json'', 0, '''||buuid||''')'
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
            AND hvi.proc_inst_id_ not in (
                select alr.proc_inst_id_
                from act_hi_varinst alr
                where alr.name_ = 'siteWorksFiles'
                and alr.var_type_ = 'json'
            )
  ) t
  group by t.proc_def_key_, t.proc_def_id_, t.proc_inst_id_ , t.execution_id_ , t.act_inst_id_
) l;


select 'insert into act_ge_bytearray(id_, rev_, name_, bytes_) VALUES ('''||buuid||''', 1, ''trFilesName'', '''||json||''')',
      'insert into act_hi_varinst(id_, proc_def_key_, proc_def_id_, proc_inst_id_, execution_id_, act_inst_id_, name_, var_type_, rev_, bytearray_id_)'||
      ' VALUES ('''||vuuid||''', '''||proc_def_key_||''', '''||proc_def_id_||''', '''||proc_inst_id_||''', '''||execution_id_||''','''||act_inst_id_||''', ''trFilesName'', ''json'', 0, '''||buuid||''')'
from (
  select uuid_generate_v4() as buuid, uuid_generate_v4() as vuuid, json_agg(json_build_object('path', path, 'description', id, 'name', filename)) as json,
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