### 简介
这是一个通过自然语言查询数据库的小demo，可以把自然语言转换成SQL，并将查询到的数据生成为echarts图表
#### Ollama 安装
- [Download and install Ollama](https://ollama.com/download)
### 安装千问模型
- ollama pull qwen2.5
### Chroma 安装与运行
- docker run -it --rm --name chroma -p 8000:8000 ghcr.io/chroma-core/chroma:0.6.2
### PostgreSQL 安装与运行
```
docker run -d -e POSTGRESQL_PASSWORD=123456 --name postgresql \
  -p 5432:5432 \
  bitnami/postgresql:12.14.0
```
### 执行Postgresql脚本
```
CREATE TABLE dtp_hospital (
id SERIAL PRIMARY KEY,                  -- 主键，使用 BIGINT，设置为主键
province VARCHAR(20) DEFAULT NULL,              -- 省份
city VARCHAR(20) DEFAULT NULL,                  -- 城市
reporting_team VARCHAR(50) DEFAULT NULL,        -- 提报团队
district VARCHAR(20) DEFAULT NULL,              -- 区
hospital_name VARCHAR(100) DEFAULT NULL,        -- 申请的DTP药房 主要对应的医院名称
hospital_code VARCHAR(50) DEFAULT NULL,         -- 申请DTP主要对应的医院code
hospital_address VARCHAR(255) DEFAULT NULL,     -- 医院具体地址
location VARCHAR(100) DEFAULT NULL,             -- 医院所在经纬度
del_flag INTEGER DEFAULT 0,                     -- 删除状态 0正常 1已删除
create_by VARCHAR(32) DEFAULT NULL,             -- 创建人
create_time TIMESTAMP DEFAULT NULL,             -- 创建时间，使用 TIMESTAMP 替代 datetime
update_by VARCHAR(32) DEFAULT NULL,             -- 更新人
update_time TIMESTAMP DEFAULT NULL,             -- 更新时间，使用 TIMESTAMP 替代 datetime
image VARCHAR(255) DEFAULT NULL                 -- 图片
);

COMMENT ON TABLE dtp_hospital IS '医院表';
COMMENT ON COLUMN dtp_hospital.id IS '主键';
COMMENT ON COLUMN dtp_hospital.province IS '省份';
COMMENT ON COLUMN dtp_hospital.city IS '城市';
COMMENT ON COLUMN dtp_hospital.reporting_team IS '提报团队';
COMMENT ON COLUMN dtp_hospital.district IS '区';
COMMENT ON COLUMN dtp_hospital.hospital_name IS '申请的DTP药房 主要对应的医院名称';
COMMENT ON COLUMN dtp_hospital.hospital_code IS '申请DTP主要对应的医院code';
COMMENT ON COLUMN dtp_hospital.hospital_address IS '医院具体地址';
COMMENT ON COLUMN dtp_hospital.location IS '医院所在经纬度';
COMMENT ON COLUMN dtp_hospital.del_flag IS '删除状态 0正常 1已删除';
COMMENT ON COLUMN dtp_hospital.create_by IS '创建人';
COMMENT ON COLUMN dtp_hospital.create_time IS '创建时间';
COMMENT ON COLUMN dtp_hospital.update_by IS '更新人';
COMMENT ON COLUMN dtp_hospital.update_time IS '更新时间';
COMMENT ON COLUMN dtp_hospital.image IS '图片';

INSERT INTO dtp_hospital (
    province, city, reporting_team, district, hospital_name, 
    hospital_code, hospital_address, location, del_flag, 
    create_by, create_time, update_by, update_time, image
) VALUES
    ('上海', '上海市', '团队A', '黄浦区', '上海交通大学医学院附属仁济医院西院', 
     'RJXY001', '上海市黄浦区重庆南路139号', '121.4934,31.2394', 0, 
     'admin', '2025-03-21 15:30:55', 'admin', '2025-03-21 15:30:55', 
     'http://example.com/image1.jpg'),
    ('上海', '上海市', '团队B', '黄浦区', '上海交通大学医学院附属瑞金医院', 
     'RJYY002', '上海市黄浦区瑞金二路197号', '121.4934,31.2394', 0, 
     'admin', '2025-03-21 15:31:00', 'admin', '2025-03-21 15:31:00', 
     'http://example.com/image2.jpg');
```
### 运行程序
- mvn spring-boot:run -DskipTest
### 添加数据库DDL训练数据
```
curl --location 'localhost:8081/sandy/training' \
--header 'Content-Type: application/json' \
--data '{
    "content": "CREATE TABLE dtp_hospital (id SERIAL PRIMARY KEY, province VARCHAR(20) DEFAULT NULL, city VARCHAR(20) DEFAULT NULL, reporting_team VARCHAR(50) DEFAULT NULL, district VARCHAR(20) DEFAULT NULL, hospital_name VARCHAR(100) DEFAULT NULL, hospital_code VARCHAR(50) DEFAULT NULL, hospital_address VARCHAR(255) DEFAULT NULL, location VARCHAR(100) DEFAULT NULL, del_flag INTEGER DEFAULT 0, create_by VARCHAR(32) DEFAULT NULL, create_time TIMESTAMP DEFAULT NULL, update_by VARCHAR(32) DEFAULT NULL, update_time TIMESTAMP DEFAULT NULL, image VARCHAR(255) DEFAULT NULL); COMMENT ON TABLE dtp_hospital IS '\''医院表'\''; COMMENT ON COLUMN dtp_hospital.id IS '\''主键'\''; COMMENT ON COLUMN dtp_hospital.province IS '\''省份'\''; COMMENT ON COLUMN dtp_hospital.city IS '\''城市'\''; COMMENT ON COLUMN dtp_hospital.reporting_team IS '\''提报团队'\''; COMMENT ON COLUMN dtp_hospital.district IS '\''区'\''; COMMENT ON COLUMN dtp_hospital.hospital_name IS '\''申请的DTP药房 主要对应的医院名称'\''; COMMENT ON COLUMN dtp_hospital.hospital_code IS '\''申请DTP主要对应的医院code'\''; COMMENT ON COLUMN dtp_hospital.hospital_address IS '\''医院具体地址'\''; COMMENT ON COLUMN dtp_hospital.location IS '\''医院所在经纬度'\''; COMMENT ON COLUMN dtp_hospital.del_flag IS '\''删除状态 0正常 1已删除'\''; COMMENT ON COLUMN dtp_hospital.create_by IS '\''创建人'\''; COMMENT ON COLUMN dtp_hospital.create_time IS '\''创建时间'\''; COMMENT ON COLUMN dtp_hospital.update_by IS '\''更新人'\''; COMMENT ON COLUMN dtp_hospital.update_time IS '\''更新时间'\''; COMMENT ON COLUMN dtp_hospital.image IS '\''图片'\'';",
    "policy":"DDL"
}'
返回：
{"code":200,"message":"请求成功","data":null}
```
### 添加数据库SQL训练数据
```
curl --location 'localhost:8081/sandy/training' \
--header 'Content-Type: application/json' \
--data '{
    "question":"在黄浦区的医院有哪些？",
    "content": "SELECT * FROM DTP_HOSPITAL WHERE DISTRICT LIKE '\''%黄浦区%'\''",
    "policy":"SQL"
}'
返回：
{"code":200,"message":"请求成功","data":null}
```
### 查询数据库训练数据列表
```
curl --location 'localhost:8081/sandy/training?question=%22%22' \
--data ''
返回：
{"code":200,"message":"请求成功","data":[{"id":"aa321b9f-a683-4a5e-a233-0902b3548daf","text":"CREATE TABLE dtp_hospital (id SERIAL PRIMARY KEY, province VARCHAR(20) DEFAULT NULL, city VARCHAR(20) DEFAULT NULL, reporting_team VARCHAR(50) DEFAULT NULL, district VARCHAR(20) DEFAULT NULL, hospital_name VARCHAR(100) DEFAULT NULL, hospital_code VARCHAR(50) DEFAULT NULL, hospital_address VARCHAR(255) DEFAULT NULL, location VARCHAR(100) DEFAULT NULL, del_flag INTEGER DEFAULT 0, create_by VARCHAR(32) DEFAULT NULL, create_time TIMESTAMP DEFAULT NULL, update_by VARCHAR(32) DEFAULT NULL, update_time TIMESTAMP DEFAULT NULL, image VARCHAR(255) DEFAULT NULL); COMMENT ON TABLE dtp_hospital IS '医院表'; COMMENT ON COLUMN dtp_hospital.id IS '主键'; COMMENT ON COLUMN dtp_hospital.province IS '省份'; COMMENT ON COLUMN dtp_hospital.city IS '城市'; COMMENT ON COLUMN dtp_hospital.reporting_team IS '提报团队'; COMMENT ON COLUMN dtp_hospital.district IS '区'; COMMENT ON COLUMN dtp_hospital.hospital_name IS '申请的DTP药房 主要对应的医院名称'; COMMENT ON COLUMN dtp_hospital.hospital_code IS '申请DTP主要对应的医院code'; COMMENT ON COLUMN dtp_hospital.hospital_address IS '医院具体地址'; COMMENT ON COLUMN dtp_hospital.location IS '医院所在经纬度'; COMMENT ON COLUMN dtp_hospital.del_flag IS '删除状态 0正常 1已删除'; COMMENT ON COLUMN dtp_hospital.create_by IS '创建人'; COMMENT ON COLUMN dtp_hospital.create_time IS '创建时间'; COMMENT ON COLUMN dtp_hospital.update_by IS '更新人'; COMMENT ON COLUMN dtp_hospital.update_time IS '更新时间'; COMMENT ON COLUMN dtp_hospital.image IS '图片';","media":null,"metadata":{"distance":0.7830759,"script_type":"DDL"},"score":0.21692407131195068},{"id":"862f3850-0d85-4720-b354-88853a85c9fa","text":"{\"question\":\"在黄浦区的医院有哪些？\",\"content\":\"SELECT * FROM DTP_HOSPITAL WHERE DISTRICT LIKE '%黄浦区%'\",\"policy\":\"SQL\"}","media":null,"metadata":{"distance":0.871068,"script_type":"SQL"},"score":0.12893199920654297}]}
```
### 添加数据库Document训练数据
```
curl --location 'localhost:8081/sandy/training' \
--header 'Content-Type: application/json' \
--data '{
    "content": "test",
    "policy":"DOCUMENTATION"
}'
返回：
{"code":200,"message":"请求成功","data":null}
```
### 删除数据库训练数据
```
curl --location --request DELETE 'localhost:8081/sandy/training/6a003036-e205-47db-98b8-9fa0e1276579' \
--data ''
返回：
{"code":200,"message":"请求成功","data":null}
```



### 提问自然语言查询数据
- 浏览器调用

- 命令行调用
```
请求：curl --location 'localhost:8081/sandy/chat' \
--header 'Content-Type: application/json' \
--data '{
    "question": "黄浦区的医院有哪些"
}'
响应的头信息里Content-Type=text/event-stream，响应体是黄浦区的医院有：收到您的问题，正快马加鞭进行解决<br>生成SQL中...<br>生成SQL：SELECT hospital_name, district FROM dtp_hospital WHERE district = '黄浦区';<br>查询数据中...<br>查询到数据：[{"district":"黄浦区","hospital_name":"上海交通大学医学院附属仁济医院西院"},{"district":"黄浦区","hospital_name":"上海交通大学医学院附属瑞金医院"}]<br>生成图表中...<br><iframe src="http://localhost:8081/3d5ef9f3-82de-4c39-84f7-482f28a26822.html" width="800" height="500" frameborder="0" title="黄浦区的医院有哪些"></iframe>
```
### 感谢
感谢[SuperSQL](https://github.com/guocjsh/SuperSQL)，从中学到了如何用java实现text to sql，本项目中也有多处代码，借鉴了此工程的设计与代码实现，万分感激作者的分享。

