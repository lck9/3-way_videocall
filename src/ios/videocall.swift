
@objc(videocall) class videocall : CDVPlugin{
// MARK: Properties
var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR)
    private var room: Room?
    
@objc(add:) func add(_ command: CDVInvokedUrlCommand) {
var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR)
let param1 = command.arguments[0]  as? Int
let param2 = command.arguments[1]  as? Int
    
    
    DispatchQueue.main.async {
        // Background Thread
        let storyboard = UIStoryboard(name: "Video", bundle: nil)

        let rommViewController = storyboard.instantiateViewController(withIdentifier: "lobbyViewController") as! LobbyViewController
        
      
        
    
//    guard let rootVC = UIStoryboard.init(name: "Video", bundle: nil).instantiateViewController(withIdentifier: "roomViewController") as? RoomViewController else {
//                    return
//                }
//
//                let navigationController = UINavigationController(rootViewController: rootVC)
//
//                UIApplication.shared.windows.first?.rootViewController = navigationController
//                UIApplication.shared.windows.first?.makeKeyAndVisible()
    
    
    
    
    
//        let navigationController = UINavigationController(rootViewController: rommViewController)
        
        
//        UIApplication.shared.windows.first?.rootViewController = navigationController
//        UIApplication.shared.windows.first?.makeKeyAndVisible()
        
    
        
        self.viewController?.present(rommViewController, animated: true) { [self] in
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "ok")

            commandDelegate.send(pluginResult, callbackId: command.callbackId)
        }
        
            // Run UI Updates
//            self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
    }
    }
    
    
   
    
   
    
//    nav.pushViewController(nav, animated: true)
    //    self.viewController.presnt(secondViewController, animated: true)
//    self.viewController(secondViewController, animated: true, completion: nil)
    
//if let p1 = param1 , let p2 = param2 {
//if p1 >= 0 && p1 >= 0{
// let total = String(p1 + p2)
//pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: total)
//}else {
//pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Something wrong")
//}
//}

    

}
