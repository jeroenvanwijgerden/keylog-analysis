#!/bin/bash

DEVICE_IDS=$(xinput list | grep -Po 'LITEON Technology USB Multimedia Keyboard.*id=\K\d+')

if [ $# -eq 0 ]; then
    CURRENT_DATE=$(date +'%Y%m%d-%H%M%S')
    OUTPUT_FILE="keylog_$CURRENT_DATE.log"
    echo "No output filename provided. Using default filename: $OUTPUT_FILE"
else
    OUTPUT_FILE="$1"
fi

# Function to convert keycodes to key names
keycode_to_keyname() {
  xmodmap -pke | awk -v keycode="$1" '$1 == "keycode" && $2 == keycode {print $4; exit}'
}

while IFS= read -r DEVICE_ID; do
    xinput test "$DEVICE_ID" | while IFS= read -r line; do
        if [[ $line == "key press "* || $line == "key release "* ]]; then
            keycode=$(echo "$line" | awk '{print $NF}')
            keyname=$(keycode_to_keyname "$keycode")
            event_type=$(echo "$line" | awk '{print $2}')
            echo "$(date +%s%4N) $event_type $keyname" >> "$OUTPUT_FILE"
        fi
    done
done <<< "$DEVICE_IDS"
