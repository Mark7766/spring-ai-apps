# 简介
newston是一个智能编辑代理，每六个小时会，检索一次hark news的最新story，并挑选出AI和软件开发相关内容，并用中文总结，最后通过将汇总后的内容，以邮件形式发送给接收人。

# 环境变量配置
[.env](.env)
# Java运行
可以在本地电脑将环境变量配置好，直接运行java -jar 编译好的jar包
# Docker运行
- docker build: [docker_image_build_.sh](docker_image_build.sh)
- 配置好环境变量文件[.env](.env)，[docker_container_start.sh](docker_container_start.sh)会默认使用本目录下的.env
- docker run: [docker_container_start.sh](docker_container_start.sh)
# 访问：
- 通过web调用接口，执行一次智能编辑：http://localhost:8081/newston/start
- 通过修改环境变量NEWSTON_CRON，来调整智能编辑执行的Cron