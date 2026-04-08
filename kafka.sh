#!/usr/bin/env bash

KAFKA_HOME=$(dirname "$0")
CONFIG_DIR="$KAFKA_HOME/config"

# --------- FUNCTIONS ---------
function format_all() {
    echo "🔧 Generating Cluster ID..."
    CLUSTER_ID=$("$KAFKA_HOME/bin/kafka-storage.sh" random-uuid)
    echo "✅ Cluster ID: $CLUSTER_ID"
    echo

    for i in 1 2 3; do
        CONFIG_FILE="$CONFIG_DIR/server-$i.properties"

        if [ ! -f "$CONFIG_FILE" ]; then
            echo "❌ Missing config: $CONFIG_FILE"
            continue
        fi

        echo "📦 Formatting server-$i..."
        "$KAFKA_HOME/bin/kafka-storage.sh" format -t "$CLUSTER_ID" -c "$CONFIG_FILE"
        echo "✅ server-$i formatted"
        echo
    done

    echo "🎉 All servers formatted successfully!"
    echo "✅ You can now start a Kafka process for each server"
}

function start_server() {
    SERVER_NUM=$1
    CONFIG_FILE="$CONFIG_DIR/server-$SERVER_NUM.properties"

    if [ ! -f "$CONFIG_FILE" ]; then
        echo "❌ Config file not found: $CONFIG_FILE"
        exit 1
    fi

    echo "🚀 Starting Kafka server-$SERVER_NUM..."
    bash "$KAFKA_HOME/bin/kafka-server-start.sh" "$CONFIG_FILE"
}

function stop_all_servers() {
    echo "🛑 Stopping all Kafka servers..."
    bash "$KAFKA_HOME/bin/kafka-server-stop.sh"
    kill -9 $(lsof -ti :9093)
    echo "✅ All servers stopped"
}

function list_topics(){
    # bash bin/kafka-topics.sh --list --bootstrap-server localhost:9092
    echo "✅ --- All list topics ---"
    bash "$KAFKA_HOME/bin/kafka-topics.sh" --list --bootstrap-server localhost:9092
}

# --------- SCRIPT LOGIC ---------
case "$1" in
    format)
        format_all
        ;;
    start1)
        start_server 1
        ;;
    start2)
        start_server 2
        ;;
    start3)
        start_server 3
        ;;
    stopall)
        stop_all_servers
        ;;
    listtopic|topics)
        list_topics
        ;;
    *)
        echo "Usage: $0 {format|start1|start2|start3|stopall|listtopic}"
        exit 1
        ;;
esac