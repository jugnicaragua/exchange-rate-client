
package ni.jug.ncb.exchangerate.ws;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ni.jug.ncb.exchangerate.ws package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ni.jug.ncb.exchangerate.ws
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RecuperaTCMesResponse }
     * 
     */
    public RecuperaTCMesResponse createRecuperaTCMesResponse() {
        return new RecuperaTCMesResponse();
    }

    /**
     * Create an instance of {@link RecuperaTCMesResponse.RecuperaTCMesResult }
     * 
     */
    public RecuperaTCMesResponse.RecuperaTCMesResult createRecuperaTCMesResponseRecuperaTCMesResult() {
        return new RecuperaTCMesResponse.RecuperaTCMesResult();
    }

    /**
     * Create an instance of {@link RecuperaTCDiaResponse }
     * 
     */
    public RecuperaTCDiaResponse createRecuperaTCDiaResponse() {
        return new RecuperaTCDiaResponse();
    }

    /**
     * Create an instance of {@link RecuperaTCMes }
     * 
     */
    public RecuperaTCMes createRecuperaTCMes() {
        return new RecuperaTCMes();
    }

    /**
     * Create an instance of {@link RecuperaTCDia }
     * 
     */
    public RecuperaTCDia createRecuperaTCDia() {
        return new RecuperaTCDia();
    }

}
