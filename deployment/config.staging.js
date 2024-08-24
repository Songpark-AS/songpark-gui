module.exports = {
    platform: {
        port: 443,
        host: "https://platform.songpark.com",
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
        timer: 91000
    },
    version: "VAR__VERSION",
    sha: "VAR__SHA"
}
