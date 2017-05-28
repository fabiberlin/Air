/**
 * Created by fabi on 24.04.17.
 */

class Location{

    get polyline() {
        return this._polyline;
    }

    set polyline(value) {
        this._polyline = value;
    }

    get longitude() {
        return this._longitude;
    }

    set longitude(value) {
        this._longitude = value;
    }
    get latitude() {
        return this._latitude;
    }

    set latitude(value) {
        this._latitude = value;
    }
    get accuracy() {
        return this._accuracy;
    }

    set accuracy(value) {
        this._accuracy = value;
    }
    get placedOnMap() {
        return this._placedOnMap;
    }

    set placedOnMap(value) {
        this._placedOnMap = value;
    }
    get marker() {
        return this._marker;
    }

    set marker(value) {
        this._marker = value;
    }

    constructor(longitude, latitude, accuracy){
        this._longitude = longitude;
        this._latitude = latitude;
        this._accuracy = accuracy;
        this._placedOnMap = false;
        this._marker = null;
        this._polyline = null;
    }

    draw(){

    }

    undraw(){

    }

}