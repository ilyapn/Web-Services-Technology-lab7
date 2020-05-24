import org.apache.commons.configuration.ConfigurationException;
import org.apache.juddi.api_v3.AccessPointType;
import org.apache.juddi.v3.client.config.UDDIClerk;
import org.apache.juddi.v3.client.config.UDDIClient;
import org.uddi.api_v3.*;

public class Registrar {
    private UDDIClerk clerk = new UDDIClient("src\\main\\resources\\WEB_INF\\uddi.xml").getClerk("default");

    public Registrar() throws ConfigurationException {
    }


    void register(String businessName, String serviceName, String wsdlAddress) throws Exception {
        BusinessEntity myBusEntity = new BusinessEntity();
        Name myBusName = new Name();
        myBusName.setValue(businessName);
        myBusEntity.getName().add(myBusName);

        BusinessEntity register = clerk.register(myBusEntity);
        if (register == null)
            throw new Exception("register failed");
        String myBusKey = register.getBusinessKey();
        System.out.println(String.format("Key of business '%s' is '%s'.", businessName, myBusKey));

        BusinessService myService = new BusinessService();
        myService.setBusinessKey(myBusKey);
        Name myServiceName = new Name();
        myServiceName.setValue(serviceName);
        myService.getName().add(myServiceName);

        BindingTemplate myBindingTemplate = new BindingTemplate();
        AccessPoint accessPoint = new AccessPoint();
        accessPoint.setUseType(AccessPointType.WSDL_DEPLOYMENT.toString());
        accessPoint.setValue(wsdlAddress);
        myBindingTemplate.setAccessPoint(accessPoint);
        BindingTemplates myBindingTemplates = new BindingTemplates();

        myBindingTemplates.getBindingTemplate().add(UDDIClient.addSOAPtModels(myBindingTemplate));
        myService.setBindingTemplates(myBindingTemplates);

        BusinessService svc = clerk.register(myService);
        if (svc == null) {
            System.out.println("Save failed!");
            System.exit(1);
        }
        System.out.println(String.format("Key of service '%s' is '%s'.", serviceName, svc.getServiceKey()));

        clerk.discardAuthToken();
        System.out.println("Success!");
    }
}
