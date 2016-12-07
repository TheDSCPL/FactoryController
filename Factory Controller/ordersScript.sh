#!/bin/sh

cd "C:\Users\luisp\Google Drive\MIEEC\4º ano\II\FactoryController\Factory Controller"

clear

#kill $(ps aux | grep "/Users/Alex/Documents/Alexandre/FEUP/4 Ano/II/PL/SFS.0/sfs.jar" | grep -v 'grep' | awk '{print $2}')

ps -W | awk '/javaw.exe/,NF=1' | xargs kill -f

#sleep 1

javaw -jar "C:\Users\luisp\Google Drive\MIEEC\4º ano\II\FactoryController\sfs-1.1.3_plant-2016_1.0\SFS.jar" > /dev/null &

disown

#open /Users/Alex/Documents/Alexandre/FEUP/4\ Ano/II/PL/SFS.0/sfs.jar

sleep 2

#java -jar /Users/Alex/Documents/NetBeans\ Projects/FactoryController/Factory\ Controller/dist/Factory_Controller.jar

#sleep 2

read -p "Press any key to start sending orders..." -n1 -s

echo sending :T 000 2 7 02
echo -n ":T0002702" | nc -u -w 1 127.0.0.1 54321

echo sending :T 000 1 6 03
echo -n ":T0011603" | nc -u -w 1 127.0.0.1 54321

echo sending :T 000 4 7 03
echo -n ":T0024703" | nc -u -w 1 127.0.0.1 54321

echo sending :T 000 3 5 02
echo -n ":T0033502" | nc -u -w 1 127.0.0.1 54321

echo sending :U 001 3 2 01
echo -n ":U0043201" | nc -u -w 1 127.0.0.1 54321

echo sending :U 002 7 2 01
echo -n ":U0057201" | nc -u -w 1 127.0.0.1 54321

echo sending :T 000 2 1 10
echo -n ":T0062110" | nc -u -w 1 127.0.0.1 54321

echo sending :T 001 3 4 06
echo -n ":T0073406" | nc -u -w 1 127.0.0.1 54321

echo sending :T 002 4 6 08
echo -n ":T0084608" | nc -u -w 1 127.0.0.1 54321

echo sending :T 003 1 4 12
echo -n ":T0091412" | nc -u -w 1 127.0.0.1 54321

echo sending :T 004 3 6 05
echo -n ":T0103605" | nc -u -w 1 127.0.0.1 54321

echo sending :T 004 2 6 05
echo -n ":T0103605" | nc -u -w 1 127.0.0.1 54321

#sleep 20

echo DONE!!

#read -p "Press any key to leave..." -n1 -s

exit