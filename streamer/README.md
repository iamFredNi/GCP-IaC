# Streamer

## Commande pour kafka 

```bash
wget https://downloads.apache.org/kafka/3.8.0/kafka_2.13-3.8.0.tgz

tar -xvzf kafka_2.13-3.8.0.tgz

cd kafka_2.13-3.8.0

pip install kafka-python


/usr/local/kafka_2.13-3.8.0/bin/kafka-storage.sh format -t test -c /usr/local/kafka_2.13-3.8.0/config/kraft/server.properties
sudo /usr/local/kafka_2.13-3.8.0/bin/kafka-server-start.sh /usr/local/kafka_2.13-3.8.0/config/kraft/server.properties

./bin/connect-standalone.sh config/connect-standalone.properties config/hdfs-sink-connector.properties

#Créer topic 
/usr/local/kafka_2.13-3.8.0/bin/kafka-topics.sh --create --topic coinbase --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

/usr/local/kafka_2.13-3.8.0/bin/kafka-topics.sh --create --topic gdelt --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```
