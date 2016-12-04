<?php

class db {
	private static $instance = null;
	private $db_conection;
	private $error;

	/**
	 * Establishes a connection to a Database.
	 * @param $host     Host where the Database is allocate.
	 * @param $username Username to connect whith the Database.
	 * @param $password Username password.
	 * @param $db_name  Database name.
	 */
	private function __construct($host, $username, $password, $db_name) {
		$this->db_conection = mysqli_connect($host, $username, $password, $db_name);
		$this->error = "";
	}

	/**
	 * Singleton method
	 * @param  $host     Host where the Database is allocate.
	 * @param  $username Username to connect whith the Database.
	 * @param  $password Username password.
	 * @param  $db_name  Database name.
	 * @return A new instance of db if it did not exists, the existing one otherwise.
	 */
	public static function getInstance($host, $username, $password, $db_name) {
		if (self::$instance == null) {
			self::$instance = new db($host, $username, $password, $db_name);
		}

		return self::$instance;
	}

	/**
	 * Obtain data from the Database.
	 * @param  $select [Optional] The fields to select.
	 * @param  $table  Table name.
	 * @param  $where  [Optional] Constraint condition.
	 * @return The obtained data / Error description.
	 */
	public function select($table = null, $select = null, $where = null) {
		if (($check = $this->check()) == null) {
			if ($table != null) {
				if (is_array($select)) {
					$select_string = '';

					foreach ($select as $value) {
						$select_string .= ", `$value`";
					}

					$select_string = substr($select_string, 2);

					$query = sprintf('SELECT %s FROM `%s`', $select_string, $table);
				} else {
					$query = sprintf('SELECT * FROM `%s`', $table);
				}

				if ($where != null) {
					$query = sprintf('%s WHERE %s', $query, $where);
				}

				if ($query = mysqli_query($this->db_conection, $query)) {

					$return = array();
					while ($data = mysqli_fetch_assoc($query)) {
						$return[] = $data;
					}

					return $return;
				} else {
					$this->error = mysqli_error($this->db_conection);
					return false;
				}
			} else {
				$this->error = mysqli_error($this->db_conection);
				return false;
			}
		} else {
			$this->error = $check;
			return false;
		}
	}

	/**
	 * Removes the selected data from Database.
	 * @param  $table Table name.
	 * @param  $where Constraint condition.
	 * @return Data successfully deleted / Error description.
	 */
	public function delete($table = null, $where = null) {
		$check = $this->check();
		if ($check == null) {
			if ($table != null && $where != null) {
				if (mysqli_query($this->db_conection, sprintf('DELETE FROM `%s` WHERE %s', $table, $where)) === true) {
					return true;
				} else {
					$this->error = mysqli_error($this->db_conection);
					return false;
				}
			} else {
				$this->error = mysqli_error($this->db_conection);
				return false;
			}
		} else {
			$this->error = $check;
			return false;
		}
	}

	/**
	 * Insert the selected data to Database.
	 * @param   $table  Table name.
	 * @param  	$insert Name of the fields to insert.
	 * @param  	$values Data to insert.
	 * @return  Data successfully inserted / Error description.
	 */
	public function insert($table = null, $values = null) {
		if (($check = $this->check()) == null) {
			if ($table != null && is_array($values)) {
				$inserts = array_keys($values);

				$insert_string = '';
				$values_string = '';

				foreach ($inserts as $insert) {
					$insert_string .= ", `{$insert}`";
				}

				foreach ($values as $value) {
					$values_string .= ", '{$value}'";
				}

				$insert_string = substr($insert_string, 2);
				$values_string = substr($values_string, 2);

				$query = sprintf('INSERT INTO `%s` (%s) VALUES (%s)', $table, $insert_string, $values_string);

				if (mysqli_query($this->db_conection, $query) === true) {
					return true;
				} else {
					$this->error = mysqli_error($this->db_conection);
					return false;
				}
			} else {
				$this->error = mysqli_error($this->db_conection);
				return false;
			}
		} else {
			$this->error = $check;
			return false;
		}
	}

	/**
	 * Update data from Database.
	 * @param  	$table  Table name.
	 * @param  	$field  Name of the fields to update.
	 * @param  	$values Data to update.
	 * @param  	$where  Constraint condition.
	 * @return 	[Data successfully updated / Error description.
	 */
	public function update($table = null, $values = null, $where = null) {
		$check = $this->check();
		if ($check == null) {
			if ($table != null && is_array($values) && $where != null) {
				$update = "";
				foreach ($values as $key => $value) {
					$update .= ", `$key` = '$value'";
				}
				$update = substr($update, 2);

				if (mysqli_query($this->db_conection, sprintf('UPDATE `%s` SET %s WHERE %s', $table, $update, $where)) === TRUE) {
					return true;
				} else {
					$this->error = mysqli_error($this->db_conection);
					return false;
				}
			} else {
				$this->error = mysqli_error($this->db_conection);
				return false;
			}
		} else {
			$this->error = $check;
			return false;
		}
	}

	/**
	 * Execute a query into Database.
	 * @param   $query 	[Query to execute.
	 * @return 	[Query executed successfully / Error description.
	 */
	public function query($query = null) {
		$check = $this->check();
		if ($check == null) {
			if ($query != null) {
				if (mysqli_query($this->db_conection, $query) != false) {
					return true;
				} else {
					$this->error = mysqli_error($this->db_conection);
					return false;
				}
			} else {
				$this->error = mysqli_error($this->db_conection);
				return false;
			}
		} else {
			$this->error = $check;
			return false;
		}
	}

	/**
	 * Check if the Database connection was successful.
	 * @return Connection successful / Error description.
	 */
	public function check() {
		if (mysqli_connect_errno($this->db_conection)) {
			return("Cannot connect to ther server. Check host, username and password and try again.");
		} else {
			return null;
		}
	}

	/**
	 * __destruct connection to the Database.
	 * @return True if the connection finished successful, false in other case.
	 */
	public function __destruct() {
		return mysqli_close($this->db_conection);
	}

	/**
	 * mktimestamp get the time of the connection when is made.
	 * @return timestamp in format UNIX for given arguments Y:m:d H:i:s .
	 */

	public static function mktimestamp($timestamp = null) {
		return date('YmdHis', ($timestamp == null ? time() : $timestamp));
	}

	/**
	 * The mysqli_insert_id() function returns the ID generated by a query on a table with a
	 * column having the AUTO_INCREMENT attribute. If the last query wasn't an INSERT or UPDATE
	 * statement or if the modified table does not have a column with the AUTO_INCREMENT attribute,
	 * this function will return zero.
	 * @return String with the ID generated by a query on a table with a column having the AUTO_INCREMENT attribute.
	 */
	public function getLastID(){
		return (string) mysql_insert_id($this->db_conection);
	}

	public function getError(){
		return $this->error;
	}
}

?>
