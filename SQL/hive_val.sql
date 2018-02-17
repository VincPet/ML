DROP TABLE IF EXISTS exchanges_validation ;
DROP TABLE IF EXISTS exchanges_raw_validation;

-- RAW data table and load 

CREATE TABLE exchanges_raw_validation(
exchange_date timestamp,
 address string
)
ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
WITH SERDEPROPERTIES (
   "separatorChar" = "\t",
   "quoteChar"     = "\""
)  
tblproperties("skip.header.line.count"="1"); 


LOAD DATA LOCAL INPATH '/home/student/share/ml/raw-1g/validation/'
OVERWRITE INTO TABLE exchanges_raw_validation;

-- Partition RAW data in order to make next opartions faster


SET hive.exec.dynamic.partition = true;
SET hive.exec.dynamic.partition.mode = nonstrict;
SET hive.exec.max.dynamic.partitions.pernode = 800;

CREATE TABLE exchanges_validation(
 id int,
 times int
)
PARTITIONED BY(exchange_date_part date)
CLUSTERED BY(id) SORTED BY(times) INTO 32 BUCKETS
row format delimited fields terminated by ','; 


DROP VIEW IF EXISTS exchanges_with_addr_validation;

CREATE VIEW exchanges_with_addr_validation AS 
SELECT 
DENSE_RANK() OVER (ORDER BY address ASC) as id,
TO_DATE(exchange_date) as exchange_date_part
, address
FROM exchanges_raw_validation;


FROM
exchanges_with_addr_validation
INSERT OVERWRITE TABLE
exchanges_validation
PARTITION ( exchange_date_part)
SELECT id, SUM(1) as times, 
exchange_date_part
GROUP BY id,exchange_date_part ;



-- Search for top n adresses


DROP VIEW IF EXISTS top_ids_exchanges_validation;

CREATE VIEW top_ids_exchanges_validation AS 
SELECT top_rank, exchange_date_part, times
FROM top_ids_ranked T 
LEFT JOIN exchanges_validation E ON T.id=E.id ;

DROP TABLE IF EXISTS top_n_validation;

CREATE TABLE top_n_validation (
top_rank int,
exchange_date_part date,
times int,
max_times int
);

FROM
top_ids_exchanges_validation
INSERT OVERWRITE TABLE
top_n_validation
SELECT top_rank, exchange_date_part, times, MAX(times) OVER (PARTITION BY top_rank) ORDER BY exchange_date_part, top_rank;

insert overwrite local directory '/home/student/share/ml/clean-1g/validation' row format delimited fields terminated by ','  
SELECT v.top_rank, v.exchange_date_part, v.times/t.max_times
FROM top_n_validation v join top_n t on v.top_rank=t.top_rank and v.exchange_date_part=t.exchange_date_part;