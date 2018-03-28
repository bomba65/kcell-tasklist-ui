-- отчет для Семенова

with worksDict as (
    select value->>'id' as "id", value->>'title' as "title" from jsonb_array_elements('[
      {
        "id": "1",
        "price": "115858.15",
        "title": "1.BTS Macro/BTS Ult inst(CabOnly) on-air"
      },
      {
        "id": "2",
        "price": "75270.90",
        "title": "2.BTS Micro/BTS Midi inst(CabOnly)on-air"
      },
      {
        "id": "3",
        "price": "165300.80",
        "title": "3.RBS/BTS Extension inst(cab+batt)on-air"
      },
      {
        "id": "4",
        "price": "214005.50",
        "title": "4.RBS/BTS type chge (cab+batt)on-air"
      },
      {
        "id": "5",
        "price": "191867.00",
        "title": "5.RBS/BTS add(BBS+cab+ant+feed+pole)oair"
      },
      {
        "id": "6",
        "price": "90767.85",
        "title": "6.BTS Macro removal "
      },
      {
        "id": "7",
        "price": "59036.00",
        "title": "7.BTS Micro/BTS Midi removal "
      },
      {
        "id": "8",
        "price": "619878.00",
        "title": "8.Site chge/repl (All equips)"
      },
      {
        "id": "9",
        "price": "327649.80",
        "title": "9.Site removal - all equipment including FE deinstallation"
      },
      {
        "id": "10",
        "price": "295180.00",
        "title": "10.BTS Swap-Vend (incl pack Kcell req)"
      },
      {
        "id": "11",
        "price": "66415.50",
        "title": "11.BattBackSys inst BBS on-air (^2 sets)"
      },
      {
        "id": "12",
        "price": "64201.65",
        "title": "12.BattBackSys chg/repl on-air (^2 sets)"
      },
      {
        "id": "13",
        "price": "42801.10",
        "title": "13.Battery Backup System Removal"
      },
      {
        "id": "14",
        "price": "29518.00",
        "title": "14.BattChg (de&inst)/set (1set=4batt)"
      },
      {
        "id": "15",
        "price": "17710.80",
        "title": "15.Batt inst/deinst /set (1set=4batt)"
      },
      {
        "id": "16",
        "price": "108478.65",
        "title": "16.Container inst"
      },
      {
        "id": "17",
        "price": "147590.00",
        "title": "17.Container chge/repl"
      },
      {
        "id": "18",
        "price": "90767.85",
        "title": "18.Container removal"
      },
      {
        "id": "19",
        "price": "44277.00",
        "title": "19.Site Grounding System inst"
      },
      {
        "id": "20",
        "price": "51656.50",
        "title": "20.Site Grounding System сhange/repl"
      },
      {
        "id": "21",
        "price": "25090.30",
        "title": "21.Site Grounding System removal"
      },
      {
        "id": "22",
        "price": "44277.00",
        "title": "22.Site Lightning Protection inst"
      },
      {
        "id": "23",
        "price": "23614.40",
        "title": "23.Lighting rod chge/ repl"
      },
      {
        "id": "24",
        "price": "20662.60",
        "title": "24.Lighting rod remove"
      },
      {
        "id": "25",
        "price": "29518.00",
        "title": "25.Air Conditioning inst"
      },
      {
        "id": "26",
        "price": "40587.25",
        "title": "26.Air Conditioning chge/repl"
      },
      {
        "id": "27",
        "price": "22138.50",
        "title": "27.Air Conditioning removal"
      },
      {
        "id": "28",
        "price": "36897.50",
        "title": "28.TMA inst and testing"
      },
      {
        "id": "29",
        "price": "36897.50",
        "title": "29.TMA chge/repl"
      },
      {
        "id": "30",
        "price": "29518.00",
        "title": "30.TMA remove and package"
      },
      {
        "id": "31",
        "price": "10921.66",
        "title": "31.TRU/TRX addition inst and testing"
      },
      {
        "id": "32",
        "price": "10921.66",
        "title": "32.TRU/TRX chge/repl"
      },
      {
        "id": "33",
        "price": "10921.66",
        "title": "33.TRU/TRX removal."
      },
      {
        "id": "34",
        "price": "10921.66",
        "title": "34.EDGE DTRU/TSGB Upgrade"
      },
      {
        "id": "35",
        "price": "50180.60",
        "title": "35.Repeater inst and testing"
      },
      {
        "id": "36",
        "price": "53132.40",
        "title": "36.Repeater chge/repl"
      },
      {
        "id": "37",
        "price": "36897.50",
        "title": "37.Repeater removal."
      },
      {
        "id": "38",
        "price": "19924.65",
        "title": "38.GSM Antenna inst"
      },
      {
        "id": "39",
        "price": "33207.75",
        "title": "39.GSM Ant loc chge/repl (incl poles)"
      },
      {
        "id": "40",
        "price": "13283.10",
        "title": "40.GSM Antenna direction chge"
      },
      {
        "id": "41",
        "price": "13283.10",
        "title": "41.GSM Antenna tilting"
      },
      {
        "id": "42",
        "price": "26566.20",
        "title": "42.GSM Antenna chge (with pole)"
      },
      {
        "id": "43",
        "price": "33207.75",
        "title": "43.GSM Antenna height chge (up/down)"
      },
      {
        "id": "44",
        "price": "15496.95",
        "title": "44.GSM Antenna removal"
      },
      {
        "id": "45",
        "price": "56084.20",
        "title": "45.FeederInst 0-75m(/sect,noFeedBefor)"
      },
      {
        "id": "46",
        "price": "66415.50",
        "title": "46.FeederInst 76-175m(/sec,noFeedBefor)"
      },
      {
        "id": "47",
        "price": "73795.00",
        "title": "47.FeederInst 176-^m(/sec,noFeedBefor)"
      },
      {
        "id": "48",
        "price": "73795.00",
        "title": "48.Feeder chg/rpl 0-75m(/sect,chge feed)"
      },
      {
        "id": "49",
        "price": "88554.00",
        "title": "49.Feeder chg/rpl 76-175m(/sec,chge feed"
      },
      {
        "id": "50",
        "price": "103313.00",
        "title": "50.Feeder chg/repl 176-^m(/sec,chgeFeed)"
      },
      {
        "id": "51",
        "price": "45752.90",
        "title": "51.Feeder removal 0-75m "
      },
      {
        "id": "52",
        "price": "54608.30",
        "title": "52.Feeder removal 76-175m"
      },
      {
        "id": "53",
        "price": "59036.00",
        "title": "53.Feeder removal 176-up m"
      },
      {
        "id": "54",
        "price": "36897.50",
        "title": "54.Isol&Inst&Impl grdShelRoofPoles 2-6m"
      },
      {
        "id": "55",
        "price": "59036.00",
        "title": "55.Isol&Inst&Impl grdShelRoofPoles 7-10m"
      },
      {
        "id": "56",
        "price": "44277.00",
        "title": "56.Chge/repl grd/shel/roof poles 2-6m"
      },
      {
        "id": "57",
        "price": "66415.50",
        "title": "57.Chge/repl grd/shel/roof poles 7-10m"
      },
      {
        "id": "58",
        "price": "26566.20",
        "title": "58.Removal on ground/ on shelter / on roof for all type of Poles from 2m till 6m"
      },
      {
        "id": "59",
        "price": "32469.80",
        "title": "59.Removal on ground/ on shelter / on roof for all type of Poles from 7m till 10m"
      },
      {
        "id": "60",
        "price": "7379.50",
        "title": "60.2Mb, connection of PCM cbl to DDF"
      },
      {
        "id": "61",
        "price": "7379.50",
        "title": "61.chg/rpl 2Mb conn of PCM cbl to DDF"
      },
      {
        "id": "62",
        "price": "25090.30",
        "title": "62.HDSLModem/rout ciscoDlink/SIU ins&tst"
      },
      {
        "id": "63",
        "price": "29518.00",
        "title": "63.HDSLModem/rout ciscoDlink/SIU chg/rpl"
      },
      {
        "id": "64",
        "price": "17710.80",
        "title": "64.HDSL Modem/router cisco, d-link/SIU Removal"
      },
      {
        "id": "65",
        "price": "64939.60",
        "title": "65.MW HOP ins&tst in/odoorActAdj03-1,2m"
      },
      {
        "id": "66",
        "price": "184487.50",
        "title": "66.MW HOP ins&tst in/odoorActAdj1,6-3,3m"
      },
      {
        "id": "67",
        "price": "73795.00",
        "title": "67.MW HOP chg/rpl in/odoorActAdj03-1,2m"
      },
      {
        "id": "68",
        "price": "206626.00",
        "title": "68.MW HOP chg/rpl in/odoorActAdj1,6-3,3m"
      },
      {
        "id": "69",
        "price": "44277.00",
        "title": "69.MW HOP removal including indoor, outdoor (0,3-1,2m)"
      },
      {
        "id": "70",
        "price": "147590.00",
        "title": "70.MW HOP removal including indoor, outdoor (1,6-3,3m)"
      },
      {
        "id": "71",
        "price": "36897.50",
        "title": "71.MW Ant inst and testing (0,3-1,2m)"
      },
      {
        "id": "72",
        "price": "92243.75",
        "title": "72.MW Ant inst and testing (1,6-3,3m)"
      },
      {
        "id": "73",
        "price": "45752.90",
        "title": "73.MW Antenna chg/repl (0,3-1,2m)"
      },
      {
        "id": "74",
        "price": "110692.50",
        "title": "74.MW Antenna chg/repl (1,6-3,3m)"
      },
      {
        "id": "75",
        "price": "29518.00",
        "title": "75.MW Antenna removal (0,3-1,2m)"
      },
      {
        "id": "76",
        "price": "73795.00",
        "title": "76.MW Antenna removal (1,6-3,3m)"
      },
      {
        "id": "77",
        "price": "36897.50",
        "title": "77.MW Antenna dir chg incl adj(0,3-1,2m)"
      },
      {
        "id": "78",
        "price": "59036.00",
        "title": "78.MW Ant dir chge incl adj(1,6-3,3m)"
      },
      {
        "id": "79",
        "price": "14759.00",
        "title": "79.Power Splitter chge"
      },
      {
        "id": "80",
        "price": "14759.00",
        "title": "80.Add fastening by metal pipe (/1pipe)"
      },
      {
        "id": "81",
        "price": "14759.00",
        "title": "81.LTU/ETU/PFU inst cabl&test (any type)"
      },
      {
        "id": "82",
        "price": "17710.80",
        "title": "82.LTU/ETU/PFU chge/repl (any type)"
      },
      {
        "id": "83",
        "price": "10331.30",
        "title": "83.LTU/ETU/PFU removal (any type)"
      },
      {
        "id": "84",
        "price": "44277.00",
        "title": "84.NPU inst/chge/repl"
      },
      {
        "id": "85",
        "price": "14759.00",
        "title": "85.FAU inst/chge/rem"
      },
      {
        "id": "86",
        "price": "44277.00",
        "title": "86.UpgrChngConf MWequip(capProtSoftMod)"
      },
      {
        "id": "87",
        "price": "51656.50",
        "title": "87.TraffNode 2p/6p/FIU19 inst and tst"
      },
      {
        "id": "88",
        "price": "66415.50",
        "title": "88.TraffNode 20p/Metro HUB inst and tst"
      },
      {
        "id": "89",
        "price": "73795.00",
        "title": "89.TraffNod FIU19MetroHUB chgRpl(atypes"
      },
      {
        "id": "90",
        "price": "66415.50",
        "title": "90.Traffic Node all type/MetroHUB/FIU19 removal"
      },
      {
        "id": "91",
        "price": "11807.20",
        "title": "91.MMU/SMU inst and testing"
      },
      {
        "id": "92",
        "price": "14759.00",
        "title": "92.MMU/SMU chge/repl"
      },
      {
        "id": "93",
        "price": "10331.30",
        "title": "93.MMU/SMU removal"
      },
      {
        "id": "94",
        "price": "36897.50",
        "title": "94.RAU inst+ power splitter (any type)"
      },
      {
        "id": "95",
        "price": "44277.00",
        "title": "95.RAU chge (any type)"
      },
      {
        "id": "96",
        "price": "36897.50",
        "title": "96.RAU removal with power splitter (any type)"
      },
      {
        "id": "97",
        "price": "12545.15",
        "title": "97.AMM 2U/4U/14U inst (incl grounding)"
      },
      {
        "id": "98",
        "price": "14759.00",
        "title": "98.AMM 2U/4U/14U chge/repl (incl grnd)"
      },
      {
        "id": "99",
        "price": "10331.30",
        "title": "99.AMM 2U/4U/14U removal (including grounding)"
      },
      {
        "id": "100",
        "price": "36897.50",
        "title": "100.Transmiss rack 19\" inst (incl grnd)"
      },
      {
        "id": "101",
        "price": "44277.00",
        "title": "101.Transm rack 19\" che/rpl (incl grnd)"
      },
      {
        "id": "102",
        "price": "36897.50",
        "title": "102.Transmission rack 19\" removal (including grounding)"
      },
      {
        "id": "103",
        "price": "17710.80",
        "title": "103.MW Feeder/cbl inst 0-75m"
      },
      {
        "id": "104",
        "price": "22138.50",
        "title": "104.MW Feeder/cbl inst 76-175m"
      },
      {
        "id": "105",
        "price": "29518.00",
        "title": "105.MW Feeder/cbl inst 176- up m"
      },
      {
        "id": "106",
        "price": "22138.50",
        "title": "106.MW Feeder/cbl chge/repl 0-75m"
      },
      {
        "id": "107",
        "price": "29518.00",
        "title": "107.MW Feeder/cbl chge/repl 76-175m"
      },
      {
        "id": "108",
        "price": "36897.50",
        "title": "108.MW Feeder/cbl chge/repl 176- up m"
      },
      {
        "id": "109",
        "price": "17710.80",
        "title": "109.MW Feeder/cable removal 0-75m "
      },
      {
        "id": "110",
        "price": "28042.10",
        "title": "110.MW Feeder/cable removal 75-175m"
      },
      {
        "id": "111",
        "price": "36897.50",
        "title": "111.MW Feeder/cable removal 176-up m"
      },
      {
        "id": "112",
        "price": "22138.50",
        "title": "112.Add fast+instPipesMLfeeder cbl(*len)"
      },
      {
        "id": "113",
        "price": "23614.40",
        "title": "113.PCM/UTP/KVSM/opt p.crd inst 0-75m"
      },
      {
        "id": "114",
        "price": "23614.40",
        "title": "114.PCM/UTP/KVSM/opt p.crd inst 76-175m"
      },
      {
        "id": "115",
        "price": "23614.40",
        "title": "115.PCM/UTP/KVSM/opt p.crd inst 176-^m"
      },
      {
        "id": "116",
        "price": "26566.20",
        "title": "116.PCM/UTP/KVSM/opt p.crd chg/rpl 0-75m"
      },
      {
        "id": "117",
        "price": "26566.20",
        "title": "117.PCM/UTP/KVSM/optP.crdChg/rpl76-175m"
      },
      {
        "id": "118",
        "price": "26566.20",
        "title": "118.PCM/UTP/KVSM/opt p.crd chg/rpl 176-m"
      },
      {
        "id": "119",
        "price": "20662.60",
        "title": "119.PCM/UTP/KVSM/optical patchcord Cable removal 0-75m"
      },
      {
        "id": "120",
        "price": "20662.60",
        "title": "120.PCM/UTP/KVSM/optical patchcord Cable removal 76-175m"
      },
      {
        "id": "121",
        "price": "20662.60",
        "title": "121.PCM/UTP/KVSM/optical patchcord Cable removal 176-up m"
      },
      {
        "id": "122",
        "price": "13283.10",
        "title": "122.DDF/plinth inst+ cabiling (for pair)"
      },
      {
        "id": "123",
        "price": "17710.80",
        "title": "123.DDF/plinth chge/repl (for pair)"
      },
      {
        "id": "124",
        "price": "10331.30",
        "title": "124.DDF/plinth removal (for pair) "
      },
      {
        "id": "125",
        "price": "29518.00",
        "title": "125.Ind cbl trac way i/c/r/rm 0-50m"
      },
      {
        "id": "126",
        "price": "29518.00",
        "title": "126.Ind cbl trac way i/c/r/rm 51-100m"
      },
      {
        "id": "127",
        "price": "29518.00",
        "title": "127.Ind cbl trac way i/c/r/rm 101-^m"
      },
      {
        "id": "128",
        "price": "29518.00",
        "title": "128.Odoor cbl trac way i/c/r/rm 0-50m"
      },
      {
        "id": "129",
        "price": "29518.00",
        "title": "129.Odoor cbl trac way i/c/r/rm 101-^m"
      },
      {
        "id": "130",
        "price": "29518.00",
        "title": "130.Odoor cbl trac way i/c/r/rm 51-100 m"
      },
      {
        "id": "131",
        "price": "73795.00",
        "title": "131.Odoor TR rack inst"
      },
      {
        "id": "132",
        "price": "88554.00",
        "title": "132.Odoor TR rack chge/repl"
      },
      {
        "id": "133",
        "price": "59036.00",
        "title": "133.Outdoor TR rack removal"
      },
      {
        "id": "134",
        "price": "22138.50",
        "title": "134.DC/DC convert inst, cabling connect"
      },
      {
        "id": "135",
        "price": "29518.00",
        "title": "135.DC/DC converter chge/repl"
      },
      {
        "id": "136",
        "price": "14759.00",
        "title": "136.DC/DC converter removal"
      },
      {
        "id": "137",
        "price": "12545.15",
        "title": "137.DDU inst/chge/rem"
      },
      {
        "id": "138",
        "price": "2066.26",
        "title": "138.PwrCblIns(4x10/,4x16)/m(exclMat)"
      },
      {
        "id": "139",
        "price": "2361.44",
        "title": "139.PwrCbl chg/rpl (4x10, 4x16),/m"
      },
      {
        "id": "140",
        "price": "1180.72",
        "title": "140.Power Cable Removal (4x10mm2, 4x16mm2), per m "
      },
      {
        "id": "141",
        "price": "2656.62",
        "title": "141.PwrCbl inst (4x25mm2),/m"
      },
      {
        "id": "142",
        "price": "2951.80",
        "title": "142.PwrCbl chge/repl (4x25mm2),/m"
      },
      {
        "id": "143",
        "price": "1623.49",
        "title": "143.Power cable removal (4x25mm2), per m"
      },
      {
        "id": "144",
        "price": "84864.25",
        "title": "144.Actura/Rectifier inst (any type)"
      },
      {
        "id": "145",
        "price": "95933.50",
        "title": "145.Actura/Rectifier chge (any type)"
      },
      {
        "id": "146",
        "price": "51656.50",
        "title": "146.Actura/Rectifier Removal (any type)"
      },
      {
        "id": "147",
        "price": "17710.80",
        "title": "147.Power counter inst"
      },
      {
        "id": "148",
        "price": "25090.30",
        "title": "148.Power counter chge/repl"
      },
      {
        "id": "149",
        "price": "14759.00",
        "title": "149.Power counter Removal"
      },
      {
        "id": "150",
        "price": "7379.50",
        "title": "150.Circuit breaker inst per breaker"
      },
      {
        "id": "151",
        "price": "7379.50",
        "title": "151.Circuit break chge/ repl per breaker"
      },
      {
        "id": "152",
        "price": "0.0",
        "title": "152.Cross Connection Checking "
      },
      {
        "id": "153",
        "price": "4427.70",
        "title": "153.Lock installation"
      },
      {
        "id": "154",
        "price": "7379.50",
        "title": "154.Lock change/replacement"
      },
      {
        "id": "155",
        "price": "368975.00",
        "title": "155.Mobile BTS inst (TR,Power,ON-AIR)"
      },
      {
        "id": "156",
        "price": "265662.00",
        "title": "156.Inst&On-air Colloc BTS on site"
      },
      {
        "id": "157",
        "price": "110692.50",
        "title": "157.Inst surv/build site (incl a.design)"
      },
      {
        "id": "158",
        "price": "39849.30",
        "title": "158.Impl/indoor ant (1-20 pcs per site)"
      },
      {
        "id": "159",
        "price": "36897.50",
        "title": "159.Impl/indoor ant (21-50 pcs per site)"
      },
      {
        "id": "160",
        "price": "29518.00",
        "title": "160.Impl/indoor antenna (51-^ per site)"
      }
    ]')
)
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
  worksDict.title as "Job Description",
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
  left join worksDict
    on worksDict.id = worksJson.value->>'sapServiceNumber'
  left join act_hi_varinst explanation
    on pi.id_ = explanation.proc_inst_id_ and explanation.name_ = 'explanation'
  left join act_hi_varinst materialsRequired
    on pi.id_ = materialsRequired.proc_inst_id_ and materialsRequired.name_ = 'materialsRequired'
where pi.proc_def_key_ = 'Revision' and pi.state_ <> 'EXTERNALLY_TERMINATED'
order by "Requested Date", "Job Description"
--limit 5
