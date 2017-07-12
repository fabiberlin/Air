/**
 * Created by fabi on 21.05.17.
 */

class Status{

    constructor(){
        this.statusDeviceCount = document.getElementById("statusDeviceCount");
        this.statusLocationCount = document.getElementById("statusLocationCount");
        this.statusRequestDeviceCount = document.getElementById("statusRequestDeviceCount");
        this.statusRequestTimeDB = document.getElementById("statusRequestTimeDB");

        //this.makeStartupCall();
    };

    setDeviceCount(v) {
        console.log("update deviceCount "+v);
        this.statusDeviceCount.innerHTML = v;
    };
    setLocationCount(v) {
        console.log("update locationCount "+v);
        this.statusLocationCount.innerHTML = v;

    };
    setRequestDeviceCount(v) {
        console.log("update requestDeviceCount "+v);
        this.statusRequestDeviceCount.innerHTML = v;

    };
    setSequestTimeDB(v) {
        console.log("update requestTimeDB "+v);
        this.statusRequestTimeDB.innerHTML = v;
    };

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
                console.log(data);
                that.setDeviceCount(data['devices']);
                that.setLocationCount(data['locations']);
                that.setSequestTimeDB(data['time']);
            }

        });
    };

    toString(){
        return "I'm a status";
    };

}