PuppetView is a tool for displaying reference images on screen.

It's made for my own personal use. For similar software, [PureRef](https://www.pureref.com/) and [Kuadro](http://kruelgames.com/tools/kuadro/) are much more powerful.

![](./screenshot/1.png)
![](./screenshot/2.png)

## OCR
Get your APP ID on https://console.bce.baidu.com/

Create `config.properties`
```properties
app.id=YOUR_APP_ID
app.key=YOUR_APP_KEY
app.secret=YOUR_APP_SECRET
```

See `com.github.x6ud.puppetview.OcrUtils`

## Run
```
mvn clean compile exec:java
```

## Build
```
mvn clean compile assembly:single
```

## Usage

| Operation | Action |
| --- | --- |
| Left MB Drag | Move |
| Wheel | Zoom |
| Shift + Wheel | Zoom proportionally |
| Ctrl + Wheel | Rotate |
| Shift + Ctrl + Wheel | Rotate proportionally |
| Alt + Wheel | Change opacity |
| Double Click | Collapse |
