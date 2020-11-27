### 背景
目前运营商给家庭宽带的IP都是动态的，庆幸的是虽然IP地址不固定，但是却是一个公网IP，所以我们可以购买一个域名，使用动态域名解析技术将域名解析到宽带的IP。这样就可以在家搭建各种服务并通过访问固定的域名来访问，而无需租用昂贵的公网服务器。

想实现这个目的，你需要一台一直运行的电脑来运行此程序，时刻检测宽带IP的变化。当然也需要自己解决路由器NAT映射的问题，但这不在本篇讨论。

### 简介
本程序通过访问 https://202020.ip138.com/ 获取家庭宽带的公网IP地址，通过 https://www.namesilo.com/api/ 来查询和更新DNS状态。

### 配置
为了运行这个程序，你需要安装Java，推荐jdk11。首次运行之前你需要配置_conf.txt文件，填写你[从NameSilo获取](https://guozh.net/obtain-namesilo-api-key/)的api key，以及你的域名（不带前缀）。

Linux则要做额外配置，首先要确保_conf.txt是Linux格式，否则脚本在拼接字符串时会出错。使用vi打开，然后:set fileformat=unix，最后退出保存。接着设置脚本权限 `chmod 755 linux_runDDNS.sh`

### 运行
Windows ：直接双击win_runDDNS.bat

Linux &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;：`./linux_runDDNS.sh`

### 在开机时启动
Windows ：将vbs文件[加入策略组](https://blog.csdn.net/yunmuq/article/details/110199091)

Linux &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;：添加sh到服务，可以重定向输出到文件以检查服务运行情况。