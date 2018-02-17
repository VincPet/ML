 CREATE VIEW daily_view AS
 SELECT tmp.close_p,
    tmp.day_time
   FROM ( SELECT btc_price."Close" AS close_p,
            date_trunc('day', btc_price.data) AS day_time,
            rank() OVER (PARTITION BY (date_trunc('day', btc_price.data)) ORDER BY btc_price.data DESC) AS ran
           FROM btc_price
          WHERE btc_price.data >= '2017-01-01 00:00:00'::timestamp without time zone) tmp
  WHERE tmp.ran = 1;
  
   CREATE VIEW daily_view_perc AS
   SELECT daily_view.day_time,
    daily_view.close_p,
	CASE
            WHEN
    daily_view.close_p - lag(daily_view.close_p) OVER (ORDER BY daily_view.day_time)  > 0 then true
	else false END AS gone_up 
   FROM daily_view;
   
   COPY (SELECT * FROM daily_view_perc) TO 'C:/Users/Universita/btc_data.csv' DELIMITER ',';