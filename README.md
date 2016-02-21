# Purple-Club - Dribbble Client x Material Design
小生第一个真正意义上的Android APP作品，Dribbble客户端。
完成度：70%
时间：20天+
风格：Material Design
分类：设计 
简介：
    调用Dribbble官方API，在页面展示“Shots”作品，依照不同的分类使用Fragment嵌套ViewPager与Drawer Layout分类展示作品。作品详情页面同样使用ViewPager分为“详情”与“评论”两部分。分别可以在主页面、作品详情页面对作品添加喜欢，在评论区对评论添加喜欢，数据会被提交至服务器。主页面展示中等大小图片，详情页面展示高清图片。应用采用了许多Material Design的特性，如用户头像到用户个人信息页面，和主界面作品到详情页面作品图片使用了“Shared Element Transition”动画效果，主界面采用Drawer Layout侧边栏布局等。

使用的类库：
OkHttp
Universal Image Loader
Gson
RecyclerView Animator

未完成部分：
  GIF动图显示
  设置页面及相应功能
  Shots作者信息展示
  应用启动画面
  评论功能代码已完成，因为没有评论权限无法进行调试
  代码未遵循MVC/MVP/MVVM设计模式，结构混乱

已知Bug：
  作品与用户头像未设置默认图片，由于Recycler View复用元素的特性，滑动过快有一定概率会导致图片显示错位，等待图片加载完毕即可。
  主页面作品图片到详情页面的“Shared Element Transition”动画没有效果，返回正常。
  初次加载数据失败时没有提示信息。
  主页面CardView最后一个项目显示不完全。
  
应用GIF Demo：
![image](https://github.com/TomassMaximum/Purple-Club/raw/master/Demo/基础操作逻辑.gif)
![image](https://github.com/TomassMaximum/Purple-Club/raw/master/Demo/详情及评论.gif)
![image](https://github.com/TomassMaximum/Purple-Club/raw/master/Demo/个人信息.gif)
![image](https://github.com/TomassMaximum/Purple-Club/raw/master/Demo/下拉刷新.gif)
![image](https://github.com/TomassMaximum/Purple-Club/raw/master/Demo/喜欢.gif)
