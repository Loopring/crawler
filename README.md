# crawler
crawl news

## 为防止中文乱码，数据库注意以下：
```
vi /etc/mysql/my.cnf, add this two line under [mysqld]
character-set-server=utf8
collation-server=utf8_general_ci
```

```
create database crawler CHARACTER SET utf8 COLLATE utf8_general_ci;
```

```
mvn clean package spring-boot:repackage -DskipTests docker:build
```