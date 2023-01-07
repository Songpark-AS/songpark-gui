module.exports = {
    platform: {
        port: 443,
        //host: "http://127.0.0.1",
        host:"https://platform.songpark.com",
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
    upgrade_timeout: 600000,
    version: "DEV",
    sha: "DEV"
}
