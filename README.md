# PuppetView
Screenshots and displaying reference images on the screen.

![](./screenshot-1.jpg)
![](./screenshot-2.png)

## Download

[v1.0.0.4](https://github.com/x6ud/puppet-view/releases/download/1.0.0.4/puppet-view.exe) (for Windows)

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
