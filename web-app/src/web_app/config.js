module.exports = {
    platform: {
        port: 3000,
        host: "http://127.0.0.1",
        api_base: "/api",
    },
    mqtt: {
        port: 8000,
        host: "127.0.0.1",
        client_id_prefix: "app-",
        username: "songpark",
        password: "testmctestson",
    },
    heartbeat: {
        timer: 8000
    },
    version: "DEV"
}
