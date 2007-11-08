/*
* The contents of this file are subject to the terms 
* of the Common Development and Distribution License 
* (the License).  You may not use this file except in
* compliance with the License.
* 
* You can obtain a copy of the license at 
* https://glassfish.dev.java.net/public/CDDLv1.0.html or
* glassfish/bootstrap/legal/CDDLv1.0.txt.
* See the License for the specific language governing 
* permissions and limitations under the License.
* 
* When distributing Covered Code, include this CDDL 
* Header Notice in each file and include the License file 
* at glassfish/bootstrap/legal/CDDLv1.0.txt.  
* If applicable, add the following below the CDDL Header, 
* with the fields enclosed by brackets [] replaced by
* you own identifying information: 
* "Portions Copyrighted [year] [name of copyright owner]"
* 
* Copyright 2007 Sun Microsystems, Inc. All rights reserved.
*/


/*
 * FuturesRequestManager.java
 */
package com.sun.xml.registry.uddi;

import com.sun.xml.registry.uddi.bindings_v2_2.*;
import com.sun.xml.registry.uddi.infomodel.ConceptImpl;
import com.sun.xml.registry.uddi.infomodel.KeyImpl;
import com.sun.xml.registry.common.BulkResponseImpl;
import javax.xml.bind.JAXBElement;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.ServiceBinding;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Current implementation simply spawns a thread for each
 * request that is made. Future work may pool threads or
 * use a worker thread to perform all requests serially.
 */
public class ResponseTransformer {

    private UDDIMapper mapper;

     public ResponseTransformer(UDDIMapper mapper){
        this.mapper = mapper;
    }

    //think about inner classes
    BulkResponse transformResponse(Object obj, Collection keys, String type)
     throws JAXRException {

        
        
        obj = ((JAXBElement)obj).getValue();
        if (obj instanceof BusinessList)
            return transformResponse((BusinessList)obj, keys,type);
        else if (obj instanceof AuthToken)
             return transformResponse((AuthToken)obj, keys,type);
         else if (obj instanceof AssertionStatusReport)
             return transformResponse((AssertionStatusReport)obj, keys,type);
         else if (obj instanceof RelatedBusinessesList)
             return transformResponse((RelatedBusinessesList)obj, keys,type);
         else if (obj instanceof ServiceList)
             return transformResponse((ServiceList)obj, keys,type);
         else if (obj instanceof TModelList)
             return transformResponse((TModelList)obj, keys,type);
         else if (obj instanceof BusinessDetail)
             return transformResponse((BusinessDetail)obj, keys,type);
         else if (obj instanceof ServiceDetail)
             return transformResponse((ServiceDetail)obj, keys,type);
         else if (obj instanceof BindingDetail)
             return transformResponse((BindingDetail)obj, keys,type);
         else if (obj instanceof TModelDetail)
             return transformResponse((TModelDetail)obj, keys,type);
        else if (obj instanceof RegisteredInfo)
             return transformResponse((RegisteredInfo)obj, keys,type);
        else if (obj instanceof DispositionReport)
             return transformResponse((DispositionReport)obj, keys,type);
        else if (obj instanceof PublisherAssertions)
             return transformResponse((PublisherAssertions)obj, keys,type);
        return null;
        //probably should return empty bulkResponse
    }


    BulkResponse transformResponse(AuthToken token, Collection keys, String type)
     throws JAXRException {
        

         BulkResponseImpl bulkResponse = new BulkResponseImpl();
         Collection tokens = new ArrayList();
         String authInfo = token.getAuthInfo();
         
         tokens.add(authInfo);
         bulkResponse.setCollection(tokens);

         return bulkResponse;
    }



    /**
         *
         * @param
         * @return
         * @exception
         *
         */

      BulkResponse transformResponse(BusinessList list, Collection keys, String type)
        throws JAXRException {
            
            BulkResponseImpl bResponse = null;
            if (list != null) {
                bResponse = new BulkResponseImpl();
                Collection fbResponse = new ArrayList();
                
                BusinessInfos infos = list.getBusinessInfos();
                //Object infos2 = list.getBusinessInfos();
                
                //get each businessInfo
                //BusinessInfos infos = (BusinessInfos)infos2;
                Organization org = null;
                if (infos != null) {
                    //for (int i = 0; i < infos.getBusinessInfoLength(); i++) {
                        
                        Collection binfos = infos.getBusinessInfo();
                        Iterator iter = binfos.iterator();
                        while (iter.hasNext()){
                        
                        org = mapper.businessInfo2Organization((BusinessInfo)iter.next());
                        
                        fbResponse.add(org);
                    }
                }
                bResponse.setCollection(fbResponse);
            }
            return bResponse;
        }

    /**
         *
         * @param
         * @return
         * @exception
         *
         */
    BulkResponse transformResponse(AssertionStatusReport report,
                                              Collection keys, String type)
        throws JAXRException {

        
            BulkResponseImpl bResponse = new BulkResponseImpl();;
            if (report != null) {
                String generic = report.getGeneric();
                String operator = report.getOperator();
                Collection assertionStatusItems =
                    report.getAssertionStatusItem();
                Collection associations =
                    mapper.assertionStatusItems2Associations(assertionStatusItems);
                if (associations != null)
                    bResponse.setCollection(associations);
            }
            return bResponse;
        }

    /**
        *
        * @param
        * @return
        * @exception
        *
        */

     BulkResponse transformResponse(RelatedBusinessesList list, Collection keys, String type)
       throws JAXRException {

           BulkResponseImpl bResponse = null;
           Collection fbResponse = new ArrayList();
           if (list != null) {
               bResponse = new BulkResponseImpl();
               RelatedBusinessInfos infos =
                       list.getRelatedBusinessInfos();
               //get each businessInfo
               Collection associations = null;
               Collection rinfos =
                           infos.getRelatedBusinessInfo();
               Iterator iter = rinfos.iterator();
               while (iter.hasNext()){
                   associations =
                           mapper.relatedBusinessInfo2Associations((RelatedBusinessInfo)iter.next(), keys);
                   if (associations != null){
                       fbResponse.addAll(associations);
                       //logger.finest("Added Associations");
                   }
               }
               bResponse.setCollection(fbResponse);
           }
           return bResponse;
       }


       /**
        *
        * @param
        * @return
        * @exception
        *
        */

     BulkResponse transformResponse(ServiceList list, Collection keys, String type)
       throws JAXRException {

           BulkResponseImpl bResponse = null;
           if (list != null) {
               bResponse = new BulkResponseImpl();
               Collection fServiceResponse = new ArrayList();

               ServiceInfos infos = list.getServiceInfos();
               //get each businessInfo
               Collection sinfos = infos.getServiceInfo();
               Iterator iter = sinfos.iterator();
               Service service = null;
               while (iter.hasNext()){
                   service = mapper.serviceInfo2Service((ServiceInfo)iter.next());
                   fServiceResponse.add(service);
               }
               bResponse.setCollection(fServiceResponse);
           }
           return bResponse;
       }

       /**
        *
        * @param
        * @return
        * @exception
        *
        */

      BulkResponse transformResponse(TModelList list, Collection keys,
                                             String type) throws JAXRException {

           BulkResponseImpl bResponse = new BulkResponseImpl();
           if (list != null) {
               bResponse = new BulkResponseImpl();
               Collection fTModelResponse = new ArrayList();

               TModelInfos infos = list.getTModelInfos();
               //do incremental load here so can get detail
               //get each TModelInfo
               ConceptImpl concept = null;
               Collection tkeys = new ArrayList();
               Collection tinfos = infos.getTModelInfo();
               Iterator iter = tinfos.iterator();
               while (iter.hasNext()){

                   String id = ((TModelInfo)iter.next()).getTModelKey();
                   KeyImpl key = new KeyImpl(id);
                   tkeys.add(key);
               }

               //now do a getConcepts
               //need to look at this -FIX??
               Collection schemes = new ArrayList();
               if (tkeys.size() > 0) {
                   BulkResponse response = mapper.getConcepts(tkeys);
                   if (response.getExceptions() == null) {
                       //get the objects for this
                       Collection rokeys = response.getCollection();
                       //need to return ClassificationSchemes
                       Iterator kiter = rokeys.iterator();
                       while (kiter.hasNext()) {
                           RegistryObject ro = (RegistryObject)kiter.next();
                           schemes.add(ro);
                       }
                   }
               }
               //set the bulk response
               bResponse.setCollection(schemes);
           }
           return bResponse;
       }


       /**
        *
        * @param
        * @return
        * @exception
        *
        */
       BulkResponse transformResponse(BusinessDetail detail,
                                             Collection keys, String type)
       throws JAXRException {

           BulkResponseImpl bResponse = null;
           if (detail != null) {
               bResponse = new BulkResponseImpl();
               Collection fbResponse = new ArrayList();
               Collection entities =
                       detail.getBusinessEntity();
               if (entities != null) {
                   Iterator iter = entities.iterator();
                   while (iter.hasNext()){
                       Organization org =
                               mapper.businessEntity2Organization((BusinessEntity)iter.next());
                       if (type.equalsIgnoreCase(mapper.FIND))
                           fbResponse.add(org);
                       else
                           fbResponse.add(org.getKey());
                   }
                   bResponse.setCollection(fbResponse);
               }
           }
           return bResponse;
       }

       /**
        *
        * @param
        * @return
        * @exception
        *
        */
       BulkResponse transformResponse(ServiceDetail detail, Collection keys, String type)
       throws JAXRException {

           BulkResponseImpl bResponse = null;
           if (detail != null) {
               bResponse = new BulkResponseImpl();
               Collection fbResponse = new ArrayList();
               //todo - why is this commented out?
               //bResponse.setPartialResponse(detail.getTruncated());
               Collection services =
                       detail.getBusinessService();
               if (services != null) {
                   Iterator iter = services.iterator();
                   while (iter.hasNext()){
                       Service service =
                               mapper.businessService2Service((BusinessService)iter.next());
                       if (type.equals(mapper.FIND)) {
                           fbResponse.add(service);
                       } else {
                           fbResponse.add(service.getKey());
                       }
                   }
                   bResponse.setCollection(fbResponse);
               }
           }
           return bResponse;
       }

       /**
        *
        * @param
        * @return
        * @exception
        *
        */

       BulkResponse transformResponse(BindingDetail detail, Collection keys,
       String type) throws JAXRException {

           BulkResponseImpl bResponse = null;
           if (detail != null) {
               bResponse = new BulkResponseImpl();
               Collection fbResponse = new ArrayList();

               Collection templates =
                  detail.getBindingTemplate();
               Iterator iter = templates.iterator();
               while (iter.hasNext()){
                   ServiceBinding binding =
                   mapper.bindingTemplate2ServiceBinding((BindingTemplate)iter.next());
                   if (type.equals(mapper.FIND))
                       fbResponse.add(binding);
                   else
                       fbResponse.add(binding.getKey());
               }
               bResponse.setCollection(fbResponse);
           }
           return bResponse;
       }


       /**
        *
        *
        * @return
        * @exception
        *
        */
       BulkResponse transformResponse(TModelDetail detail, Collection keys,
       String type) throws JAXRException {

           BulkResponseImpl bResponse = null;
           if (detail != null) {
               bResponse = new BulkResponseImpl();
               Collection fbResponse = new ArrayList();

               Collection tModels =
                   detail.getTModel();
               Iterator iter = tModels.iterator();
               while (iter.hasNext()){
                   RegistryObject registryObject =
                   mapper.tModel2ConceptOrClassificationScheme((TModel)iter.next());
                   if (type.equals(mapper.FIND))
                       fbResponse.add(registryObject);
                   else
                       fbResponse.add(registryObject.getKey());
               }
               bResponse.setCollection(fbResponse);
           }
           return bResponse;
       }

       /**
        *
        *
        * @return
        * @exception
        *
        */
       BulkResponse transformResponse(RegisteredInfo info,
                                             Collection keys, String type)
       throws JAXRException {

           BulkResponseImpl bResponse = null;
           if (info != null) {
               bResponse = new BulkResponseImpl();
               Collection fbResponse = new ArrayList();

               BusinessInfos infos = info.getBusinessInfos();
               Collection  binfos = infos.getBusinessInfo();
               Organization org = null;
               Iterator iter = binfos.iterator();
               while (iter.hasNext()){
                   org = mapper.businessInfo2Organization((BusinessInfo)iter.next());
                   fbResponse.add(org);
               }

               TModelInfos tminfos = info.getTModelInfos();
               Collection tinfos = tminfos.getTModelInfo();
               //get each TModelInfo
               ConceptImpl concept = null;
               Iterator titer = tinfos.iterator();
               while (titer.hasNext()){
                   concept = (ConceptImpl)
                           mapper.tModel2Concept((TModelInfo)titer.next());
                   //logger.finest("Transformed tmodel");
                   fbResponse.add(concept);
               }
               bResponse.setCollection(fbResponse);
           }
           return bResponse;
       }

       /**
        *
        *
        * @return
        * @exception
        *
        */
       BulkResponse transformResponse(DispositionReport report, Collection keys, String type)
       throws JAXRException {

           BulkResponse bResponse = null;
           if (report != null) {
               String generic = report.getGeneric();
               String operator = report.getOperator();
               Truncated truncated = report.getTruncated();
               Collection results =
                       report.getResult();
               bResponse = mapper.results2BulkResponse(results, keys, type);
           }
           return bResponse;
       }

    BulkResponse transformResponse(PublisherAssertions assertions,
                                          Collection keys, String type)
    throws JAXRException {

        BulkResponseImpl bResponse = null;
        if (assertions != null) {
            bResponse = new BulkResponseImpl();
            String generic = assertions.getGeneric();
            String operator = assertions.getOperator();
            String authorizedName = assertions.getAuthorizedName();

            Collection publisherAssertions =
                assertions.getPublisherAssertion();

            Collection associations =
                mapper.publisherAssertions2Associations(publisherAssertions);

            if (type.equalsIgnoreCase(mapper.SAVE))
                 bResponse.setCollection(keys);
            else
                bResponse.setCollection(associations);
        }
        return bResponse;
    }
}
