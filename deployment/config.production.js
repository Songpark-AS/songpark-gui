module.exports = {
    platform: {
        port: 443,
        host: "https://spp.inonit.no",
        api_base: "/api",
    },
    mqtt: {
        port: 8000,
        host: "spmqtt.inonit.no",
        client_id_prefix: "app-",
        username: "songpark",
        password: "fNhWktaTlfDGlH4mbmaW6esOpgExs8wKIOBapDcq",
        useSSL: true,
    },
    heartbeat: {
        timer: 61000
    },
    version: "VAR__VERSION",
    sha: "VAR__SHA"
}
