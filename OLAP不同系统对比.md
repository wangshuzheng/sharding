# OLAP不同系统对比

MPP架构的系统（Presto/Impala/SparkSQL/Drill等）有很好的数据量和灵活性支持，但是对响应时间是没有保证的。**当数据量和计算复杂度增加后，响应时间会变慢**，从秒级到分钟级，甚至小时级都有可能。

MPP架构的系统（Presto/Impala/SparkSQL/Drill等）有很好的数据量和灵活性支持，但是对响应时间是没有保证的。**当数据量和计算复杂度增加后，响应时间会变慢**，从秒级到分钟级，甚至小时级都有可能。

> MPP即大规模并行处理（Massively Parallel Processor ）。 在数据库非共享集群中，每个节点都有独立的磁盘存储系统和内存系统，业务数据根据数据库模型和应用特点划分到各个节点上，每台数据节点通过专用网络或者商业通用网络互相连接，彼此协同计算，作为整体提供数据 库服务。非共享数据库集群有完全的可伸缩性、高可用、高性能、优秀的性价比、资源共享等优势。

缺点：性能不稳定

搜索引擎架构的系统（Elasticsearch等）相对比MPP系统，在入库时将数据转换为倒排索引，采用Scatter-Gather计算模型，牺牲了灵活性换取很好的性能，在搜索类查询上能做到亚秒级响应。**但是对于扫描聚合为主的查询，随着处理数据量的增加**，响应时间也会退化到分钟级。
缺点：性能不稳定

预计算系统（Druid/Kylin等）则在入库时对数据进行预聚合，进一步牺牲灵活性换取性能，以实现对超大数据集的秒级响应。
缺点：不太灵活

MPP和搜索引擎系统无法满足超大数据集下的性能要求，因此很自然地会考虑预计算系统。而Druid主要面向的是实时Timeseries数据，我们虽然也有类似的场景，但主流的分析还是面向数仓中按天生产的结构化表，因此Kylin的MOLAP Cube方案是最适合作为大数据量时候的引擎。

## **ImPala**

![img](https://pic3.zhimg.com/80/v2-01195139f28a6556aca10838f4e051ca_hd.jpg)



## **Druid**

![img](https://pic2.zhimg.com/80/v2-ac109cb17d281bc562d8ec99e9eca42d_hd.jpg)

Druid是广告分析公司Metamarkets开发的一个用于大数据实时查询和分析的分布式实时处理系统，主要用于广告分析，互联网广告系统监控、度量和网络监控。

特点：

1.	快速的交互式查询——Druid的低延迟数据摄取架构允许事件在它们创建后毫秒内可被查询到。

2.	高可用性——Druid的数据在系统更新时依然可用，规模的扩大和缩小都不会造成数据丢失；

3.	可扩展——Druid已实现每天能够处理数十亿事件和TB级数据。

4.	为分析而设计——Druid是为OLAP工作流的探索性分析而构建，它支持各种过滤、聚合和查询。

应用场景：

1.	需要实时查询分析时；

2.	具有大量数据时，如每天数亿事件的新增、每天数10T数据的增加；

3.	需要一个高可用、高容错、高性能数据库时。

4.	需要交互式聚合和快速探究大量数据时

架构图：

Druid官网 [Druid | About Druid](https://link.zhihu.com/?target=http%3A//druid.io/druid.html)

Druid：一个用于大数据实时处理的开源分布式系统



## **Presto**

![img](https://pic4.zhimg.com/80/v2-53beb643399eb77385ed17bdef8106eb_hd.jpg)

Presto是Facebook开发的分布式大数据SQL查询引擎，专门进行快速数据分析。

特点：

1.	可以将多个数据源的数据进行合并，可以跨越整个组织进行分析。

2.	直接从HDFS读取数据，在使用前不需要大量的ETL操作。

查询原理：

1.	完全基于内存的并行计算

2.	流水线

3.	本地化计算

4.	动态编译执行计划

5.	小心使用内存和数据结构

6.	类BlinkDB的近似查询

7.	GC控制





Kylin



![img](https://pic1.zhimg.com/80/v2-d352662b79b771a5ee2c91d4c9a66db0_hd.jpg)



Apache Kylin最初由eBay开发并贡献至开源社区的分布式分析引擎，提供

Hadoop之上的SQL查询接口及多维分析（OLAP）能力以支持超大规模数据。

特点:

1.	用户为百亿以上数据集定义数据模型并构建立方体

2.	亚秒级的查询速度，同时支持高并发

3.	为Hadoop提供标准SQL支持大部分查询功能

4.	提供与BI工具，如Tableau的整合能力

5.	友好的web界面以管理，监控和使用立方体

6.	项目及立方体级别的访问控制安全



商业系统

- InfoBright
- Greenplum（已开源）、HP Vertica、TeraData、Palo、ExaData、RedShift、BigQuery（Dremel）

开源实现

- Impala、Presto、Spark SQL、Drill、Hawq
- Druid、Pinot
- Kylin

大数据查询目前来讲可以大体分为三类：

1.基于hbase预聚合的，比如Opentsdb,Kylin,Druid等,需要指定预聚合的指标，在数据接入的时候根据指定的指标进行聚合运算，适合相对固定的业务报表类需求，只需要统计少量维度即可满足业务报表需求

2.基于Parquet列式存储的，比如Presto, Drill，Impala等，基本是完全基于内存的并行计算，Parquet系能降低存储空间，提高IO效率，以离线处理为主，很难提高数据写的实时性，超大表的join支持可能不够好。spark sql也算类似，但它在内存不足时可以spill disk来支持超大数据查询和join

3.基于lucene外部索引的，比如ElasticSearch和Solr,能够满足的的查询场景远多于传统的数据库存储，但对于日志、行为类时序数据，所有的搜索请求都也必须搜索所有的分片，另外，对于聚合分析场景的支持也是软肋

https://www.zhihu.com/question/41541395

##### 美团数据（OLAP）对比

https://www.infoq.cn/article/kylin-apache-in-meituan-olap-scenarios-practice/



#### Amazon RedShift

[https://aws.amazon.com/cn/redshift/pricing/?sc_channel=PS&sc_campaign=acquisition_CN&sc_publisher=search360&sc_medium=redshift_b&sc_content=redshift%5Fe&sc_detail=redshift&sc_category=pc&sc_segment=200011910&sc_matchtype=phrase&sc_country=CN](https://aws.amazon.com/cn/redshift/pricing/?sc_channel=PS&sc_campaign=acquisition_CN&sc_publisher=search360&sc_medium=redshift_b&sc_content=redshift_e&sc_detail=redshift&sc_category=pc&sc_segment=200011910&sc_matchtype=phrase&sc_country=CN)



#### Analytic-DB(Alibaba)

https://www.alibabacloud.com/zh/product/analytic-db#product-details



#### Ali HiStore(怀疑已停用)

https://yq.aliyun.com/articles/162954