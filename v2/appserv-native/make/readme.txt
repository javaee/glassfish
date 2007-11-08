				INTRODUCTION
This document describes the usage of the defines*.mk and rules*.mk files.

 defines.mk - This file is intended to be the first one included by a makefile.
	The path specified should be relative, using ..'s to get back to the
	parent directory of root, followed by root/make/defines.mk. From
	.../root/http, for example the path is ../../root/make/defines.mk.
	Note that ../make/defines.mk doesn't work because http is a symbolic
	link. 

 defines_COMMON.mk - This file contains macros common to all platforms. Many
	of them are defined using other macros which are platform dependent.
	It is included automatically by defines.mk.

 defines_$(PLATFORM).mk - These files contain platform dependent macros.
	The proper version is included automatically by defines.mk.

 rules.mk - This is generally the last one included by a makefile. Assuming
	defines.mk was included, the include line for this file looks like:
	"include $(BUILD_ROOT)/make/rules.mk".

 rules_COMMON.mk - This file contains rules common to all platforms. Many
	of them are defined using macros which are platform dependent.
	It is included automatically by rules.mk.

 rules_$(PLATFORM).mk - These files contain platform dependent rules.
	The proper version is included automatically by rules.mk.

The basic goal of these makefile include files is to provide a consistent set
of macros and rules that can be used to create makefiles that will work on
multiple platforms.


			     COMMAND LINE MACROS
The following macros are designed to be used on the command line as in
"clearmake DEBUG=off" or "clearmake PROF=ON BROWSE=off".

 DEBUG   DEBUG=ON enables flags that generate debug information, DEBUG=off
	 turns the debug flags off. The default is "ON".
 PROF    PROF=ON enables flags that generate profiling information, PROF=off
	 turns the profiling flags off. The default is "off".
 BROWSE	 BROWSE=ON enables flags that generate browser information, BROWSE=off
	 turns the browser flags off. The default is "ON".
 PURIFY  PURIFY=ON enables the purify prelink operation for linking executables
	 built with the default rules. The default is "off".	 

Note: The string 'ON' is actually the only one that is recognized. Any string
      other than 'ON' can be used for 'off'.


				PRIVATE MACROS
The following macros are used internally and should not be overridden in any
makefile that uses these include files.

 CC_INCL	CC_DEFS		CC_OPTS		LD_LIBDIRS	LD_LIBS
 LD_OPTS	LD_FLAGS	CC_FLAGS	C++FLAGS	PLATFORM_INC
 SYSTEM_INC	PLATFORM_DEFS	SYSTEM_DEFS	PLATFORM_LIB	SYSTEM_LIB
 PLATFORM_CC_OPTS	SYSTEM_CC_OPTS		PLATFORM_LD_OPTS
 SYSTEM_LD_OPTS		PLATFORM_LIBDIRS	SYSTEM_LIBDIRS

				 PUBLIC MACROS
The following macros are intended to be used to modify the default behavior of
the build rules in a controlled fashion. Examples are shown in ()'s. 

 LOCAL_INC, PROJECT_INC, SUBSYS_INC - add include file paths (-I../foo)
 LOCAL_DEFS, PROJECT_DEFS, SUBSYS_DEFS - add defines (-Dfoo)
 LOCAL_CC_OPTS, PROJECT_CC_OPTS, SUBSYS_CC_OPTS - add CC options (-O2)
 LOCAL_LIBDIRS, PROJECT_LIBDIRS, SUBSYS_LIBDIRS - add library paths (-L../foo)
 LOCAL_LIB, PROJECT_LIB, SUBSYS_LIB - add libraries (-lfoo)
 LOCAL_LD_OPTS, PROJECT_LD_OPTS, SUBSYS_LD_OPTS - add LD options (-map)
 LD_PREFLAGS, CC_PREFLAGS, C++PREFLAGS - add other arguments at beginning
 LD_POSTFLAGS, CC_POSTFLAGS, C++POSTFLAGS - add other arguments at end

Note: CC_FLAGS are used when $(CC) is used as the linker, while C++FLAGS are
      used when $(CC) is used to compile C++ code. They both use many of the
      same public macros. Only CC_PREFLAGS, CC_POSTFLAGS, C++PREFLAGS and
      C++POSTFLAGS are not common.


				RECURSIVE MAKE
To invoke a subordinate makefile and pass the necessary arguments, use:
 $(MAKE) $(MAKE_ARGS) ... This will pass any command line options and the
command line macros DEBUG, PROF and BROWSE.


			      SIMPLE RULES MACROS
There are a number of macros that can be used to trigger simple rules. 

If a makefile sets the following macros:

 EXE_TARGET=some_executable
 EXE_OBJS=some_object another_object ...

it will cause the following rule to be activated:

 $(EXE_TARGET)$(EXE): $(EXE_OBJS:.$(OBJ)) ;\
	$(CC) $(CC_FLAGS) -o $(EXE_TARGET)$(EXE) $(EXE_OBJS:.$(OBJ)) \
	      $(LD_LIBDIRS) $(LD_LIBS)

Note: Do not include the extensions when defining the macros. They will be
      added by the rule.

The macros EXE1_TARGET/EXE1_OBJS and EXE2_TARGET/EXE2_OBJS trigger similar
rules. More can be added if needed.

--

If a makefile sets the following macros:

 DLL_TARGET=some_dll
 DLL_OBJS=some_object another_object ...

it will cause the following rule to be activated:

 $(LIBPREFIX)$(DLL_TARGET).$(DYNAMIC_LIB_SUFFIX): $(DLL_OBJS:=.$(OBJ)) ; \
	$(CC) $(CC_FLAGS) $(LD_DYNAMIC) \
	      -o $(LIBPREFIX)$(DLL_TARGET).$(DYNAMIC_LIB_SUFFIX) \
	      $(DLL_OBJS:=.$(OBJ)) $(LD_LIBDIRS) $(LD_LIBS)

Note: Do not include the extensions when defining the macros. They will be
      added by the rule.

--

Under construction:

 IDL_SOURCE=some_idl
 IDL_CAPS=SOME_IDL

--

Other simple-rule macros are planned.


			     BROWSER FILE MACROS
To automatically process browser files, there are two set of macros for NT and
Solaris that can be used. If the following form is used, the rule will be
enabled only if BROWSE=ON.

For NT, if the following macros are set:

 BSC_TARGET=project.bsc
 BSC_OBJS=$(EXE_OBJS)				# without extensions as above

and $(BSC_TARGET) is added as a dependency to the 'link' target it will cause
the following rule to be activated:

 $(BSC_TARGET): $(BSC_OBJS:=.sbr); \
	$(BSCMAKE) $(BSC_FLAGS) $(BSC_TARGET) $(BSC_OBJS:=.sbr)

For Solaris, if the following macros are set:

 SB_INIT=.sbinit
 SB_DIR=$(BUILD_ROOT)/http

and $(SB_INIT) is added as a dependency to the 'headers' target it will cause
the following rule to be activated:

 $(SB_INIT): ; \
	$(ECHO) export / into $(SB_DIR) > $@

This will cause all browser information to be placed in $(SB_DIR)/.sb

Note: On a platform other than the one for which the rules are targeted, a one
      line text file will be created to satisfy the dependency.

