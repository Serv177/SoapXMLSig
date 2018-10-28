package hello;

import hello.wsdl.GetCountryRequest;
import hello.wsdl.GetCountryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

public class CountryClient extends WebServiceGatewaySupport {

    private static final Logger log = LoggerFactory.getLogger(CountryClient.class);

    public GetCountryResponse getCountry(String country) throws Exception {

        GetCountryRequest request = new GetCountryRequest();
        request.setName(country);

        log.info("Requesting location for " + country);

        WebServiceTemplate webServiceTemplate = getWebServiceTemplate();
        webServiceTemplate.setDefaultUri("http://localhost:8080/ws/countries");
        /*
        HeaderInterceptor headerInterceptor = new HeaderInterceptor();
        headerInterceptor.setSecurementActions("Signature Encrypt");
        headerInterceptor.setSecurementUsername("server");
        headerInterceptor.setSecurementPassword("changeit");
        org.apache.xml.security.Init.init();
        CryptoFactoryBean cryptoFactoryBean = new CryptoFactoryBean();
        cryptoFactoryBean.setKeyStorePassword("1234");
        cryptoFactoryBean.setKeyStoreLocation(new ClassPathResource("C:\\Users\\Daniel\\Desktop\\pkijs_pkcs12.p12"));
        cryptoFactoryBean.setKeyStoreType("pkcs12");
        cryptoFactoryBean.afterPropertiesSet();
        headerInterceptor.setSecurementSignatureCrypto(cryptoFactoryBean.getObject());

        ClientInterceptor[] interceptors = {headerInterceptor};
        webServiceTemplate.setInterceptors(interceptors);*/
        TestWebServiceMessageCallback testWebServiceMessageCallback = new TestWebServiceMessageCallback();
        //SoapActionCallback soapActionCallback = new SoapActionCallback("http://spring.io/guides/gs-producing-web-service/GetCountryRequest");
        GetCountryResponse response = (GetCountryResponse) webServiceTemplate.marshalSendAndReceive(request, testWebServiceMessageCallback);

        return response;
    }

}