#!/bin/sh

if [ $# -eq 0 ]
then
    echo "请输入导出的日期参数，格式为YYYY-MM-DD"
    exit -1
fi

DAY_STR=$1
NEXT_DAY=$(date +%Y-%m-%d -d "${DAY_STR} + 1 day")

echo " 导出写入时间在 ${NEXT_DAY} 0点和 ${NEXT_DAY} 0点之间的数据..."

mysql -u root -proot crawler <<EOF

SELECT fund_code, fund_name, fund_rate_str, date_str INTO OUTFILE './output/fundftp-${DAY_STR}.txt'
FIELDS TERMINATED BY '\001'
LINES TERMINATED BY '\n'
FROM fund_ftp
WHERE insert_time > ${DAY_STR} and insert_time < ${NEXT_DAY};

EOF

echo " 导出写入时间在 ${NEXT_DAY} 0点和 ${NEXT_DAY} 0点之间的数据完成！结果文件：output/fundftp-${DAY_STR}.txt"
