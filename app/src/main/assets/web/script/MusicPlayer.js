class MusicPlayer {

    constructor(audioId) {

        this.audio = document.getElementById(audioId);

    }

    get currentTime() {
        return this.audio.currentTime;
    }

    get duration() {
        return this.audio.duration;
    }

    /**
     * @param {string} s
     */
    set src(s) {
        this.audio.src = s;
    }

    enableAnalyser() {
        if (!this.analyser)
            this.analyser = this.audioContext.createAnalyser();
        this.mediaElementSource.connect(this.analyser);
        // this.analyser.minDecibels = -80;
        // this.analyser.maxDecibels = -40;
    }

    disableAnalyser() {
        this.mediaElementSource.disconnect(this.analyser);
        this.analyser = null;
    }

    isAnalyserEnabled() {
        return this.analyser != null && this.analyser !== undefined;
    }

    play() {
        if (!this.audioContext) {
            this.audioContext = new AudioContext();
            this.mediaElementSource = this.audioContext.createMediaElementSource(this.audio);
            this.mediaElementSource.connect(this.audioContext.destination);
        }
        this.audio.play();
    }

    pause() {
        this.audio.pause();
    }

    paused() {
        return this.audio.paused;
    }

    get volume() {
        return this.audio.volume;
    }

    set volume(v) {
        this.audio.volume = v;
    }

    /**
     * @param {function} func
     *  */
    onTimeupdate(func) {
        this.audio.ontimeupdate = func;
    }

    onComplete(func) {
        this.audio.onended = func;
    }

    get fftSize() {
        return this.analyser.fftSize;
    }

    set fftSize(s) {
        this.analyser.fftSize = s;
    }

    get frequencyBinCount() {
        return this.analyser.frequencyBinCount;
    }

    /**
     * 
     * @param {Uint8Array} data
     */
    getFft(data) {
        this.analyser.getByteFrequencyData(data);
    }

    /**
     * 
     * @param {Uint8Array} data 
     */
    getWave(data) {
        this.analyser.getByteTimeDomainData(data);
    }
}