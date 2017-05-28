package baensch.de.airlocator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


public class SniffSink {

    List<Device> sink;

    public SniffSink() {
        this.sink = new ArrayList<>();
    }

    public void addDevice(Device device){
        if (!sink.contains(device)) {
            sink.add(device);
        }
    }

    public List<Device> getDevices(){
        List<Device> list = new ArrayList<>(sink);
        sink.clear();
        return list;
    }
}
