<?php

class DefaultContentTypeMiddleware extends \Slim\Middleware {
    public function call() {
        $this->app->response->headers->set('Content-Type', 'application/json');

        $this->next->call();
    }
}

?>
