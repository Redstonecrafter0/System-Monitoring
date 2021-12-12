class Media extends HTMLElement {

    static instance;

    constructor() {
        super();
        Media.instance = this
        this._shadowRoot = this.attachShadow({mode: 'open'})
        this._shadowRoot.innerHTML = `
<style>
* {
    color: white;
    font-family: "JetBrains Mono", sans-serif;
}
img {
    z-index: 1;
}
#container {
    width: 500px;
    background: #121212;
    height: 85px;
    background-repeat: no-repeat;
    background-size: cover;
    background-position: center center;
}
#main {
    display: flex;
    overflow: hidden;
    backdrop-filter: blur(8px);
    width: 100%;
    background: linear-gradient(to bottom, transparent, #121212DD);
}
#content {
    display: flex;
    flex-direction: column;
    justify-content: space-evenly;
}
#content span {
    display: inline-block;
    white-space: nowrap;
    margin: 0 5px;
    font-size: .8rem;
}
#progress {
    position: relative;
    height: 3px;
    width: 100%;
    background: white;
}
#progressbar {
    background: red;
    position: relative;
    height: 3px;
    width: 100%;
}
.scroll {
    animation: scroll 25s linear infinite;
}
@keyframes scroll {
    0% {
        transform: none;
    }
    30% {
        transform: none;
    }
    64% {
        transform: translateX(-100%);
        color: white;
    }
    65% {
        transform: translateX(-100%);
        color: transparent;
    }
    66% {
        transform: translateX(100%);
        color: transparent;
    }
    67% {
        transform: translateX(100%);
        color: white;
    }
    100% {
        transform: none;
    }
}
.paused {
    animation: pause 5s infinite;
}
@keyframes pause {
    0% {
        background: red;
    }
    50% {
        background: transparent;
    }
    100% {
        background: red;
    }
}
</style>

<div id="container">
    <div id="main">
        <img id="thumbnail" height="82px" src="" alt="">
        <div id="content">
            <span class="scroll" id="title"></span>
            <span class="scroll" id="album"><span style="margin: 0; color: inherit">from </span><span id="albumtext" style="color: inherit"></span></span>
            <span class="scroll" id="artist"></span>
            <span id="progresstext" style="font-family: 'Courier New', 'Segoe UI', sans-serif"></span>
            <svg id="pauseicon" xmlns="http://www.w3.org/2000/svg" x="0px" y="0px" width="50px" height="50px" viewBox="0 0 519.479 519.479" xml:space="preserve" fill="#ffffff44" style="position: fixed; left: 430px; filter: blur(1px); transition: 200ms; opacity: 0">
                <path d="M193.441,0h-75.484c-16.897,0-30.6,13.703-30.6,30.6v458.277c0,16.898,13.703,30.602,30.6,30.602h75.484c16.897,0,30.6-13.703,30.6-30.602V30.6C224.042,13.703,210.339,0,193.441,0z"/>
                <path d="M401.521,0h-75.484c-16.896,0-30.6,13.703-30.6,30.6v458.277c0,16.898,13.703,30.602,30.6,30.602h75.484c16.896,0,30.6-13.703,30.6-30.602V30.6C432.121,13.703,418.418,0,401.521,0z"/>
            </svg>
        </div>
    </div>
    <div id="progress"><div id="progressbar" style="transition: width 500ms linear"></div></div>
</div>
`
        this.container = this._shadowRoot.getElementById('container')
        this.thumbnail = this._shadowRoot.getElementById('thumbnail')
        this.titleText = this._shadowRoot.getElementById('title')
        this.albumText = this._shadowRoot.getElementById('albumtext')
        this.album = this._shadowRoot.getElementById('album')
        this.artist = this._shadowRoot.getElementById('artist')
        this.progressText = this._shadowRoot.getElementById('progresstext')
        this.progressBar = this._shadowRoot.getElementById('progressbar')
        this.pauseicon = this._shadowRoot.getElementById('pauseicon')
    }

    update(data) {
        this.container.style.display = data.on ? '' : 'none'
        this.setPlaying(data.playing)
        this.setThumbnail(data.img)
        this.setTitle(data.title)
        this.setAlbum(data.album)
        this.setArtist(data.artist)
        this.setTime(data.currentTime, data.duration)
        this.setProgress(data.progress)
    }

    setPlaying(playing) {
        this.progressBar.className = playing ? '' : 'paused'
        this.pauseicon.style.opacity = playing ? '0' : '1'
    }

    setTitle(title) {
        if (this.titleText.innerText !== title) {
            this.titleText.innerText = title
            this.titleText.className = (500 - this.thumbnail.clientWidth - this.titleText.clientWidth) > 0 ? '' : 'scroll'
        }
    }

    setAlbum(album) {
        if (album === '') {
            this.album.style.display = 'none'
        } else {
            if (this.albumText.innerText !== album) {
                this.albumText.innerText = album
            }
            this.album.style.display = ''
            this.album.className = (500 - this.thumbnail.clientWidth - this.album.clientWidth) > 0 ? '' : 'scroll'
        }
    }

    setArtist(artist) {
        if (this.artist.innerText !== artist) {
            this.artist.innerText = artist
            this.artist.className = (500 - this.thumbnail.clientWidth - this.artist.clientWidth) > 0 ? '' : 'scroll'
        }
    }

    setThumbnail(url) {
        if (this.thumbnail.src !== url) {
            this.thumbnail.src = url
            this.container.style.backgroundImage = `url(${url})`
        }
    }

    setTime(currentTime, duration) {
        this.progressText.innerText = `${currentTime} / ${duration}`
    }

    setProgress(progress) {
        this.progressBar.style.width = progress
    }

}

window.customElements.define('mo-media', Media)
