//------------------------------------------------------------------------------------------------//
//                                                                                                //
//                                       M y C o m p o u n d                                      //
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
package omr.jaxb.itf;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class {@code MyCompound}
 *
 * @author Hervé Bitteur
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MyCompound
{
    //~ Instance fields ----------------------------------------------------------------------------

    @XmlIDREF
    MyGlyph topGlyph;

    @XmlIDREF
    MyGlyph bottomGlyph;

    @XmlIDREF
    MySymbol leftSymbol;

    @XmlIDREF
    MySymbol rightSymbol;

    MyBasicIndex<MyEntity> index;

    //~ Constructors -------------------------------------------------------------------------------
    /**
     * Creates a new {@code MyCompound} object.
     */
    public MyCompound ()
    {
    }
}
