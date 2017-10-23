package com.example

object HttpVerticle {

  val HTTP_HOST = "http_host"
  val HTTP_PORT = "http_port"

  val DEFAULT_HTTP_HOST = "localhost"
  val DEFAULT_HTTP_PORT = 8080
}

class HttpVerticle extends ScalaVerticle {

  var server: HttpServer = _

  /**
    * Override ScalaVerticle method. This Method starting https server with vertx.io
    */
  override final def start(): Unit = {
    val router = getVertxRouter
    val (port: Integer, host: String) = configHostAndPort
    val promise: Promise[Unit] = createHttpServer(router, port, host)
    promise.future
  }

  /**
    * Override ScalaVerticle method. This method stoping https server
    */
  override final def stop(): Unit = {
    for {
      _ <- server.closeFuture()
      _ <- vertx.undeployFuture(this.deploymentID)
    } yield ()
  }

  private def createHttpServer(router: Router, port: Integer, host: String) = {
    val promise = Promise[Unit]()
    vertx
      .createHttpServer()
      .requestHandler(router.accept)
      .listenFuture(port, host)
      .onComplete({
        case Success(startedServer) =>
          println(s"Server successfully started on port: $port")
          this.server = startedServer
          promise.success(())
        case Failure(ex) =>
          println(s"Server failed to start on port: $port, b/c ${ex.getCause}")
          promise.failure(ex)
      })
    promise
  }

  private def configHostAndPort = {
    val port = config.getInteger(HttpVerticle.HTTP_PORT, HttpVerticle.DEFAULT_HTTP_PORT)
    val host = config.getString(HttpVerticle.HTTP_HOST, HttpVerticle.DEFAULT_HTTP_HOST)
    (port, host)
  }

  private def getVertxRouter = {
    val router = Router.router(vertx)
    router.route("/aa").handler(routeRoot)
    router
  }

  private def routeRoot(ctx: RoutingContext) = {
    val currentThread = "Current thread " + Thread.currentThread().getId
    ctx.response().end(currentThread)
  }
}