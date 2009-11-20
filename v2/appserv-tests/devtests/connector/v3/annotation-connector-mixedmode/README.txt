AdminObject_1
* Test to make sure that administed objects defined in ra.xml (without @AdministeredObject annotation)
  are also considered for @ConfigProperty annotation

MyAdminObject_2
* Test to make sure that multiple admin objects are created (one with Interface_1 and
  another with Interface_2 with all config properties

MyAdminObject_3
* Test to make sure that only one admin object is created (an admin object Interface_2 & MyAdminObject_3
  should not be created)

MyAdminObject_5
* Test to make sure that only one admin object is created (one specified as 'implements' Interface_3 as no "interfaces" attribute
in annotation is specified)

SimpleResourceAdapterImpl_1 & SimpleResuourceAdapterImpl_2 
* This @Connector should not be considered as component definition annotation.
  if there are multiple @Connector annotations,one that is a class as specified in ra.xml should be considered

Refer @Readme annotation in the source code of RA (ra/src/connector) for complete details as there are cases that are
not explicitly tested but documented.



