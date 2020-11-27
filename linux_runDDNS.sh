#
list="key domain frequency"
param=""
for idx in ${list};
do
str=`grep ${idx} _conf.txt`
#将冒号右边的截取出来
str=${str#*:}
param="${param} ${str}"
done

java -jar DDNSjar.jar ${param}

