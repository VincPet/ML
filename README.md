## Machine Learning Project Readme

Vincenzo Petito

I provided two codes because for the first code I developed I was assuming something that finally was not true and stopped me from using it. So, in order to provide a code that does some training and predictions I developed a second project trying to reuse what I already did.

### Requirements

In order to execute the codes you will need to install *Keras* nd *Tensorflow* with all their dependencies):

*pip install TensorFlow*

*pip install Keras*

I used *TensorFlow* 1.4 and *Keras* 2.1.3 in a *Conda* environment. If using *Conda*, it is better to install libraries using the command line: using *Conda* GUI, it was only able to install *TensorFlow* 1.2 which was too old.

### Code description

#### ML1

The code "ML1" is the code relative to the project I submitted on aulaweb as project proposal.

What i wanted to do was to find addresses in the blockchain which could be used as indicators of the bitcoin price future trend.

In order to do this, I downloaded a dataset containing bitcoin prices from [here](https://www.kaggle.com/mczielinski/bitcoin-historical-data/data) (*bitstampUSD_1-min_data_2012-01-01_to_2018-01-08.csv*) , and the entire blockchain using [Bitcoin Core](https://bitcoin.org/it/scarica). 

Then I developed a java code (folder *javaBlockchainParser*) to extract the informations i needed from the blockchain: foreach block I extracted all the addresses that made transactions and saved them in some CSVs (one csv file every 10 millions transaction). I used  [bitcoinj ](https://github.com/bitcoinj/bitcoinj)and [OpenCSV 4.1](https://mvnrepository.com/artifact/com.opencsv/opencsv/4.1) libraries.

Finally imported the CSVs in Hive, where I executed some preprocessing of the data (available in SQL/queries.sql).

More precisely  grouped addresses by dates, summing up the number of transactions they made in each day. There were too much addresses to manage for the machine learning part and in order to cut their number down I selected the 16384 addresses that made more transactions in the period from 01-07-17 to 15-08-17 which was the training date interval, and extracted the transaction of those addresses from the interval that goes from 16-08-17 to 15-09-17 as the test set. 

That’s when I found out that it was not going to work: I found only one day were some of the top addresses executed some transactions and nothing else. It was not enough so i had to stop the project without being able to execute predictions on the test set.

The code is not "refined", I stopped working on it as soon as I understood that it was useless. I’m including it just because I’ve spent some time on it.

#### ML2

In order to be able to do some predictions and evaluate a model, I tried to solve a simpler problem which was to train a network to predict the bitcoin trend in the next 15 minutes, given the past 300 minutes (1 minute ticks). 

I used the same dataset containing bitcoin values in 1 min ticks as before. I choose to use a 1D Convolutional Neural Network because the input data has some "structural" information: adjacent candles have some sort of correlation.

### How to use

#### ML1

Download the needed preprocessed datasets (uploaded on github) and change the path in the Python code (line 3) to match the folder where the dataset are stored. 

If you don’t want to rely on the preprocessed datasets, than you’ll have to:

1. Download the blockchain

2. Use javaBlockchainParser 

    * In SimpleDailyTxCount the two paths need to be updated:

        *  PREFIX is the path where the blockchain is stored

        * tmpPath is a temporary path where files will be copied, analized and deleted

    * In BlocksWriter, csvPath needs to be updated as well, is the output path

3. Use Hive

    * Using the just obtained CSVs, specifying their path in SQL\hive.sql (line 19)

    * Update output path in line 100

    * Execute all the queries

    * Rename the obtained csv to "training.csv"

4. Use Kettle - Hitachi

    * Open the transformation ml.ktr

    * Edit the first input phase, set the path to the dataset containing bitcoin value (first time we use it now)

    * Edit the Output table phase, add new connection to Postgres and set it up with user and password

    * Open the downloaded dataset with a text editor search & replace "e" with “E”

    * Run the transformation: it will load data to Postgres

5. Use Postgres

    * We need to transform the data from minute ticks to day ticks

    * Update in SQL\ML1 output path in line 20 

    * Run SQL\ML1.sql

6. Use Python - Keras

    * Specify the path where "training.csv" and “btc_data.csv” are stored (must be in the same folder) in line 3

    * Execute python code 

#### ML2

Download the needed preprocessed dataset (uploaded on github) and change the path in the Python code (line 3) to match the folder where the dataset is stored. 

If you don’t want to rely on the preprocessed datasets, than you’ll have to:

* Execute num 4 of ML1

* Use Postgress

    * Update in SQL\ML2 output path in line 14

    * Run SQL\ML2.sql

* Use Python - Keras

    * Specify the path where "btc_value_min.csv" is stored in line 3

    * Execute python code 

