//------------------------------------------------------------------------------------------------//
//                                                                                                //
//                                 S l u r N o t e R e l a t i o n                                //
//                                                                                                //
//------------------------------------------------------------------------------------------------//
// <editor-fold defaultstate="collapsed" desc="hdr">
//  Copyright © Herve Bitteur and others 2000-2014. All rights reserved.
//  This software is released under the GNU General Public License.
//  Goto http://kenai.com/projects/audiveris to report bugs or suggestions.
//------------------------------------------------------------------------------------------------//
// </editor-fold>
package omr.sig;

import omr.util.HorizontalSide;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class {@code SlurNoteRelation} represents a link between a slur and one of the two
 * embraced notes.
 * <p>
 * Distance from slur end to embraced note is quite complex. For the time being, we simply record
 * the detected relation without further evaluation detail.
 *
 * @author Hervé Bitteur
 */
public class SlurNoteRelation
        extends BasicSupport
{
    //~ Static fields/initializers -----------------------------------------------------------------

    //    private static final Constants constants = new Constants();
    private static final Logger logger = LoggerFactory.getLogger(SlurNoteRelation.class);

    //~ Instance fields ----------------------------------------------------------------------------
    //    private static final double[] IN_WEIGHTS = new double[]{
    //        constants.xInWeight.getValue(),
    //        constants.yWeight.getValue()
    //    };
    //
    //    private static final double[] OUT_WEIGHTS = new double[]{
    //        constants.xOutWeight.getValue(),
    //        constants.yWeight.getValue()
    //    };
    //
    /** Left or right side of the slur. */
    private final HorizontalSide side;

    //~ Constructors -------------------------------------------------------------------------------
    /**
     * Creates a new SlurNoteRelation object.
     *
     * @param side the left or right side of the slur
     */
    public SlurNoteRelation (HorizontalSide side)
    {
        this.side = side;
    }

    //~ Methods ------------------------------------------------------------------------------------
    //---------//
    // getName //
    //---------//
    @Override
    public String getName ()
    {
        return "Slur-Note";
    }

    //---------//
    // getSide //
    //---------//
    /**
     * @return the side
     */
    public HorizontalSide getSide ()
    {
        return side;
    }

    //----------//
    // toString //
    //----------//
    @Override
    public String toString ()
    {
        return super.toString() + "/" + side;
    }

    //----------------//
    // getTargetCoeff //
    //----------------//
    /**
     * SlurNoteRelation brings no support on target (note) side.
     *
     * @return 0
     */
    @Override
    protected double getTargetCoeff ()
    {
        return 0.0;
    }

    //    //-------------------//
    //    // getXOutGapMaximum //
    //    //-------------------//
    //    public static Scale.Fraction getXOutGapMaximum ()
    //    {
    //        return constants.xOutGapMax;
    //    }
    //
    //    //----------------//
    //    // getYGapMaximum //
    //    //----------------//
    //    public static Scale.Fraction getYGapMaximum ()
    //    {
    //        return constants.yGapMax;
    //    }
    //
    //    //------------------//
    //    // getXInGapMaximum //
    //    //------------------//
    //    public static Scale.Fraction getXInGapMaximum ()
    //    {
    //        return constants.xInGapMax;
    //    }
    //
    //    //--------------//
    //    // getInWeights //
    //    //--------------//
    //    @Override
    //    protected double[] getInWeights ()
    //    {
    //        return IN_WEIGHTS;
    //    }
    //
    //    //---------------//
    //    // getOutWeights //
    //    //---------------//
    //    @Override
    //    protected double[] getOutWeights ()
    //    {
    //        return OUT_WEIGHTS;
    //    }
    //
    //    //----------------//
    //    // getTargetCoeff //
    //    //----------------//
    //    /**
    //     * StaccatoNoteRelation brings no support on target (Note) side.
    //     *
    //     * @return 0
    //     */
    //    @Override
    //    protected double getTargetCoeff ()
    //    {
    //        return 0.0;
    //    }
    //
    //    @Override
    //    protected Scale.Fraction getXInGapMax ()
    //    {
    //        return getXInGapMaximum();
    //    }
    //
    //    @Override
    //    protected Scale.Fraction getXOutGapMax ()
    //    {
    //        return getXOutGapMaximum();
    //    }
    //
    //    @Override
    //    protected Scale.Fraction getYGapMax ()
    //    {
    //        return getYGapMaximum();
    //    }
    //
    //    //~ Inner Classes ------------------------------------------------------------------------------
    //    //-----------//
    //    // Constants //
    //    //-----------//
    //    private static final class Constants
    //            extends ConstantSet
    //    {
    //        //~ Instance fields ------------------------------------------------------------------------
    //
    //        final Scale.Fraction xInGapMax = new Scale.Fraction(
    //                0.5,
    //                "Maximum horizontal overlap between slur end & note reference point");
    //
    //        final Scale.Fraction xOutGapMax = new Scale.Fraction(
    //                0.75,
    //                "Maximum horizontal gap between slur end & note reference point");
    //
    //        final Scale.Fraction yGapMax = new Scale.Fraction(
    //                6.0,
    //                "Maximum vertical gap between slur end & note reference point");
    //
    //        final Constant.Ratio xInWeight = new Constant.Ratio(3, "Relative impact weight for xInGap");
    //
    //        final Constant.Ratio xOutWeight = new Constant.Ratio(
    //                3,
    //                "Relative impact weight for xOutGap");
    //
    //        final Constant.Ratio yWeight = new Constant.Ratio(1, "Relative impact weight for yGap");
    //    }
}