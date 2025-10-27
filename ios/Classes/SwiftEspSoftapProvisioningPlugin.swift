import Flutter
import UIKit

public class SwiftEspSoftapProvisioningPlugin: NSObject, FlutterPlugin {
    private var cryptoAES: CryptoAES?
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(
            name: "esp_provisioning_softap",
            binaryMessenger: registrar.messenger()
        )
        let instance = SwiftEspSoftapProvisioningPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch call.method {
        case "init":
            handleInit(call: call, result: result)
        case "crypt":
            handleCrypt(call: call, result: result)
        default:
            result(FlutterMethodNotImplemented)
        }
    }
    
    private func handleInit(call: FlutterMethodCall, result: @escaping FlutterResult) {
        guard let args = call.arguments as? [String: Any],
              let keyData = args["key"] as? FlutterStandardTypedData,
              let ivData = args["iv"] as? FlutterStandardTypedData else {
            result(FlutterError(
                code: "INVALID_ARGUMENT",
                message: "Missing or invalid key/iv arguments",
                details: nil
            ))
            return
        }
        
        cryptoAES = CryptoAES(key: keyData.data, iv: ivData.data)
        result(true)
    }
    
    private func handleCrypt(call: FlutterMethodCall, result: @escaping FlutterResult) {
        guard let cryptoAES = self.cryptoAES else {
            result(FlutterError(
                code: "CIPHER_NOT_INITIALIZED",
                message: "Cipher not initialized. Call init first.",
                details: nil
            ))
            return
        }
        
        guard let args = call.arguments as? [String: Any],
              let dataTyped = args["data"] as? FlutterStandardTypedData else {
            result(FlutterError(
                code: "INVALID_ARGUMENT",
                message: "Missing or invalid data argument",
                details: nil
            ))
            return
        }
        
        guard let encrypted = cryptoAES.encrypt(data: dataTyped.data) else {
            result(FlutterError(
                code: "ENCRYPTION_ERROR",
                message: "Failed to encrypt data",
                details: nil
            ))
            return
        }
        
        result(FlutterStandardTypedData(bytes: encrypted))
    }
}