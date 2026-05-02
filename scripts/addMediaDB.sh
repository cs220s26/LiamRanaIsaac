#!/bin/bash

# Command starts with the type then list out the 7 params
# params for movie: movie title rating genre platform release director runtime
# params for show: show title rating genre platform start end seasons

if [[ "$1" == "help" ]]; then
  echo "[movie|show] [title] [rating] [genre] [platform] [release|start] [director|end] [runtime|seasons]"
  exit 1
fi

if [[ $# -lt 8 ]]; then
    echo "More parameters required, use help for more information"
    exit 1
fi

if [[ ! "$3" =~ ^[0-9]+(\.[0-9]+)?$ ]] ||
[[ ! "$6" =~ ^[0-9]+(\.[0-9]+)?$ ]] ||
[[ ! "$8" =~ ^[0-9]+(\.[0-9]+)?$ ]]; then
    echo "One of these parameters must be a numeric value: $3, $6, $8"
    exit 1
fi


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
    redis-cli SADD "watchlist:movie" "$ID" > /dev/null

elif [ "$TYPE" == "show" ]; then
    params=(
        "rating"   "$3" "true"
        "genre"    "$4" "false"
        "platform" "$5" "false"
        "start"    "$6" "true"
        "end"      "$7" "present_check"
        "seasons"  "$8" "true"
    )
    redis-cli SADD "watchlist:show" "$ID" > /dev/null
fi

redis-cli HSET "media:$ID" title "$TITLE" type "$TYPE" > /dev/null

for (( i=0; i<${#params[@]}; i+=3 )); do
    FIELD=${params[$i]}
    VALUE=${params[$i+1]}
    IS_NUM=${params[$i+2]}

    redis-cli HSET "media:$ID" "$FIELD" "$VALUE" > /dev/null

    if [ "$IS_NUM" == "true" ]; then
      redis-cli ZADD "has:$FIELD" "$VALUE" "$ID" > /dev/null
    elif [ "$IS_NUM" == "present_check" ]; then
      shopt -s nocasematch
      if [[ "$VALUE" =~ ^[0-9]+(\.[0-9]+)?$ ]]; then
        redis-cli ZADD "has:$FIELD" "$VALUE" "$ID" > /dev/null
      fi
      shopt -u nocasematch

    else
        LOWER_VAL=$(echo "$VALUE" | tr '[:upper:]' '[:lower:]')
        redis-cli SADD "has:$FIELD:$LOWER_VAL" "$ID" > /dev/null
    fi
done

redis-cli LPUSH "watchlist" "$ID" > /dev/null

echo "$TITLE added as media: $ID"
