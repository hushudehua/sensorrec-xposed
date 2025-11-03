SensorRec — Step-by-step deployment for Mi 8 (dipper) Android 10

This step-by-step assumes you have a PC with adb/fastboot and Android Studio.

1) Preliminaries
   - Install adb & fastboot on your PC.
   - Install Java JDK & Android Studio.
   - Install protoc (optional; recommended for generating protobuf code).

2) Unlock bootloader (Xiaomi official flow)
   - On phone: Settings -> Mi Account -> bind account.
   - Request unlock via Mi Unlock tool (requires login).
   - Reboot to bootloader and use Mi Unlock to unlock.

3) Root with Magisk
   - Patch boot image with Magisk Manager and flash via fastboot.
   - Reboot, install Magisk APK, verify root.

4) Install LSPosed
   - Install Riru + LSPosed via Magisk modules (choose versions compatible with Android 10).
   - Reboot and enable LSPosed.

5) Build & Install Recorder App
   - Open recorder-app in Android Studio.
   - Generate protobuf Java classes:
     protoc --java_out=app/src/main/java proto/sensor_record.proto
   - Build & install APK.
   - Grant location & sensor permissions, start Recorder.

6) Record a short session
   - Start Recorder, collect ~30s of movement.
   - Pull file: adb root; adb pull /data/local/tmp/sensorrec_record.bin ./record.bin

7) Build & Install Replay App
   - Open replay-app or replay-app-extended in Android Studio.
   - Generate protobuf Java classes (same as above).
   - Install Replay APK. In Developer options, set it as 'Mock location' app.
   - Place record file on device (/sdcard/sensorrec_record.bin).
   - Start Replay -> Start Replay Service.

8) Install Xposed Module
   - Build xposed-module (adjust dependency to local Xposed/LSPosed API).
   - Install module using LSPosed manager, enable module, and target the app(s) you want to inject.
   - Reboot for Xposed module activation.

9) Advanced: Socket replay + Xposed IPC
   - Start SocketReplayService (in replay-app-extended) to open TCP loopback port 33333.
   - Modify Xposed module to connect to localhost:33333 and receive frames, then feed listeners.
   - This avoids reading big file repeatedly.

10) Troubleshooting
   - If SensorEvent construction fails on your ROM, check Xposed logs (logcat) for reflection errors and paste them to me; I will patch the module to use alternative hook (SystemSensorManager).
   - If you can't access /data paths, ensure Magisk grants required file access or use /sdcard for temporary testing.

Safety & rollback:
   - To remove module: disable in LSPosed -> uninstall, remove Magisk module if used.
   - To remove files: adb shell su -c "rm -rf /data/local/tmp/sensorrec* /sdcard/sensorrec_record.bin"

If you want, I will now:
- Compile updated package and upload zip with all new files (xposed source, replay extended, python protos placeholders, manuals).
- Or attempt to generate protoc outputs for Java/Python if protoc available. (I can include guidance.)
