#!/system/bin/sh
# simple service.sh executed by Magisk on boot
MODDIR=${0%/*}
OUT_DIR="/data/local/tmp"
mkdir -p $OUT_DIR/sensorrec
# if you ship a native daemon binary inside module, copy it:
# cp $MODDIR/daemon_binary $OUT_DIR/sensorrec/recorder_daemon
# chmod 755 $OUT_DIR/sensorrec/recorder_daemon
# start daemon as root (adjust path)
# su -c "$OUT_DIR/sensorrec/recorder_daemon --out $OUT_DIR/sensorrec/record.bin &"
# Alternatively, start a simple command placeholder
echo "magisk service started" > /data/local/tmp/sensorrec/service_started.txt
