
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
 *         &lt;element name="Ano" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Mes" type="{http://www.w3.org/2001/XMLSchema}int"/>
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
    "ano",
    "mes"
})
@XmlRootElement(name = "RecuperaTC_Mes")
public class RecuperaTCMes {

    @XmlElement(name = "Ano")
    protected int ano;
    @XmlElement(name = "Mes")
    protected int mes;

    /**
     * Gets the value of the ano property.
     * 
     */
    public int getAno() {
        return ano;
    }

    /**
     * Sets the value of the ano property.
     * 
     */
    public void setAno(int value) {
        this.ano = value;
    }

    /**
     * Gets the value of the mes property.
     * 
     */
    public int getMes() {
        return mes;
    }

    /**
     * Sets the value of the mes property.
     * 
     */
    public void setMes(int value) {
        this.mes = value;
    }

}
