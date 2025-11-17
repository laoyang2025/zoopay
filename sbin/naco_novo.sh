#!/bin/bash

tn=$(ps -ef | grep CfNg | grep 8849 | grep -v grep | awk '{ print $2; }');
if [ x$tn = "x" ]; then
   echo "create tunnel...";
else
   kill $tn;
   echo "recreate tunnel...";
fi

ssh -CfNg -L8849:127.0.0.1:8848 novo;


open "http://127.0.0.1:8849/nacos";



