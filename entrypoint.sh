#!/bin/bash

source /opt/bin/functions.sh
export GEOMETRY="$SCREEN_WIDTH""x""$SCREEN_HEIGHT""x""$SCREEN_DEPTH"

exec "$@"
