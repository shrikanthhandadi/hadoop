sudo dpkg-reconfigure debconf
# When asked, answer interface=Dialog and priority=low.
	
cd /var/log; sudo tail -F daemon.log sulog user.log auth.log debug kern.log syslog dmesg messages {krb5kdc,kadmin,krb5lib}.log

sudo vi /etc/hosts
#	192.168.1.141	ubuntu1.exp.com ubuntu krb1.exp.com krb1


sudo apt-get install krb5-admin-server krb5-kdc


#Default Kerberos version 5 realm? EXP.COM
#Add locations of default Kerberos servers to /etc/krb5.conf? Yes
#Kerberos servers for your realm: krb1.exp.com 
#Administrative server for your Kerberos realm: krb1.exp.com
#Create the Kerberos KDC configuration automatically? Yes
#//you may get some dialogue here press OK
#Run the Kerberos V5 administration daemon (kadmind)? Yes

sudo krb5_newrealm


# is split into sections; you should search for section "[domain_realm]" (not "[realms]") and append your definitions:

sudo vi /etc/krb5.conf

[logging]
	default = FILE:/var/log/krb5lib.log
	kdc = FILE:/var/log/krb5kdc.log
	admin_server = FILE:/var/log/kadmin.log

[libdefaults]
	default_realm = EXP.COM
	dns_lookup_realm = false
	dns_lookup_kdc = false
	ticket_lifetime = 24h
	renew_lifetime = 7d
	forwardable = true
	kdc_timesync = 0

[realms]
	EXP.COM = {
		kdc = krb1.exp.com
		admin_server = krb1.exp.com
	}
	

sudo vi /etc/krb5kdc/kdc.conf

[kdcdefaults]
        kdc_ports = 750,88
        default_realm = EXP.COM

[realms]
	EXP.COM = {
		database_name = /var/lib/krb5kdc/principal
		admin_keytab = FILE:/etc/krb5kdc/kadm5.keytab
		acl_file = /etc/krb5kdc/kadm5.acl
		key_stash_file = /etc/krb5kdc/stash
		kadmind_port = 749
		kdc_ports = 750,88
		max_life = 10h 0m 0s
		max_renewable_life = 7d 0h 0m 0s
		master_key_type = aes256-cts-hmac-sha1-96
		supported_enctypes = aes256-cts-hmac-sha1-96:normal aes128-cts-hmac-sha1-96:normal arcfour-hmac-md5:normal
		default_principal_flags = +preauth
	}

[logging]
    kdc = FILE:/var/log/krb5kdc.log
	admin_server = FILE:/var/log/kadmin.log
    default = FILE:/var/log/krb5lib.log
	
	
sudo vi /etc/krb5kdc/kadm5.acl
#uncomment below line
*/admin *

sudo invoke-rc.d krb5-admin-server restart
sudo invoke-rc.d krb5-kdc restart


#add Kerberos policies
sudo kadmin.local

	add_policy -minlength 1 -minclasses 1 admin
	add_policy -minlength 1 -minclasses 1 host
	add_policy -minlength 1 -minclasses 1 service
	add_policy -minlength 1 -minclasses 1 user
	
	addprinc -policy service -randkey host/ubuntu1.exp.com
	ktadd -k /etc/krb5.keytab -norandkey host/ubuntu1.exp.com
	
	addprinc -policy admin kadmin1/admin
	addprinc -policy user kuser1

	

-------------------------- client installs -------------------------------------------------

#copy from server below file on same location in client
#/etc/krb5.conf
scp shrikanth@ubuntu1.exp.com:/tmp/krb5.conf .


#install package
sudo apt-get install krb5-user openssh-server krb5-config

#Edit /etc/ssh/sshd_config and enable the following lines
sudo vi /etc/ssh/sshd_config
GSSAPIAuthentication yes
GSSAPICleanupCredentials yes

#Edit /etc/ssh/ssh_config and enable the following lines
sudo vi /etc/ssh/ssh_config
GSSAPIAuthentication yes
GSSAPIDelegateCredentials yes

#Note: authentication first try system & if fails fallbacks to kerberos. You can set the fail always by changig the password to *K* literal
sudo usermod -p '*K*' <username>




--------------------------- cleint installs for PAM (not needed)---------------------------------

#Installing kerberized clients
sudo apt-get install krb5-clients

#PAM configuration
sudo apt-get install libpam-krb5




//aes256-cts:normal arcfour-hmac:normal des3-hmac-sha1:normal des-cbc-crc:normal des:normal des:v4 des:norealm des:onlyrealm des:afs3















