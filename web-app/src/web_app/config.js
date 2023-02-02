module.exports = {
    platform: {
        port: 3000,
        host: "http://localhost",
        api_base: "/api",
    },
    mqtt: {
        port: 8000,
        host: "10.100.200.8",
        userName: "songpark",
        password: "fNhWktaTlfDGlH4mbmaW6esOpgExs8wKIOBapDcq",
        useSSL: true,
        reconnect: true,
    },
    heartbeat: {
        timer: 61000
    },
    version: "DEV",
    sha: "DEV"
}
