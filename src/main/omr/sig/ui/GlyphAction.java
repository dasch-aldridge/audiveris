//------------------------------------------------------------------------------------------------//
//                                                                                                //
//                                      G l y p h A c t i o n                                     //
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
package omr.sig.ui;

import omr.glyph.Glyph;
import omr.glyph.GlyphIndex;

import omr.ui.selection.EntityListEvent;
import omr.ui.selection.MouseMovement;
import omr.ui.selection.SelectionHint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;

/**
 * Action related to UI glyph selection.
 *
 * @author Hervé Bitteur
 */
public class GlyphAction
        extends AbstractAction
{
    //~ Static fields/initializers -----------------------------------------------------------------

    private static final Logger logger = LoggerFactory.getLogger(GlyphAction.class);

    //~ Instance fields ----------------------------------------------------------------------------
    /** The underlying glyph. */
    private final Glyph glyph;

    //~ Constructors -------------------------------------------------------------------------------
    /**
     * Creates a new GlyphAction object.
     *
     * @param glyph the underlying glyph
     */
    public GlyphAction (Glyph glyph)
    {
        this(glyph, null);
    }

    /**
     * Creates a new GlyphAction object.
     *
     * @param glyph the underlying glyph
     * @param text  specific item text, if any
     */
    public GlyphAction (Glyph glyph,
                        String text)
    {
        this.glyph = glyph;
        putValue(NAME, (text != null) ? text : ("" + glyph.getId()));
        putValue(SHORT_DESCRIPTION, tipOf(glyph));
    }

    //~ Methods ------------------------------------------------------------------------------------
    //-----------------//
    // actionPerformed //
    //-----------------//
    @Override
    public void actionPerformed (ActionEvent e)
    {
        publish();
    }

    //---------//
    // publish //
    //---------//
    public void publish ()
    {
        GlyphIndex glyphIndex = glyph.getIndex();

        if (glyphIndex == null) {
            logger.warn("No index for {}", glyph);
        } else {
            glyphIndex.getEntityService().publish(
                    new EntityListEvent<Glyph>(
                            this,
                            SelectionHint.ENTITY_INIT,
                            MouseMovement.PRESSING,
                            Arrays.asList(glyph)));
        }
    }

    //-------//
    // tipOf //
    //-------//
    private String tipOf (Glyph glyph)
    {
        String tip = "groups: " + glyph.getGroups();

        return tip;
    }
}
