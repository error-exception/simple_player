var html = document.getElementsByTagName("html")[0];

html.addEventListener("mousemove", function (e) {
    let ratioString = getComputedStyle(html).backgroundSize;
    let ratio = parseInt(ratioString.substring(0, ratioString.lastIndexOf("%"))) / 100;
    //当前鼠标的坐标
    let clientX = e.clientX;
    let clientY = e.clientY;
    //窗口尺寸
    let windowX = window.innerWidth;
    let windowY = window.innerHeight;
    //背景图尺寸
    let imgWidth = windowX * ratio;
    let imgHeight = windowY * ratio;
    //鼠标在图片上的区域的尺寸
    let rectX = imgWidth - windowX;
    let rectY = imgHeight - windowY;

    let x = rectX * (clientX / windowX);
    let y = rectY * (clientY / windowY);

    html.style.backgroundPosition = -x + "px " + -y + "px";
});
var backgroundJSON = `
[{
    "name": "bg0",
    "url": "res/bg0.png",
    "thumb": "res/thumb/bg00.png"
}]
`