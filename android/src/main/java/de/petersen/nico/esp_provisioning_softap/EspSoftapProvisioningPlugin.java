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
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "esp_provisioning_softap");
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("init")) {
      byte[] key = call.argument("key");
      byte[] iv = call.argument("iv");

      if (key == null || iv == null) {
        result.error("INVALID_ARGUMENT", "Key and IV must not be null", null);
        return;
      }

      IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
      SecretKeySpec secretKeySpec = new SecretKeySpec(key, 0, key.length, "AES");
      try {
        this.cipher = Cipher.getInstance("AES/CTR/NoPadding");
        this.cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        result.success(true);
      } catch (NoSuchAlgorithmException | InvalidKeyException | 
               InvalidAlgorithmParameterException | NoSuchPaddingException e) {
        e.printStackTrace();
        result.error("CRYPTO_ERROR", "Failed to initialize cipher: " + e.getMessage(), null);
      }
    } else if (call.method.equals("crypt")) {
      if (cipher == null) {
        result.error("NOT_INITIALIZED", "Cipher not initialized. Call init first.", null);
        return;
      }

      byte[] data = call.argument("data");
      if (data == null) {
        result.error("INVALID_ARGUMENT", "Data must not be null", null);
        return;
      }

      try {
        byte[] ret = cipher.update(data);
        result.success(ret);
      } catch (Exception e) {
        e.printStackTrace();
        result.error("CRYPTO_ERROR", "Encryption failed: " + e.getMessage(), null);
      }
    } else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
    channel = null;
    cipher = null;
  }
}