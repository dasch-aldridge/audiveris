//------------------------------------------------------------------------------------------------//
//                                                                                                //
//                                      L e d g e r s S t e p                                     //
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
package omr.sheet.ledger;

import omr.lag.Section;

import omr.sheet.Sheet;
import omr.sheet.SystemInfo;

import omr.step.AbstractSystemStep;
import omr.step.Step;
import omr.step.StepException;

import java.util.List;
import java.util.Map;

/**
 * Class {@code LedgersStep} implements <b>LEDGERS</b> step, which retrieves all
 * possible ledger interpretations.
 *
 * @author Hervé Bitteur
 */
public class LedgersStep
        extends AbstractSystemStep<LedgersStep.Context>
{
    //~ Constructors -------------------------------------------------------------------------------

    /**
     * Creates a new LedgersStep object.
     */
    public LedgersStep ()
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
        // Add ledger checkboard
        new LedgersBuilder(sheet.getSystems().get(0), null).addCheckBoard();
    }

    //----------//
    // doSystem //
    //----------//
    @Override
    public void doSystem (SystemInfo system,
                          Context context)
            throws StepException
    {
        final List<Section> sections = context.sectionMap.get(system);
        new LedgersBuilder(system, sections).buildLedgers();
    }

    //----------//
    // doProlog //
    //----------//
    /**
     * {@inheritDoc}
     * <p>
     * Retrieve horizontal sticks for ledger candidates, mapped by related system(s).
     *
     * @return the context (map of sections per system)
     */
    @Override
    protected Context doProlog (Sheet sheet)
            throws StepException
    {
        return new Context(new LedgersFilter(sheet).process());
    }

    //~ Inner Classes ------------------------------------------------------------------------------
    //---------//
    // Context //
    //---------//
    protected static class Context
    {
        //~ Instance fields ------------------------------------------------------------------------

        public final Map<SystemInfo, List<Section>> sectionMap;

        //~ Constructors ---------------------------------------------------------------------------
        public Context (Map<SystemInfo, List<Section>> sectionMap)
        {
            this.sectionMap = sectionMap;
        }
    }
}
