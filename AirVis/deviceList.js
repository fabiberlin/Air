/**
 * Created by fabian on 06.02.17.
 */

class DeviceList {

    constructor() {
        this.devices = [];
    }

    contains(device){
        for (var i = 0; i < this.devices.length; i++){
            if (this.devices[i].address == device.address){
                return true;
            }
        }
        return false;
    }

    add(device){
        if (!this.contains(device)) {
            this.devices.push(device);
        }
    }

    size(){
        return this.devices.length;
    }

    get(index){
        return this.devices[index];
    }

    updateFilter(){
        for (var i = 0; i < this.devices.length; i++){
            if (this.devices[i].matchesFilter()){
                this.devices[i].dest_visibilty = true;
            }else{
                this.devices[i].dest_visibilty = false;
            }
        }
    }

    updateDrawing(){
        for (var i = 0; i < this.devices.length; i++){
            this.devices[i].redraw();
        }
    }
}