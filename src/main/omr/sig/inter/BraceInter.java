//------------------------------------------------------------------------------------------------//
//                                                                                                //
//                                       B r a c e I n t e r                                      //
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
package omr.sig.inter;

import omr.glyph.Glyph;
import omr.glyph.Shape;

import omr.sheet.Staff;
import omr.sheet.SystemInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Point;
import java.awt.Rectangle;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class {@code BraceInter} represents a brace.
 *
 * @author Hervé Bitteur
 */
@XmlRootElement(name = "brace")
public class BraceInter
        extends AbstractInter
{
    //~ Static fields/initializers -----------------------------------------------------------------

    private static final Logger logger = LoggerFactory.getLogger(BraceInter.class);

    //~ Constructors -------------------------------------------------------------------------------
    /**
     * Creates a new BraceInter object.
     *
     * @param glyph underlying glyph
     * @param grade evaluation value
     */
    public BraceInter (Glyph glyph,
                       double grade)
    {
        super(glyph, null, Shape.BRACE, grade);
    }

    /**
     * No-arg constructor meant for JAXB.
     */
    private BraceInter ()
    {
        super(null, null, null, null);
    }

    //~ Methods ------------------------------------------------------------------------------------
    //--------//
    // accept //
    //--------//
    @Override
    public void accept (InterVisitor visitor)
    {
        visitor.visit(this);
    }

    //-----------//
    // getBounds //
    //-----------//
    @Override
    public Rectangle getBounds ()
    {
        if (bounds == null) {
            if (glyph != null) {
                // Extend brace glyph box to related part
                final SystemInfo system = sig.getSystem();
                final Rectangle box = glyph.getBounds();
                final int xRight = box.x + box.width;

                try {
                    final Staff staff1 = system.getClosestStaff(new Point(xRight, box.y));
                    final Staff staff2 = system.getClosestStaff(
                            new Point(xRight, box.y + box.height - 1));
                    final int y1 = staff1.getFirstLine().yAt(xRight);
                    final int y2 = staff2.getLastLine().yAt(xRight);
                    bounds = new Rectangle(box.x, y1, box.width, y2 - y1 + 1);
                } catch (Exception ex) {
                    logger.warn("Error in getBounds for {}", this, ex);
                }
            }
        }

        if (bounds != null) {
            return new Rectangle(bounds);
        }

        return null;
    }
}
