/**
 * Created by fabi on 20.05.17.
 */
class Filter{

    constructor() {
        this.reset()
    }

    reset(){
        this.name = "";
        this.minNumLocations = 1;
        this.maxNumLocations = 1000;
        this.minRadius = 1;
        this.maxRadius = 200;
        this.showWifi = true;
        this.showFreeWifiOnly = false;
        this.showBlEdr = true;
        this.showBlLe = true;
    }

    toString(){
        return "Filter Setings: \n"+
            "\tname " + this.name + "\n"+
            "\tminNumLocations " + this.minNumLocations + "\n"+
            "\tmaxNumLocations " + this.maxNumLocations + "\n"+
            "\tminRadius " + this.minRadius + "\n"+
            "\tmaxRadius " + this.maxRadius + "\n"+
            "\tshowWifi"  + this.showWifi + "\n"+
            "\tshowBlEdr " + this.showBlEdr + "\n"+
            "\tshowBlLe " + this.showBlLe;

    }
}