# ImageSelectorLikeWeChat
仿微信的图片选择器

一言不合就上图

![](http://i.imgur.com/JSHuN0Q.png) ![](http://i.imgur.com/29rXRjI.png) ![](http://i.imgur.com/hrqrFUP.png)

使用 **Runtime Permission** 特性来获取权限。代码中通过 [PermissionsDispatcher](https://github.com/hotchemi/PermissionsDispatcher "PermissionDispather") 来简化权限的申请。  
加载图片使用的是 [glide](https://github.com/bumptech/glide "glide")。  
第二张图中弹出的 “PopupWindow” 使用 BottomSheetBehavior 来实现，具有弹出后背景变暗跟 touchOuside 功能哦。  
查看大图使用的是 [PhotoView](https://github.com/chrisbanes/PhotoView "PhotoView")  。  
使用 Drawable 的上色（setTint/setTintList）功能，这样就可以使用一张图片来实现各种想要的颜色了。  
查看大图时，全屏模式用的是 LOW_PROFILE ，也就是隐藏状态栏，然后把导航栏的图标缩小为 3 个小点。  

欢迎大家来找茬！


