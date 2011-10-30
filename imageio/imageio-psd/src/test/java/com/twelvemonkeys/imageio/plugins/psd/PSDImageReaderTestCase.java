package com.twelvemonkeys.imageio.plugins.psd;

import com.twelvemonkeys.imageio.util.ImageReaderAbstractTestCase;
import com.twelvemonkeys.imageio.util.ProgressListenerBase;
import org.junit.Test;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.ImageReader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

/**
 * PSDImageReaderTestCase
 *
 * @author <a href="mailto:harald.kuhr@gmail.com">Harald Kuhr</a>
 * @author last modified by $Author: haraldk$
 * @version $Id: PSDImageReaderTestCase.java,v 1.0 Apr 1, 2008 10:39:17 PM haraldk Exp$
 */
public class PSDImageReaderTestCase extends ImageReaderAbstractTestCase<PSDImageReader> {

    static ImageReaderSpi provider = new PSDImageReaderSpi();

    protected List<TestData> getTestData() {
        return Arrays.asList(
                // 5 channel, RGB
                new TestData(getClassLoaderResource("/psd/photoshopping.psd"), new Dimension(300, 225)),
                // 1 channel, gray, 8 bit samples
                new TestData(getClassLoaderResource("/psd/buttons.psd"), new Dimension(20, 20)),
                // 5 channel, CMYK
                new TestData(getClassLoaderResource("/psd/escenic-liquid-logo.psd"), new Dimension(595, 420)),
                // 3 channel RGB, "no composite layer"
                new TestData(getClassLoaderResource("/psd/jugware-icon.psd"), new Dimension(128, 128)),
                // 3 channel RGB, old data, no layer info/mask 
                new TestData(getClassLoaderResource("/psd/MARBLES.PSD"), new Dimension(1419, 1001)),
                // 1 channel, indexed color
                new TestData(getClassLoaderResource("/psd/coral_fish.psd"), new Dimension(800, 800)),
                // 1 channel, bitmap, 1 bit samples
                new TestData(getClassLoaderResource("/psd/test_bitmap.psd"), new Dimension(710, 512)),
                // 1 channel, gray, 16 bit samples
                new TestData(getClassLoaderResource("/psd/test_gray16.psd"), new Dimension(710, 512)),
                // 4 channel, CMYK, 16 bit samples
                new TestData(getClassLoaderResource("/psd/cmyk_16bits.psd"), new Dimension(1000, 275))
                // TODO: Need uncompressed PSD
                // TODO: Need more recent ZIP compressed PSD files from CS2/CS3+
        );
    }

    protected ImageReaderSpi createProvider() {
        return provider;
    }

    @Override
    protected PSDImageReader createReader() {
        return new PSDImageReader(provider);
    }

    protected Class<PSDImageReader> getReaderClass() {
        return PSDImageReader.class;
    }

    protected List<String> getFormatNames() {
        return Arrays.asList("psd");
    }

    protected List<String> getSuffixes() {
        return Arrays.asList("psd");
    }

    protected List<String> getMIMETypes() {
        return Arrays.asList("image/x-psd");
    }

    public void testSupportsThumbnail() {
        PSDImageReader imageReader = createReader();
        assertTrue(imageReader.readerSupportsThumbnails());
    }

    public void testThumbnailReading() throws IOException {
        PSDImageReader imageReader = createReader();

        imageReader.setInput(getTestData().get(0).getInputStream());

        assertEquals(1, imageReader.getNumThumbnails(0));

        BufferedImage thumbnail = imageReader.readThumbnail(0, 0);
        assertNotNull(thumbnail);

        assertEquals(128, thumbnail.getWidth());
        assertEquals(96, thumbnail.getHeight());
    }

    public void testThumbnailReadingNoInput() throws IOException {
        PSDImageReader imageReader = createReader();

        try {
            imageReader.getNumThumbnails(0);
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().toLowerCase().contains("input"));
        }

        try {
            imageReader.getThumbnailWidth(0, 0);
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().toLowerCase().contains("input"));
        }

        try {
            imageReader.getThumbnailHeight(0, 0);
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().toLowerCase().contains("input"));
        }

        try {
            imageReader.readThumbnail(0, 0);
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().toLowerCase().contains("input"));
        }
    }

    public void testThumbnailReadingOutOfBounds() throws IOException {
        PSDImageReader imageReader = createReader();

        imageReader.setInput(getTestData().get(0).getInputStream());

        int numImages = imageReader.getNumImages(true);

        try {
            imageReader.getNumThumbnails(numImages + 1);
            fail("Expected IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().toLowerCase().contains("index"));
        }

        try {
            imageReader.getThumbnailWidth(-1, 0);
            fail("Expected IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().toLowerCase().contains("index"));
        }

        try {
            imageReader.getThumbnailHeight(0, -2);
            fail("Expected IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException expected) {
            // Sloppy...
            assertTrue(expected.getMessage(), expected.getMessage().toLowerCase().contains("-2"));
        }

        try {
            imageReader.readThumbnail(numImages + 99, 42);
            fail("Expected IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().toLowerCase().contains("index"));
        }
    }

    public void testThumbnailDimensions() throws IOException {
        PSDImageReader imageReader = createReader();

        imageReader.setInput(getTestData().get(0).getInputStream());

        assertEquals(1, imageReader.getNumThumbnails(0));

        assertEquals(128, imageReader.getThumbnailWidth(0, 0));
        assertEquals(96, imageReader.getThumbnailHeight(0, 0));
    }

    public void testThumbnailReadListeners() throws IOException {
        PSDImageReader imageReader = createReader();

        imageReader.setInput(getTestData().get(0).getInputStream());

        final List<Object> sequnce = new ArrayList<Object>();
        imageReader.addIIOReadProgressListener(new ProgressListenerBase() {
            private float mLastPercentageDone = 0;

            @Override
            public void thumbnailStarted(final ImageReader pSource, final int pImageIndex, final int pThumbnailIndex) {
                sequnce.add("started");
            }

            @Override
            public void thumbnailComplete(final ImageReader pSource) {
                sequnce.add("complete");
            }

            @Override
            public void thumbnailProgress(final ImageReader pSource, final float pPercentageDone) {
                // Optional
                assertTrue("Listener invoked out of sequence", sequnce.size() == 1);
                assertTrue(pPercentageDone >= mLastPercentageDone);
            }
        });

        BufferedImage thumbnail = imageReader.readThumbnail(0, 0);
        assertNotNull(thumbnail);

        assertEquals("Listeners not invoked", 2, sequnce.size());
        assertEquals("started", sequnce.get(0));
        assertEquals("complete", sequnce.get(1));
    }

    @Test
    public void testReadLayers() throws IOException {
        PSDImageReader imageReader = createReader();

        imageReader.setInput(getTestData().get(3).getInputStream());

        int numImages = imageReader.getNumImages(true);

        assertEquals(3, numImages);

        for (int i = 0; i < numImages; i++) {
            BufferedImage image = imageReader.read(i);
            assertNotNull(image);

            // Make sure layers are correct size
            assertEquals(image.getWidth(), imageReader.getWidth(i));
            assertEquals(image.getHeight(), imageReader.getHeight(i));
        }
    }

    @Test
    public void testImageTypesLayers() throws IOException {
        PSDImageReader imageReader = createReader();

        imageReader.setInput(getTestData().get(3).getInputStream());

        int numImages = imageReader.getNumImages(true);
        for (int i = 0; i < numImages; i++) {
            ImageTypeSpecifier rawType = imageReader.getRawImageType(i);
//            System.err.println("rawType: " + rawType);
            assertNotNull(rawType);

            Iterator<ImageTypeSpecifier> types = imageReader.getImageTypes(i);

            assertNotNull(types);
            assertTrue(types.hasNext());

            boolean found = false;

            while (types.hasNext()) {
                ImageTypeSpecifier type = types.next();
//                System.err.println("type: " + type);

                if (!found && (rawType == type || rawType.equals(type))) {
                    found = true;
                }
            }

            assertTrue("RAW image type not in type iterator", found);
        }
    }

    @Test
    public void testReadLayersExplicitType() throws IOException {
        PSDImageReader imageReader = createReader();

        imageReader.setInput(getTestData().get(3).getInputStream());

        int numImages = imageReader.getNumImages(true);
        for (int i = 0; i < numImages; i++) {
            Iterator<ImageTypeSpecifier> types = imageReader.getImageTypes(i);

            while (types.hasNext()) {
                ImageTypeSpecifier type = types.next();
                ImageReadParam param = imageReader.getDefaultReadParam();
                param.setDestinationType(type);
                BufferedImage image = imageReader.read(i, param);

                assertEquals(type.getBufferedImageType(), image.getType());

                if (type.getBufferedImageType() == 0) {
                    // TODO: If type.getBIT == 0, test more
                    // Compatible color model
                    assertEquals(type.getNumComponents(), image.getColorModel().getNumComponents());

                    // Same color space
                    assertEquals(type.getColorModel().getColorSpace(), image.getColorModel().getColorSpace());

                    // Same number of samples
                    assertEquals(type.getNumBands(), image.getSampleModel().getNumBands());

                    // Same number of bits/sample
                    for (int j = 0; j < type.getNumBands(); j++) {
                        assertEquals(type.getBitsPerBand(j), image.getSampleModel().getSampleSize(j));
                    }
                }
            }
        }
    }

    @Test
    public void testReadLayersExplicitDestination() throws IOException {
        PSDImageReader imageReader = createReader();

        imageReader.setInput(getTestData().get(3).getInputStream());

        int numImages = imageReader.getNumImages(true);
        for (int i = 0; i < numImages; i++) {
            Iterator<ImageTypeSpecifier> types = imageReader.getImageTypes(i);
            int width = imageReader.getWidth(i);
            int height = imageReader.getHeight(i);

            while (types.hasNext()) {
                ImageTypeSpecifier type = types.next();
                ImageReadParam param = imageReader.getDefaultReadParam();
                BufferedImage destination = type.createBufferedImage(width, height);
                param.setDestination(destination);

                BufferedImage image = imageReader.read(i, param);

                assertSame(destination, image);
            }
        }
    }
}