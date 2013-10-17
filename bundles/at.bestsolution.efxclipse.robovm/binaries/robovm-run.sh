#!/bin/bash

TYPE=$1
NAME=$2

export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.7.0_40.jdk/Contents/Home

if [ "${TYPE}" == "sim-ipad" ] ; then
	echo "Running iPad"
	robovm/bin/robovm -config robovm.xml -verbose -properties robovm.properties -cp robovm/lib/robovm-objc.jar:robovm/lib/robovm-cocoatouch.jar -arch x86 -os ios -ios-sim-family ipad -run $NAME
elif [ "${TYPE}" == "sim-iphone" ] ; then
	echo "Running iphone"
	robovm/bin/robovm -config robovm.xml -verbose -properties robovm.properties -cp robovm/lib/robovm-objc.jar:robovm/lib/robovm-cocoatouch.jar -arch x86 -os ios -ios-sim-family iphone -run $NAME
else
	echo "Running Device"
	robovm/bin/robovm -config robovm.xml -verbose -properties robovm.properties -cp robovm/lib/robovm-objc.jar:robovm/lib/robovm-cocoatouch.jar -arch thumbv7 -os ios -run $NAME
fi