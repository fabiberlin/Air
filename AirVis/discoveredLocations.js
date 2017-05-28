/**
 * Created by fabian on 24.02.17.
 */
class DiscoveredLocations {

    constructor() {
        this.locations = [];

    }

    addLocationsForDevice(device){

        console.log(device);

        for (var i = 0; i < device.locations.length; i++) {

            //console.log("add a location");

            var devicelongitude = device.loc[0];
            var devicelatitude = device.loc[1];

            var longitude = device.locations[i].loc[0];
            var latitude = device.locations[i].loc[1];

            var location = new Location(longitude, latitude, device.locations[i].accuracy);

            var latLng = new google.maps.LatLng( latitude, longitude);
            var devicelatLng = new google.maps.LatLng( devicelatitude, devicelongitude);

            var marker = new google.maps.Marker({
                position : latLng,
                map      : map,
                zIndex   : 10
            });
            marker.setIcon('http://maps.google.com/mapfiles/ms/icons/green-dot.png')
            marker.setMap(map);
            location.marker = marker;

            var path = new google.maps.Polyline({
                path: [
                    devicelatLng,
                    latLng
                ],
                geodesic: true,
                strokeColor: '#00FF00',
                strokeOpacity: 1.0,
                strokeWeight: 2,
                zIndex   : 9,
                map: map
            });

            path.setMap(map);
            location.polyline = path

            this.locations.push(location);
        }
        console.log(this.locations);
    }

    getNiceString(){
        var s = "todo";
        return s;
    }

    getNiceHTMLString(){
        var s = "todo";
        return s;
    }

    removeMarkers(){
        for (var i = 0; i < this.locations.length; i++) {
            this.locations[i].marker.setMap(null);
        }
    }

    removePolylines(){
        for (var i = 0; i < this.locations.length; i++) {
            this.locations[i].polyline.setMap(null);
        }
    }

    drawMarkers(){
        for (var i = 0; i < this.locations.length; i++) {
            this.locations[i].marker.setMap(map);
        }
    }

}