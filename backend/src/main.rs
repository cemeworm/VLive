use std::convert::Infallible;
use std::net::SocketAddr;

use hyper::service::{make_service_fn, service_fn};
use hyper::{Body, Request, Response, Server, StatusCode};

use backend::basic::VLiveErr;
use backend::model;

type EntryResult<T> = Result<T, Infallible>;

async fn entry(req: Request<Body>) -> EntryResult<Response<Body>> {
    println!("Receive request from {}", req.uri());

    let mut response = Response::new(Body::empty());

    let (parts, body) = req.into_parts();
    let data = match hyper::body::to_bytes(body).await {
        Ok(data) => data.to_vec(),
        Err(_) => {
            *response.status_mut() = StatusCode::BAD_REQUEST;
            return Ok(response);
        }
    };

    let path = parts.uri.path();

    *response.body_mut() = Body::from(
        match path {
            "/user/reg" => model::register(data),
            "/channel/join" => model::join_channel(data),
            "/channel/leave" => model::leave_channel(data),
            "/channel/list" => model::list_channel(data),
            _ => Err(VLiveErr::not_found(path)),
        }
        .map_or_else(
            |e| serde_json::to_vec(&e).unwrap(),
            |r| serde_json::to_vec(&r).unwrap(),
        ),
    );

    println!(
        "Process request for {}, status = {}",
        path,
        response.status()
    );
    Ok(response)
}

async fn shutdown_signal() {
    tokio::signal::ctrl_c()
        .await
        .expect("fail to install CTRL+C signal handler")
}

#[tokio::main]
async fn main() {
    let addr = SocketAddr::from(([127, 0, 0, 1], 12346));

    let make_svc = make_service_fn(|_conn| async { Ok::<_, Infallible>(service_fn(entry)) });

    let server = Server::bind(&addr).serve(make_svc);

    let graceful = server.with_graceful_shutdown(shutdown_signal());

    if let Err(e) = graceful.await {
        eprintln!("server error: {}", e);
    }
}