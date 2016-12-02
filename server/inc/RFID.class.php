<?php

class RFID {
    private $slim;

    private $config;
    private $db;

    private $params;

    private $user;

    public function __construct($slim) {
        $this->config = json_decode(file_get_contents(__DIR__ . '/config.json'));

        $this->db = db::getInstance($this->config->mysql_host, $this->config->mysql_name, $this->config->mysql_user, $this->config->mysql_pass);

        $this->slim = $slim;
        $this->params = json_decode($slim->request->getBody(), true);
        $this->user = null;
    }


    public function createUser() {
	$this->checkParams(array('user', 'pwd', 'name', 'surname'));
	$this->db->insert("users", $this->params);
    }

    public function updateUser($id) {
	$this->db->update("users", $this->params, "`id` = '{$id}'");
    }

    public function deleteUser($id) {
        $this->db->delete("users", "`id` = '{$id}'");
    }

    public function createBusiness() {
        $this->checkParams(array('name', 'lat', 'lon', 'radio'));
        $this->db->insert("business", $this->params);
    }

    public function updateBusiness($id) {
        $this->db->update("business", $this->params, "`id` = '{$id}'");
    }

    public function deleteBusiness($id) {
        $this->db->delete("business", "`id` = '{$id}'");
    }

    public function addUserToBusiness($id) {
	$this->params['bid'] = $id;
        $this->checkParams(array('uid'));
        $this->db->insert("user_business", $this->params);
    }

    public function deleteUserFromBusiness($id, $uid) {
        $this->db->delete("user_business", "`bid` = '{$id}' AND `uid` = '{$uid}'");
    }

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

    private function distance($lat1, $lon1, $lat2, $lon2) {
	$theta = $lon1 - $lon2;
	$dist = sin(deg2rad($lat1)) * sin(deg2rad($lat2)) +  cos(deg2rad($lat1)) * cos(deg2rad($lat2)) * cos(deg2rad($theta));
	$dist = acos($dist);
	$dist = rad2deg($dist);
	$miles = $dist * 60 * 1.1515;

	return ($miles * 1609.344);
    }

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

}

?>
