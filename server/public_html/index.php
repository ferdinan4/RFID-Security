
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
*This is a POST request, for creating new users, and call 'createUser'  of the RFID class
**/
$app->post('/users/', array($rfid, 'authenticate'), array($rfid, 'createUser'));

/**
*This is a PUT request, which recieve an "id" from the user that we want to update, calling the method 'updateUser' from the RFID class
**/
$app->put('/users/:id/', array($rfid, 'authenticate'), array($rfid, 'updateUser'));

/**
*This is a DELETE request, which recieve an "id" from the user that we want to delete, calling the method 'deleteUser' from the RFID class
**/
$app->delete('/users/:id/', array($rfid, 'authenticate'), array($rfid, 'deleteUser'));

/**
*This is a POST request, for creating new Business, and call 'createBusiness'  of the RFID class
**/
$app->post('/business/', array($rfid, 'authenticate'), array($rfid, 'createBusiness'));

/**
*This is a PUT request, which recieve an "id" from the business that we want to update, calling the method 'updateBusiness' from the RFID class
**/
$app->put('/business/:id/', array($rfid, 'authenticate'), array($rfid, 'updateBusiness'));

/**
*This is a DELETE request, which recieve an "id" from the business that we want to delete, calling the method 'deleteBusiness' from the RFID class
**/
$app->delete('/business/:id/', array($rfid, 'authenticate'), array($rfid, 'deleteBusiness'));

/**
*This is a GET request, which recieve an "id" from the card and from the business asociated, calling the method 'checkGPSLock' from the RFID class
*This 'checkGPSLock' check if the geoposition of the mobile asociated to the "card id" is allow range.
**/
$app->get('/business/:bid/check/gps/:cid/', array($rfid, 'authenticate'), array($rfid, 'checkGPSLock'));

/**
*This is a GET request, which recieve an "id" from the card that we want to delete, calling the method 'deleteUser' from the RFID class
*This 'checkUserLock' check if this id card is associated to that bussiness
**/
$app->get('/business/:bid/check/:cid/', array($rfid, 'authenticate'), array($rfid, 'checkUserLock'));

/**
*This is a POST request, add an user to the business
**/
$app->post('/business/:id/employees', array($rfid, 'authenticate'), array($rfid, 'addUserToBusiness'));

/**
*This is a DELETE request, delete users associated to a business
**/
$app->delete('/business/:id/employees/:uid', array($rfid, 'authenticate'), array($rfid, 'deleteUserFromBusiness'));

/**
*Execute the APIRest
**/
$app->run();

?>
