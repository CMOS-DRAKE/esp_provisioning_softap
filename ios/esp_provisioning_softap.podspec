Pod::Spec.new do |s|
  s.name             = 'esp_provisioning_softap'
  s.version          = '1.0.0'
  s.summary          = 'ESP Provisioning SoftAP Plugin'
  s.description      = 'Flutter plugin for ESP device provisioning via SoftAP'
  s.homepage         = 'https://github.com/yourusername/esp_provisioning_softap'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Name' => 'your.email@example.com' }
  s.source           = { :path => '.' }
  s.source_files     = 'Classes/**/*'
  s.dependency 'Flutter'
  s.platform = :ios, '12.0'
  s.swift_version    = '5.0'

  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES' }
end