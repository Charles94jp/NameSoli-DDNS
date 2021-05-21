outString=`ps -ef|grep DDNS|grep -v grep`
if [ -z "$outString" ]
then
    echo "DDNS is stopped!"
else
    echo "Program is running!"
    echo $outString
    echo "================="
    echo "Latest operation:"
    echo `tail log_DDNS.log`
fi
