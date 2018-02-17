DROP TABLE IF EXISTS exchanges ;
DROP TABLE IF EXISTS exchanges_raw;


-- RAW data table and load 

CREATE TABLE exchanges_raw(
exchange_date timestamp,
 address string
)
ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
WITH SERDEPROPERTIES (
   "separatorChar" = "\t",
   "quoteChar"     = "\""
)  
tblproperties("skip.header.line.count"="1"); 


LOAD DATA LOCAL INPATH '/home/student/share/ml/raw-1g/training/'
OVERWRITE INTO TABLE exchanges_raw;

-- Partition RAW data in order to make next opartions faster


SET hive.exec.dynamic.partition = true;
SET hive.exec.dynamic.partition.mode = nonstrict;
SET hive.exec.max.dynamic.partitions.pernode = 800;

CREATE TABLE exchanges(
 id int,
 times int
)
PARTITIONED BY(exchange_date_part date)
CLUSTERED BY(id) SORTED BY(times) INTO 32 BUCKETS
row format delimited fields terminated by ','; 


DROP VIEW IF EXISTS exchanges_with_addr;

CREATE VIEW exchanges_with_addr AS 
SELECT 
DENSE_RANK() OVER (ORDER BY address ASC) as id,
TO_DATE(exchange_date) as exchange_date_part
, address
FROM exchanges_raw;


FROM
exchanges_with_addr
INSERT OVERWRITE TABLE
exchanges
PARTITION ( exchange_date_part)
SELECT id, SUM(1) as times, 
exchange_date_part
GROUP BY id,exchange_date_part ;



-- Search for top n adresses
DROP VIEW IF EXISTS top_ids;

CREATE VIEW top_ids AS
SELECT id,
 SUM(times) as total
 FROM exchanges 
GROUP BY id;

DROP VIEW IF EXISTS top_ids_ranked;

CREATE VIEW top_ids_ranked AS
SELECT
DENSE_RANK() OVER(ORDER BY total DESC, id DESC) as top_rank,
id
FROM top_ids t
ORDER BY top_rank ASC
LIMIT 16384;

DROP VIEW IF EXISTS top_ids_exchanges;

CREATE VIEW top_ids_exchanges AS 
SELECT top_rank, exchange_date_part, times
FROM top_ids_ranked T 
LEFT JOIN exchanges E ON T.id=E.id ;

DROP TABLE IF EXISTS top_n;

CREATE TABLE top_n (
top_rank int,
exchange_date_part date,
times int,
max_times int
);

FROM
top_ids_exchanges
INSERT OVERWRITE TABLE
top_n
SELECT top_rank, exchange_date_part, times, MAX(times) OVER (PARTITION BY top_rank) ORDER BY exchange_date_part, top_rank;

insert overwrite local directory '/home/student/share/ml/clean-1g/training' row format delimited fields terminated by ','  
SELECT top_rank, exchange_date_part, times/max_times
FROM top_n;