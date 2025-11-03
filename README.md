SensorRec — 快速入门

前提:
- Android Studio + NDK 已安装
- protoc 已安装（用于生成 Java/Python 的 protobuf）
- 目标设备已 root（用于直接把文件放 /data/local/tmp 或安装 Magisk/Xposed）

步骤概览:

1) 生成 protobuf Java/Python
   protoc --java_out=recorder-app/app/src/main/java proto/sensor_record.proto
   protoc --python_out=tools proto/sensor_record.proto

2) 在 Android Studio 中打开 recorder-app，Sync Gradle，编译并安装到设备.
   - 录制文件默认写入 /data/local/tmp/sensorrec_record.bin（可在 RecorderService.kt 修改）
   - 运行 MainActivity，允许权限，点击 Start Recorder Service 开始录制

3) 获取录制文件
   - 设备为 root：adb root; adb pull /data/local/tmp/sensorrec_record.bin ./ 
   - 或在 RecorderService 中改路径到 /sdcard/ 以便非 root 拷贝（仅测试）

4) 回放
   - 把 record.bin 放到 replay-app 指定路径 (示例代码期待 /sdcard/sensorrec_record.bin)
   - 安装 replay-app，将其设为允许模拟位置信息的应用（开发者选项）
   - 启动 ReplayActivity -> Start Replay Service
   - 目标 app 将从 Mock Provider 接收位置

5) 高级：
   - 要把位置/传感器注入到任意第三方 app，不被其发现: 参阅 xposed-module 模板并实现 zygote 注入 (EdXposed/LSPosed + Magisk 环境)
   - 如果需要 native 层注入或向 GPS 串口写 NMEA，请自行分析设备 /dev 节点并谨慎操作（风险高）

调试与注意:
- 高频传感器要批量写入，避免频繁 JNI 调用
- 时间同步基于 elapsedRealtimeNanos(); 回放时映射到 SystemClock.elapsedRealtimeNanos()
- 某些设备 SELinux 强制模式会阻挡直接访问 /dev/，需 Magisk 模块或临时降级 SELinux（仅限实验室环境）
- 请仅在你有权限的设备上使用本工具链，尊重隐私与法律
