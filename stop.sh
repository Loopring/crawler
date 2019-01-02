#!/bin/sh
#
# Copyright 2018 Loopring All Rights Reserved.
# Author: autumn84
ps -ef | grep "crawler-1.0.0.jar" | grep -v "grep" | awk '{print $2}' | xargs kill
