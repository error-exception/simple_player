class Particle {

    #isEnd = false;  //当前粒子运动是否结束
    #speed = 0;  //粒子最终的运动速度
    #endLocation = [0, 0];  //粒子运动的初始坐标
    #startLocation = [0, 0];  //粒子的终点坐标
    #size = 0;  //粒子的大小
    #distance = 0;  //初始坐标到终点坐标的距离
    #curDistance = 0;  //当前粒子的坐标与初始坐标的距离
/*
   width canvas的宽度
   height canvas的高度
   drawLocation 每次update()以后得到的下一个要绘制粒子的坐标
*/
    #width = 0;
    #height = 0;
    #drawLocation = [0, 0];
    #img = null;
    #y = 0; //终点坐标的y
    
    constructor(width, height) {
        this.#width = width;
        this.#height = height;
        this.#img = document.createElement("img");
        this.#img.src = "res/x-light2.png";  //粒子的图片
    }

    //得到从 from 到 to 之间的随机数
    random(from, to) {
        return from + (Math.random() * (to - from));
    }
    /*
        设置粒子的速度，速度的最终值是 speed 再加上在 -random 和 random 之间的随机数
    */
    setSpeed(speed, random) {
        this.#speed = speed + this.random(-random, random);
        return this;
    };
    /*
        设置粒子的大小，原理同上
    */
    setSize(size, random) {
        this.#size = size + this.random(-random, random);
        return this;
    };
    /*
        在 setSpeed 和 setSize 之后，计算出初始坐标和终点坐标
    */
    ready() {

        this.#height = this.#height + this.#size * 4;

        this.#y = this.#height + this.#size * 2;

        this.#endLocation = [this.random(this.#width * -0.25, this.#width + this.#width * 0.25), this.#y];
        this.#startLocation = [this.random(this.#width * -0.25, this.#width + this.#width * 0.25), -(this.#size * 2)];

        this.#drawLocation[0] = this.#startLocation[0];
        this.#drawLocation[1] = this.#startLocation[1];
        return this;
    };
    /*
        用于更新粒子的 drawLocation, 并判断
        是否粒子是否接近终点，若接近终点，重新
        计算初始坐标和终点坐标
    */
    update() {
        if (this.#isEnd) {
            this.#endLocation = [this.random(this.#width * -0.25, this.#width + this.#width * 0.25), this.#y];
            this.#startLocation = [this.random(this.#width * -0.25, this.#width + this.#width * 0.25), -(this.#size * 2)];
            this.#drawLocation[0] = this.#startLocation[0];
            this.#drawLocation[1] = this.#startLocation[1];
            this.#curDistance = 0;
            this.#isEnd = false;
        } else {
            //采用平面向量来计算出下一个要绘制的坐标
            var a = this.#endLocation[0] - this.#startLocation[0],
                b = this.#endLocation[1] - this.#startLocation[1];
            this.#distance = Math.sqrt(a * a + b * b);
            //得到向量vector
            var vector = [this.#endLocation[0] - this.#startLocation[0], this.#endLocation[1] - this.#startLocation[1]];
            //得到单位向量
            var unit = [1 / this.#distance * vector[0], 1 / this.#distance * vector[1]];
            //得到下一个要绘制的坐标到初始坐标的距离
            this.#curDistance += this.#speed;
            //计算出下一个要绘制的坐标
            this.#drawLocation[0] = unit[0] * this.#curDistance + this.#startLocation[0];
            this.#drawLocation[1] = unit[1] * this.#curDistance + this.#startLocation[1];
            if (this.#distance - this.#curDistance < this.#speed) {
                this.#isEnd = true;
            }
        }
        return this;

    };

    /**
     * 
     * @param {CanvasRenderingContext2D} canvasCtx
     */
    onDraw(canvasCtx) {
        canvasCtx.drawImage(this.#img, 0, 0, parseInt(this.#img.width), parseInt(this.#img.height), this.#drawLocation[0], this.#drawLocation[1], this.#size, this.#size);
    };

};

class Song {
    constructor() {
        this.title = "";
        this.artist = "";
        this.path = "";
    }
};

const NORMAL = 1;
const SINGLE = 2;
const RANDOM = 3;
/*

http://music.163.com/api/song/enhance/player/url?id=123456&ids=[123456]&br=3200000
https://api.imjad.cn/cloudmusic/?type=song&id=1824454151&br=320000
 */
var player = new Vue({
    el: "#app",
    data: {
        songs: [],
        currentSong: null,
        audio: null,
        currentPosition: 0,
        playIcon: "\ue037",
        playModeIcon: "\ue040",
        backgrounds: [],
        playMode: NORMAL,  //1. 循环播放  2. 单曲循环  3. 随机播放
        html: null,
        time: "",
        cloudMusicId: "",
        tabIndex: 1,
        cloudMusicUrl: "",
        canvasCtx: null,
        dataArray: null,
        bufferLength: 0,
        particles: [],
        particlesId: 0,
        isDrawParticals: false,
        visualOffset: 0,
        roundVisualData: new Uint8Array(40),
        milli: 0,
        date: new Date(),
        Settings: {
            showVisualization: true,
            backgroundMove: true,
            volume: 100
        },
        Window: {
            title: "播放器信息",
            Frame: {
                showWindow: false,
                showBackgroundSettings: false,
                showSettings: false,
                showScanMusic: false,
                showCloudMusicAPI: false
            },
            Panel: {
                showPlayerInfo: false,
                showAdjustVolume: false,
                showPlayerPanel: false
            }
        }
    },
    created () {
        this.audio = new MusicPlayer("audio");
        this.html = document.getElementsByTagName("html")[0];
        this.backgrounds = JSON.parse(backgroundJSON);

        let thiz = this;
        $.ajax({
            url: "/list",
            type: "GET",
            async: false,
            success (res) {
                thiz.songs = res;

            }
        });
        this.currentSong = this.songs[this.currentPosition];
        let that = this;
        this.audio.onTimeupdate(() => {
            let total = that.audio.duration;
            let cur = that.audio.currentTime;
            if (that.$refs.progress)
                that.$refs.progress.style.width = parseInt((cur / total) * 100) + "%";
        });
        this.audio.onComplete(() => {
            if (that.playMode == NORMAL) {
                that.next();
            } else if (that.playMode == SINGLE) {
                that.play(that.currentPosition);
            } else {
                that.randomPlay();
            }
        });

        setInterval(() => {
            // let d = new Date();
            // let h = d.getHours();
            // let m = d.getMinutes();
            // this.time = h + ":" + m;
            this.visualOffset = (this.visualOffset + 2) % 40;

        }, 16);
        window.onload = () => {
            this.$refs.canvas.width = window.innerWidth;
            this.$refs.canvas.height = window.innerHeight - 40;
            this.canvasCtx = this.$refs.canvas.getContext("2d");
            this.particles = new Array();
            for (var g = 0; g < 100; g++) {
                let p = new Particle(window.innerWidth, window.innerHeight);
                p.setSize(8, 2).setSpeed(1, 0.05).ready();
                this.particles.push(p);
            }
        };
    },
    methods: {
        drawPartical() {
           this.particlesId = requestAnimationFrame(this.drawPartical);
            this.clearCanvas();
            for (var g = 0; g < this.particles.length; g++) {
                let par = this.particles[g];
                par.update();
                par.onDraw(this.canvasCtx);
            }
        },
        drawViusalizer() {
            drawVisual = requestAnimationFrame(this.drawViusalizer);
            
            this.audio.getFft(this.dataArray);

            this.clearCanvas();

            
            this.canvasCtx.beginPath();
            
            let length = this.bufferLength - 40;
            var sliceWidth = this.$refs.canvas.width * 1.0 / length;
            this.canvasCtx.lineWidth = sliceWidth - 1;
            this.canvasCtx.translate(sliceWidth / 2, 0);
            var x = 0;
            var vol = 0;

            for (var i = 0; i < length; i+=1) {

                let v = this.dataArray[i] / (128.0 * 3);
                let v1 = 0
                let j = 0;
                if (i >= length / 2)
                    j = (length / 2) - (i - length / 2);
                else
                    j = i;
                v1 = this.dataArray[j] / (128.0 * 4);
                var y = v1 * this.$refs.canvas.height / 2;

                this.canvasCtx.moveTo(x, this.$refs.canvas.height);
                this.canvasCtx.lineTo(x, this.$refs.canvas.height - y);
                
                x += sliceWidth;
                
                vol += v;
            }
            vol /= length;
            this.canvasCtx.strokeStyle = `rgba(0, 0, 255, ${vol})`;
            this.$refs.logoCircle.style.transform = `scale(${1 - vol / 6})`;
            
            // let innerRadius = 180;
            // let per = 360.0 / 200.0;
            // let idx = 1;
            // let maxLength = 100;
            // this.getAudioData(this.roundVisualData);
        
            // let cx = this.$refs.canvas.width / 2;
            // let cy = this.$refs.canvas.height / 2;
            // for (let i = 0; i < 5; i++) {
            //     for (let j = 0; j < 40; j++) {
            //         let angle = idx * per;
            //         let sin = Math.sin(this.getRadius(angle));
            //         let cos = Math.cos(this.getRadius(angle));
            //         this.canvasCtx.moveTo(cx + innerRadius * cos, cy + innerRadius * sin);

            //         //let ti = (j + this.visualOffset) % 40;
            //         let v = this.roundVisualData[j] / 256.0;
            //         let outRadius = innerRadius + v * maxLength;
            //         this.canvasCtx.lineTo(cx + outRadius * cos, cy + outRadius * sin);
            //         idx++;
            //     }
            // }
            
            this.canvasCtx.stroke();
        },
        getAudioData(arr) {
            let tmp = new Uint8Array(this.bufferLength);
            
            this.audio.getFft(tmp);
            for (let i = 0; i < 40; i++) {
                let j = (i + this.visualOffset) % 40;
                if (i < 2) {
                    arr[j] = tmp[i + 1];
                } else {
                    let dcrease = 8;
                    if (arr[j] <= dcrease)
                        arr[j] = 0;
                    else
                        arr[j] -= dcrease;
                }
            }

        },
        getRadius(angle) {
            return (Math.PI / 180) * angle;
        },
        play (index) {
            if (index == this.currentPosition) {
                this.playBtn();
                return;
            }
            let s = this.songs[index];
            this.currentSong = s;
            this.audio.src = "/song?id=" + s.id;
            console.log(s.path);
            this.audio.play();
            this.currentPosition = index;
            this.playIcon = "\ue034";
            // if (!this.isDrawParticals) {
            //     this.drawPartical();
            //     this.isDrawParticals = true;
            // }
            if (this.audio.isAnalyserEnabled()) {
                return;
            }
            this.audio.enableAnalyser();
            this.canvasCtx = this.$refs.canvas.getContext("2d");
            this.audio.fftSize = 256;
            this.bufferLength = this.audio.frequencyBinCount;
            console.log(this.bufferLength);
            this.dataArray = new Uint8Array(this.bufferLength);
            this.drawViusalizer();
        },
        playBtn() {
            if (this.audio.paused()) {
                this.playIcon = "\ue034";
                this.audio.play();
                // if (!this.isDrawParticals) {
                //     this.drawPartical();
                //     this.isDrawParticals = true;
                // }
            } else {
                this.playIcon = "\ue037";
                this.audio.pause();
                // cancelAnimationFrame(this.particlesId);
                // this.isDrawParticals = false;
            }
        },
        next () {
            if (this.playMode == RANDOM) {
                this.randomPlay()
            } else {
                this.currentPosition++;
                if (this.currentPosition >= this.songs.length) {
                    this.currentPosition = 0;
                }
                this.currentSong = this.songs[this.currentPosition];
                this.audio.src = "/song?id=" + this.currentSong.id;
                this.audio.play();
                this.playIcon = "\ue034";
            }
        },
        previous () {
            if (this.playMode == RANDOM) {
                this.randomPlay();
            } else {
                this.currentPosition--;
                if (this.currentPosition < 0) {
                    this.currentPosition = this.songs.length - 1;
                }
                this.currentSong = this.songs[this.currentPosition];
                this.audio.src = "/song?id=" + this.currentSong.id;
                this.audio.play();
                this.playIcon = "\ue034";
            }
        },
        openPlayerPanel () {
            this.Window.Panel.showPlayerPanel = !this.Window.Panel.showPlayerPanel;
        },
        randomPlay () {
            let random = Math.random() * this.songs.length;
            this.currentSong = this.songs[parseInt(random)];
            this.audio.src = "/song?id=" + this.currentSong.id;
            this.audio.play();
            this.playIcon = "\ue034";
        },
        changePlayMode () {
            if (this.playMode == NORMAL) {
                this.playMode = SINGLE;
                this.playModeIcon = "\ue041";
            } else if (this.playMode == SINGLE) {
                this.playMode = RANDOM;
                this.playModeIcon = "\ue043";
            } else {
                this.playMode = NORMAL;
                this.playModeIcon = "\ue040";
            }
        },
        closeWindow () {
            for (p in this.Window.Frame) {
                if (this.Window.Frame[p])
                    this.Window.Frame[p] = false;
            }
            for (q in this.Window.Panel) {
                if (this.Window.Panel[q]) 
                    this.Window.Panel[q] = false;
            }
        },
        openBackgroundSettings () {
            this.closeWindow();
            this.Window.Frame.showWindow = true;
            this.Window.Frame.showBackgroundSettings = true;
            this.Window.title = "背景设置";
        },
        setBackground (index) {
            document.getElementsByTagName("html")[0].style.backgroundImage = "url(" + this.backgrounds[index].url + ")";
        },
        openScanMusic () {
            this.closeWindow();
            this.Window.Frame.showWindow = true;
            this.Window.Frame.showScanMusic = true;
            this.Window.title = "音乐扫描";
        },
        openPlayerInfo () {
            this.Window.Panel.showPlayerInfo = !this.Window.Panel.showPlayerInfo;
        },
        openAdjustVolume () {
            this.Window.Panel.showAdjustVolume = !this.Window.Panel.showAdjustVolume;
        },
        adjustVolume (e) {
            let step = 5;
            if (e.wheelDelta > 0) {
                if (this.Settings.volume <= 100) {
                    this.Settings.volume += step;
                }
                if (this.Settings.volume > 100) {
                    this.Settings.volume = 100;
                }
            } else {
                if (this.Settings.volume >= 0) {
                    this.Settings.volume -= step;
                }
                if (this.Settings.volume < 0) {
                    this.Settings.volume = 0;
                }
            }
            this.audio.volume = this.Settings.volume / 100;
        },
        openCloudMusicAPI() {
            this.closeWindow();
            this.Window.Frame.showWindow = true;
            this.Window.Frame.showCloudMusicAPI = true;
            this.Window.title = "网易云音乐";
        },
        downloadCloudMusic() {
            let id = parseInt(this.cloudMusicId);
            let u = "";
            if (this.tabIndex == 1) {
                u = "http://music.163.com/api/song/enhance/player/url?id=" + id + "&ids=[" + id + "]&br=3200000"; 
            } else if (this.tabIndex == 2) {
                u = "https://api.imjad.cn/cloudmusic/?type=song&id=" + id + "&br=320000";
            }
            let that = this;
            $.ajax({
                url: u,
                method: "GET",
                // beforeSend(req) {
                //     req.setRequestHeader("Access-Control-Request-Headers", "content-type");
                // },
                success(result) {
                    that.cloudMusicUrl = result.data[0].url;
                    alert(that.cloudMusicUrl);
                }
            });
        },
        changeApi(index) {
            this.tabIndex = index;
        },
        openSettings() {
            this.closeWindow();
            this.Window.Frame.showWindow = true;
            this.Window.Frame.showSettings = true;
            this.Window.title = "设置";
        },
        clearCanvas() {
            this.$refs.canvas.width = window.innerWidth;
            this.$refs.canvas.height = window.innerHeight - 40;
        },
        p(e) {
            console.log(e);
        }
     
    }
});
/*
function timeString(t){
    if (t%60 < 10) {
        return "0" + parseInt(t/60) + ':0' + parseInt(t%60);
    } else {
        return "0" + parseInt(t/60) + ':' + parseInt(t%60);
    }
}
*/