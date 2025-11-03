Mi 8 (dipper) — Device-specific quick manual (Android 10)

This file summarizes the recommended, device-specific steps for Mi 8 (dipper) running Android 10.

WARNING: These steps involve bootloader unlocking, root, Magisk, and hooking system services. Make a full backup before proceeding.

1) Unlock bootloader (official Xiaomi flow)
   - Bind Mi account on phone (Settings -> Mi Account)
   - Apply for unlocking at https://en.miui.com/unlock/ (or using official Mi Unlock tool)
   - Use Mi Unlock tool on PC to unlock in fastboot (fastboot mode required)
   - Commands:
     adb reboot bootloader
     fastboot oem device-info   # check unlocked state
     # use Mi Unlock tool GUI to unlock

2) Install Magisk (recommended latest stable for Android 10)
   - Flash patched boot image (Magisk Manager patches boot)
   - Install Magisk APK
   - Reboot; confirm magisk installed via Magisk Manager.

3) Install LSPosed (recommended over classic Xposed)
   - Install Riru + LSPosed module compatible with Android 10 (choose versions from LSPosed release page)
   - Reboot, enable LSPosed manager, configure per-app modules.

4) Deploy SensorRec stack
   - Push recorder-app APK; grant location/sensor permissions; start Recorder service.
   - Or compile and install from provided Android Studio projects.
   - Use Magisk module to host native daemon if desired (place daemon in magisk-module and flash module).
   - For hooking/injection, install the compiled Xposed/LSPosed module (place the module apk under LSPosed and enable it).

5) SELinux notes
   - Check current status: adb shell su -c getenforce
   - If Enforcing and you encounter permission failures accessing /dev nodes, consider debugging with logcat and temporary permissive only in isolated test environment:
     adb shell su -c setenforce 0
     # After tests restore:
     adb shell su -c setenforce 1

6) Finding GPS / sensor nodes
   - Run the included find_gps_nodes.sh and collect outputs.
   - If a tty GPS node is present (e.g., /dev/ttyMSM0), you may be able to write NMEA sentences (risky).
   - Prefer app-level mock + Xposed injection first.

7) Safety rollback
   - To undo Magisk module: use Magisk Manager to uninstall or remove module zip via recovery or Magisk.
   - To remove native daemon: adb shell su -c "rm -rf /data/local/tmp/sensorrec && rm /data/local/tmp/sensorrec_record.bin"

If you provide the outputs of the included detection scripts (find_gps_out.txt, dumpsys sensorservice), I will produce a custom low-level injection snippet tuned for your device nodes.
