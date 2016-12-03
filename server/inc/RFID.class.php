<?php

/**
*Class that include methods that are called when the someone make request agains the APIRest
*
**/
class RFID {

    private $slim;

    private $memcached;
    private $config;
    private $db;

    private $params;

    private $user;

    const SESSION_LENGTH = 32;

    /**
    * RFID constructor
    * @param $slim recieve an instance from the framework
    **/
    public function __construct($slim) {

        // Decode JSON object from the DB config 
        $this->config = json_decode(file_get_contents(__DIR__ . '/config.json')); 

        //Obtain an instace from the DB
        $this->db = db::getInstance($this->config->mysql_host, $this->config->mysql_name, $this->config->mysql_user, $this->config->mysql_pass); 

        $this->memcached = new Memcached($this->config->memcached_key);
        $this->memcached->setOption(\Memcached::OPT_BINARY_PROTOCOL, true);
        $this->memcached->addServer('localhost', 11211);

        $this->slim = $slim;

        //package the parameter of the HTTP request in an array
        $this->params = json_decode($slim->request->getBody(), true); 
        $this->user = null;
    }

    public function createSession() {
        $this->checkParams(array('user', 'pwd'));

        if ($user = $this->db->select("users", null, "`user` = '{$this->params['user']}'")) {
	    $user = $user[0];
            if (password_verify($this->params['pwd'], $user['pwd'])) {
                $session = bin2hex(mcrypt_create_iv(static::SESSION_LENGTH / 2, MCRYPT_DEV_URANDOM));

                if ($oldSession = $this->memcached->get($user['user'])) {
                    $this->memcached->delete($user['user']);
                    $this->memcached->delete($oldSession);
                }

                $this->memcached->set($session, $user['id'], $this->config->memcached_expiration);
                $this->memcached->set($user['user'], $session, $this->config->memcached_expiration);

                $this->slim->response->setStatus(201);
                $this->slim->response->setBody($this->jsonEncode(array('session' => $session)));
            } else {
                $this->slim->halt(403, "Password mismatch for {$this->params['user']}.");
            }
        } else {
            $this->slim->halt(403, "Unknown user {$this->params['user']}.");
        }
    }

    public function getSession() {
        $this->slim->response->setBody(static::jsonEncode($this->user));
    }


    /**
    * Funtion to create an user   
    **/
    public function createUser() {
          //Verify that at least we recieve the parameters 'user' 'pwd' 'name' and 'surname'
	   $this->checkParams(array('user', 'pwd', 'name', 'surname'));
	   $this->params['pwd'] = password_hash($this->params['pwd'], PASSWORD_DEFAULT);
	   $this->db->insert("users", $this->params);
    }

    /**
    * Funtion to create an Update an user 
    * @param $id id from the user that we want to apply this function  
    **/
    public function updateUser($id) {
	   $this->db->update("users", $this->params, "`id` = '{$id}'");
    }

    /**
    * Funtion to delete an User
    * @param $id id from the user that we want to delete
    **/
    public function deleteUser($id) {
        $this->db->delete("users", "`id` = '{$id}'");
    }

    /**
    * Funtion to create a Business
    **/
    public function createBusiness() {
       //Verify that at least we recieve the necessary parameters 
        $this->checkParams(array('name', 'lat', 'lon', 'radio'));
        $this->db->insert("business", $this->params);
    }

    /**
    * Funtion to update a Business
    * @param $id id from the business that we want to update
    **/
    public function updateBusiness($id) {
        //Verify that at least we recieve the necessary parameters 
        $this->db->update("business", $this->params, "`id` = '{$id}'");
    }

    /**
    * Funtion to delete a Business
    * @param $id id from the business that we want to delete
    **/
    public function deleteBusiness($id) {
        $this->db->delete("business", "`id` = '{$id}'");
    }

    /**
    * Function to associate an user to a bussines
    * @param $id id from the business
    **/
    public function addUserToBusiness($id) {
	$this->params['bid'] = $id;
        $this->checkParams(array('uid'));
        $this->db->insert("user_business", $this->params);
    }

    /**
    * Function to delete an user from a business
    * @param $id business id
    * @param $uid user id
    **/
    public function deleteUserFromBusiness($id, $uid) {
        $this->db->delete("user_business", "`bid` = '{$id}' AND `uid` = '{$uid}'");
    }

    /**
    * Funtion that check if the geoposition of the mobile asociated to the "card id" is in allow range.
    * @param $bid business id
    * @param $cid card id
    **/
    public function checkGPSLock($bid, $cid) {
	$udata = $this->db->select("users", array("id", "lat", "lon"), "`card` = '{$cid}'");
	if(count($udata)) {
		$udata = $udata[0];
		$id = $udata['id'];
		if($this->db->select("user_business", array("bid", "uid"), "`bid` = '{$bid}' AND `uid` = '{$id}'")) {
			$bdata = $this->db->select("business", array("id", "lat", "lon", "radio"), "`id` = '{$bid}'")[0];
			if($this->distance($udata['lat'], $udata['lon'], $bdata['lat'], $bdata['lon']) < $bdata['radio']) {
	                        $this->slim->halt(200, "");
			}
		}
	}
	$this->slim->halt(403, "");
    }

    /**
    * Function to check if this id card is associated to that bussiness
    * @param $bid business id
    * @param $cid card id
    **/
    public function checkUserLock($bid, $cid) {
        $data = $this->db->select("users", array("id"), "`card` = '{$cid}'");
	if(count($data)) {
		$data = $data[0]['id'];
		if($this->db->select("user_business", array("bid", "uid"), "`bid` = '{$bid}' AND `uid` = '{$data}'")) {
			$this->slim->halt(200, "");
		}
	}
	$this->slim->halt(403, "");
    }

    /**
    * Function that calculate the distance between two positions given in metters
    * @param $lat1 latitude of the one position
    * @param $lon1 longitude of the one position
    * @param $lat2 latitude of the two position
    * @param $lon2 longitude of the two position
    **/
    private function distance($lat1, $lon1, $lat2, $lon2) {
	$theta = $lon1 - $lon2;
	$dist = sin(deg2rad($lat1)) * sin(deg2rad($lat2)) +  cos(deg2rad($lat1)) * cos(deg2rad($lat2)) * cos(deg2rad($theta));
	$dist = acos($dist);
	$dist = rad2deg($dist);
	$miles = $dist * 60 * 1.1515;

	return ($miles * 1609.344);
    }

    /**
    * Funtion that verify the params of the methods of the requests
    * @param $params array of parameters that want to check
    **/
    private function checkParams($params) {
        if (!is_array($this->params)) {
            $this->slim->halt(400, "This method requires parameters.");
        }

        foreach ($params as $required) {
            if (!isset($this->params[$required])) {
                $this->slim->halt(400, "Missing required parameter \"{$required}\"");
            }
        }
    }

    private static function jsonEncode($object) {
        return json_encode($object, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES | JSON_UNESCAPED_UNICODE);
    }

    public function authenticate() {
        if (isset($_SERVER['PHP_AUTH_USER']) && ($user = $this->memcached->get($_SERVER['PHP_AUTH_USER']))) {
            if ($user = $this->db->select("users", null, "`id` = '{$user}'")) {
                $this->user = $user;
            } else {
                $this->slim->halt(500, "An active session was found, but had no user associated to it.");
            }
        } else {
            $this->slim->halt(401, "This method requires an active session.");
        }
    }

}

?>
