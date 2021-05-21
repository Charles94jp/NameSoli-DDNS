# 清空日志
echo > log_DDNS.log

# 修改配置文件格式，避免拼接错误
vi _conf.txt -c 'set fileformat=unix' -c 'wq'

# 解析出配置文件参数
list="key domain frequency"
param=""
for idx in ${list};
do
str=`grep ${idx} _conf.txt`
#将冒号右边的截取出来
str=${str#*:}
param="$param $str"
done

# 运行
nohup java -jar DDNSjar.jar ${param}>log_DDNS.log 2>&1 &
