package hello;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import sun.misc.BASE64Encoder;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TestWebServiceMessageCallback implements WebServiceMessageCallback {

    @Override
    public void doWithMessage(WebServiceMessage webServiceMessage) throws IOException, TransformerException {

        try {
            SaajSoapMessage message = (SaajSoapMessage) webServiceMessage;
            SOAPMessage soapMessage = message.getSaajMessage();
            SOAPHeader header = soapMessage.getSOAPHeader();
            KeyPair keyPair = getKeyPair();
            SOAPHeaderElement security = addHeader(header, keyPair.getPublic());

            sign(security, keyPair.getPrivate());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            soapMessage.writeTo(out);
            String strMsg = new String(out.toByteArray());
            System.out.println(strMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SOAPHeaderElement addHeader(SOAPHeader header, PublicKey aPublic) throws SOAPException {
        SOAPHeaderElement security = header.addHeaderElement(new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security", "wsse"));

        SOAPElement binarySecurityToklen = security.addChildElement("BinarySecurityToken", "wsse");
        binarySecurityToklen.addNamespaceDeclaration("wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
        binarySecurityToklen.addAttribute(new QName("EncodingType"), "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary");
        binarySecurityToklen.addAttribute(new QName("ValueType"), "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3");
        binarySecurityToklen.addAttribute(new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id", "wsu"), "Security_1");
        String publicKey = new BASE64Encoder().encode(aPublic.getEncoded());
        binarySecurityToklen.setTextContent(publicKey);
        SOAPElement child = security.addChildElement("child", "wsse");
        SOAPElement child2 = security.addChildElement("child", "wsse");

        child.addAttribute(new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id", "wsu"), "test");
        child2.addAttribute(new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id", "wsu"), "test2");

        return security;
    }

    private void sign(SOAPHeaderElement securityHeader, PrivateKey aPrivate) throws MarshalException, XMLSignatureException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, SOAPException {
        SOAPElement securityRef = addReference(securityHeader);
        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
        DigestMethod digestMethod = fac.newDigestMethod("http://www.w3.org/2001/04/xmlenc#sha256", null);
        List<Transform> transform = Collections.singletonList(fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null));
        Reference ref = fac.newReference("#test", digestMethod, transform, null, null);
        Reference ref2 = fac.newReference("#test2", digestMethod, transform, null, null);
        ArrayList refList = new ArrayList();
        refList.add(ref);
        refList.add(ref2);
        C14NMethodParameterSpec c14NMethodParameterSpec = null;
        CanonicalizationMethod cm = fac.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE, c14NMethodParameterSpec);
        SignatureMethod sm = fac.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", null);
        SignedInfo signedInfo = fac.newSignedInfo(cm, sm, refList);
        DOMSignContext signContext = null;
        signContext = new DOMSignContext(aPrivate, securityHeader);
        KeyInfoFactory keyFactory = KeyInfoFactory.getInstance();
        DOMStructure domKeyInfo = new DOMStructure(securityRef);
        KeyInfo keyInfo =
                keyFactory.newKeyInfo(Collections.singletonList(domKeyInfo));
        XMLSignature signature = fac.newXMLSignature(signedInfo, keyInfo);
        signature.sign(signContext);
    }

    private SOAPElement addReference(SOAPHeaderElement securityHeader) throws SOAPException {
        SOAPElement refernce = securityHeader.addChildElement("SecurityTokenReference", "wsse");
        SOAPElement refChild = refernce.addChildElement("Reference", "wsse");
        refChild.addAttribute(new QName("ValueType"), "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3");
        refChild.addAttribute(new QName("URI"), "#Security_1");
        return refernce;
    }

    private KeyPair getKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();
        return kp;
    }
}
