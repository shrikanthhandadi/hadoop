Domain we use is EXP.COM, kerberos KDC & Admin server is krbserver.exp.com

##### kerberos server setup.
* Add kerberos server as entry for /etc/hosts. Assuming your IP is 192.168.1.151 execute below line.
```javascript
sudo sed -i '3i 192.168.1.151 krbserver.exp.com' /etc/hosts 
```

* install kerberos admin server & key distribution center.
``` sudo apt-get install krb5-admin-server krb5-kdc ```
*Default Kerberos version 5 realm? EXP.COM
Add locations of default Kerberos servers to /etc/krb5.conf? Yes
Kerberos servers for your realm: krb1.exp.com 
Administrative server for your Kerberos realm: krb1.exp.com
Create the Kerberos KDC configuration automatically? Yes
//you may get some dialogue here press OK
Run the Kerberos V5 administration daemon (kadmind)? Yes*

* Replace /etc/krb5.conf content with below entry
```
sudo sh -c 'echo "[logging]
	default = FILE:/var/log/krb5lib.log
	kdc = FILE:/var/log/krb5kdc.log
	admin_server = FILE:/var/log/kadmin.log

[libdefaults]
	default_realm = EXP.COM
	dns_lookup_realm = false
	dns_lookup_kdc = false
	ticket_lifetime = 240h
	renew_lifetime = 7d
	forwardable = true
	kdc_timesync = 0

[realms]
	EXP.COM = {
		kdc = krbserver.exp.com
		admin_server = krbserver.exp.com
	}" > /etc/krb5.conf'
```

	
* add below content to /etc/krb5kdc/kdc.conf
```
sudo sh -c 'echo "[kdcdefaults]
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
		supported_enctypes = aes256-cts-hmac-sha1-96:normal
		default_principal_flags = +preauth
	}

[logging]
	kdc = FILE:/var/log/krb5kdc.log
	admin_server = FILE:/var/log/kadmin.log
	default = FILE:/var/log/krb5lib.log
" > /etc/krb5kdc/kdc.conf'
```
* setup new kerberos realm
``` sudo krb5_newrealm ```
*Enter kerberos master key: [enter the password & remember it]*

* In Access Control List enable the admin type of user to do administration by uncommenting # */admin *
``` sudo sed -i 's/\# \*\/admin \*/\*\/admin \*/g' /etc/krb5kdc/kadm5.acl ```

* restart both admin and kdc servers to set above changes effective
``` sudo invoke-rc.d krb5-admin-server restart ```
``` sudo invoke-rc.d krb5-kdc restart ```

* Below is the optional step to tail the kerberos related logs
``` cd /var/log; sudo tail -F daemon.log sulog user.log auth.log debug kern.log syslog dmesg messages {krb5kdc,kadmin,krb5lib}.log ```

* add admin login which will enable you to login from remote host
```
sudo kadmin.local
    addpol -minlength 1 -minclasses 1 admin
    addprinc -policy admin kadmin1/admin
```

##### Client side setups 
Considering server1.exp.com server2.exp.com server3.exp.com server4.exp.com as list of clients, execute below steps in all servers 

* install kerberos user package
``` sudo apt-get install krb5-user krb5-config ```

* copy kerberos config file /etc/krb5.conf from kerberos server to similcar location in local server
``` sudo scp shrikanth@krbserver.exp.com:/etc/krb5.conf /etc/krb5.conf ```

**SSH related setups**

* install ssh server if already not installed. All the servers will use ssh as the means for connectivity.
``` sudo apt-get install openssh-server ```

* Edit SSH daemon (ssh server side) config /etc/ssh/sshd_config and update following lines. This will suggest to enable GSSAPI & clean the credental cache once logged out
``` sudo sed -i 's/#GSSAPIAuthentication no/GSSAPIAuthentication yes/g' /etc/ssh/sshd_config ```
``` sudo sed -i 's/#GSSAPICleanupCredentials yes/GSSAPICleanupCredentials yes/g' /etc/ssh/sshd_config ```

* Edit SSH client config /etc/ssh/ssh_config and enable the following lines. This will suggest to enable delegation to kerberos server & host authentication should be enabled as described below.
``` sudo sed -i 's/#   GSSAPIAuthentication no/GSSAPIAuthentication yes/g' /etc/ssh/ssh_config ```
``` sudo sed -i 's/    GSSAPIDelegateCredentials no/    GSSAPIDelegateCredentials yes/g' /etc/ssh/ssh_config ```

##### -------- demo of user authentication using kerberos ---------
Lets say user called kuser1 who is local user of server server2.exp.com & he will be logging in from node server3.exp.com. Per kerberos model kuser1 & host server2.exp.com will be addded to kerberos principals & underneath ssh will delegate the login.

**@server2.exp.com**
* Add host into kerberos database & generate keytab file. Note that this keytab is equivalent of password & will be used by ssh to communicate to kerberos server to validate the tickets users passed as part of ssh login. 
```
sudo kadmin -p kadmin1/admin 
	addpol -minlength 1 -minclasses 1 host
	addprinc -policy host -randkey host/server2.exp.com
	ktadd -k /etc/krb5.keytab -norandkey host/server2.exp.com
```
* add user also into kerberos database & remember the password
```
sudo kadmin -p kadmin1/admin 
	addpol -minlength 1 -minclasses 1 user
	addprinc -policy user kuser1
```
* now add test user kuser1 with special password as '*K*' -this is the indicator that will suggest ssh server to use kerberos for user logins
```sudo adduser kuser1```
```sudo usermod -p '*K*' kuser1```

**@server3.exp.com**
* do a kinit to generate kerberos token & login to server2.exp.com. For any troubleshooting look at the log tail which is described in above steps.
``` kinit kuser1 ```
``` ssh kuser1@server2.exp.com ```




##### --------- hadoop specifics -----------------------------------











