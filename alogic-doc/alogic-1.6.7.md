alogic-1.6.7
============

文档记录了alogic-1.6.7的更新日志。

### 1.6.7.1 [20170116 duanyy]

- alogic-common:公式解析器(Parser)修改方法为protected，增加可定制性;
- alogic-jms:淘汰alogic-jms;
	
### 1.6.7.2 [20170117 duanyy]
	 
- alogic-commom:trace日志调用链中的调用次序采用xx.xx.xx.xx字符串模式;

### 1.6.7.3 [20170118 duanyy]
- alogic-common:新增com.alogic.tlog，替代com.alogic.tracer.log包;
- alogic-common:trace日志的时长单位改为ns;
- alogic-common:对tlog的开启开关进行了统一;

### 1.6.7.4 [20170118 duanyy]
- alogic-common:增加发送指标所需的xscript插件;
- alogic-common:淘汰com.anysoft.metrics包;
- alogic-core:服务耗时统计修改为ns;

### 1.6.7.5 [20170119 duanyy]
- alogic-common:JsonTools允许输入的json为空;
- alogic-vfs:增加VFS相关的XScript插件；

### 1.6.7.6 [20170125 duanyy] 
- alogic-common:Batch框架可以装入额外的CLASSPATH;

### 1.6.7.7 [20170126 duanyy]
- alogic-common:Properties增加loadFrom系列方法，用于从Json对象，Element节点，Element属性列表中装入变量列表
- alogic-core:删除缺省的log4j.properties配置
- alogic-common:xscript插件Set增加缺省值和引用模式

### 1.6.7.8 [20170128 duanyy]
- alogic-vfs:增加文件内容读取，保存，文件删除等xscript插件
- alogic-vfs:增加terminal框架，提供local和ssh两种实现

### 1.6.7.9 [20170201 duanyy]
- 采用SLF4j日志框架输出日志

### 1.6.7.10 [20170202 duanyy] 
- 修正tlog作为logger输出时的缓冲区并发问题

### 1.6.7.11 [20170202 duanyy] 
- alogic-vfs:SSH实现支持密码加密

### 1.6.7.12 [20170204 duanyy] 
- alogic-core:增加bizlog的handler

### 1.6.7.13 [20170206 duanyy]
- alogic-vfs:写文件接口增加permissions参数，以便在创建文件时指定文件的权限

### 1.6.7.14 [20170210 duanyy]
- alogic-vfs:修正文件比较和文件同步时信息填入问题
- alogic-vfs:支持多来源的check和sync操作

### 1.6.7.15 [20170216 duanyy]
- alogic-rpc:增加rpc框架
- alogic-core:为部分Message增加Content-Length字段，以便支持keep-alive
- alogic-core:增加bizlog.enable环境变量，以便关闭bizlog
- alogic-core:增加acm.enable环境变量，以便关闭ac控制器

