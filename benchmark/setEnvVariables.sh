export MODE=constant-rate;
export DEPLOYMENT_TYPE=local;
export KAFKA_BOOTSTRAP_SERVERS=$(hostname -I | head -n1 | awk '{print $1;}'):9092;
export ZOOKEEPER_SERVER=$(hostname -I | head -n1 | awk '{print $1;}'):2181