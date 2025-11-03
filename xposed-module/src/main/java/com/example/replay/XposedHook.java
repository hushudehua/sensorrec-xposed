package com.example.replay;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import android.hardware.SensorEventListener;
import android.hardware.Sensor;
import android.os.Handler;
import android.os.Looper;

/**
 * 这是一个模板示例，展示如何 hook SensorManager.registerListener()
 * 真实实现需要：
 * - 在 afterHookedMethod 中保存 listener 实例
 * - 启动线程按记录文件给 listener 回调 onSensorChanged()（构造 SensorEvent 较复杂）
 * - 同理 hook LocationManager.requestLocationUpdates()
 *
 * 注意：不同 Android 版本内部实现不同，需用 reflection 创建/写入 SensorEvent 内部字段或直接 hook SystemSensorManager 底层方法。
 */
public class XposedHook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        XposedHelpers.findAndHookMethod("android.hardware.SensorManager", lpparam.classLoader,
            "registerListener", SensorEventListener.class, Sensor.class, int.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    final SensorEventListener listener = (SensorEventListener) param.args[0];
                    // 示例：在主线程每秒喂一次伪造数据（严格实现需读取实际记录并按时间戳重放）
                    final Handler h = new Handler(Looper.getMainLooper());
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // 伪造回调：listener.onSensorChanged(...)
                                // 真实构造 SensorEvent 需 reflection（此处仅提示）
                            } catch (Throwable t) { t.printStackTrace(); }
                            h.postDelayed(this, 1000);
                        }
                    }, 1000);
                }
        });
    }
}
