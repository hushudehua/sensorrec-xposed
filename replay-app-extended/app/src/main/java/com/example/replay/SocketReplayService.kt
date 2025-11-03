package com.example.replay;

import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.SystemClock;
import java.io.File;
import kotlin.concurrent.thread;
import java.io.FileInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import sensorrec.FrameOuterClass.Frame;
import sensorrec.FrameOuterClass.Header;
import android.util.Log;

/**
 * Extended replay service that optionally opens a UNIX domain socket (here simplified as TCP loopback)
 * to feed recorded data to a local Xposed/LSPosed module. Useful when the module prefers
 * an IPC channel instead of reading the big file.
 *
 * This example opens a loopback server on port 33333 (adjust as needed).
 */
public class SocketReplayService extends Service {
    private static final int PORT = 33333;
    private static final String TAG = "SocketReplayService";
    private final String recordPath = "/sdcard/sensorrec_record.bin";

    @Override
    public void onCreate() {
        super.onCreate();
        startServer();
        // Also continue to support mock location playback (not shown here for brevity)
    }

    private void startServer() {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(PORT)) {
                Log.i(TAG, "Socket server listening on " + PORT);
                while (true) {
                    Socket client = server.accept();
                    Log.i(TAG, "Client connected");
                    // send frames to client
                    try (FileInputStream fis = new FileInputStream(recordPath)) {
                        byte[] buf4 = new byte[4];
                        while (fis.read(buf4) == 4) {
                            int len = ByteBuffer.wrap(buf4).order(ByteOrder.LITTLE_ENDIAN).getInt();
                            byte[] data = new byte[len];
                            int read = 0;
                            while (read < len) {
                                int r = fis.read(data, read, len - read);
                                if (r < 0) break;
                                read += r;
                            }
                            // send length + data to client
                            client.getOutputStream().write(buf4);
                            client.getOutputStream().write(data);
                            client.getOutputStream().flush();
                            // small sleep to avoid hogging
                            Thread.sleep(1);
                        }
                    } catch (Throwable ex) {
                        Log.e(TAG, "Error sending frames: " + ex.getMessage());
                    } finally {
                        client.close();
                    }
                }
            } catch (Throwable e) {
                Log.e(TAG, "Server failed: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
