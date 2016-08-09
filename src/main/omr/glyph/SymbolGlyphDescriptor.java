//------------------------------------------------------------------------------------------------//
//                                                                                                //
//                           S y m b o l G l y p h D e s c r i p t o r                            //
//                                                                                                //
//------------------------------------------------------------------------------------------------//
// <editor-fold defaultstate="collapsed" desc="hdr">
//
//  Copyright © Hervé Bitteur and others 2000-2016. All rights reserved.
//
//  This program is free software: you can redistribute it and/or modify it under the terms of the
//  GNU Affero General Public License as published by the Free Software Foundation, either version
//  3 of the License, or (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
//  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//  See the GNU Affero General Public License for more details.
//
//  You should have received a copy of the GNU Affero General Public License along with this
//  program.  If not, see <http://www.gnu.org/licenses/>.
//------------------------------------------------------------------------------------------------//
// </editor-fold>
package omr.glyph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Point;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Class {@code SymbolGlyphDescriptor} brings additional information to a mere shaped
 * glyph.
 * <p>
 * Such a descriptor contains a simple reference to the related shape, whose appearance will be
 * drawn thanks to MusicFont.
 * The descriptor can be augmented by informations such as stem number, with
 * ledger, pitch position, reference point.
 * These informations are thus copied to the {@link SymbolSample} instance for better training.
 * We can have several descriptors from the same shape, which allows different values for additional
 * informations (for example, the stem number may be 1 or 2 for NOTEHEAD_BLACK shape).
 *
 * @author Hervé Bitteur
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "", propOrder = {
    "refPoint", "pitchPosition"}
)
@XmlRootElement(name = "symbol-desc")
public class SymbolGlyphDescriptor
{
    //~ Static fields/initializers -----------------------------------------------------------------

    private static final Logger logger = LoggerFactory.getLogger(
            SymbolGlyphDescriptor.class);

    /** Un/marshalling context for use with JAXB */
    private static JAXBContext jaxbContext;

    //~ Instance fields ----------------------------------------------------------------------------
    /** Image related interline value */
    @XmlAttribute
    private Integer interline;

    /** Related name (generally the name of the related shape if any) */
    @XmlAttribute
    private String name;

    /** Pitch position within staff lines */
    @XmlElement(name = "pitch-position")
    private Double pitchPosition;

    /** Reference point, if any */
    @XmlElement(name = "ref-point")
    private Point refPoint;

    //~ Constructors -------------------------------------------------------------------------------
    /**
     * No-arg constructor for the XML mapper.
     */
    private SymbolGlyphDescriptor ()
    {
    }

    //~ Methods ------------------------------------------------------------------------------------
    //---------//
    // getName //
    //---------//
    /**
     * Report the name (generally the shape name) of the symbol
     *
     * @return the symbol name
     */
    public String getName ()
    {
        return name;
    }

    //------------------//
    // getPitchPosition //
    //------------------//
    /**
     * Report the pitch position within the staff lines
     *
     * @return the pitch position
     */
    public Double getPitchPosition ()
    {
        return pitchPosition;
    }

    //-------------//
    // getRefPoint //
    //-------------//
    /**
     * Report the assigned reference point
     *
     * @return the ref point, which may be null
     */
    public Point getRefPoint ()
    {
        return refPoint;
    }

    //-------------------//
    // loadFromXmlStream //
    //-------------------//
    /**
     * Load a symbol description from an XML stream.
     *
     * @param is the input stream
     * @return a new SymbolGlyphDescriptor, or null if loading has failed
     */
    public static SymbolGlyphDescriptor loadFromXmlStream (InputStream is)
    {
        try {
            return (SymbolGlyphDescriptor) jaxbUnmarshal(is);
        } catch (Exception ex) {
            ex.printStackTrace();

            // User already notified
            return null;
        }
    }

    //----------//
    // toString //
    //----------//
    @Override
    public String toString ()
    {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append("{");

        if (name != null) {
            sb.append(" name:").append(name);
        }

        if (interline != null) {
            sb.append(" interline:").append(interline);
        }

        if (pitchPosition != null) {
            sb.append(" pitch-position:").append(pitchPosition);
        }

        sb.append("}");

        return sb.toString();
    }

    //----------------//
    // getJaxbContext //
    //----------------//
    private static JAXBContext getJaxbContext ()
            throws JAXBException
    {
        // Lazy creation
        if (jaxbContext == null) {
            jaxbContext = JAXBContext.newInstance(SymbolGlyphDescriptor.class);
        }

        return jaxbContext;
    }

    //---------------//
    // jaxbUnmarshal //
    //---------------//
    private static Object jaxbUnmarshal (InputStream is)
            throws JAXBException
    {
        Unmarshaller um = getJaxbContext().createUnmarshaller();

        return um.unmarshal(is);
    }
}
