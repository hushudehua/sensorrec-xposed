#!/usr/bin/env python3
# usage: python3 parse_and_export.py /path/to/record.bin
import struct, sys
from sensorrec_pb2 import Frame
import simplekml, pandas as pd

def read_frames(path):
    with open(path, 'rb') as f:
        while True:
            lenb = f.read(4)
            if not lenb: break
            l = struct.unpack('I', lenb)[0]
            data = f.read(l)
            fr = Frame()
            fr.ParseFromString(data)
            yield fr

def export_csv(path):
    rows=[]
    for fr in read_frames(path):
        t = fr.header.timestamp_nanos
        if fr.HasField('location'):
            loc = fr.location
            rows.append({'ts_nanos':t,'type':'LOC','lat':loc.latitude,'lon':loc.longitude,'alt':loc.altitude,'acc':loc.accuracy})
        elif fr.HasField('sensor'):
            sv = fr.sensor.values[0]
            rows.append({'ts_nanos':t,'type':'SENSOR','sensor':fr.sensor.sensor_type,'x':sv.x,'y':sv.y,'z':sv.z})
    df = pd.DataFrame(rows)
    df.to_csv(path + '.csv', index=False)
    print("Wrote", path + '.csv')

def export_kml(path):
    kml = simplekml.Kml()
    coords=[]
    for fr in read_frames(path):
        if fr.HasField('location'):
            coords.append((fr.location.longitude, fr.location.latitude, fr.location.altitude))
    if coords:
        ls = kml.newlinestring(name="trajectory", coords=coords)
        ls.altitudemode = simplekml.AltitudeMode.absolute
    kml.save(path + '.kml')
    print("Wrote", path + '.kml')

if __name__ == '__main__':
    p = sys.argv[1]
    export_csv(p)
    export_kml(p)
