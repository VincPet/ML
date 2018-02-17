CREATE VIEW minutes_view AS
 SELECT btc_price.data AS ex_date,
    btc_price."Close" AS close_price
   FROM btc_price;
   
 CREATE VIEW minutes_view_perc AS
  SELECT minutes_view.ex_date,
    minutes_view.close_price,
    ( minutes_view.close_price*1.0) / (lag(minutes_view.close_price)OVER (ORDER BY minutes_view.ex_date)*1.0) -1.0  AS perc
   FROM minutes_view;


 COPY (SELECT * FROM minutes_view_perc where ex_date>'2016-01-01') TO 'C:/Users/Universita/btc_value_min.csv' DELIMITER ',';