//------------------------------------------------------------------------------------------------//
//                                                                                                //
//                                     S y m b o l s S t e p                                      //
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
package omr.sheet.symbol;

import omr.glyph.Glyph;
import omr.glyph.ui.SymbolsEditor;

import omr.sheet.Sheet;
import omr.sheet.SystemInfo;
import omr.sheet.note.ChordsBuilder;

import omr.step.AbstractSystemStep;
import omr.step.Step;
import omr.step.StepException;

import omr.ui.selection.EntityListEvent;
import omr.ui.selection.SelectionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class {@code SymbolsStep} retrieves fixed-shape symbols in a system.
 * <p>
 * This accounts for rests, flags, dots, tuplets, alterations, ...
 *
 * @author Hervé Bitteur
 */
public class SymbolsStep
        extends AbstractSystemStep<SymbolsStep.Context>
{
    //~ Static fields/initializers -----------------------------------------------------------------

    private static final Logger logger = LoggerFactory.getLogger(SymbolsStep.class);

    //~ Constructors -------------------------------------------------------------------------------
    /**
     * Creates a new SymbolsStep object.
     */
    public SymbolsStep ()
    {
    }

    //~ Methods ------------------------------------------------------------------------------------
    //-----------//
    // displayUI //
    //-----------//
    @Override
    public void displayUI (Step step,
                           Sheet sheet)
    {
        final SymbolsEditor editor = sheet.getSymbolsEditor();

        if (editor != null) {
            editor.refresh();
        }

        // Update glyph board if needed (to see OCR'ed data)
        final SelectionService service = sheet.getGlyphIndex().getEntityService();
        final EntityListEvent<Glyph> listEvent = (EntityListEvent<Glyph>) service.getLastEvent(
                EntityListEvent.class);

        if (listEvent != null) {
            service.publish(listEvent);
        }
    }

    //----------//
    // doSystem //
    //----------//
    @Override
    public void doSystem (SystemInfo system,
                          Context context)
            throws StepException
    {
        final SymbolFactory factory = new SymbolFactory(system);

        // Retrieve symbols inters
        new SymbolsBuilder(system, factory).buildSymbols(context.optionalsMap);

        // Allocate rest-based chords
        new ChordsBuilder(system).buildRestChords();

        // Retrieve relations between symbols inters
        factory.linkSymbols();

        // Compute all contextual grades (for better UI)
        system.getSig().contextualize();
    }

    //----------//
    // doProlog //
    //----------//
    @Override
    protected Context doProlog (Sheet sheet)
            throws StepException
    {
        /**
         * Prepare image without staff lines, with all good inters erased and with all
         * weak inters saved as optional symbol parts.
         */
        final Context context = new Context();
        new SymbolsFilter(sheet).process(context.optionalsMap);

        return context;
    }

    //~ Inner Classes ------------------------------------------------------------------------------
    //---------//
    // Context //
    //---------//
    protected static class Context
    {
        //~ Instance fields ------------------------------------------------------------------------

        /** Map of optional (weak) glyphs per system. */
        public final Map<SystemInfo, List<Glyph>> optionalsMap = new TreeMap<SystemInfo, List<Glyph>>();
    }
}
