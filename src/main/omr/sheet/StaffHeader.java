//------------------------------------------------------------------------------------------------//
//                                                                                                //
//                                      S t a f f H e a d e r                                     //
//                                                                                                //
//------------------------------------------------------------------------------------------------//
// <editor-fold defaultstate="collapsed" desc="hdr">
//  Copyright © Herve Bitteur and others 2000-2014. All rights reserved.
//  This software is released under the GNU General Public License.
//  Goto http://kenai.com/projects/audiveris to report bugs or suggestions.
//------------------------------------------------------------------------------------------------//
// </editor-fold>
package omr.sheet;

import omr.sig.inter.ClefInter;
import omr.sig.inter.KeyInter;
import omr.sig.inter.TimeInter;

import java.util.List;

/**
 * Class {@code StaffHeader} gathers information about the (Clef + Key + Time) sequence
 * at the beginning of a staff.
 *
 * @author Hervé Bitteur
 */
public class StaffHeader
{
    //~ Instance fields ----------------------------------------------------------------------------

    /**
     * Abscissa for start of staff header.
     * This is typically the point right after the right-most bar line of the starting bar group,
     * or the beginning abscissa of staff lines when there is no left bar line.
     */
    public final int start;

    /** Abscissa for end of staff header. */
    public int stop;

    /** Abscissa range for clef. */
    public Range clefRange;

    /** Clef found. */
    public ClefInter clef;

    /** Abscissa range for key. */
    public Range keyRange;

    /** Abscissa for start of each key alter. */
    public List<Integer> alterStarts;

    /** Key-sig found, if any. */
    public KeyInter key;

    /** Abscissa range for time. */
    public Range timeRange;

    /** Time-sig found, if any. */
    public TimeInter time;

    //~ Constructors -------------------------------------------------------------------------------

    /**
     * Creates a new {@code StaffHeader} object.
     *
     * @param start start of header (measure start)
     */
    public StaffHeader (int start)
    {
        this.start = start;
    }

    //~ Methods ------------------------------------------------------------------------------------

    @Override
    public String toString ()
    {
        StringBuilder sb = new StringBuilder("Header{");

        sb.append("start=").append(start);
        sb.append(" stop=").append(stop);

        if (clefRange != null) {
            sb.append(" CLEF(").append(clefRange).append(")");
        }

        if (keyRange != null) {
            sb.append(" KEY(").append(keyRange);

            if (alterStarts != null) {
                sb.append(" alters=").append(alterStarts);
            }

            sb.append(")");
        }

        if (timeRange != null) {
            sb.append(" TIME(").append(timeRange).append(")");
        }

        sb.append(" clef:").append((clef != null) ? clef : "null");
        sb.append(" key:").append((key != null) ? key : "null");
        sb.append(" time:").append((time != null) ? time : "null");

        sb.append("}");

        return sb.toString();
    }

    //~ Inner Classes ------------------------------------------------------------------------------

    public static class Range
    {
        //~ Instance fields ------------------------------------------------------------------------

        /** Was the item successfully retrieved?. */
        public boolean valid;

        /** Abscissa for beginning of browsing. */
        public int browseStart;

        /** Abscissa for end of browsing. */
        public int browseStop;

        /** Precise beginning of item. */
        public int start;

        /** Precise end of item. */
        public int stop;

        /** Precise end of system-aligned item. */
        public int systemStop;

        //~ Methods --------------------------------------------------------------------------------

        @Override
        public String toString ()
        {
            StringBuilder sb = new StringBuilder();

            if (valid) {
                sb.append("SUCCESS");
            } else {
                sb.append("FAILURE");
            }

            if (browseStart != 0) {
                sb.append(" bStart=").append(browseStart);
            }

            if (browseStop != 0) {
                sb.append(" bStop=").append(browseStop);
            }

            if (start != 0) {
                sb.append(" start=").append(start);
            }

            if (stop != 0) {
                sb.append(" stop=").append(stop);
            }

            if (systemStop != 0) {
                sb.append(" systemStop=").append(systemStop);
            }

            return sb.toString();
        }
    }
}