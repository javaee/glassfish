das_address=$1
das_port=$2
das_domain=$3

${das_port:-"8080"}
${das_domain:-"domain1"}

echo "Install supplemental software"
sudo apt-get -y -q update
sudo apt-get -y -q upgrade
sudo apt-get -y -q install wget
sudo apt-get -y -q install sun-java6-jdk
sudo apt-get -y -q install unzip
sudo apt-get -y -q install openssh-server
sudo apt-get clean

echo "Installing GlassFish"
cd /opt
sudo mkdir glassfishvm
sudo chmod a+rwx glassfishvm
cd glassfishvm
wget http://$das_address:$das_port/glassfish.zip
unzip glassfish.zip
rm glassfish.zip

echo "Installing vitualization code"
wget http://$das_address:$das_port/vmcluster.jar
mv vmcluster.jar glassfish3/glassfish/modules

#echo "Installing Network reset script"
#mkdir bin
#cd bin
#wget http://$das_address:$das_port/ubuntu/initial-setup.sh
#chmod a+x initial-setup.sh

echo "Installing GlassFish as a startup service"
cd /etc/init.d
#sudo wget http://$das_address:$das_port/ubuntu/glassfish
sudo wget http://$das_address:$das_port/ubuntu/initial-setup.sh
sudo mv initial-setup.sh glassfish
sudo chmod a+x glassfish
sudo update-rc.d glassfish defaults

