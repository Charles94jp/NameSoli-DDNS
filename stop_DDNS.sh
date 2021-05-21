findps=`ps -ef|grep DDNS|grep -v grep`
if [ -z "$findps" ]
then
    echo "DDNS is not running!"
else
    ps -ef|grep DDNS|grep -v grep|cut -c 9-15|xargs kill -9
fi
