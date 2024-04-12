package analysis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.awt.geom.Point2D;
import java.awt.Point;

public class ConvexHull {
    final static String FIXATIONX_INDEX = "FPOGX";
    final static String FIXATIONY_INDEX = "FPOGY";
    
    static public LinkedHashMap<String,String> analyze(DataEntry data) {
        LinkedHashMap<String,String> results = new LinkedHashMap<String,String>();
        List<Point2D.Double> allPoints = new ArrayList<>();

        for (int row = 0; row < data.rowCount(); row++) {
            double x = Double.valueOf(data.getValue(FIXATIONX_INDEX, row));
            double y = Double.valueOf(data.getValue(FIXATIONY_INDEX, row));
            allPoints.add(new Point2D.Double(x, y));
        }  
        List<Point2D.Double> boundingPoints = getConvexHull(allPoints);

        results.put(
            "convex hull area", //Output Header
            String.valueOf(getPolygonArea(boundingPoints))
            );

        return results;
    }

 /**
     * An enum denoting a directional-turn between 3 points (vectors).
     */
    protected static enum Turn { CLOCKWISE, COUNTER_CLOCKWISE, COLLINEAR }

    /**
     * Returns true iff all points in <code>points</code> are collinear.
     *
     * @param points the list of points.
     * @return       true iff all points in <code>points</code> are collinear.
     */
    protected static boolean areAllCollinear(List<Point2D.Double> points) {

        if(points.size() < 3) {
            return true;
        }

        final Point2D.Double a = points.get(0);
        final Point2D.Double b = points.get(1);

        for(int i = 2; i < points.size(); i++) {


        	Point2D.Double c = points.get(i);
            
            if(getTurn(a, b, c) != Turn.COLLINEAR) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the convex hull of the points created from the list
     * <code>points</code>. Note that the first and last point in the
     * returned <code>List&lt;java.awt.Point&gt;</code> are the same
     * point.
     *
     * @param points the list of points.
     * @return       the convex hull of the points created from the list
     *               <code>points</code>.
     * @throws IllegalArgumentException if all points are collinear or if there
     *                                  are less than 3 unique points present.
     */
    public static List<Point2D.Double> getConvexHull(List<Point2D.Double> points) throws IllegalArgumentException {
        if (points.size() == 0) return new ArrayList<>();
        
        List<Point2D.Double> sorted = new ArrayList<>(getSortedPointSet(points));

        // if(sorted.size() < 3) {
        //     throw new IllegalArgumentException("can only create a convex hull of 3 or more unique points");
        // }

        // if(areAllCollinear(sorted)) {
        //     throw new IllegalArgumentException("cannot create a convex hull from collinear points");
        // }

        // To prevent program from crashing when no convex hull is available, return an empty list
        if (sorted.size() < 3 || areAllCollinear(sorted)) {
            return new ArrayList<>();
        }


        Stack<Point2D.Double> stack = new Stack<>();
        stack.push(sorted.get(0));
        stack.push(sorted.get(1));
        for (int i = 2; i < sorted.size(); i++) {
            // Invalid stack size, cancel calculations
            if (stack.size() < 2) return new ArrayList<>();

            Point2D.Double head = sorted.get(i);
            Point2D.Double middle = stack.pop();
            Point2D.Double tail = stack.peek();
            
            Turn turn = getTurn(tail, middle, head);

            switch(turn) {
                case COUNTER_CLOCKWISE:
                    stack.push(middle);
                    stack.push(head);
                    break;
                case CLOCKWISE:
                    i--;
                    break;
                case COLLINEAR:
                    stack.push(head);
                    break;
            }
        }


        // close the hull
        stack.push(sorted.get(0));

        return new ArrayList<>(stack);
    }

    /**
     * Returns the points with the lowest y coordinate. In case more than 1 such
     * point exists, the one with the lowest x coordinate is returned.
     *
     * @param points the list of points to return the lowest point from.
     * @return       the points with the lowest y coordinate. In case more than
     *               1 such point exists, the one with the lowest x coordinate
     *               is returned.
     */
    protected static Point2D.Double getLowestPoint(List<Point2D.Double> points) {
        Point2D.Double lowest = points.get(0);

        for(int i = 1; i < points.size(); i++) {

        	Point2D.Double temp = points.get(i);

            if((temp.y < lowest.y )|| (temp.y == lowest.y && temp.x < lowest.x )) {
                lowest = temp;
            }
        }

        return lowest;
    }

    /**
     * Returns a sorted set of points from the list <code>points</code>. The
     * set of points are sorted in increasing order of the angle they and the
     * lowest point <tt>P</tt> make with the x-axis. If tow (or more) points
     * form the same angle towards <tt>P</tt>, the one closest to <tt>P</tt>
     * comes first.
     *
     * @param points the list of points to sort.
     * @return       a sorted set of points from the list <code>points</code>.
     * @see GrahamScan#getLowestPoint(java.util.List)
     */
    protected static Set<Point2D.Double> getSortedPointSet(List<Point2D.Double> points) {
        final Point2D.Double lowest = getLowestPoint(points);

        TreeSet<Point.Double> set = new TreeSet<>(new Comparator<Point2D.Double>() {
            @Override
            public int compare(Point2D.Double a, Point2D.Double b) {

                if(a == b || a.equals(b)) {
                    return 0;
                }

                // use longs to guard against int-underflow
                double thetaA = Math.atan2((long)a.y - lowest.y, (long)a.x - lowest.x);
                double thetaB = Math.atan2((long)b.y - lowest.y, (long)b.x - lowest.x);

                if(thetaA < thetaB) {
                    return -1;
                }
                else if(thetaA > thetaB) {
                    return 1;
                }
                else {
                    // collinear with the 'lowest' point, let the point closest to it come first

                    // use longs to guard against int-over/underflow
                    double distanceA = Math.sqrt((((long)lowest.x - a.x) * ((long)lowest.x - a.x)) +
                                                (((long)lowest.y - a.y) * ((long)lowest.y - a.y)));
                    double distanceB = Math.sqrt((((long)lowest.x - b.x) * ((long)lowest.x - b.x)) +
                                                (((long)lowest.y - b.y) * ((long)lowest.y - b.y)));

                    if(distanceA < distanceB) {
                        return -1;
                    }
                    else {
                        return 1;
                    }
                }
            }
        });

        set.addAll(points);

        return set;
    }

    /**
     * Returns the GrahamScan#Turn formed by traversing through the
     * ordered points <code>a</code>, <code>b</code> and <code>c</code>.
     * More specifically, the cross product <tt>C</tt> between the
     * 3 points (vectors) is calculated:
     *
     * <tt>(b.x-a.x * c.y-a.y) - (b.y-a.y * c.x-a.x)</tt>
     *
     * and if <tt>C</tt> is less than 0, the turn is CLOCKWISE, if
     * <tt>C</tt> is more than 0, the turn is COUNTER_CLOCKWISE, else
     * the three points are COLLINEAR.
     *
     * @param a the starting point.
     * @param b the second point.
     * @param c the end point.
     * @return the GrahamScan#Turn formed by traversing through the
     *         ordered points <code>a</code>, <code>b</code> and
     *         <code>c</code>.
     */
    protected static Turn getTurn(Point2D.Double a, Point2D.Double b, Point2D.Double c) {

        // use longs to guard against int-over/underflow
        long crossProduct = (long) ((((long)b.x - a.x) * ((long)c.y - a.y)) -
                            (((long)b.y - a.y) * ((long)c.x - a.x)));

        if(crossProduct > 0) {
            return Turn.COUNTER_CLOCKWISE;
        }
        else if(crossProduct < 0) {
            return Turn.CLOCKWISE;
        }
        else {
            return Turn.COLLINEAR;
        }
    }

	public static double getPolygonArea(List<Point2D.Double> allPoints) {
        if (allPoints.size() == 0) return Double.NaN;

		int i, j, n = allPoints.size();
		double area = 0;

		for (i = 0; i < n; i++) {
			j = (i + 1) % n;
			area += allPoints.get(i).getX() * allPoints.get(j).getY();
			area -= allPoints.get(j).getX() * allPoints.get(i).getY();
		}
		area /= 2.0;
		return (area);
	}
}
