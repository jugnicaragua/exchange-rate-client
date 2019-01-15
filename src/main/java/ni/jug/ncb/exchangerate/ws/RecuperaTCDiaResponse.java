
package ni.jug.ncb.exchangerate.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RecuperaTC_DiaResult" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "recuperaTCDiaResult"
})
@XmlRootElement(name = "RecuperaTC_DiaResponse")
public class RecuperaTCDiaResponse {

    @XmlElement(name = "RecuperaTC_DiaResult")
    protected double recuperaTCDiaResult;

    /**
     * Gets the value of the recuperaTCDiaResult property.
     * 
     */
    public double getRecuperaTCDiaResult() {
        return recuperaTCDiaResult;
    }

    /**
     * Sets the value of the recuperaTCDiaResult property.
     * 
     */
    public void setRecuperaTCDiaResult(double value) {
        this.recuperaTCDiaResult = value;
    }

}
