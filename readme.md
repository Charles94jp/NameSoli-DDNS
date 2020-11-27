##### 配置
为了运行这个程序，你需要安装Java，推荐jdk11。首次运行之前你需要配置_conf.txt文件，填写你[从NameSilo获取](https://guozh.net/obtain-namesilo-api-key/)的api key，以及你的域名（不带前缀）。

Linux要做额外配置，首先要确保_conf.txt是Linux格式，否则脚本在拼接字符串时会出错。使用vi打开，然后:set fileformat=unix，最后退出保存。接着设置脚本权限 `chmod 755 linux_runDDNS.sh`

##### 运行
Windows ：直接双击win_runDDNS.bat

Linux &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;：`./linux_runDDNS.sh`

##### 在开机时启动
Windows ：将vbs文件[加入策略组](https://blog.csdn.net/yunmuq/article/details/110199091)

Linux &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;：添加服务