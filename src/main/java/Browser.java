import org.apache.juddi.v3.client.UDDIConstants;
import org.apache.juddi.v3.client.config.UDDIClient;
import org.apache.juddi.v3.client.transport.Transport;
import org.uddi.api_v3.*;
import org.uddi.v3_service.UDDIInquiryPortType;
import org.uddi.v3_service.UDDISecurityPortType;
import org.apache.juddi.api_v3.AccessPointType;

import java.util.List;

class Browser {
    private static UDDISecurityPortType security = null;
    private static UDDIInquiryPortType inquiry = null;

    Browser() {
        try {
            UDDIClient client = new UDDIClient("src\\main\\resources\\WEB_INF\\uddi.xml");
            Transport transport = client.getTransport("default");
            security = transport.getUDDISecurityService();
            inquiry = transport.getUDDIInquiryService();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void Browse() {
        try {
            String token = GetAuthKey("uddiadmin", "da_password1");
            BusinessList findBusiness = GetBusinessList(token);
            PrintBusinessInfo(findBusiness.getBusinessInfos());
            PrintBusinessDetails(findBusiness.getBusinessInfos(), token);
            PrintServiceDetailsByBusiness(findBusiness.getBusinessInfos(), token);
            security.discardAuthToken(new DiscardAuthToken(token));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void serviceInfo(String findServiceName) throws Exception {
        String token = GetAuthKey("uddiadmin", "da_password1");
        BusinessList findBusiness = GetBusinessList(token);
        List<BusinessInfo> businessInfo = findBusiness.getBusinessInfos().getBusinessInfo();
        for (BusinessInfo info : businessInfo) {
            for (ServiceInfo serviceInfo : info.getServiceInfos().getServiceInfo()) {
                if (serviceInfo.getName().get(0).getValue().equalsIgnoreCase(findServiceName))
                    System.out.println("Business name: " + info.getName().get(0).getValue() + "\n" +
                            "\tBusiness description: " + (!info.getDescription().isEmpty() ? info.getDescription().get(0).getValue() : "non") + "\n" +
                            "\tBusiness key: " + serviceInfo.getBusinessKey() + "\n"+
                            "\tService name:" + serviceInfo.getName().get(0).getValue() + "\n" +
                            "\tService key: " + serviceInfo.getServiceKey());

            }
        }
    }

    /**
     * Find all of the registered businesses. This list may be filtered based on access control rules
     */
    private BusinessList GetBusinessList(String token) throws Exception {
        FindBusiness fb = new FindBusiness();
        fb.setAuthInfo(token);
        org.uddi.api_v3.FindQualifiers fq = new org.uddi.api_v3.FindQualifiers();
        fq.getFindQualifier().add(UDDIConstants.APPROXIMATE_MATCH);

        fb.setFindQualifiers(fq);
        Name searchname = new Name();
        searchname.setValue(UDDIConstants.WILDCARD);
        fb.getName().add(searchname);
        return inquiry.findBusiness(fb);
    }

    /**
     * Converts category bags of tmodels to a readable string
     */
    private String CatBagToString(CategoryBag categoryBag) {
        StringBuilder sb = new StringBuilder();
        if (categoryBag == null) {
            return "no data";
        }
        for (int i = 0; i < categoryBag.getKeyedReference().size(); i++) {
            sb.append(KeyedReferenceToString(categoryBag.getKeyedReference().get(i)));
        }
        for (int i = 0; i < categoryBag.getKeyedReferenceGroup().size(); i++) {
            sb.append("Key Ref Grp: TModelKey=");
            for (int k = 0; k < categoryBag.getKeyedReferenceGroup().get(i).getKeyedReference().size(); k++) {
                sb.append(KeyedReferenceToString(categoryBag.getKeyedReferenceGroup().get(i).getKeyedReference().get(k)));
            }
        }
        return sb.toString();
    }

    private String KeyedReferenceToString(KeyedReference item) {
        return String.format("Key Ref: Name=%s  Value=%s  tModel=%s",
                item.getKeyName(), item.getKeyValue(), item.getTModelKey() + System.getProperty("line.separator"));
    }

    private void PrintContacts(Contacts contacts) {
        if (contacts == null) {
            return;
        }
        for (int i = 0; i < contacts.getContact().size(); i++) {
            System.out.println("Contact " + i + " type:" + contacts.getContact().get(i).getUseType());
            for (int k = 0; k < contacts.getContact().get(i).getPersonName().size(); k++) {
                System.out.println("Name: " + contacts.getContact().get(i).getPersonName().get(k).getValue());
            }
            for (int k = 0; k < contacts.getContact().get(i).getEmail().size(); k++) {
                System.out.println("Email: " + contacts.getContact().get(i).getEmail().get(k).getValue());
            }
            for (int k = 0; k < contacts.getContact().get(i).getAddress().size(); k++) {
                System.out.println("Address sort code " + contacts.getContact().get(i).getAddress().get(k).getSortCode());
                System.out.println("Address use type " + contacts.getContact().get(i).getAddress().get(k).getUseType());
                System.out.println("Address tmodel key " + contacts.getContact().get(i).getAddress().get(k).getTModelKey());
                for (int x = 0; x < contacts.getContact().get(i).getAddress().get(k).getAddressLine().size(); x++) {
                    System.out.println("Address line value " + contacts.getContact().get(i).getAddress().get(k).getAddressLine().get(x).getValue());
                    System.out.println("Address line key name " + contacts.getContact().get(i).getAddress().get(k).getAddressLine().get(x).getKeyName());
                    System.out.println("Address line key value " + contacts.getContact().get(i).getAddress().get(k).getAddressLine().get(x).getKeyValue());
                }
            }
            for (int k = 0; k < contacts.getContact().get(i).getDescription().size(); k++) {
                System.out.println("Desc: " + contacts.getContact().get(i).getDescription().get(k).getValue());
            }
            for (int k = 0; k < contacts.getContact().get(i).getPhone().size(); k++) {
                System.out.println("Phone: " + contacts.getContact().get(i).getPhone().get(k).getValue());
            }
        }
    }

    private void PrintServiceDetail(BusinessService get) {
        if (get == null) {
            return;
        }
        System.out.println("Name " + ListToString(get.getName()));
        System.out.println("Desc " + ListToDescString(get.getDescription()));
        System.out.println("Key " + (get.getServiceKey()));
        System.out.println("Cat bag " + CatBagToString(get.getCategoryBag()));
        if (!get.getSignature().isEmpty()) {
            System.out.println("Item is digitally signed");
        } else {
            System.out.println("Item is not digitally signed");
        }
        PrintBindingTemplates(get.getBindingTemplates());
    }

    /**
     * This function is useful for translating UDDI's somewhat complex data format to something that is more useful.
     */
    private void PrintBindingTemplates(BindingTemplates bindingTemplates) {
        if (bindingTemplates == null) {
            return;
        }
        for (int i = 0; i < bindingTemplates.getBindingTemplate().size(); i++) {
            System.out.println("Binding Key: " + bindingTemplates.getBindingTemplate().get(i).getBindingKey());

            if (bindingTemplates.getBindingTemplate().get(i).getAccessPoint() != null) {
                System.out.println("Access Point: " + bindingTemplates.getBindingTemplate().get(i).getAccessPoint().getValue() + " type " + bindingTemplates.getBindingTemplate().get(i).getAccessPoint().getUseType());
                if (bindingTemplates.getBindingTemplate().get(i).getAccessPoint().getUseType() != null) {
                    if (bindingTemplates.getBindingTemplate().get(i).getAccessPoint().getUseType().equalsIgnoreCase(AccessPointType.END_POINT.toString())) {
                        System.out.println("Use this access point value as an invocation endpoint.");
                    }
                    if (bindingTemplates.getBindingTemplate().get(i).getAccessPoint().getUseType().equalsIgnoreCase(AccessPointType.BINDING_TEMPLATE.toString())) {
                        System.out.println("Use this access point value as a reference to another binding template.");
                    }
                    if (bindingTemplates.getBindingTemplate().get(i).getAccessPoint().getUseType().equalsIgnoreCase(AccessPointType.WSDL_DEPLOYMENT.toString())) {
                        System.out.println("Use this access point value as a URL to a WSDL document, which presumably will have a real access point defined.");
                    }
                    if (bindingTemplates.getBindingTemplate().get(i).getAccessPoint().getUseType().equalsIgnoreCase(AccessPointType.HOSTING_REDIRECTOR.toString())) {
                        System.out.println("Use this access point value as an Inquiry URL of another UDDI registry, look up the same binding template there (usage varies).");
                    }
                }
            }
        }
    }

    /**
     * Gets a UDDI style auth token, otherwise, appends credentials to the ws proxies (not yet implemented)
     */
    private String GetAuthKey(String username, String password) {
        try {
            GetAuthToken getAuthTokenRoot = new GetAuthToken();
            getAuthTokenRoot.setUserID(username);
            getAuthTokenRoot.setCred(password);

            // Making API call that retrieves the authentication token for the user.
            AuthToken rootAuthToken = security.getAuthToken(getAuthTokenRoot);
            System.out.println(username + " AUTHTOKEN = (don't log auth tokens!");
            return rootAuthToken.getAuthInfo();
        } catch (Exception ex) {
            System.out.println("Could not authenticate with the provided credentials " + ex.getMessage());
        }
        return null;
    }


    private void PrintBusinessInfo(BusinessInfos businessInfos) {
        if (businessInfos == null) {
            System.out.println("No data returned");
        } else {
            for (int i = 0; i < businessInfos.getBusinessInfo().size(); i++) {
                System.out.println("===============================================");
                System.out.println("Business Key: " + businessInfos.getBusinessInfo().get(i).getBusinessKey());
                System.out.println("Name: " + ListToString(businessInfos.getBusinessInfo().get(i).getName()));
                System.out.println("Description: " + ListToDescString(businessInfos.getBusinessInfo().get(i).getDescription()));
                System.out.println("Services:");
                PrintServiceInfo(businessInfos.getBusinessInfo().get(i).getServiceInfos());
                System.out.println();
            }
        }
    }

    private String ListToString(List<Name> name) {
        StringBuilder sb = new StringBuilder();
        for (Name value : name) {
            sb.append(value.getValue()).append(" ");
        }
        return sb.toString();
    }

    private String ListToDescString(List<Description> name) {
        StringBuilder sb = new StringBuilder();
        for (Description description : name) {
            sb.append(description.getValue()).append(" ");
        }
        return sb.toString();
    }

    private void PrintServiceInfo(ServiceInfos serviceInfos) {
        for (int i = 0; i < serviceInfos.getServiceInfo().size(); i++) {
            System.out.println("-------------------------------------------");
            System.out.println("Service Key: " + serviceInfos.getServiceInfo().get(i).getServiceKey());
            System.out.println("Owning Business Key: " + serviceInfos.getServiceInfo().get(i).getBusinessKey());
            System.out.println("Name: " + ListToString(serviceInfos.getServiceInfo().get(i).getName()));
        }
    }

    private void PrintBusinessDetails(BusinessInfos businessInfos, String token) throws Exception {
        GetBusinessDetail gbd = new GetBusinessDetail();
        gbd.setAuthInfo(token);
        for (int i = 0; i < businessInfos.getBusinessInfo().size(); i++) {
            gbd.getBusinessKey().add(businessInfos.getBusinessInfo().get(i).getBusinessKey());
        }
        BusinessDetail businessDetail = inquiry.getBusinessDetail(gbd);
        for (int i = 0; i < businessDetail.getBusinessEntity().size(); i++) {
            System.out.println("Business Detail - key: " + businessDetail.getBusinessEntity().get(i).getBusinessKey());
            System.out.println("Name: " + ListToString(businessDetail.getBusinessEntity().get(i).getName()));
            System.out.println("CategoryBag: " + CatBagToString(businessDetail.getBusinessEntity().get(i).getCategoryBag()));
            PrintContacts(businessDetail.getBusinessEntity().get(i).getContacts());
        }
    }

    private void PrintServiceDetailsByBusiness(BusinessInfos businessInfos, String token) throws Exception {
        for (int i = 0; i < businessInfos.getBusinessInfo().size(); i++) {
            org.uddi.api_v3.GetServiceDetail gsd = new GetServiceDetail();
            for (int k = 0; k < businessInfos.getBusinessInfo().get(i).getServiceInfos().getServiceInfo().size(); k++) {
                gsd.getServiceKey().add(businessInfos.getBusinessInfo().get(i).getServiceInfos().getServiceInfo().get(k).getServiceKey());
            }
            gsd.setAuthInfo(token);
            System.out.println("Fetching data for business " + businessInfos.getBusinessInfo().get(i).getBusinessKey());
            ServiceDetail serviceDetail = inquiry.getServiceDetail(gsd);
            for (int k = 0; k < serviceDetail.getBusinessService().size(); k++) {
                PrintServiceDetail(serviceDetail.getBusinessService().get(k));
            }
            System.out.println("................");
        }
    }
}