//----------------------------------------------------------------------------//
//                                                                            //
//                     C l u s t e r s R e t r i e v e r                      //
//                                                                            //
//----------------------------------------------------------------------------//
// <editor-fold defaultstate="collapsed" desc="hdr">                          //
//  Copyright (C) Herve Bitteur 2000-2010. All rights reserved.               //
//  This software is released under the GNU General Public License.           //
//  Goto http://kenai.com/projects/audiveris to report bugs or suggestions.   //
//----------------------------------------------------------------------------//
// </editor-fold>
package omr.sheet.grid;

import omr.constant.Constant;
import omr.constant.ConstantSet;

import omr.log.Logger;

import omr.math.Histogram;

import omr.score.common.PixelPoint;
import omr.score.common.PixelRectangle;
import omr.score.ui.PagePainter;

import omr.sheet.Scale;
import omr.sheet.Sheet;

import omr.util.Wrapper;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;

/**
 * Class {@code ClustersRetriever} performs vertical samplings of the
 * horizontal filaments in order to detect regular patterns of a preferred
 * interline value and aggregate the filament into clusters of lines.
 *
 * @author Hervé Bitteur
 */
public class ClustersRetriever
{
    //~ Static fields/initializers ---------------------------------------------

    /** Specific application parameters */
    private static final Constants constants = new Constants();

    /** Usual logger utility */
    private static final Logger logger = Logger.getLogger(
        ClustersRetriever.class);

    /** Stroke for drawing patterns */
    private static final Stroke patternStroke = new BasicStroke(0.5f);

    //~ Instance fields --------------------------------------------------------

    /** Comparator on cluster ordinate */
    public Comparator<LineCluster> ordinateComparator = new Comparator<LineCluster>() {
        public int compare (LineCluster c1,
                            LineCluster c2)
        {
            return Integer.signum(ordinateOf(c1) - ordinateOf(c2));
        }
    };

    /** Related sheet */
    private final Sheet sheet;

    /** Related scale */
    private final Scale scale;

    /** Picture width to sample for patterns */
    private final int pictureWidth;

    /** Long filaments found, sorted by Y then x */
    private final Set<Filament> filaments;

    /** Global slope of the sheet */
    private double globalSlope;

    /** Sheet top edge, tilted as global slope */
    private Line2D.Double sheetTopEdge;

    /** A map (colIndex -> vertical list of samples), sorted on colIndex */
    private Map<Integer, List<FilamentPattern>> colPatterns;

    /**
     * The popular length of patterns detected for the specified interline
     * (typically: 4, 5 or 6)
     */
    private int popLength;

    /** X values per column index */
    private int[] colX;

    /** Collection of clusters */
    private final List<LineCluster> clusters = new ArrayList<LineCluster>();

    //~ Constructors -----------------------------------------------------------

    //-------------------//
    // ClustersRetriever //
    //-------------------//
    /**
     * Creates a new ClustersRetriever object.
     *
     * @param sheet the sheet to process
     * @param filaments the current collection of filaments
     * @param globalSlope the global scope detected for the sheet
     */
    public ClustersRetriever (Sheet         sheet,
                              Set<Filament> filaments,
                              double        globalSlope)
    {
        this.sheet = sheet;
        this.filaments = filaments;
        this.globalSlope = globalSlope;

        pictureWidth = sheet.getWidth();
        scale = sheet.getScale();
        colPatterns = new TreeMap<Integer, List<FilamentPattern>>();
    }

    //~ Methods ----------------------------------------------------------------

    //------------------//
    // getPopularLength //
    //------------------//
    /**
     * @return the most popular cluster length
     */
    public int getPopularLength ()
    {
        return popLength;
    }

    //-----------//
    // buildInfo //
    //-----------//
    public void buildInfo ()
    {
        // Retrieve all vertical patterns of filaments
        retrievePatterns();

        // Remember the most popular length
        retrievePopularLength();

        // Interconnect filaments using the network of patterns
        followPatternsNetwork();
        purgeFilaments();

        // Retrieve clusters
        retrieveClusters();
    }

    //----------//
    // canMerge //
    //----------//
    /**
     * Check for merge possibility between two clusters
     * @param one first cluster
     * @param two second cluster
     * @param deltaPos output: the delta in positions between these clusters
     * if the test has succeeded
     * @return true if successful
     */
    public boolean canMerge (LineCluster      one,
                             LineCluster      two,
                             Wrapper<Integer> deltaPos)
    {
        final int       maxMergeDy = scale.toPixels(constants.maxMergeDy);
        final int       maxMergeDx = scale.toPixels(constants.maxMergeDx);

        final Rectangle oneBox = one.getContourBox();
        final Rectangle twoBox = two.getContourBox();

        final int       oneLeft = oneBox.x;
        final int       oneRight = (oneBox.x + oneBox.width) - 1;
        final int       twoLeft = twoBox.x;
        final int       twoRight = (twoBox.x + twoBox.width) - 1;

        final int       minRight = Math.min(oneRight, twoRight);
        final int       maxLeft = Math.max(oneLeft, twoLeft);
        final int       gap = maxLeft - minRight;
        double          dist;

        if (logger.isFineEnabled()) {
            logger.fine("gap:" + gap);
        }

        if (gap <= 0) {
            // Overlap: use middle of common part
            int mid = (maxLeft + minRight) / 2;
            dist = bestMatch(
                ordinatesOf(
                    one.getPointsAt(mid, scale.interline(), globalSlope)),
                ordinatesOf(
                    two.getPointsAt(mid, scale.interline(), globalSlope)),
                deltaPos);
        } else if (gap > maxMergeDx) {
            if (logger.isFineEnabled()) {
                logger.fine("Gap too wide between " + one + " & " + two);
            }

            return false;
        } else {
            // True gap: use proper edges
            if (oneLeft < twoLeft) { // Case one --- two
                dist = bestMatch(
                    ordinatesOf(one.getStops()),
                    ordinatesOf(two.getStarts()),
                    deltaPos);
            } else { // Case two --- one
                dist = bestMatch(
                    ordinatesOf(one.getStarts()),
                    ordinatesOf(two.getStops()),
                    deltaPos);
            }
        }

        // Check best distance
        if (logger.isFineEnabled()) {
            logger.fine(
                "canMerge dist: " + dist + " one:" + one + " two:" + two);
        }

        if (dist <= maxMergeDy) {
            return true;
        } else {
            return false;
        }
    }

    //-------------//
    // renderItems //
    //-------------//
    void renderItems (Graphics2D g)
    {
        Stroke oldStroke = g.getStroke();
        g.setStroke(patternStroke);
        g.setColor(PagePainter.musicColor);

        for (Entry<Integer, List<FilamentPattern>> entry : colPatterns.entrySet()) {
            int col = entry.getKey();
            int x = colX[col];

            for (FilamentPattern pattern : entry.getValue()) {
                ///if (pattern.getCount() == popLength) {
                g.drawLine(
                    x,
                    pattern.getY(0),
                    x,
                    pattern.getY(pattern.getCount() - 1));

                ///}
            }
        }

        g.setStroke(oldStroke);
    }

    //-----------------//
    // getSheetTopEdge //
    //-----------------//
    /**
     * Report the top edge of sheet, parallel to the global slope
     * @return the tilted top line reference
     */
    private Line2D.Double getSheetTopEdge ()
    {
        if (sheetTopEdge == null) {
            // Build this reference line
            double          angleRad = Math.atan(globalSlope);
            AffineTransform at = AffineTransform.getRotateInstance(angleRad);
            Point2D         right = new Point(sheet.getWidth(), 0);
            right = at.transform(right, right);

            sheetTopEdge = new Line2D.Double(0, 0, right.getX(), right.getY());
        }

        return sheetTopEdge;
    }

    //-----------//
    // bestMatch //
    //-----------//
    /**
     * Find the best match between provided sequences
     * @param one first sequence
     * @param two second sequence
     * @param bestDelta output: best delta between the two sequences
     * @return the best distance found
     */
    private double bestMatch (int[]            one,
                              int[]            two,
                              Wrapper<Integer> bestDelta)
    {
        final int deltaMax = one.length - 1;
        final int deltaMin = -deltaMax;

        double    bestDist = Double.MAX_VALUE;
        bestDelta.value = null;

        for (int delta = deltaMin; delta <= deltaMax; delta++) {
            int distSum = 0;
            int count = 0;

            for (int oneIdx = 0; oneIdx < one.length; oneIdx++) {
                int twoIdx = oneIdx + delta;

                if ((twoIdx >= 0) && (twoIdx < two.length)) {
                    count++;
                    distSum += Math.abs(two[twoIdx] - one[oneIdx]);
                }
            }

            if (count > 0) {
                double dist = (double) distSum / count;

                if (dist < bestDist) {
                    bestDist = dist;
                    bestDelta.value = delta;
                }
            }
        }

        return bestDist;
    }

    //------------------//
    // connectAncestors //
    //------------------//
    private void connectAncestors (Filament one,
                                   Filament two)
    {
        Filament oneAnc = one.getAncestor();
        Filament twoAnc = two.getAncestor();

        if (oneAnc != twoAnc) {
            if (oneAnc.getLength() >= twoAnc.getLength()) {
                ///logger.info("Inclusion " + twoAnc + " into " + oneAnc);
                oneAnc.include(twoAnc);
                oneAnc.getPatterns()
                      .putAll(twoAnc.getPatterns());
            } else {
                ///logger.info("Inclusion " + oneAnc + " into " + twoAnc);
                twoAnc.include(oneAnc);
                twoAnc.getPatterns()
                      .putAll(oneAnc.getPatterns());
            }
        }
    }

    //---------------//
    // expandCluster //
    //---------------//
    /**
     * Try to expand the provided cluster with appropriate filaments out of
     * the provided sorted collection
     * @param cluster the cluster to work on
     * @param fils the (properly sorted) collection of filaments
     */
    private void expandCluster (LineCluster    cluster,
                                List<Filament> fils)
    {
        final int marginX = scale.toPixels(constants.clusterXMargin);
        final int marginY = scale.toPixels(constants.clusterYMargin);
        final int maxClusterDy = scale.toPixels(constants.maxClusterDy);

        Rectangle clusterBox = null;

        for (Filament fil : fils) {
            if (fil.getCluster() != null) {
                continue;
            }

            if (clusterBox == null) {
                clusterBox = cluster.getContourBox();
                clusterBox.grow(marginX, marginY);
            }

            PixelRectangle filBox = fil.getContourBox();
            PixelPoint     middle = new PixelPoint();
            middle.x = filBox.x + (filBox.width / 2);
            middle.y = (int) Math.rint(fil.getCurve().yAt(middle.x));

            if (clusterBox.contains(middle)) {
                // Check if this filament matches a cluster line ordinate
                List<PixelPoint> points = cluster.getPointsAt(
                    middle.x,
                    scale.interline(),
                    globalSlope);

                for (PixelPoint point : points) {
                    // Check vertical distance
                    int dy = middle.y - point.y;

                    if (Math.abs(dy) <= maxClusterDy) {
                        int index = points.indexOf(point);

                        if (cluster.includeFilamentByIndex(fil, index)) {
                            if (logger.isFineEnabled()) {
                                logger.warning(
                                    "Aggregate dy:" + dy + " " + fil + " to " +
                                    cluster + " at index " + index);
                            }

                            clusterBox = null; // Invalidate cluster box

                            break;
                        }
                    }
                }
            }
        }
    }

    //----------------//
    // expandClusters //
    //----------------//
    /**
     * Aggregate non-clustered filaments to close clusters when appropriate
     */
    private void expandClusters ()
    {
        purgeFilaments();

        List<Filament> startFils = new ArrayList<Filament>(filaments);
        Collections.sort(startFils, Filament.startComparator);

        List<Filament> stopFils = new ArrayList<Filament>(filaments);
        Collections.sort(stopFils, Filament.stopComparator);

        for (LineCluster cluster : clusters) {
            // Expanding on left side
            expandCluster(cluster, stopFils);
            // Expanding on right side
            expandCluster(cluster, startFils);
        }
    }

    //-----------------------//
    // followPatternsNetwork //
    //-----------------------//
    /**
     * Use the network of patterns and filaments to interconnect filaments via
     * common patterns
     */
    private void followPatternsNetwork ()
    {
        if (logger.isFineEnabled()) {
            logger.info("Following patterns network");
        }

        for (Filament fil : filaments) {
            Map<Integer, FilamentPattern> patterns = fil.getPatterns();

            if ((patterns == null) || patterns.isEmpty()) {
                continue;
            }

            // Sequence of lines around the filament, indexed by relative pos
            Map<Integer, Filament> lines = new TreeMap<Integer, Filament>();

            // Loop on all patterns this filament is involved in
            for (FilamentPattern pattern : patterns.values()) {
                int posPivot = pattern.getIndex(fil);

                for (int pos = 0; pos < pattern.getCount(); pos++) {
                    int line = pos - posPivot;

                    if (line != 0) {
                        Filament f = lines.get(line);

                        if (f != null) {
                            connectAncestors(f, pattern.getFilament(pos));
                        } else {
                            lines.put(line, pattern.getFilament(pos));
                        }
                    }
                }
            }
        }
    }

    //---------------//
    // mergeClusters //
    //---------------//
    /**
     * Assuming the clusters list is sorted according to their ordinate, merge
     * compatible clusters as much as possible
     */
    private void mergeClusters ()
    {
        int marginX = scale.toPixels(constants.clusterXMargin);
        int marginY = scale.toPixels(constants.clusterYMargin);

        for (LineCluster current : clusters) {
            LineCluster candidate = current;

            // Keep on working while we do have a candidate to check for merge
            while (candidate != null) {
                Wrapper<Integer> deltaPos = new Wrapper<Integer>();
                Rectangle        candidateBox = candidate.getContourBox();
                candidateBox.grow(marginX, marginY);

                // Check the candidate vs all clusters until current excluded
                HeadsLoop: 
                for (LineCluster head : clusters) {
                    if (head == current) { // Actual end of sub list
                        candidate = null;

                        break;
                    }

                    if ((head == candidate) || (head.getParent() != null)) {
                        continue;
                    }

                    // Check rough proximity
                    Rectangle headBox = head.getContourBox();

                    if (!headBox.intersects(candidateBox)) {
                        continue;
                    }

                    // Try a merge
                    if (canMerge(head, candidate, deltaPos)) {
                        if (logger.isFineEnabled()) {
                            logger.info(
                                "Merged " + candidate + " with " + head +
                                " delta:" + deltaPos.value);
                        }

                        // Do the merge
                        candidate.mergeWith(head, deltaPos.value);

                        break;
                    }

                    // No more possibility?
                    if (headBox.y > (candidateBox.y + candidateBox.height)) {
                        break;
                    }
                }
            }
        }
    }

    //------------//
    // ordinateOf //
    //------------//
    /**
     * Report the orthogonal distance of the provided point
     * to the sheet top edge tilted with global slope.
     */
    private int ordinateOf (Point point)
    {
        return (int) Math.rint(getSheetTopEdge().ptLineDist(point));
    }

    //------------//
    // ordinateOf //
    //------------//
    /**
     * Report the orthogonal distance of the filament mid-point
     * to the sheet top edge tilted with global slope.
     */
    private int ordinateOf (Filament fil)
    {
        if (fil.getTopDistance() == null) {
            PixelPoint start = fil.getStartPoint();
            PixelPoint stop = fil.getStopPoint();
            fil.setTopDistance(
                ordinateOf(
                    new Point((start.x + stop.x) / 2, (start.y + stop.y) / 2)));
        }

        return fil.getTopDistance();
    }

    //------------//
    // ordinateOf //
    //------------//
    /**
     * Report the orthogonal distance of the cluster center
     * to the sheet top edge tilted with global slope.
     */
    private int ordinateOf (LineCluster cluster)
    {
        return ordinateOf(cluster.getCenter());
    }

    //-------------//
    // ordinatesOf //
    //-------------//
    private int[] ordinatesOf (Collection<PixelPoint> points)
    {
        int[] ys = new int[points.size()];
        int   index = 0;

        for (Point p : points) {
            ys[index++] = ordinateOf(p);
        }

        return ys;
    }

    //---------------//
    // purgeClusters //
    //---------------//
    private void purgeClusters ()
    {
        for (Iterator<LineCluster> it = clusters.iterator(); it.hasNext();) {
            LineCluster cluster = it.next();

            if (cluster.getParent() != null) {
                it.remove();
            }
        }
    }

    //----------------//
    // purgeFilaments //
    //----------------//
    private void purgeFilaments ()
    {
        // Remove merged filaments
        for (Iterator<Filament> it = filaments.iterator(); it.hasNext();) {
            Filament fil = it.next();

            if (fil.getParent() != null) {
                it.remove();
            }
        }
    }

    //----------------//
    // registerStaves //
    //----------------//
    /**
     * Register line clusters as staves
     */
    private void registerStaves ()
    {
        StaffManager staffManager = sheet.getStaffManager();
        int          staffId = 0;
        staffManager.reset();

        for (LineCluster cluster : clusters) {
            if (cluster.getSize() != popLength) {
                continue;
            }

            List<LineInfo> lines = new ArrayList<LineInfo>(cluster.getLines());
            int            left = Integer.MAX_VALUE;
            int            right = Integer.MIN_VALUE;

            for (LineInfo line : lines) {
                left = Math.min(left, line.getLeftPoint().x);
                right = Math.max(right, line.getRightPoint().x);
            }

            StaffInfo staff = new StaffInfo(
                ++staffId,
                left,
                right,
                scale,
                lines);
            staffManager.addStaff(staff);
        }

        staffManager.computeStaffLimits();

        // Cache (not really needed)
        for (StaffInfo staff : staffManager.getStaves()) {
            staff.getArea();
        }
    }

    //------------------//
    // retrieveClusters //
    //------------------//
    /**
     * Connect filaments via the patterns they are involved in,
     * and come up with clusters of lines
     */
    private void retrieveClusters ()
    {
        // Create clusters recursively out of filements
        List<Filament> filList = new ArrayList<Filament>(filaments);
        Collections.sort(filList, Filament.reverseLengthComparator);

        for (Filament fil : filList) {
            if ((fil.getCluster() == null) && !fil.getPatterns()
                                                  .isEmpty()) {
                clusters.add(new LineCluster(fil));
            }
        }

        // Remove clusters merged into other ones
        purgeClusters();

        // Sort clusters according to their ordinate in page
        Collections.sort(clusters, ordinateComparator);

        // Merge clusters
        mergeClusters();
        purgeClusters();

        // Trim clusters with too many lines
        for (LineCluster cluster : clusters) {
            cluster.cleanup(popLength);
        }

        // Aggregate filaments left over, if possible
        expandClusters();

        purgeFilaments();

        // Remove non-clustered filaments
        for (Iterator<Filament> it = filaments.iterator(); it.hasNext();) {
            Filament fil = it.next();

            if (fil.getCluster() == null) {
                it.remove();
            }
        }

        // Sort clusters according to their ordinate in page
        Collections.sort(clusters, ordinateComparator);

        ///if (logger.isFineEnabled()) {
        logger.info("\nClusters: " + clusters.size());

        for (LineCluster cluster : clusters) {
            logger.info(cluster.toString());
        }

        ///}

        // Register clusters as staves
        registerStaves();
    }

    //----------------------//
    // retrieveFilamentsAtX //
    //----------------------//
    /**
     * For a given abscissa, retrieve the filaments that are intersected by
     * vertical x, and sort them according to their ordinate at x
     * @param x the desired abscissa
     * @return the sorted list of structures (Fil + Y), perhaps empty
     */
    private SortedSet<FilY> retrieveFilamentsAtX (int x)
    {
        SortedSet<FilY> set = new TreeSet<FilY>();

        for (Filament fil : filaments) {
            if ((x >= fil.getStartPoint().x) && (x <= fil.getStopPoint().x)) {
                set.add(new FilY(fil, (int) Math.rint(fil.getCurve().yAt(x))));
            }
        }

        return set;
    }

    //------------------//
    // retrievePatterns //
    //------------------//
    /**
     * Detect patterns of (staff) lines.
     * Use vertical sampling on regularly-spaced abscissae
     */
    private void retrievePatterns ()
    {
        /** Minimum acceptable delta y */
        int dMin = (int) Math.floor(
            scale.interline() * (1 - constants.maxJitter.getValue()));

        /** Maximum acceptable delta y */
        int dMax = (int) Math.ceil(
            scale.interline() * (1 + constants.maxJitter.getValue()));

        /** Number of vertical samples to collect */
        int sampleCount = -1 +
                          (int) Math.rint(
            (double) pictureWidth / scale.toPixels(constants.samplingDx));

        /** Exact columns abscissae */
        colX = new int[sampleCount + 1];

        /** Precise x interval */
        double samplingDx = (double) pictureWidth / (sampleCount + 1);

        for (int col = 1; col <= sampleCount; col++) {
            List<FilamentPattern> colList = new ArrayList<FilamentPattern>();
            colPatterns.put(col, colList);

            int x = (int) Math.rint(samplingDx * col);
            colX[col] = x;

            // First, retrieve Filaments and their ordinate at x
            // And sort them by increasing y
            SortedSet<FilY> filys = retrieveFilamentsAtX(x);

            // Second, check y deltas to detect patterns
            FilamentPattern pattern = null;
            FilY            prevFily = null;

            for (FilY fily : filys) {
                if (prevFily != null) {
                    int dy = (int) Math.rint(fily.y - prevFily.y);

                    if ((dy >= dMin) && (dy <= dMax)) {
                        if (pattern == null) {
                            // Start of a new pattern
                            pattern = new FilamentPattern(col);
                            colList.add(pattern);
                            pattern.append(prevFily.filament, prevFily.y);
                        }

                        // Extend pattern
                        pattern.append(fily.filament, fily.y);
                    } else {
                        // No pattern active
                        pattern = null;
                    }
                }

                prevFily = fily;
            }
        }
    }

    //-----------------------//
    // retrievePopularLength //
    //-----------------------//
    /**
     * Retrieve the most popular length (line count) among all patterns
     */
    private void retrievePopularLength ()
    {
        // Build histogram of patterns lengths
        Histogram<Integer> histo = new Histogram();

        for (List<FilamentPattern> list : colPatterns.values()) {
            for (FilamentPattern pattern : list) {
                histo.increaseCount(pattern.getCount(), pattern.getCount());
            }
        }

        ///if (logger.isFineEnabled()) {
        histo.print(System.out);
        ///}

        // Use the most popular length
        // Should be 4 for bass tab, 5 for standard notation, 6 for guitar tab
        popLength = histo.getMaxBucket();
        logger.info("Most popular cluster length is " + popLength);
    }

    //~ Inner Classes ----------------------------------------------------------

    //-----------//
    // Constants //
    //-----------//
    private static final class Constants
        extends ConstantSet
    {
        //~ Instance fields ----------------------------------------------------

        Scale.Fraction  samplingDx = new Scale.Fraction(
            1,
            "Typical delta X between two vertical samplings");
        Scale.Fraction  lookupDx = new Scale.Fraction(
            8,
            "Window width when looking for connections");
        Scale.Fraction  lookupDy = new Scale.Fraction(
            1,
            "Window height when looking for connections");
        Scale.Fraction  maxClusterDy = new Scale.Fraction(
            0.15,
            "Maximum dy to aggregate a filament to a cluster");
        Scale.Fraction  maxMergeDx = new Scale.Fraction(
            10,
            "Maximum dx to merge two clusters");
        Scale.Fraction  maxMergeDy = new Scale.Fraction(
            0.4,
            "Maximum dy to merge two clusters");
        Scale.Fraction  clusterXMargin = new Scale.Fraction(
            10,
            "Rough margin around cluster abscissa");
        Scale.Fraction  clusterYMargin = new Scale.Fraction(
            2,
            "Rough margin around cluster ordinate");
        Constant.Double tangentMargin = new Constant.Double(
            "tangent",
            0.08,
            "Maximum slope of gap between filaments");
        Constant.Ratio  maxJitter = new Constant.Ratio(
            0.05,
            "Maximum gap from standard pattern dy");
    }

    //------//
    // FilY //
    //------//
    /**
     * Class meant to define an ordering relationship between filaments, knowing
     * their ordinate at a common abscissa value.
     */
    private static class FilY
        implements Comparable<FilY>
    {
        //~ Instance fields ----------------------------------------------------

        final Filament filament;
        final int      y;

        //~ Constructors -------------------------------------------------------

        public FilY (Filament filament,
                     int      y)
        {
            this.filament = filament;
            this.y = y;
        }

        //~ Methods ------------------------------------------------------------

        public int compareTo (FilY that)
        {
            return Integer.signum(this.y - that.y);
        }
    }
}