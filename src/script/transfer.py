#!/usr/bin/env python2
# -*- coding: utf-8 -*-

import sys
import MySQLdb

if __name__ == '__main__':

    mysql_conn_list = {
        'sh1': MySQLdb.connect(host='10.36.40.99', user='root', passwd='root', db='crawler', charset="utf8"),
        'sh2': MySQLdb.connect(host='10.36.40.110', user='root', passwd='root', db='crawler', charset="utf8"),
        'dev': MySQLdb.connect(host='10.143.252.21', user='root', passwd='root', db='crawler', charset="utf8"),
        'local': MySQLdb.connect(host='localhost', user='root', passwd='root', db='crawler', charset="utf8")
    }
    # 1. 参数校验
    print '1. check parameter'
    if len(sys.argv) != 4:
        print 'invalid parameter，example: python ufis_code.py <src-db:sh1|sh2|dev|local> <des-db:sh1|sh2|dev|local> <table>'
        sys.exit('invalid parameter')

    # 2. 源数据库读取数据
    print '2. data transfer'
    reload(sys)
    sys.setdefaultencoding('utf-8')
    src_conn = mysql_conn_list.get(sys.argv[1])
    des_conn = mysql_conn_list.get(sys.argv[2])
    src_cursor = src_conn.cursor(cursorclass=MySQLdb.cursors.DictCursor)
    des_cursor = des_conn.cursor(cursorclass=MySQLdb.cursors.DictCursor)

    table = sys.argv[3]
    src_sql = 'select * from ' + table
    src_cursor.execute(src_sql)

    for ri, row in enumerate(src_cursor.fetchall()):
        row.pop('id')
        des_sql = 'insert into ' + table
        des_sql += str(tuple(row)) + ' VALUES ('
        values = []
        for ki, key in enumerate(row.keys()):
            des_sql += '%s'
            if ki != len(row) - 1:
                des_sql += ', '
            else:
                des_sql += ')'
            value = row.get(key)
            if value:
                values.append(str(value))
            else:
                values.append(None)
        des_sql = des_sql.replace('\'', '')
        try:
            des_cursor.execute(des_sql, values)
            des_conn.commit()
            print 'insert row: ' + str(ri) + ' done...'
        except MySQLdb.Error, e:
            pass

    src_cursor.close()
    des_cursor.close()
    src_conn.close()
    des_conn.close()

    print '3. insert done total:', ri - 1
