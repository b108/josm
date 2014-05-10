// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.gui;

import static org.junit.Assert.fail;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openstreetmap.josm.JOSMFixture;
import org.openstreetmap.josm.TestUtils;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.gui.preferences.map.TaggingPresetPreference;
import org.openstreetmap.josm.gui.tagging.TaggingPresetReader;
import org.openstreetmap.josm.io.Compression;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.OsmReader;
import org.xml.sax.SAXException;

/**
 * Unit tests of {@link DefaultNameFormatter} class.
 *
 */
public class DefaultNameFormatterTest {

    /**
     * Setup tests
     */
    @BeforeClass
    public static void setUpBeforeClass() {
        JOSMFixture.createUnitTestFixture().init();
    }

    /**
     * Non-regression test for ticket <a href="https://josm.openstreetmap.de/ticket/9632">#9632</a>.
     * @throws IllegalDataException
     * @throws IOException
     * @throws SAXException
     */
    @Test
    public void testTicket9632() throws IllegalDataException, IOException, SAXException {
        String source = "http://josm.openstreetmap.de/josmfile?page=Presets/BicycleJunction&amp;preset";
        TaggingPresetPreference.taggingPresets = TaggingPresetReader.readAll(source, true);

        Comparator<Relation> comparator = DefaultNameFormatter.getInstance().getRelationComparator();

        try (InputStream is = new FileInputStream(TestUtils.getTestDataRoot() + "regress/9632/data.osm.zip")) {
            DataSet ds = OsmReader.parseDataSet(Compression.ZIP.getUncompressedInputStream(is), null);
            Relation[] relations = new ArrayList<>(ds.getRelations()).toArray(new Relation[0]);
            System.out.println(Arrays.toString(relations));
            // Check each compare possibility
            for (int i=0; i<relations.length; i++) {
                Relation r1 = relations[i];
                for (int j=i; j<relations.length; j++) {
                    Relation r2 = relations[j];
                    int a = comparator.compare(r1, r2);
                    int b = comparator.compare(r2, r1);
                    if (i==j || a==b) {
                        if (a != 0 || b != 0) {
                            fail(getFailMessage(r1, r2, a, b));
                        }
                    } else {
                        if (a != -b) {
                            fail(getFailMessage(r1, r2, a, b));
                        }
                    }
                    for (int k=j; k<relations.length; k++) {
                        Relation r3 = relations[k];
                        int c = comparator.compare(r1, r3);
                        int d = comparator.compare(r2, r3);
                        if (a > 0 && d > 0) {
                            if (c <= 0) {
                               fail(getFailMessage(r1, r2, r3, a, b, c, d));
                            }
                        } else if (a == 0 && d == 0) {
                            if (c != 0) {
                                fail(getFailMessage(r1, r2, r3, a, b, c, d));
                            }
                        } else if (a < 0 && d < 0) {
                            if (c >= 0) {
                                fail(getFailMessage(r1, r2, r3, a, b, c, d));
                            }
                        }
                    }
                }
            }
            // Sort relation array
            Arrays.sort(relations, comparator);
        }
    }

    private static String getFailMessage(Relation r1, Relation r2, int a, int b) {
        return new StringBuilder("Compared\nr1: ").append(r1).append("\nr2: ")
        .append(r2).append("\ngave: ").append(a).append("/").append(b)
        .toString();
    }

    private static String getFailMessage(Relation r1, Relation r2, Relation r3, int a, int b, int c, int d) {
        return new StringBuilder(getFailMessage(r1, r2, a, b))
        .append("\nCompared\nr1: ").append(r1).append("\nr3: ").append(r3).append("\ngave: ").append(c)
        .append("\nCompared\nr2: ").append(r2).append("\nr3: ").append(r3).append("\ngave: ").append(d)
        .toString();
    }
}