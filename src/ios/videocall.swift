
@objc(videocall) class videocall : CDVPlugin{
// MARK: Properties
var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR)
    private var room: Room?
    private  var navigationController: UINavigationController?
@objc(coolMethod:) func coolMethod(_ command: CDVInvokedUrlCommand) {
var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR)
    let roomName = command.arguments[0]  as? String
    let identity = command.arguments[1]  as? String
    let token = command.arguments[2]  as? String
    let id = command.arguments[3]  as? String
    
    DispatchQueue.main.async {
        // Background Thread

        let lobyViewController = LobbyViewController()
        lobyViewController.modalPresentationStyle = .fullScreen
        self.viewController?.present(lobyViewController, animated: true) { [self] in
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "ok")

            commandDelegate.send(pluginResult, callbackId: command.callbackId)
        }
    
    }
    }
    
}
