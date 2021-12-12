(function () {
    function catchNull(func) {
        try {
            return func()
        } catch (e) {
            return null
        }
    }
    let ws = null
    let element = catchNull(() => document.getElementsByTagName('video')[0])
    function wsClose() {
        ws = null
    }
    function loop() {
        try {
            if (element === undefined || element === null) {
                element = catchNull(() => document.getElementsByTagName('video')[0])
            }
            if (ws === null) {
                ws = new WebSocket('ws://localhost:1234/api/ws/media')
                ws.onclose = wsClose
            } else {
                let session = navigator.mediaSession
                let meta = 'metadata' in session ? session.metadata : null
                let duration = element.duration
                if (duration === null) {
                    duration = 0
                }
                if (session.playbackState !== 'none' && meta !== null) {
                    ws.send(JSON.stringify({
                        'state': element.paused ? 'PAUSED' : 'PLAYING',
                        'album': meta.album,
                        'title': meta.title,
                        'artist': meta.artist,
                        'artwork': meta.artwork,
                        'duration': duration,
                        'currentTime': element.currentTime
                    }))
                } else {
                    ws.send(JSON.stringify({
                        'state': 'NONE'
                    }))
                }
            }
        } catch (e) {
        }
        setTimeout(loop, 500)
    }
    loop()
})()

console.log('Redstoneclient Integration started.');
