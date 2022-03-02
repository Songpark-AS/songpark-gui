module.exports = {
    platform: {
        port: 3000,
        host: "http://127.0.0.1",
        api_base: "/api",
    },
    mqtt: {
        port: 8000,
        host: "127.0.0.1",
        username: "songpark",
        password: "testmctestson",
    },
    heartbeat: {
        timer: 61000
    },
    upgrade_timeout: 600000,
    version: "DEV",
    sha: "DEV"
}
