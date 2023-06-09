# Simple Player

本地音乐播放器（not stable），内置一个简单的 HTTP 服务器，可与 https://github.com/error-exception/Simple-Player-Web 配合使用，也可根据 API 自行开发（具体查看`com.simple.player.web.controller`包下的类）

初次扫描完成且授予存储权限后，到设置 -> 播放器 -> 切换音乐源 -> 选择”External Storage“，体验完整功能。

release 目录中为非最新版

## 概况

最低支持安卓6.0（SDK 23），测试机型 Redmi note 8 MIUI11 Android 9.0

不保证高版本能够正常使用

## 功能

1. 基本的播放控制
2. kgm、kge、ncm、uc 文件支持
3. 耳机线控切歌
4. 音量建切歌
5. 其他

## 不足

1. 不支持夜间模式（强开会导致 UI 配色异常）
2. 某些按钮不起作用（未添加处理事件）
3. 锁屏界面未完成
4. 某些地方会出现主题颜色应用不完全
5. 音量键切歌采用非标准接口实现，其他安卓版本的安卓机型不一定可用
6. 歌曲信息界面的比特率是瞎填的，不是真正的比特率（一直 250）
7. 其他

## TODO

1. [ ] 作为音频文件的打开方式
