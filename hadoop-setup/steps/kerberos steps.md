sudo dpkg-reconfigure debconf
# When asked, answer interface=Dialog and priority=low.
	
cd /var/log; sudo tail -F daemon.log sulog user.log auth.log debug kern.log syslog dmesg messages kerberos/{krb5kdc,kadmin,krb5lib}.log

sudo vi /etc/hosts
#	192.168.1.141	ubuntu1.exp.com ubuntu krb1.exp.com krb1


sudo apt-get install krb5-{admin-server,kdc}

#Default Kerberos version 5 realm? EXP.COM
#Add locations of default Kerberos servers to /etc/krb5.conf? Yes
#Kerberos servers for your realm: krb1.exp.com 
#Administrative server for your Kerberos realm: krb1.exp.com
#Create the Kerberos KDC configuration automatically? Yes
#//you may get some dialogue here press OK
#Run the Kerberos V5 administration daemon (kadmind)? Yes

sudo krb5_newrealm

#Enter KDC database master key: PASSWORD
#Re-enter KDC database master key to verify: PASSWORD

# is split into sections; you should search for section "[domain_realm]" (not "[realms]") and append your definitions:
.exp.com = EXP.COM
exp.com = EXP.COM
#and At the bottom of the file, you should add the logging section:
[logging]
	kdc = FILE:/var/log/kerberos/krb5kdc.log
	admin_server = FILE:/var/log/kerberos/kadmin.log
	default = FILE:/var/log/kerberos/krb5lib.log
sudo vi /etc/krb5.conf

#To create the logging directory and set up permissions, run:
sudo mkdir /var/log/kerberos
sudo touch /var/log/kerberos/{krb5kdc,kadmin,krb5lib}.log
sudo chmod -R 750  /var/log/kerberos

#To apply changes to the Kerberos server, run:
sudo invoke-rc.d krb5-admin-server restart
sudo invoke-rc.d krb5-kdc restart

#Initial test
sudo kadmin.local
#kadmin.local:  listprincs
#kadmin.local:  quit

#Access rights
sudo vi /etc/krb5kdc/kadm5.acl
#uncomment below line
*/admin *
sudo invoke-rc.d krb5-admin-server restart

#add Kerberos policies
sudo kadmin.local

add_policy -minlength 6 -minclasses 2 admin
add_policy -minlength 8 -minclasses 2 host
add_policy -minlength 8 -minclasses 2 service
add_policy -minlength 6 -minclasses 2 user


#Creating first privileged principal kadmin1
sudo kadmin.local

addprinc -policy admin kadmin1/admin

#Kadmin test
sudo kadmin -p kadmin1/admin

listprincs


#Creating first unprivileged principal kuser1
sudo kadmin -p kadmin1/admin

addprinc -policy user kuser1


#Obtaining Kerberos ticket
klist -f
kinit kuser1
klist -f


#Installing kerberized services with rsh+kerberos (did not worked skip this rsh step)
#install krb5-rsh-server and ensure that it runs.
sudo apt-get install openbsd-inetd krb5-rsh-server

sudo update-rc.d -f openbsd-inetd remove
sudo update-rc.d openbsd-inetd defaults

sudo update-inetd --enable kshell
sudo update-inetd --enable eklogin

sudo invoke-rc.d openbsd-inetd restart

#add host as principal & export the key to a keytab file as shown (that is, within the same invocation of kadmin) to save yourself from getting an error about the "Key version number" being incorrect:
sudo kadmin -p kadmin1/admin

addprinc -policy service -randkey host/ubuntu1.exp.com
ktadd -k /etc/krb5.keytab -norandkey host/ubuntu1.exp.com

--------------------------- cleint installs --------------------------------------------

Note: authentication first try system & if fails fallbacks to kerberos. You can set the fail always by changig the password to *K* literal
sudo usermod -p '*K*' <username>

#Installing kerberized clients
sudo apt-get install krb5-clients

#PAM configuration
sudo apt-get install libpam-krb5

#edit PAM configuration

#back up
sudo su -
cd /etc
cp -a pam.d pam.d,orig

sudo vi /etc/pam.d/common-account

account [success=1 new_authtok_reqd=done default=ignore]        pam_unix.so
account requisite                       pam_deny.so
account required                        pam_permit.so
account required                        pam_krb5.so minimum_uid=1000

sudo vi /etc/pam.d/common-auth

auth    [success=2 default=ignore]      pam_krb5.so minimum_uid=1000
auth    [success=1 default=ignore]      pam_unix.so nullok_secure try_first_pass
auth    requisite                       pam_deny.so
auth    required                        pam_permit.so

sudo vi /etc/pam.d/common-password

password        [success=2 default=ignore]      pam_krb5.so minimum_uid=1000
password        [success=1 default=ignore]      pam_unix.so obscure use_authtok try_first_pass sha512
password        requisite                       pam_deny.so
password        required                        pam_permit.so


sudo vi /etc/pam.d/common-session

session [default=1]                     pam_permit.so
session requisite                       pam_deny.so
session required                        pam_permit.so
session optional                        pam_krb5.so minimum_uid=1000
session required        pam_unix.so





















