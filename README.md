# PuppetView
Screenshots and displaying reference images on the screen.

![](./screenshot-1.jpg)
![](./screenshot-2.png)

## Download

[v1.0.0.4 EXE](https://github.com/x6ud/puppet-view/releases/download/1.0.0.4/puppet-view.exe)

[v1.0.0.4 JAR](https://github.com/x6ud/puppet-view/releases/download/1.0.0.4/jar.zip)

## Usage

| Button               | Action |
|----------------------| --- |
| Left MB Drag         | Move |
| Wheel                | Zoom |
| Shift + Wheel        | Zoom proportionally |
| Ctrl + Wheel         | Rotate |
| Shift + Ctrl + Wheel | Rotate proportionally |
| Alt + Wheel          | Change opacity |
| Double Click         | Collapse |

## Run

```
mvn clean compile exec:java
```

## Build

```
mvn clean compile assembly:single
```
