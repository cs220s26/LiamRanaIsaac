#!/bin/bash

# Command starts with the type then list out the 7 params

TYPE=$1
TITLE=$2

# Get the ID
ID=$(redis-cli INCR media:counter)

if [ "$TYPE" == "movie" ]; then
     # the boolean is if the value is numeric or not (helpful for the loop)
     params=(
        "rating"   "$3" "true"
        "genre"    "$4" "false"
        "platform" "$5" "false"
        "release"  "$6" "true"
        "director" "$7" "false"
        "runtime"  "$8" "true"
    )
    redis-cli SADD "watchlist:movie" "$ID"

elif [ "$TYPE" == "show" ]; then
    params=(
        "rating"   "$3" "true"
        "genre"    "$4" "false"
        "platform" "$5" "false"
        "start"    "$6" "true"
        "end"      "$7" "true"
        "seasons"  "$8" "true"
    )
    redis-cli SADD "watchlist:show" "$ID"
fi

redis-cli HMSET "media:$ID" title "$TITLE" type "$TYPE"

for (( i=0; i<${#params[@]}; i+=3 )); do
    FIELD=${params[$i]}
    VALUE=${params[$i+1]}
    IS_NUM=${params[$i+2]}

    redis-cli HSET "media:$ID" "$FIELD" "$VALUE"

    if [ "$IS_NUM" == "true" ]; then
        if [[ "$VALUE" =~ ^[0-9]+$ ]]; then
           redis-cli ZADD "has:$FIELD" "$VALUE" "$ID"
        else
           redis-cli ZADD "has:$FIELD" "-1" "$ID"
        fi
    else
        LOWER_VAL=$(echo "$VALUE" | tr '[:upper:]' '[:lower:]')
        redis-cli SADD "has:$FIELD:$LOWER_VAL" "$ID"
    fi
done

redis-cli LPUSH "watchlist" "$ID"

echo "$TITLE added as media:$ID"
