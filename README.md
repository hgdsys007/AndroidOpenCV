# Android平台上实现全景图拼接

#### 介绍

毕业项目——基于Android平台的全景图拼接系统设计与实现。
虚拟现实技术是当代仿真技术的重要方向,近几年发展迅猛。在不同的应用场景下,实现技术随之改变。基于全景图的虚拟现实技术被广泛应用于街景地图、智能家居、室内展示、虚拟旅游等场景,具有效率高、展示效果真实的优势。全景图是一种能覆盖大范围场景的宽视角图像。除了用特殊的相机获取外，目前多采用图像拼接技术，即将普通相机拍摄的两幅或多幅来自同一场景的有重叠区域的图像拼接合成为一幅宽视角的高质量图像。
随着智能手机的普及,基于移动终端实现全景图系统已成为当前的研究热点。本课题拟采用Android平台对全景图技术展开研究。在android平台上实现了全景图技术,制作了一款小型的全景图应用程序,相比较于传统方法, 更简便、轻巧。
主要使用OpenCV中的Stitcher类进行全景图的拼接，然后使用CMakeList + Ninja来进行跨平台的调用。
要获得更多详细的介绍，可以参考本人写的博客：[使用OpenCV的Stitcher类来实现Android平台上的全景图拼接](https://blog.csdn.net/qq_41151659/article/details/104299512)

#### 相关技术栈
- JDK
- JNI
- Android SDK
- Android NDK
- Gradle
- XML
- JSON
- Ninja
- C++
- CMakeList
- OpenCV
- Sticher
- Android Studio
- CLion

#### 调用图
![输入图片说明](https://images.gitee.com/uploads/images/2020/0325/184543_8e0dc2e6_5037130.png "屏幕截图.png")

#### 流程图
![输入图片说明](https://images.gitee.com/uploads/images/2020/0325/184605_1150c9db_5037130.png "屏幕截图.png")

#### 效果图
![输入图片说明](https://images.gitee.com/uploads/images/2020/0325/184617_2960fa01_5037130.png "屏幕截图.png")
![输入图片说明](https://images.gitee.com/uploads/images/2020/0325/184624_e70623bc_5037130.png "屏幕截图.png")