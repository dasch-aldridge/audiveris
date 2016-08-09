//------------------------------------------------------------------------------------------------//
//                                                                                                //
//                                           T e m p o                                            //
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
package omr.score;

import omr.constant.Constant;
import omr.constant.ConstantSet;

import omr.util.Param;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class {@code Tempo} handles the default tempo value.
 *
 * @author Hervé Bitteur
 */
public abstract class Tempo
{
    //~ Static fields/initializers -----------------------------------------------------------------

    private static final Constants constants = new Constants();

    private static final Logger logger = LoggerFactory.getLogger(Tempo.class);

    /** Default parameter. */
    public static final Param<Integer> defaultTempo = new Default();

    //~ Constructors -------------------------------------------------------------------------------
    /** Not meant to be instantiated. */
    private Tempo ()
    {
    }

    //~ Inner Classes ------------------------------------------------------------------------------
    //-----------//
    // Constants //
    //-----------//
    private static final class Constants
            extends ConstantSet
    {
        //~ Instance fields ------------------------------------------------------------------------

        private final Constant.Integer defaultTempo = new Constant.Integer(
                "QuartersPerMn",
                120,
                "Default tempo, stated in number of quarters per minute");
    }

    //---------//
    // Default //
    //---------//
    private static class Default
            extends Param<Integer>
    {
        //~ Methods --------------------------------------------------------------------------------

        @Override
        public Integer getSpecific ()
        {
            return constants.defaultTempo.getValue();
        }

        @Override
        public boolean setSpecific (Integer specific)
        {
            if (!getSpecific().equals(specific)) {
                constants.defaultTempo.setValue(specific);
                logger.info("Default tempo is now {}", specific);

                return true;
            } else {
                return false;
            }
        }
    }
}
