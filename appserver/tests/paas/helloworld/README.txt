This is a simple Hello World application actually picked up from quicklook helloworld cluster test and cloud-enabled to run on a 2 instance GlassFish cluster.
The application has been tested on OVM, KVM and native mode.

Tests Setup commands:
1) Native mode
setup.sh -r native
2) KVM
setup.sh -r -d /srv/kvm/yamini/images -s jee kvm

Test Duration: It takes approx. 20min for a successful run on OVM setup and 10min on KVM.

Please refer ../README.txt for more generic guidelines.
