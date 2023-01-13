module.exports = {
    platform: {
        port: 443,
        host: "https://songpark.dev.glace.com",
        api_base: "/api",
    },
    mqtt: {
        port: 8000,
        host: "mqtt.songpark.com",
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
