#!/bin/bash
TESTHOME=`pwd`
GFHOME=$TESTHOME/glassfish5
export INSTALL_LOC=$HOME/testnode
export DOMAIN=domain1
echo "DOMAIN is set to $DOMAIN. Reset this if working with another domain."
PATH=$GFHOME/bin:$PATH
GFBUILDDIR=/net/bat.sfbay.sun.com/repine/export2/hudson/jobs/gf-trunk-build-continuous/lastSuccessful
TIMEFORMAT="real: %E"
rm -rf $TESTHOME/logs
mkdir $TESTHOME/logs

start_instances() {
  echo Starting instances...
  asadmin --terse list-instances | egrep 'no response|requires restart|not running' | awk '{print $1}' |
    (time xargs -t -n 1 -P 0 asadmin --terse start-instance)
}

stop_instances() {
  echo Stopping instances...
  asadmin --terse list-instances | grep running | grep -v 'not running' | awk '{print $1}' |
    (time xargs -t -n 1 -P 0 asadmin --terse stop-instance)
}

ping_instances() {
  id=$1
  url=$2
  asadmin get '*' |
    url=$url awk -F= '$1 ~ /nodes.node..*\.node-host/ { host[$1] = $2 }
             $1 ~ /servers.server..*\.node/ { node[$1] = $2 }
             $1 ~ /configs.config..*.system-property.HTTP_LISTENER_PORT.value/ { cport[$1] = $2 }
             $1 ~ /servers.server..*.system-property.HTTP_LISTENER_PORT.value/ { port[$1] = $2 }
             $1 ~ /servers\.server\.[^.]*\.name$/ { iname[$2] = $2 }
             $1 ~ /servers\.server\.[^.]*\.config-ref$/ { cname[$1] = $2 }
             END { 
               ck = "configs.config.ch1-config.system-property.HTTP_LISTENER_PORT.value";
               for (i in iname) { 
                 if (i == "server") continue;
                 if (cname["servers.server." i ".config-ref"] != "ch1-config") continue;
                 k = "servers.server." i ".system-property.HTTP_LISTENER_PORT.value";
                 if (port[k] == "") port[k] = cport[ck];
                 nk = "servers.server." i ".node";
                 hk = "nodes.node." node[nk] ".node-host";
                 printf "http://%s:%s/%s\n", host[hk], port[k], ENVIRON["url"]
               }
             }' |
     xargs -t -n 1 -P 0 wget -q --no-proxy -P $TESTHOME/logs/$id -x 
}

bench() {
  cmd=$*
  key=$1
  echo -n $key ": "
  (time asadmin $cmd > /dev/null) 2>&1 || echo "<-- FAILURE"
}

benchmark_commands() {
  bench list-instances
  bench list-clusters
  bench create-cluster cx
  bench delete-cluster cx
  bench create-instance --node localhost-$DOMAIN ix
  bench start-instance ix
  bench stop-instance ix
  bench delete-instance ix
  bench deploy --name ax $TESTHOME/apps/helloworld.war
  bench undeploy ax
  bench list-applications
  bench stop-domain $DOMAIN
  bench start-domain $DOMAIN
  bench restart-domain $DOMAIN
  grep 'time to parse domain.xml' $GFHOME/glassfish/domains/$DOMAIN/logs/server.log | 
    sed -e 's/^.*Total //' -e 's/|#]//' | tail -1
  echo 'size of domain.xml: ' `ls -l $GFHOME/glassfish/domains/$DOMAIN/config/domain.xml | awk '{ print $5 }'`
  PIDFILE=$TESTHOME/glassfish5/glassfish/domains/domain1/config/pid
  # wait for pid file to be there, as restart-domain returns before it is actually there
  while [ ! -f $PIDFILE ]; do sleep 1; done
  daspid=`cat $PIDFILE`
  echo 'size of DAS process: ' `ps -o vsz= -p $daspid` ' KB'
}

benchmark_deploy() {
  sz=$1
  echo "deploying $sz app to stopped cluster"
  time asadmin deploy --target ch1 --name hello1 $TESTHOME/apps/helloworld-${sz}.war
  echo "starting instances"
  time asadmin start-cluster ch1
  benchmark_commands 
  echo "undeploying app from running cluster"
  time asadmin undeploy --target ch1 hello1
  echo "deploying $sz app to running cluster"
  time asadmin deploy --target ch1 --name hello1 $TESTHOME/apps/helloworld-${sz}.war
  echo "stopping instances"
  time asadmin stop-cluster ch1
  echo "undeploying app from stopped cluster"
  time asadmin undeploy --target ch1 hello1
  # one more start/stop to get the instances sync'd again
  time asadmin start-cluster ch1
  time asadmin stop-cluster ch1
}

create_nodes_and_clusters() {
  cnum=$1
  inum=$2
  echo "Creating $cnum clusters with $inum instances per cluster (one instance per node)"
  c=0
  while [ "$c" -lt "$cnum" ]
  do
    c=`expr $c + 1`
    echo "create-cluster cn$c"
    i=0
    while [ "$i" -lt "$inum" ]
    do
      i=`expr $i + 1`
      echo "create-node-config --nodehost h${c}_${i} n${c}_${i}"
      echo "create-instance --cluster c$c --node n${c}_${i} i${c}_${i}"
    done
  done | asadmin 
}

create_hosted_clusters() {
  cnum=$1
  inum=$2
  nnum=$3
  echo "Creating $cnum clusters with $inum instances on each of $nnum nodes"
  c=0
  while [ "$c" -lt "$cnum" ]
  do
    c=`expr $c + 1`
    echo "create-cluster ch$c"
    # turn off command replication while creating instances
    #echo "set configs.config.ch$c-config.dynamic-reconfiguration-enabled=false"
    n=0
    while [ "$n" -lt "$nnum" ]
    do
      n=`expr $n + 1`
      i=0
      while [ "$i" -lt "$inum" ]
      do
        i=`expr $i + 1`
        echo "create-instance --cluster ch$c --node n-ssh-${DOMAIN}-${n} i${c}-${n}-${i}"
      done
    done
    #echo "set configs.config.ch$c-config.dynamic-reconfiguration-enabled=true"
  done | asadmin 
}

create_local_clusters() {
  cnum=$1
  inum=$2
  echo "Creating $cnum clusters with $inum local instances per cluster"
  c=0
  while [ "$c" -lt "$cnum" ]
  do
    c=`expr $c + 1`
    echo "create-cluster cl$c"
    i=0
    while [ "$i" -lt "$inum" ]
    do
      i=`expr $i + 1`
      pb=`expr 20000 + $c \* $inum \* 10 + $i \* 10`
      HLP=$pb
      HSLP=`expr $pb + 1`
      ISLP=`expr $pb + 2`
      ILP=`expr $pb + 3`
      JSCP=`expr $pb + 4`
      ISMP=`expr $pb + 5`
      JPP=`expr $pb + 6`
      ALP=`expr $pb + 7`
      echo "create-local-instance --checkports=false --cluster cl$c --systemproperties HTTP_LISTENER_PORT=${HLP}:HTTP_SSL_LISTENER_PORT=${HSLP}:IIOP_SSL_LISTENER_PORT=${ISLP}:IIOP_LISTENER_PORT=${ILP}:JMX_SYSTEM_CONNECTOR_PORT=${JSCP}:IIOP_SSL_MUTUALAUTH_PORT=${ISMP}:JMS_PROVIDER_PORT=${JPP}:ASADMIN_LISTENER_PORT=${ALP} i${c}_${i}"
    done
  done | asadmin || return 1
}

create_hosted_nodes() {
  echo Creating SSH nodes from the hosted_nodes file for domain $DOMAIN...
  grep -v '^#' $TESTHOME/hosted-nodes |
    awk '{ printf "create-node-ssh --nodehost %s --installdir '$INSTALL_LOC'/glassfish5 --sshuser %s n-ssh-'$DOMAIN'-%d\n", $2, $1, ++n }' | 
    asadmin
  asadmin list-nodes
}

install_hosted_nodes() {
  echo Installing GlassFish on nodes from hosted_nodes file...
  asadmin install-node --installdir $INSTALL_LOC/glassfish5 `grep -v '^#' hosted-nodes | cut -d" " -f2`
}

cmd_on_hosted_nodes() {
  cmd=$1
  grep -v '^#' $TESTHOME/hosted-nodes |
    awk '{print $1 "@" $2}' |
    xargs -L 1 -i -t ssh -n -T {} $cmd
}

delete_hosted_nodes() {
  echo Deleting SSH nodes...
  asadmin --terse list-nodes | egrep -v '^[ \t]*$' | grep n-ssh- |
    xargs -n 1 echo delete-node-ssh | 
    asadmin || return 1
}

delete_clusters() {
  echo Deleting clusters and instances...
  asadmin --terse list-instances | grep 'not running' | awk '{print $1}' |
    xargs -n 1 echo delete-instance |
    asadmin || return 1
  asadmin --terse list-clusters | grep 'not running' | awk '{print $1}' |
    xargs -n 1 echo delete-cluster |
    asadmin || return 1
}

delete_nodes() {
  echo Deleting nodes...
  asadmin --terse list-nodes | egrep -v '^[ \t]*$' | grep -v localhost |
    xargs -n 1 echo delete-node-config |
    asadmin || return 1
}

deploy_app_to_clusters() {
  name=$1
  file=$2
  echo Deploying an app to all clusters and instances...
  asadmin deploy --name $name $file
  asadmin --terse list-clusters | egrep -v '^[ \t]*$' | awk '{print $1}' |
  while read cname 
  do
    echo "create-application-ref --target $cname $name"
  done | asadmin || return 1
}

