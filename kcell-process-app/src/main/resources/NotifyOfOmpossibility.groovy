import org.camunda.bpm.engine.delegate.DelegateExecution

def getUserEmail(DelegateExecution execution) {
    String reason = execution.getVariable("reason").toString()

    def userList = ""
    if(reason.equals("1")){
        userList = "Alexey.Kolyagin@kcell.kz, Anastassiya.Shenojak@kcell.kz, Dmitriy.Saidashev@kcell.kz, Lazizbek.Kurbantayev@kcell.kz, Nurzhan.Kochshigulov@kcell.kz, Sergey.Michshenko@kcell.kz, Kanat.Kulmukhambetov@kcell.kz, Kerey.Zatilda@kcell.kz, Nurlan.Shokparov@kcell.kz, Evgeniy.Degtyarev@kcell.kz"
    } else if(reason.equals("2")){
        userList = "Maulen.Kempirbayev@kcell.kz, Nurzhan.Mynbayev@kcell.kz, Asset.Rashitov@kcell.kz, Vladimir.Yefanov@kcell.kz, Aslan.Shalov@kcell.kz, Shyngys.Kassabekov@kcell.kz"
    } else if(reason.equals("3")){
        userList = "Alexey.Khudaev@kcell.kz, Evgeniy.Semenov@kcell.kz, Sergey.Chekh@kcell.kz, Sergey.Lee@kcell.kz, Vladimir.Yefanov@kcell.kz, Yevgeniy.Elunin@kcell.kz, Yermek.Tanabekov@kcell.kz, Andrei.Lugovoy@kcell.kz, Alexey.Kolesnikov@kcell.kz, Kali.Esimbekov@kcell.kz"
    } else if(reason.equals("4")){
        userList = "Kali.Esimbekov@kcell.kz, Alexey.Khudaev@kcell.kz, Evgeniy.Semenov@kcell.kz, Sergey.Chekh@kcell.kz, Sergey.Lee@kcell.kz"
    }
    userList
}

getUserEmail(execution)
