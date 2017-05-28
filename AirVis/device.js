/**
 * Created by fabian on 06.02.17.
 */

class Device {

    get marker() {
        return this._marker;
    }

    set marker(value) {
        this._marker = value;
    }

    constructor(apiDeviceData) {
        this.longitude = apiDeviceData['loc'][0];
        this.latitude = apiDeviceData['loc'][1];
        this.radius = apiDeviceData['radius'];
        this.name = apiDeviceData['name'];
        this.address = apiDeviceData['address'];
        this.security = apiDeviceData['security'];
        this.type = apiDeviceData['type'];
        this.numOfLocations = apiDeviceData['numL'];
        this._marker = null;
        this.visibilty = false;

        if (this.matchesFilter()){
            this.dest_visibilty = true;
        }else{
            this.dest_visibilty = false;
        }
        //console.log('Created a Device\n' + this.getNiceString());
    }

    getNiceString(){
        var s = "";
        s += this.name + '\n';
        s += this.address + '\n';
        s += this.longitude + '\n';
        s += this.latitude + '\n';
        s += this.type + '\n';
        s += this.radius + '\n';
        s += this.security + '\n';
        return s;
    }

    redraw(){
        if (this.dest_visibilty && !this.visibilty){
            this.draw();
            this.visibilty = true;
        } else if (!this.dest_visibilty && this.visibilty){
            this.undraw();
            this.visibilty = false;
        }
    }

    draw(){
        if (this.marker == null){
            var latLng = new google.maps.LatLng( this.latitude, this.longitude);
            // console.log(latLng);
            this.marker = new google.maps.Marker({
                position : latLng,
                zIndex   : 8,
                map      : map
            });
            var that = this;
            google.maps.event.addListener(this.marker, 'click', function(){
                infowindow.close(); // Close previously opened infowindow
                infowindow.setContent( "<div id='infowindow'>"+ that.getNiceHTMLString() +"</div>");
                infowindow.open(map, that.marker);

                var param = that.address;
                var url = BASEPATH+'/device?address='+param;
                doAsyncCallForAdditionalData(url);
            });

            google.maps.event.addListener(infowindow,'closeclick',function(){
                infowindow.close();
                discoveredLocations.removeMarkers();
                discoveredLocations.removePolylines();
            });
        }
        else{
            this.marker.setMap(map);
        }
    }

    undraw(){
        if (this.marker != null){
            this.marker.setMap(null);
        }
    }

    matchesFilter(){
        if (this.type == "WiFi" && !filter.showWifi){
            return false;
        }
        if (this.type == "BtEdr" && !filter.showBlEdr){
            return false;
        }
        if (this.type == "BtLe" && !filter.showBlLe){
            return false;
        }
        if (this.name.toLowerCase().indexOf(filter.name.toLowerCase()) > -1) {
            // matches
        } else {
            return false;
        }
        if (this.numOfLocations < filter.minNumLocations){
            return false
        }
        if (this.numOfLocations > filter.maxNumLocations){
            if (filter.maxNumLocations != 100){
                return false;
            }
        }
        if (this.radius < filter.minRadius){
            return false
        }
        if (this.radius > filter.maxRadius) {
            if (filter.maxRadius != 200) {
                return false;
            }
        }
        if (this.security != "[ESS]" && filter.showFreeWifiOnly  && this.type == "WiFi") {
            return false;
        }
        return true;
    }

    getNiceHTMLString(){
        var s = "<h2>"+this.name+"</h2>" +
            "<table>" +
            "<tr>" +
            "<td>" +
            "Address:" +
            "</td>" +
            "<td>" +
            this.address +
            "</td>" +
            "</tr>" +
            "<tr>" +
            "<td>" +
            "Type:" +
            "</td>" +
            "<td>" +
            this.type +
            "</td>" +
            "</tr>" +
            "<tr>" +
            "<td>" +
            "Radius:" +
            "</td>" +
            " <td>" +
            this.radius +
            "</td>" +
            "</tr>" +
            "<tr>" +
            "<td>" +
            "Security:" +
            "</td>" +
            "<td>" +
            this.security +
            "</td>" +
            "</tr>" +
            "<tr>" +
            "<td>" +
            "Locations:" +
            "</td>" +
            "<td>" +
            this.numOfLocations +
            "</td>" +
            "</tr>" +
            "<tr>" +
            "<td>" +
            "Latitude:" +
            "</td>" +
            "<td>" +
            this.latitude +
            "</td>" +
            "</tr>" +
            "<tr>" +
            "<td>" +
            "Longitude:" +
            "</td>" +
            "<td>" +
            this.longitude +
            "</td>" +
            "</tr>" +
            "</table>";
        return s;
    }

};