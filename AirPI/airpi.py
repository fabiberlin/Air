from flask import Flask, jsonify, request
from flask_pymongo import PyMongo
from flask import abort
from pymongo import GEO2D
from math import radians, cos, sin, asin, sqrt
from bson.son import SON
from flask_cors import CORS
from functools import wraps
from flask import request, Response
import time
import api_prefs

app = Flask(__name__)
CORS(app)
app.config['MONGO_DBaddress'] = 'airSnifferTest2'
app.config['MONGO_URI'] = 'mongodb://localhost:27017/airSnifferTest2'
mongo = PyMongo(app)


def check_auth(username, password):
    """This function is called to check if a username /
    password combination is valid.
    """
    return username == api_prefs.username and password == api_prefs.password


def authenticate():
    """Sends a 401 response that enables basic auth"""
    return Response(
        'Could not verify your access level for that URL.\n'
        'You have to login with proper credentials', 401,
        {'WWW-Authenticate': 'Basic realm="Login Required"'})


def requires_auth(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        auth = request.authorization
        if not auth or not check_auth(auth.username, auth.password):
            return authenticate()
        return f(*args, **kwargs)

    return decorated


def isvalid_type(type_param):
    toreturn = type_param == "WiFi" or type_param == "BtLe" or type_param == "BtEdr"
    app.logger.info("isvalid_param " + str(toreturn))
    return toreturn


@app.route('/devices', methods=['GET'])
@requires_auth
def get_all_devices():
    starttime = getTime()
    type = str(request.args.get('type'))
    counter = 0
    devices = mongo.db.devices
    output = []
    if type == "None":
        db_devices = devices.find()
    else:
        if isvalid_type(type):
            db_devices = devices.find({"type": type})
        else:
            abort(400)

    for q in db_devices:
        app.logger.info(str(q))
        if len(q['locations']) == 0:
            continue
        output.append({
            'address': q['address'],
            'name': q['name'],
            'loc': q['loc'],
            'timestamp': q['timestamp'],
            'radius': q['radius'],
            'numL': q['numL'],
            'type': q['type'],
            'security': q['security'],
            'locations': q['locations']})
        counter += 1
    end_time = getTime()
    app.logger.info(
        "get_all_devices- Get Device took " + str(end_time - starttime) + " ms - Devicecount: " + str(counter) + " for Type: " + type)
    return jsonify({'count': counter, 'time': (end_time - starttime), 'result': output})


def has_valid_query_params(request):
    if request.args.get('lon') is None or request.args.get('lat') is None or request.args.get('distance') is None:
        app.logger.info(request.args.get('Missing Querry Parameter'))
        return False

    lon = float(request.args.get('lon'))
    lat = float(request.args.get('lat'))

    if lon > 180 or lon < -180:
        app.logger.info(request.args.get('longitude not in range'))
        return False

    if lat > 180 or lat < -180:
        app.logger.info(request.args.get('latitude not in range'))
        return False

    return True


@app.route('/devicesAtPos', methods=['GET'])
@requires_auth
def get_devices_at_pos():
    starttime = getTime()
    mongo.db.devices.create_index([("loc", GEO2D)])
    if not has_valid_query_params(request):
        app.logger.info("Aborted Request due lack of query params")
        abort(400)
    longitude = float(request.args.get('lon'))
    latitude = float(request.args.get('lat'))
    distance = float(request.args.get('distance'))
    type = str(request.args.get('type'))
    app.logger.info("get_devices_at_pos - Get Devices at " + str(longitude) + ", " + str(latitude) + ", " + str(
        distance) + ", " + type)

    query = [{"loc": SON([("$near", [longitude, latitude]), ("$maxDistance", distance)])}]
    if request.args.get('type') is not None:
        query.append({"type": type})
    else:
        query.append({})

    devices = mongo.db.devices
    output = []

    for q in devices.find({"$and": query}):
        if len(q['locations']) == 0:
            continue
        output.append({
            'address': q['address'],
            'name': q['name'],
            'loc': q['loc'],
            'timestamp': q['timestamp'],
            'radius': q['radius'],
            'numL': q['numL'],
            'type': q['type'],
            'security': q['security'],
            'locations': q['locations']})
    endtime = getTime()
    app.logger.info(
        "get_devices_at_pos - took " + str(endtime - starttime) + " ms - deliver : " + str(len(output)) + " devices")
    return jsonify({'count': len(output), 'time': (endtime - starttime), 'result': output})

def has_valid_query_params_for_rect(request):
    return True


@app.route('/devicesAtRect', methods=['GET'])
@requires_auth
def get_devices_at_rect():
    starttime = getTime()
    mongo.db.devices.create_index([("loc", GEO2D)])
    if not has_valid_query_params_for_rect(request):
        app.logger.info("Aborted Request due lack of query params")
        abort(400)
    neLat = float(request.args.get('neLat'))
    neLon = float(request.args.get('neLon'))
    swLat = float(request.args.get('swLat'))
    swLon = float(request.args.get('swLon'))

    type = str(request.args.get('type'))
    app.logger.info("get_devices_at_pos - Get Devices at " + str(neLat) + ", " + str(neLon) + str(swLat) + ", " + str(
        swLon) + ", " + type)

    query = [{"loc": {"$within": {"$box": [[swLon, swLat], [neLon, neLat]]}}}]
    if request.args.get('type') is not None:
        query.append({"type": type})
    else:
        query.append({})

    devices = mongo.db.devices
    output = []

    for q in devices.find({"$and": query}):
        if len(q['locations']) == 0:
            continue
        output.append({
            'address': q['address'],
            'name': q['name'],
            'loc': q['loc'],
            'timestamp': q['timestamp'],
            'radius': q['radius'],
            'numL': q['numL'],
            'type': q['type'],
            'security': q['security'],
            'locations': q['locations']})
    endtime = getTime()
    app.logger.info(
        "get_devices_at_rect - took " + str(endtime - starttime) + " ms - deliver : " + str(len(output)) + " devices")
    return jsonify({'count': len(output), 'time': (endtime - starttime), 'result': output})


@app.route('/device', methods=['GET'])
@requires_auth
def get_one_device():
    starttime = getTime()
    app.logger.info("get_one_device")
    address = str(request.args.get('address'))

    devices = mongo.db.devices
    q = devices.find_one({'address': address})
    app.logger.info("DB said: " + str(q))

    if q:
        output = {
            'address': q['address'],
            'name': q['name'],
            'loc': q['loc'],
            'timestamp': q['timestamp'],
            'radius': q['radius'],
            'numL': q['numL'],
            'type': q['type'],
            'security': q['security'],
            'locations': q['locations']}
    else:
        abort(400)

    endtime = getTime()
    return jsonify({'count': 1, 'time': (endtime - starttime), 'result': output})


@app.route('/devices', methods=['POST'])
@requires_auth
def add_devices():
    devices = mongo.db.devices
    app.logger.info("POST REQUEST")
#    app.logger.info(str(request))
#    app.logger.info(json.dumps(request.json, indent=3))

    devices = request.json['devices']

    for device in devices:
        # app.logger.info(json.dumps(device, indent=3))

        result = mongo.db.devices.find_one({'address': device["address"]})
        # app.logger.info("DB Device: " + str(result))

        if result is None:
            # This one is new! insert it
            # app.logger.info("Device is new -> Add it: " + str(device))
            mongo.db.devices.insert(device)
        else:
            # This already exist - update locations
            # app.logger.info("Device is not new-> Merge it: " + str(device))
            locations_new_device = device["locations"]
            locations_d_b_device = result["locations"]

            for locationNewDevice in locations_new_device:
                if location_addable(locationNewDevice, locations_d_b_device):
                    mongo.db.devices.update({"_id": result["_id"]},
                                            {"$addToSet": {"locations": locationNewDevice}})
#        app.logger.info("Post Process Device: " + str(device))
        post_process_device(device)

    return jsonify({'result': "Thx"})


def post_process_device(device):
    device = mongo.db.devices.find_one({'address': device["address"]})
    lo = 0
    la = 0
    i = 0
    for location in device["locations"]:
        i += 1
        lo += location["loc"][0]
        la += location["loc"][1]
    if i == 0:
        i = 1
    lo /= i
    la /= i
    loc = [lo, la]
    mongo.db.devices.update({"_id": device["_id"]},
                            {"$set": {"loc": loc}})
    mongo.db.devices.update({"_id": device["_id"]},
                            {"$set": {"numL": i}})

    maxDistance = 10;
    for location in device["locations"]:
        dist = haversineDistance(lo, la, location["loc"][0], location["loc"][1])
        if dist > maxDistance:
            maxDistance = dist
    mongo.db.devices.update({"_id": device["_id"]},
                            {"$set": {"radius": maxDistance}})

    pass


def location_addable(locationCandidate, locations):
    for location in locations:
        distance = calcDistance(locationCandidate, location)
        # app.logger.info("Distance was "+str(distance))
        if distance < 2:
            return False
    return True


def calcDistance(location1, location2):
    return haversineDistance(location1["loc"][0], location1["loc"][1], location2["loc"][0], location2["loc"][1])


def haversineDistance(lon1, lat1, lon2, lat2):
    lon1, lat1, lon2, lat2 = map(radians, [lon1, lat1, lon2, lat2])
    dlon = lon2 - lon1
    dlat = lat2 - lat1
    a = sin(dlat / 2) ** 2 + cos(lat1) * cos(lat2) * sin(dlon / 2) ** 2
    c = 2 * asin(sqrt(a))
    m = 6367 * c * 1000
    return m


def getTime():
    return int(round(time.time() * 1000))

@app.route('/status', methods=['GET'])
@requires_auth
def get_status():
    starttime = getTime()
    counterLocations = 0
    devices = mongo.db.devices
    db_devices = devices.find()
    counterDevices = db_devices.count()
    for q in db_devices:
        counterLocations += len(q['locations'])

    end_time = getTime()
    app.logger.info("get_status - took " + str(end_time - starttime) )
    return jsonify({
        'devices': counterDevices,
        'locations': counterLocations,
        'time': (end_time - starttime)})


@app.route('/alive', methods=['GET'])
def alive():
    return jsonify({'result': "my heart goes boom, la di da da, la la la la la la"})


@app.route('/doStuff', methods=['GET'])
def doStuff():
    starttime = getTime()
    devices = mongo.db.devices
    dbDevices = devices.find()
    for device in dbDevices:
        post_process_device(device)

    endtime = getTime()
    return jsonify({'time': (endtime - starttime), 'result': "Done"})


@app.route('/location', methods=['POST'])
@requires_auth
def get_locations():
    # app.logger.info(str(request))
    # app.logger.info(json.dumps(request.json, indent=3))
    starttime = getTime()

    devices = request.json['devices']
    db_devices = []

    num_devices = 0

    for device in devices:

        #        app.logger.info("Find: " + str(device["address"]))
        result = mongo.db.devices.find_one({'address': device["address"]})
        #        app.logger.info("DB Device: " + str(result))

        if result is None:
            # This one is new! Forget it
            pass
        else:
            # This already exist
            #            app.logger.info("Found matching Device in DB " + str(device))
            db_devices.append(result)
            num_devices += 1

    if num_devices == 0:
        # cancel
        abort(400)
    else:
        lo = 0
        la = 0
        for device in db_devices:
            lo += device["loc"][0]
            la += device["loc"][1]
        lo /= num_devices
        la /= num_devices
        loc = [lo, la]

        endtime = getTime()
        return jsonify({'time': (endtime - starttime), 'result': loc})


if __name__ == '__main__':
    # handler = RotatingFileHandler('api.log', maxBytes=10000, backupCount=1)
    # handler.setLevel(logging.INFO)
    # app.logger.addHandler(handler)

    '''
    releasing port
    sudo pkill -9 python
    '''
    app.run(debug=True, host='0.0.0.0', port=22222)
