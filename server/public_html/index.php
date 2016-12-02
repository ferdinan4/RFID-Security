<?php
require __DIR__ . '/../inc/vendor/autoload.php';
require __DIR__ . '/../inc/api_db.php';
require __DIR__ . '/../inc/RFID.class.php';
require __DIR__ . '/../inc/DefaultContentTypeMiddleware.class.php';

$app = new \Slim\Slim();
$app->add(new DefaultContentTypeMiddleware());

$rfid = new RFID($app);

$app->get('/', function() {
    echo "Hi there! I am using RFID-Security.";
});

$app->post('/users/', array($rfid, 'createUser'));
$app->put('/users/:id/', array($rfid, 'updateUser'));
$app->delete('/users/:id/', array($rfid, 'deleteUser'));

$app->post('/business/', array($rfid, 'createBusiness'));
$app->put('/business/:id/', array($rfid, 'updateBusiness'));
$app->delete('/business/:id/', array($rfid, 'deleteBusiness'));

$app->get('/business/:bid/check/gps/:cid/', array($rfid, 'checkGPSLock'));
$app->get('/business/:bid/check/:cid/', array($rfid, 'checkUserLock'));

$app->post('/business/:id/employees', array($rfid, 'addUserToBusiness'));
$app->delete('/business/:id/employees/:uid', array($rfid, 'deleteUserFromBusiness'));

$app->run();

?>
