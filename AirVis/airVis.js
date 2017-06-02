const MIN_ZOOM = 16;
const START_POS = {lat: 52.52, lng: 13.42};
const START_ZOOM = 18;

var map;
var devices;
var discoveredLocations;
var filter;
var status;


/**
 * On Startup Called
 */
function initMap() {
    $(':checkbox').checkboxpicker();
    infowindow = new google.maps.InfoWindow(); /* SINGLE */
    map = new google.maps.Map(document.getElementById('map'), {
        center: START_POS,
        zoom: START_ZOOM
    });

    devices = new DeviceList();
    discoveredLocations = new DiscoveredLocations();
    filter = new Filter();
    status = new Status();

    map.addListener('mouseup', function(e) {
        //console.log(map.getCenter().lat() + ' ' + map.getCenter().lng() + ' ' + map.getZoom());
        var bounds = map.getBounds();
        getDataFromApiWithin(bounds);
        // getDataFromApiAt(map.getCenter().lat(), map.getCenter().lng(), 0.001);
    });

    // Limit the zoom level
    google.maps.event.addListener(map, 'zoom_changed', function() {
        if (map.getZoom() < MIN_ZOOM) map.setZoom(MIN_ZOOM);
    });
    getDataFromApiAt(map.getCenter().lat(), map.getCenter().lng(), 0.0003);


}

function getDataFromApiAt(lat, lng, dist) {
    var url = BASEPATH+'/devicesAtPos?lon='+lng+'&lat='+lat+'&distance='+dist;
    doAsyncCall(url);
}

function getDataFromApiWithin(bounds) {
    var northEast = bounds.getNorthEast();
    var neLat = northEast.lat();
    var neLon = northEast.lng();
    var southWest = bounds.getSouthWest();
    var swLat = southWest.lat();
    var swLon = southWest.lng();
    var url = BASEPATH+'/devicesAtRect?neLat='+neLat+'&neLon='+neLon+'&swLat='+swLat+'&swLon='+swLon;
    doAsyncCall(url);
    var url = "https://api.what3words.com/v2/grid?bbox="+neLat+","+neLon+","+swLat+","+swLon+"&format=json&key="+WhatThreeWords_KEY;
    //callWhat3Words(url);
}

function processWhat3Words(data) {
    //console.log(data);
    for (var i = 0; i < data["lines"].length; i++) {
        var line = data["lines"][i];
        //console.log(line);

        var startPos = new google.maps.LatLng( line["start"]["lat"], line["start"]["lng"]);
        var endPos = new google.maps.LatLng( line["end"]["lat"], line["end"]["lng"]);

        console.log(line["start"]["lng"]);

        var path = new google.maps.Polyline({
            path: [
                startPos,
                endPos
            ],
            geodesic: true,
            strokeColor: '#00FF00',
            strokeOpacity: 0.3,
            strokeWeight: 1,
            zIndex   : 9,
            map: map
        });
        path.setMap(map);
    }

}
function callWhat3Words(url) {
    $.ajax
    ({
        type: "GET",
        url: url,
        success: function (data){
            processWhat3Words(data);
        }
    });
}

function doAsyncCall(url){
    $.ajax
    ({
        type: "GET",
        url: url,
        beforeSend: function (xhr){
            xhr.setRequestHeader('Authorization', make_base_auth(USER, KEY));
        },
        success: function (data){
            processIncomingData(data);
        }
    });
}

function processIncomingData(data) {
    console.log("incoming data");

    document.getElementById("statusRequestDeviceCount").innerHTML = data['count'];
    document.getElementById("statusRequestTimeDB").innerHTML = data['time'];

    data = data['result'];
    for (var i = 0; i < data.length; i++) {
        var device = new Device(data[i]);
        devices.add(device);
    }
    updatemap();
}

function updatemap() {
    devices.updateDrawing();
}

function doAsyncCallForAdditionalData(url){
    $.ajax
    ({
        type: "GET",
        url: url,
        beforeSend: function (xhr){
            xhr.setRequestHeader('Authorization', make_base_auth(USER, KEY));
        },
        success: function (data){
            processIncomingAdditionalData(data['result']);
        }
    });
}

function processIncomingAdditionalData(data) {
    //console.log("processIncomingAdditionalData");
    discoveredLocations.removeMarkers();
    discoveredLocations.removePolylines();
    discoveredLocations.locations = [];
    discoveredLocations.addLocationsForDevice(data);
}

function make_base_auth(user, password) {
    var tok = user + ':' + password;
    var hash = btoa(tok);
    return "Basic " + hash;
}

$("#search_input").on("input", function (e) {
    if ($(this).data("lastval") != $(this).val()) {
        $(this).data("lastval", $(this).val());
        //change action
        var value = $(this).val();
        filter.name = value;
        devices.updateFilter();
        updatemap();
    };
});

$("#sliderRangeNumLocations").slider().on('slide', rangeNumLocationsChange);
function rangeNumLocationsChange() {
    var rangeValue = $("#sliderRangeNumLocations").data('slider').getValue();
    var min = rangeValue[0];
    var max = rangeValue[1];
    filter.minNumLocations = min;
    filter.maxNumLocations = max;
    devices.updateFilter();
    updatemap();
}

$("#sliderRangRadius").slider().on('slide', rangeRadiusChange);
function rangeRadiusChange() {
    var rangeValue = $("#sliderRangRadius").data('slider').getValue();
    var min = rangeValue[0];
    var max = rangeValue[1];
    filter.minRadius = min;
    filter.maxRadius = max;
    devices.updateFilter();
    updatemap();
}

$("#buttonResetFilter").click(function(){
    filter.reset();
    $('#checkBoxWiFi').prop('checked', true);
    $('#checkBoxFreeWiFiOnly').prop('checked', false);
    $('#checkBoxFreeWiFiOnly').prop('disabled', false);
    $('#checkBoxBlEdr').prop('checked', true);
    $('#checkBoxBlLe').prop('checked', true);
    $("#sliderRangeNumLocations").slider('setValue', [1,100]);
    $("#sliderRangRadius").slider('setValue', [1,200]);
    $("#search_input").val('');
    devices.updateFilter();
    updatemap();
});

$('#checkBoxWiFi').on('change', function() {
    filter.showWifi = !filter.showWifi;
    if(filter.showWifi){
        $('#checkBoxFreeWiFiOnly').prop('disabled', false);
    }else{
        $('#checkBoxFreeWiFiOnly').prop('disabled', true);
        $('#checkBoxFreeWiFiOnly').prop('checked', false);

    }
    devices.updateFilter();
    updatemap();
});

$('#checkBoxFreeWiFiOnly').on('change', function() {
    filter.showFreeWifiOnly = !filter.showFreeWifiOnly;
    devices.updateFilter();
    updatemap();
});

$('#checkBoxBlEdr').on('change', function() {
    filter.showBlEdr = !filter.showBlEdr;
    devices.updateFilter();
    updatemap();
});

$('#checkBoxBlLe').on('change', function() {
    filter.showBlLe = !filter.showBlLe;
    devices.updateFilter();
    updatemap();
});