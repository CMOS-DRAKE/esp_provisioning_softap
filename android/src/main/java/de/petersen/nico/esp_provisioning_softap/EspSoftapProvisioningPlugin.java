package de.petersen.nico.esp_provisioning_softap;

import androidx.annotation.NonNull;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/** EspSoftapProvisioningPlugin */
public class EspSoftapProvisioningPlugin implements FlutterPlugin, MethodCallHandler {
  private MethodChannel channel;
  private Cipher cipher;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(
        flutterPluginBinding.getBinaryMessenger(), 
        "esp_provisioning_softap"
    );
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    switch (call.method) {
      case "init":
        handleInit(call, result);
        break;
      case "crypt":
        handleCrypt(call, result);
        break;
      default:
        result.notImplemented();
        break;
    }
  }

  private void handleInit(@NonNull MethodCall call, @NonNull Result result) {
    try {
      byte[] key = call.argument("key");
      byte[] iv = call.argument("iv");

      if (key == null || iv == null) {
        result.error("INVALID_ARGUMENT", "Key or IV is null", null);
        return;
      }

      IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
      SecretKeySpec secretKeySpec = new SecretKeySpec(key, 0, key.length, "AES");
      
      this.cipher = Cipher.getInstance("AES/CTR/NoPadding");
      this.cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
      
      result.success(true);
    } catch (NoSuchAlgorithmException | InvalidKeyException | 
             InvalidAlgorithmParameterException | NoSuchPaddingException e) {
      result.error("CIPHER_ERROR", "Failed to initialize cipher: " + e.getMessage(), null);
    }
  }

  private void handleCrypt(@NonNull MethodCall call, @NonNull Result result) {
    try {
      byte[] data = call.argument("data");
      
      if (data == null) {
        result.error("INVALID_ARGUMENT", "Data is null", null);
        return;
      }

      if (cipher == null) {
        result.error("CIPHER_NOT_INITIALIZED", "Cipher not initialized. Call init first.", null);
        return;
      }

      byte[] ret = cipher.update(data);
      result.success(ret);
    } catch (Exception e) {
      result.error("CRYPT_ERROR", "Failed to encrypt/decrypt: " + e.getMessage(), null);
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    if (channel != null) {
      channel.setMethodCallHandler(null);
      channel = null;
    }
    cipher = null;
  }
}