package com.example.sensorrec

class ProtoUtils {
    companion object {
        init { System.loadLibrary("recorder_native") }
        @JvmStatic external fun nativeOpenFile(path: String): Boolean
        @JvmStatic external fun nativeCloseFile()
        @JvmStatic external fun nativeWriteFrame(bytes: ByteArray): Boolean
    }
}
