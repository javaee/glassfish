Message not found for errorCode: 0x80040154
essential scrap of data: name of TrustedInstaller user == "NT SERVICE\TrustedInstaller"


========================================================================
this means remote registry is not running:
Could not run the test script on xps using DCOM. : org.jinterop.dcom.common.JIException: Message not found for errorCode: 0xC0000034


=========================================


This means that WScript.Shell won't run a script:
Could not run the test script on xps using DCOM. : org.jinterop.dcom.common.JIException: Class not registered. If you are using a DLL/OCX , please mak e sure it has "DllSurrogate" flag set. Faq A(6) in readme.html. [0x80040154]

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
I found the root cause of remote WMI access against a Windows 7 host. The following registry key (WbemScripting.SWbemLocator) is owned by an non-existing group called "TrunstedInstaller", and no other group or user have write access to it: HKLM\SOFTWARE\Classes\Wow6432Node\CLSID\{76A64158-CB41-11D1-8B02-00600806D9B6} My solution was (for the time being) to take ownership of this key and assign it to the Administrators group. Everything works immediately (with firewall disabled or properly configured). I have not found the Microsoft documentation that offers the "correct" ways to do this. It seems that the WbemScripting.SWbemLocator services was never properly registered and with the permission mentioned above, it prevented the autoRegistration feature of j-Interop to finish the job. Thanks --Michael 


====   Nov 2, 2011
Tried a new approach.  Since it works perfectly on XP and not at all on W7, why not try and make it break on XP to get information.

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
