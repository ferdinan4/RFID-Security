
<?php

require __DIR__ . '/../inc/vendor/autoload.php';
require __DIR__ . '/../inc/api_db.php';
require __DIR__ . '/../inc/RFID.class.php';
require __DIR__ . '/../inc/DefaultContentTypeMiddleware.class.php';

/**
*we create a instance of framework for developing the Web's  APIs
**/
$app = new \Slim\Slim();
$app->add(new DefaultContentTypeMiddleware());

/**
*This object is which contents the methods that will call HTTP request
**/
$rfid = new RFID($app);

/**
*Return default message of the API
**/
$app->get('/', function() {
    echo "Hi there! I am using RFID-Security.";
});

/**
*here we defined all request of the API
**/

$app->post('/session/', array($rfid, 'createSession'));
$app->get('/session/', array($rfid, 'authenticate'), array($rfid, 'getSession'));

/**
*This is a PUT request, which recieve an "id" from the user that we want to update, calling the method 'updateUser' from the RFID class
**/
$app->put('/users/', array($rfid, 'authenticate'), array($rfid, 'updateUser'));

/**
*This is a DELETE request, which recieve an "id" from the user that we want to delete, calling the method 'deleteUser' from the RFID class
**/
$app->delete('/users/', array($rfid, 'authenticate'), array($rfid, 'deleteUser'));

/**
*This is a GET request, which recieve an "id" from the card and from the business asociated, calling the method 'checkGPSLock' from the RFID class
*This 'checkGPSLock' check if the geoposition of the mobile asociated to the "card id" is allow range.
**/
$app->get('/business/check/gps/:cid/', array($rfid, 'authenticate'), array($rfid, 'checkGPSLock'));

/**
*This is a GET request, which recieve an "id" from the card that we want to delete, calling the method 'deleteUser' from the RFID class
*This 'checkUserLock' check if this id card is associated to that bussiness
**/
$app->get('/business/check/:cid/', array($rfid, 'authenticate'), array($rfid, 'checkUserLock'));

/**
*Execute the APIRest
**/
$app->run();

?>
