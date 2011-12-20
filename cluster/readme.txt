Message not found for errorCode: 0x80040154
essential scrap of data: name of TrustedInstaller user == "NT SERVICE\TrustedInstaller"


========================================================================
this means remote registry is not running:
Could not run the test script on xps using DCOM. :
org.jinterop.dcom.common.JIException:
Message not found for errorCode: 0xC0000034


=========================================


This means that WScript.Shell won't run a script:
Could not run the test script on xps using DCOM. :
org.jinterop.dcom.common.JIException: Class not registered.
If you are using a DLL/OCX , please mak e sure it has "DllSurrogate" flag set.
Faq A(6) in readme.html. [0x80040154]

From Hudson:

         If this fails with 0x00000005 or 0x80070005,
            I found that the following makes a difference:

            "Control Panel" -> "Administrative Tools" -> "Local Security Policy"
             -> "Local Policies" -> "Security Options" -> "Network Access: Sharing security model for local accounts"

            Set this to classic, so that the specified credential of the local user is authenticaed accordingly,
            instead of as a guest, which won't have the access to the system.

            Also see more comprehensive trobule-shooting guide on this topic at pages like:
            http://www.pcreview.co.uk/forums/thread-2164135.php
            http://www.computerperformance.co.uk/Logon/code/code_80070005.htm
            http://social.answers.microsoft.com/Forums/en-US/vistawu/thread/8eee0f53-9d95-46c8-89b7-5f12538e9f88

            TODO: user account with empty password also doesn't seem to work.






[Avatar]
2011-06-08 17:39:21 UTC
I found the root cause of remote WMI access against a Windows 7 host.
The following registry key (WbemScripting.SWbemLocator) is owned by an non-existing
group called "TrunstedInstaller", and no other group or user have write access to it:
HKLM\SOFTWARE\Classes\Wow6432Node\CLSID\{76A64158-CB41-11D1-8B02-00600806D9B6}
My solution was (for the time being) to take ownership of this key and assign
it to the Administrators group. Everything works immediately
(with firewall disabled or properly configured).
I have not found the Microsoft documentation that offers the "correct" ways to
do this. It seems that the WbemScripting.SWbemLocator services was never properly
registered and with the permission mentioned above, it prevented the
autoRegistration feature of j-Interop to finish the job. Thanks --Michael


====   Nov 2, 2011
Tried a new approach.  Since it works perfectly on XP and not at all on W7, why not
try and make it break on XP to get information.

Experiment start.  Ran these 2 commands

call asadmin -W \pw install-node-dcom --force -w hudson --archive gfsmall.zip  --save --installdir c:\glassfish23 wnevins-lnr // XP
call asadmin -W \pw install-node-dcom --force -w hudson --archive gfsmall.zip  --save --installdir c:\glassfish23 vaio        // Windows 7

results:
1. lnr
-- worked perfectly.  unpack script ran just fine.

2. vaio
Copying 45483 bytes.
com.sun.enterprise.universal.process.WindowsException: org.jinterop.dcom.common.JIException: Class not registered.
If you are using a DLL/OCX , please make sure it has "DllSurrogate" flag set. Faq A(6) in readme.html. [0x80040154]

3.  Now I went to the Registry and changed permissions of WScript.Shell like so:

KEY ==> HKEY_LOCAL_MACHINE\SOFTWARE\Classes\CLSID\{72C24DD5-D70A-438B-8A42-98424B88AFB8}

change Permissions so that Administrators do NOT have "Full Control"

Copying 45483 bytes.
com.sun.enterprise.universal.process.WindowsException: org.jinterop.dcom.common.JIException: Class not registered.
  If you are using a DLL/OCX , please make sure it has "DllSurrogate" flag set. Faq A(6) in readme.html. [0x80040154]

  YEAH!!  I at least can get EXACTLY the same error by tweaking the Win XP registry.
  I can make the error come and go at will simply by changing permissions on the Registry Key
=======================


http://msdn.microsoft.com/en-us/library/h976cd1t%28v=VS.85%29.aspx
An administrator who wants to enable Remote WSH should add a subkey entry named Remote of type REG_SZ to the registry key HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows Script Host\Settings. To enable Remote WSH, set the value of Remote to 1; to disable Remote WSH, set the value to 0. If the value of the Remote value is not set, by default Remote WSH is disabled.

Wow.  Maybe it's hopeless?  The XP machine has died -- BSOD

I tried it on "sony" my ancient XP machine and I get yet another error:


d:\gf\branches\3.1.2\cluster\cli>call asadmin -W \pw_vaio install-node-dcom --force -w bnevins --archive gfsmall.zip  --save --installdir c:\glassfish
23 sony
Copying 45483 bytes.
com.sun.enterprise.universal.process.WindowsException: org.jinterop.dcom.common.JIException: An internal error occurred. [0x8001FFFF]



http://stackoverflow.com/questions/5935714/dcom-access-via-j-interop-in-server-2008-r2

1. Firewall
2. set permissions on HKEY_CLASSES_ROOT\CLSID{76A64158-CB41-11D1-8B02-00600806D9B6},
3. Turn on remote registry
4. JISystem.setAutoRegisteration(true);
5. Deactivate UAC

MSWMI sample runs perfectly
Copied code over to common-util.  Exact same code \ (of course!!) doesn't work:


remote failure: java.lang.IllegalAccessError: tried to access method rpc.Stub.call(ILndr/NdrObject;)V from class org.jinterop.dcom.core.JIComServer
tried to access method rpc.Stub.call(ILndr/NdrObject;)V from class org.jinterop.dcom.core.JIComServer

****  stupid j-interop hides real error messages for my convenience.  Finally saw this one by
painstaking debugger work:


"No process is on the other end of the pipe."

which comes from:


d:\data\phone>asadmin -W \pw validate-dcom -w hudson  bigapp-oblade-1
remote failure:
Successfully resolved host name to: bigapp-oblade-1/10.133.184.150
Successfully connected to the DCOM port (135).
Successfully accessed C: on bigapp-oblade-1 using DCOM.
Successfully wrote delete_me.bat to C: on bigapp-oblade-1 using DCOM.
Could not connect to WMI (Windows Management Interface) on bigapp-oblade-1. : Error setting up remote connection to WMI


After a full hour of painful debugger stepping:

===================================
JIRemActivation, line 187 is where the exception is thrown for this error:


d:\gf>vd bigapp-oblade-1
asadmin -W d:\pw validate-dcom -w hudson bigapp-oblade-1
remote failure:
Successfully resolved host name to: bigapp-oblade-1/10.133.184.150
Successfully connected to the DCOM port (135).
The remote file, C: doesn't exist on bigapp-oblade-1 : The parameter is incorrect.
======================================
Nov 9, 2011

D:\gf\branches\3.1.2\cluster>asadmin -W d:\pw validate-dcom -w hudson bigapp-oblade-1
remote failure:
Successfully resolved host name to: bigapp-oblade-1/10.133.184.150
Successfully connected to the DCOM port (135).
Successfully accessed C: on bigapp-oblade-1 using DCOM.
Successfully wrote delete_me.bat to C: on bigapp-oblade-1 using DCOM.
Could not connect to WMI (Windows Management Interface) on bigapp-oblade-1. : Error setting up remote connection to WMI


D:\gf\branches\3.1.2\cluster>asadmin -W d:\pw validate-dcom -w hudson bigapp-oblade-2
remote failure:
Successfully resolved host name to: bigapp-oblade-2/10.133.184.174
Successfully connected to the DCOM port (135).
The remote file, C: doesn't exist on bigapp-oblade-2 : Access is denied.


try with bad username - note error is different...

D:\gf\branches\3.1.2\cluster>asadmin -W d:\pw validate-dcom -w hudsonxxx bigapp-oblade-2
remote failure:
Successfully resolved host name to: bigapp-oblade-2/10.133.184.174
Successfully connected to the DCOM port (135).
The remote file, C: doesn't exist on bigapp-oblade-2 : Logon failure: unknown user name or bad password.

==========================

1. Launch 'regedit.exe' as 'Administrator'
2. Find the following registry key: 'HKEY_CLASSES_ROOT\CLSID\{76A64158-CB41-11D1-8B02-00600806D9B6}'
3. Right click and select 'Permissions'
4. Click the 'Advanced' button.
5. Select the tab labeled 'Owner'
6. Add the user you want to allow to connect to the owners list
7. Click the 'Ok' button.
8. Now highlight the user and grant Full Control
9. Click 'Ok'
Retrieved from "http://www.opennms.org/wiki/WmiConfiguration";
==================================

seems like you're trying to do this the hard way.... You can install MSIs as group policy objects in an Active Directory domain. See: [Microsoft support info][1] If you want to do this in the manner you describe above, then you should put the MSI in a share with file share access, then you could use a remote command execution to execute the MSI. To do the remote execution you need to create a new instance of a Win32\_Process. See [this][2] for some C#/VB examples, but the mindeset is similar. Create the Win32_Process set the cmd line properties on it, then execute the "Create" method on the Win32\_Process. [1]: http://support.microsoft.com/kb/816102 [2]: http://www.dalun.com/blogs/05.09.2007.htm
-------------------------
WOW -- 2 hours of debugging to find this one!

VAIO is working perfectly
XPS fails in remote scripting

Tne placve it fails is in JIWinRegStub.java method winreg_CloseKey()
it is closing HKLM\Software\Classes\Wow6432Node

"Received unexpected PDU from server."
============  Nov 17, 2011 ==============
At SCA.  Trying to run validate-dcom from wnevins-lap to wnevins-loan

Ping -- works fine both  ways.
v-d from lap to loan -->  WMI error.
run examples\MSWMI from lap to loan ->

org.jinterop.dcom.common.JIException: Message not found for errorCode: 0xC0000034
        at org.jinterop.winreg.smb.JIWinRegStub.winreg_OpenHKCR(JIWinRegStub.java:134)
        at org.jinterop.dcom.core.JIComServer.initialise(JIComServer.java:509)
        at org.jinterop.dcom.core.JIComServer.<init>(JIComServer.java:414)
        at org.jinterop.dcom.test.MSWMI.<init>(MSWMI.java:38)
        at org.jinterop.dcom.test.MSWMI.main(MSWMI.java:145)
Caused by: jcifs.smb.SmbException: The system cannot find the file specified.
        at jcifs.smb.SmbTransport.checkStatus(SmbTransport.java:522)
        at jcifs.smb.SmbTransport.send(SmbTransport.java:622)
        at jcifs.smb.SmbSession.send(SmbSession.java:239)
        at jcifs.smb.SmbTree.send(SmbTree.java:109)
        at jcifs.smb.SmbFile.send(SmbFile.java:718)
        at jcifs.smb.SmbFile.open0(SmbFile.java:923)
        at jcifs.smb.SmbFile.open(SmbFile.java:940)
        at jcifs.smb.SmbFileOutputStream.<init>(SmbFileOutputStream.java:142)
        at jcifs.smb.TransactNamedPipeOutputStream.<init>(TransactNamedPipeOutputStream.java:32)
        at jcifs.smb.SmbNamedPipe.getNamedPipeOutputStream(SmbNamedPipe.java:187)
        at rpc.ncacn_np.RpcTransport.attach(RpcTransport.java:92)
        at rpc.Stub.attach(Stub.java:106)
        at rpc.Stub.call(Stub.java:110)
        at org.jinterop.winreg.smb.JIWinRegStub.winreg_OpenHKCR(JIWinRegStub.java:132)
        ... 4 more
		remote registry?!?

========  found remote registry turned off -- started it & set to automatic
then got this:

d:\gf_other\j-Interop208\j-Interop\examples\MSWMI>execute wnevins-loan wnevins-loan hudson hudson
org.jinterop.dcom.common.JIException: Access is denied, please check whether the [domain-username-password] are correct. Also, if not already done ple
ase check the GETTING STARTED and FAQ sections in readme.htm. They provide information on how to correctly configure the Windows machine for DCOM acce
ss, so as to avoid such exceptions.  [0x00000005]
        at org.jinterop.winreg.smb.JIWinRegStub.winreg_CreateKey(JIWinRegStub.java:310)
        at org.jinterop.dcom.core.JIComServer.initialise(JIComServer.java:510)
        at org.jinterop.dcom.core.JIComServer.<init>(JIComServer.java:414)
        at org.jinterop.dcom.test.MSWMI.<init>(MSWMI.java:38)
        at org.jinterop.dcom.test.MSWMI.main(MSWMI.java:145)
Caused by: org.jinterop.dcom.common.JIRuntimeException: Access is denied, please check whether the [domain-username-password] are correct. Also, if no
t already done please check the GETTING STARTED and FAQ sections in readme.htm. They provide information on how to correctly configure the Windows mac
hine for DCOM access, so as to avoid such exceptions.  [0x00000005]
        at org.jinterop.winreg.IJIWinReg$createKey.read(IJIWinReg.java:459)
        at ndr.NdrObject.decode(NdrObject.java:36)
        at rpc.ConnectionOrientedEndpoint.call(ConnectionOrientedEndpoint.java:137)
        at rpc.Stub.call(Stub.java:113)
        at org.jinterop.winreg.smb.JIWinRegStub.winreg_CreateKey(JIWinRegStub.java:304)
        ... 4 more


	OK something is wrong wirth connecting to WMI.  I tried this next:

Then go to Control Panel > Administrative Tools > Local Security Policy > Security Settings > Local Policies > Security Options :-

    Double-click "DCOM: Machine Access Restrictions" policy, click Edit Security, add the user created above, allow "Remote Access"
    Double-click "DCOM: Machine Launch Restrictions" policy, click Edit Security, add the user created above, allow "Local Launch", "Remote Launch", "Local Activation", "Remote Activation"
	--- No help ---
	Then tried this:

Go to Control Panel > Administrative Tools > Component Services > Computers > right-click My Computer > click Properties > click COM Security tab :-

    In Access Permissions section, click Edit Default > add the user created above, allow "Remote Access"
    In Launch and Activation Permissions section > click Edit Default > add the user created above, allow "Local Launch", "Remote Launch", "Local Activation", "Remote Activation"

	===  no help at all ===

	 Turned off UAc

	 =============
	 changed ownership and permissions on
	 HKCR\WBemScripting.SWBemlocator


------
Now MSWMI example works but v-d still does not.  Getting closer!



I found the root cause of remote WMI access against a Windows 7 host. The following registry key (WbemScripting.SWbemLocator) is owned by an non-existing group called "TrunstedInstaller", and no other group or user have write access to it: HKLM\SOFTWARE\Classes\Wow6432Node\CLSID\{76A64158-CB41-11D1-8B02-00600806D9B6} My solution was (for the time being) to take ownership of this key and assign it to the Administrators group. Everything works immediately (with firewall disabled or properly configured). I have not found the Microsoft documentation that offers the "correct" ways to do this. It seems that the WbemScripting.SWbemLocator services was never properly registered and with the permission mentioned above, it prevented the autoRegistration feature of j-Interop to finish the job.

========================
   From MSWMI example's readme.txt file
   This means WMI has rejected your connection request. This is not a j-Interop related problem. Make
   sure you have access to WMI on both the managing and the managed machines. On the WMI enabled
   machines, open "Control Panel/Administrative Tools/Computer Management/", then click on  "Services
   and Applications", then right-click on the "WMI Control", choose "Properties"; open the "Security"
   pane, click on the "Security" Button, and add the necessary account. If you are still having
   troubles, please consult WMI documentation at Platform SDK: Windows Management Instrumentation
   (http://msdn.microsoft.com/library/en-us/wmisdk/wmi/wmi_start_page.asp)

==================================================================

Bone-headed J-interop message:


d:\gf_other\j-Interop208\j-Interop\examples\MSWMI>asadmin install-node-dcom -w bnevins -W \pw_vaio --save LOANER XPS
Created installation zip D:\gf_other\j-Interop208\j-Interop\examples\MSWMI\glassfish823632885693820612.zip
Copying 87377982 bytes....................................................................................
com.sun.enterprise.util.cluster.windows.process.WindowsException: org.jinterop.dcom.common.JIException: Message not found for errorCode: 0xC0000034

==================================================================

Dec 5

Interesting:  ran validate-dcom on bigapp-oblade-1 -- it failed.  Ran it a second time with no changes -- it passed!!

===================================================================

d:\gf_other\j-Interop208\j-Interop\examples\MSWMI>asadmin install-node-dcom -W \pw -w hudson --installdir c:/glassfish3  --archive glassfish8236328856
93820612.zip bigapp-oblade-2
Copying 87377982 bytes....................................................................................
com.sun.enterprise.util.cluster.windows.process.WindowsException: org.jinterop.dcom.common.JIException: Class not registered. If you are using a DLL/O
CX , please make sure it has "DllSurrogate" flag set. Faq A(6) in readme.html. [0x80040154]
==================================================

Dec 7, 2011
Attempting to configure bigapp-oblade-3

validate-dcom says this:

Could not connect to WMI (Windows Management Interface) on bigapp-oblade-3. : Error setting up remote connection to WMI

===============================
Then I changed permissions on HKLM\Software\Classes\CLSID\76a64....
and I got a bit further:

Successfully accessed WMI (Windows Management Interface) on bigapp-oblade-3.  There are 76 processes running on bigapp-oblade-3.
Could not run the test script on bigapp-oblade-3 using DCOM. :
org.jinterop.dcom.common.JIException: Access is denied, please check whether
 the [domain-username-password] are correct. Also, if not already done
please check the GETTING STARTED and FAQ sections in readme.htm.
They provide information on how to correctly configure the Windows
machine for DCOM access, so as to avoid such exceptions.  [0x00000005]

Then I changed permissions on HKLM\Software\Classes\CLSID\72C24DD5....
Boom!  Works!!
Successfully accessed WMI (Windows Management Interface) on bigapp-oblade-3.  There are 76 processes running on bigapp-oblade-3.
Successfully ran the test script on bigapp-oblade-3 using DCOM.
The script simply ran the DIR command.  Here are the first few lines from the output of the dir command on the remote machine:

C:\Windows\system32>dir C:\
 Volume in drive C has no label.
 Volume Serial Number is 5851-CD05

 Directory of C:\

12/07/2011  05:26 PM    <DIR>          b
12/07/2011  05:19 PM    <DIR>          batch
12/07/2011  05:50 PM                 8 delete_me.bat
07/08/2011  01:53 PM           272,748 ff5.jpg
12/07/2011  05:15 PM    <DIR>          glassfish3

=======================================
bigapp-oblade-3 appeared to have a problem with the unpack script.
I ran it in a debugger.  Just before remotel;y running the script I edited it on b-o-3 and
replace "jar" with the full path to "jar"
then it worked!!!
There may be Path issues.  I set the jdk in the path for EVERYONE.  Maybe it needs to be in there explicitly
for the actual user?!?

Later -- it apparently needed a reboot.  It would have an old stale Path until the reboot.

========================================





$objUser = New-Object System.Security.Principal.NTAccount("kenmyer")
$strSID = $objUser.Translate([System.Security.Principal.SecurityIdentifier])
$strSID.Value



Set-ExecutionPolicy RemoteSigned



I believe you'll have to enable the SeBackupPrivilege first before you can set the owner of a file to be someone other than yourself. With an elevated shell:
 
PS C:\Windows\system32> ipmo pscx
PS C:\Windows\system32> $p = get-privilege
PS C:\Windows\system32> $p.Enable("sebackupprivilege")
PS C:\Windows\system32> Set-Privilege $p
PS C:\Windows\system32> get-privilege
Name                                     Status
----                                     ------
SeIncreaseQuotaPrivilege                 Disabled
SeMachineAccountPrivilege                Disabled
SeSecurityPrivilege                      Disabled
SeTakeOwnershipPrivilege                 Disabled
SeLoadDriverPrivilege                    Disabled
SeSystemProfilePrivilege                 Disabled
SeSystemtimePrivilege                    Disabled
SeProfileSingleProcessPrivilege          Disabled
SeIncreaseBasePriorityPrivilege          Disabled
SeCreatePagefilePrivilege                Disabled
SeBackupPrivilege                        Enabled  <<<<<<
SeRestorePrivilege                       Disabled

This should give you the neccessary rights to set the owner. The reason you need backup rights is because you are subverting the auditing; normally you can only give someone the right to _take_ ownership back.
 
-Oisin
