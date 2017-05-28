/**
 * Created by fabi on 21.05.17.
 */

class Status{

    setDeviceCount(value) {
        console.log("update deviceCount "+value);
        this.statusDeviceCount.innerHTML = value;
    }
    setLocationCount(value) {
        console.log("update locationCount "+value);
        this.statusLocationCount.innerHTML = value;

    }
    setRequestDeviceCount(value) {
        console.log("update requestDeviceCount "+value);
        this.statusRequestDeviceCount.innerHTML = value;

    }
    setSequestTimeDB(value) {
        console.log("update requestTimeDB "+value);
        this.statusRequestTimeDB.innerHTML = value;
    }

    constructor(){
        this.statusDeviceCount = document.getElementById("statusDeviceCount");
        this.statusLocationCount = document.getElementById("statusLocationCount");
        this.statusRequestDeviceCount = document.getElementById("statusRequestDeviceCount");
        this.statusRequestTimeDB = document.getElementById("statusRequestTimeDB");

        // fucking slow - improve on server
        //this.makeStartupCall();
    }


    makeStartupCall() {
        var that = this;
        $.ajax
        ({
            type: "GET",
            url: BASEPATH + "/status",
            beforeSend: function (xhr) {
                xhr.setRequestHeader('Authorization', make_base_auth(USER, KEY));
            },
            success: function (data) {
                that.setDeviceCount(data['devices']);
                that.setLocationCount(data['locations']);
                that.setSequestTimeDB(data['time']);
            }

        });
    }

}