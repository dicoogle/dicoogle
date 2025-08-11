/**
 * Copyright (C) 2014  Universidade de Aveiro, DETI/IEETA, Bioinformatics Group - http://bioinformatics.ua.pt/
 *
 * This file is part of Dicoogle/dicoogle.
 *
 * Dicoogle/dicoogle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Dicoogle/dicoogle is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Dicoogle.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.ua.dicoogle.core.settings;

import org.dcm4che2.data.UID;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;
import pt.ua.dicoogle.core.settings.part.DicomServicesImpl;
import pt.ua.dicoogle.sdk.datastructs.AdditionalSOPClass;
import pt.ua.dicoogle.sdk.datastructs.AdditionalTransferSyntax;
import pt.ua.dicoogle.sdk.datastructs.MoveDestination;
import pt.ua.dicoogle.sdk.datastructs.SOPClass;
import pt.ua.dicoogle.sdk.settings.server.ServerSettings;
import pt.ua.dicoogle.sdk.settings.server.ServerSettingsReader;
import pt.ua.dicoogle.server.SOPList;
import pt.ua.dicoogle.server.TransfersStorage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * @author Eduardo Pinho <eduardopinho@ua.pt>
 * @author Luís A. Bastião Silva <bastiao@bmd-software.com>
 */
public class ServerSettingsTest {

    private final URL testConfig = this.getClass().getResource("test-config-new.xml");
    private final URL testConfigDIM = this.getClass().getResource("test-config-multi-dim.xml");
    private final URL testConfigTs = this.getClass().getResource("test-config-ts.xml");
    private final URL testConfigSopClasses = this.getClass().getResource("test-config-sopclasses.xml");
    private final URL testConfigAdditionals = this.getClass().getResource("test-config-additionals.xml");

    @Test
    public void testLoadConfig1() throws IOException {
        // read the settings from our test config file
        ServerSettings settings = ServerSettingsManager.loadSettingsAt(this.testConfig);
        final ServerSettings.Archive ar = settings.getArchiveSettings();
        final ServerSettings.DicomServices dcm = settings.getDicomServicesSettings();
        final ServerSettingsReader.DicomServices.QueryRetrieve qr = dcm.getQueryRetrieveSettings();

        assertTrue(settings instanceof ServerSettingsImpl);

        // assertions follow

        assertEquals("/opt/my-data", ar.getMainDirectory());
        assertEquals(98, ar.getIndexerEffort());
        assertEquals("/opt/my-data/watched", ar.getWatchDirectory());
        assertEquals("dicoogle01", ar.getNodeName());
        assertEquals(true, ar.isEncryptUsersFile());
        assertEquals(true, ar.isCallShutdown());
        assertEquals(true, ar.isUseIIORegistry());

        assertSameContent(Collections.singleton("lucene"), ar.getDIMProviders());
        assertSameContent(Collections.singleton("filestorage"), ar.getDefaultStorage());

        assertEquals("TEST-STORAGE", dcm.getAETitle());

        // SOP Classes

        // default
        Collection<String> defaultTS = Arrays.asList("1.2.840.10008.1.2", "1.2.840.10008.1.2.1",
                "1.2.840.10008.1.2.4.80", "1.2.840.10008.1.2.4.50");
        assertSameContent(defaultTS, ((DicomServicesImpl) dcm).getDefaultTransferSyntaxes());

        Collection<SOPClass> sopClasses = Arrays.asList(
                // CT Image Storage
                new SOPClass("1.2.840.10008.5.1.4.1.1.2", Collections.emptyList()),
                // Enhanced CT Image Storage
                new SOPClass("1.2.840.10008.5.1.4.1.1.2.1", Collections.emptyList()),
                // Enhanced CT Image Storage
                new SOPClass("1.2.840.10008.5.1.4.1.1.12.1.1",
                        Arrays.asList("1.2.840.10008.1.2", "1.2.840.10008.1.2.1", "1.2.840.10008.1.2.5")),
                // Ultrasound Multi-frame Image Storage
                new SOPClass("1.2.840.10008.5.1.4.1.1.3.1", Arrays.asList("1.2.840.10008.1.2", "1.2.840.10008.1.2.1",
                        "1.2.840.10008.1.2.4.50", "1.2.840.10008.1.2.4.70")));
        Collection<SOPClass> sopClassesFromSettings = ((DicomServicesImpl) dcm).getRawSOPClasses();
        assertSameContent(sopClasses, sopClassesFromSettings);

        // QR settings
        assertFalse(qr.isAutostart());
        assertEquals(1033, qr.getPort());
        assertEquals("192.168.1.0", qr.getHostname());
        assertSameContent(Collections.singleton("any"), dcm.getAllowedLocalInterfaces());
        assertSameContent(Collections.singleton("any"), dcm.getAllowedHostnames());
        assertEquals(1, qr.getRspDelay());
        assertEquals(50, qr.getDIMSERspTimeout());
        assertEquals(51, qr.getIdleTimeout());
        assertEquals(52, qr.getAcceptTimeout());
        assertEquals(53, qr.getConnectionTimeout());
        assertEquals(25, qr.getMaxClientAssoc());
        assertEquals(16374, qr.getMaxPDULengthSend());
        assertEquals(16374, qr.getMaxPDULengthReceive());
        assertSameContent(Arrays.asList("1.2.840.10008.1.2.2", "1.2.840.10008.1.2.1"), qr.getTransferCapabilities());
        assertSameContent(Collections.singleton("1.2.840.10008.5.1.4.1.2.1.1"), qr.getSOPClass());

        // DICOM Storage settings
        assertTrue(dcm.getStorageSettings().isAutostart());
        assertEquals(6777, dcm.getStorageSettings().getPort());
        assertEquals(null, dcm.getStorageSettings().getHostname());

        // Web server settings
        final ServerSettings.WebServer web = settings.getWebServerSettings();
        assertTrue(web.isAutostart());
        assertEquals(8282, web.getPort());
        assertEquals("127.0.0.1", web.getHostname());
        assertEquals("test.dicoogle.com", web.getAllowedOrigins());

        // Known C-MOVE destinations
        List<MoveDestination> destinations =
                Arrays.asList(new MoveDestination("ANOTHER-STORAGE", "192.168.42.42", 6666, false, "Our test storage"),
                        new MoveDestination("BACKUP-STORAGE", "192.168.42.46", 104, false, "Backup storage"));
        assertSameContent(destinations, dcm.getMoveDestinations());
    }

    @Test
    public void testDefaultSettings() throws IOException {
        // test that all default settings are ok

        // create default settings
        ServerSettings settings = ServerSettingsImpl.createDefault();
        final ServerSettings.Archive ar = settings.getArchiveSettings();
        final ServerSettings.DicomServices dcm = settings.getDicomServicesSettings();
        final ServerSettingsReader.DicomServices.QueryRetrieve qr = dcm.getQueryRetrieveSettings();

        // assertions follow

        assertEquals(null, ar.getMainDirectory());
        assertEquals(100, ar.getIndexerEffort());
        assertEquals("", ar.getWatchDirectory());
        assertEquals(null, ar.getNodeName());
        assertEquals(false, ar.isCallShutdown());

        assertEquals("DICOOGLE-STORAGE", dcm.getAETitle());

        // default
        Collection<String> defaultTS = Arrays.asList("1.2.840.10008.1.2", "1.2.840.10008.1.2.1", "1.2.840.10008.1.2.2",
                "1.2.840.10008.1.2.4.70", "1.2.840.10008.1.2.4.80", "1.2.840.10008.1.2.4.57", "1.2.840.10008.1.2.4.90",
                "1.2.840.10008.1.2.4.91", "1.2.840.10008.1.2.5", "1.2.840.10008.1.2.4.100", "1.2.840.10008.1.2.4.51");
        assertSameContent(defaultTS, ((DicomServicesImpl) dcm).getDefaultTransferSyntaxes());

        Collection<SOPClass> expectedRawSOP = SOPList.getInstance().asSOPClassList();
        assertSameContent(expectedRawSOP, ((DicomServicesImpl) dcm).getRawSOPClasses());

        Collection<SOPClass> expectedSOP = new ArrayList<>(expectedRawSOP.size());
        for (SOPClass sop : expectedRawSOP) {
            expectedSOP.add(sop.withDefaultTS(defaultTS));
        }
        assertSameContent(expectedSOP, dcm.getSOPClasses());

        // QR settings
        assertTrue(qr.isAutostart());
        assertEquals(1045, qr.getPort());
        assertEquals(null, qr.getHostname());
        assertSameContent(Collections.singleton("any"), dcm.getAllowedLocalInterfaces());
        assertSameContent(Collections.singleton("any"), dcm.getAllowedHostnames());
        assertEquals(0, qr.getRspDelay());
        assertEquals(60, qr.getDIMSERspTimeout());
        assertEquals(60, qr.getIdleTimeout());
        assertEquals(60, qr.getAcceptTimeout());
        assertEquals(60, qr.getConnectionTimeout());
        assertEquals(20, qr.getMaxClientAssoc());
        assertEquals(16364, qr.getMaxPDULengthSend());
        assertEquals(16364, qr.getMaxPDULengthReceive());
        assertSameContent(Arrays.asList("1.2.840.10008.1.2", "1.2.840.10008.1.2.2", "1.2.840.10008.1.2.1"),
                qr.getTransferCapabilities());
        assertSameContent(Arrays.asList("1.2.840.10008.5.1.4.1.2.2.1", "1.2.840.10008.5.1.4.1.2.1.1"),
                qr.getSOPClass());

        // DICOM Storage settings
        assertTrue(dcm.getStorageSettings().isAutostart());
        assertEquals(6666, dcm.getStorageSettings().getPort());

        // Web server settings
        final ServerSettings.WebServer web = settings.getWebServerSettings();
        assertTrue(web.isAutostart());
        assertEquals(8080, web.getPort());
        assertEquals(null, web.getAllowedOrigins());

        assertTrue(dcm.getMoveDestinations().isEmpty());
    }


    @Test
    public void testSave() throws IOException {
        Path target = Files.createTempFile("conf", ".xml");

        // Create default settings
        ServerSettings settings = ServerSettingsImpl.createDefault();
        final ServerSettings.DicomServices dcm = settings.getDicomServicesSettings();

        settings.getArchiveSettings().setNodeName("dicoogle17");
        settings.getArchiveSettings().setDefaultStorage(Arrays.asList("filestorage", "dropbox"));
        MoveDestination md = new MoveDestination("MORE-TESTING", "192.168.0.55", 6060, true, "DESCRIPTION");
        dcm.addMoveDestination(md);
        dcm.getStorageSettings().setPort(6767);

        ServerSettingsManager.saveSettingsTo(settings, target);

        // read from file
        settings = ServerSettingsManager.loadSettingsAt(target);
        assertEquals("dicoogle17", settings.getArchiveSettings().getNodeName());
        assertSameContent(Arrays.asList("filestorage", "dropbox"), settings.getArchiveSettings().getDefaultStorage());
        assertEquals(6767, dcm.getStorageSettings().getPort());

        Collection<MoveDestination> destinations = Collections.singleton(md);
        assertSameContent(destinations, dcm.getMoveDestinations());

        // default
        Collection<String> defaultTS = Arrays.asList("1.2.840.10008.1.2", "1.2.840.10008.1.2.1", "1.2.840.10008.1.2.2",
                "1.2.840.10008.1.2.4.70", "1.2.840.10008.1.2.4.80", "1.2.840.10008.1.2.4.57", "1.2.840.10008.1.2.4.90",
                "1.2.840.10008.1.2.4.91", "1.2.840.10008.1.2.5", "1.2.840.10008.1.2.4.100", "1.2.840.10008.1.2.4.51");
        assertSameContent(defaultTS, ((DicomServicesImpl) dcm).getDefaultTransferSyntaxes());

        Collection<SOPClass> expectedRawSOP = SOPList.getInstance().asSOPClassList();
        assertSameContent(expectedRawSOP, ((DicomServicesImpl) dcm).getRawSOPClasses());

        // test remove move destination
        boolean removed = settings.getDicomServicesSettings().removeMoveDestination(md.getAETitle());
        assertTrue(removed);
        assertTrue(settings.getDicomServicesSettings().getMoveDestinations().isEmpty());

        // clean up
        Files.delete(target);
    }

    /** Test that transfer options are well loaded from the settings file
     * and installed on the global SOP list correctly.
     */
    @Test
    public void testLoadTransferOptions() throws IOException {
        // Load default settings and update transfer options

        // read the settings from our test config file
        ServerSettings settings = ServerSettingsManager.loadSettingsAt(this.testConfigTs);
        ServerSettings.DicomServices dcm = settings.getDicomServicesSettings();

        // should distinguish between an empty list and missing list
        for (SOPClass sopClass : dcm.getSOPClasses()) {
            switch (sopClass.getUID()) {
                case UID.CTImageStorage:
                    assertEquals(Arrays.asList("1.2.840.10008.1.2", "1.2.840.10008.1.2.1"),
                            sopClass.getTransferSyntaxes());
                    break;
                case UID.UltrasoundMultiFrameImageStorage:
                    assertEquals(Arrays.asList("1.2.840.10008.1.2", "1.2.840.10008.1.2.1", "1.2.840.10008.1.2.4.50",
                            "1.2.840.10008.1.2.4.70"), sopClass.getTransferSyntaxes());
                    break;
                case UID.UltrasoundMultiFrameImageStorageRetired:
                    // no transfer syntaxes here,
                    // and no default transfer syntaxes,
                    // means no acceptable transfer syntax
                    assertEquals(Collections.emptyList(), sopClass.getTransferSyntaxes());
                    break;
                default:
            }
        }

        // update global SOP list based on settings
        SOPList list = SOPList.getInstance();
        list.readFromSettings(settings);

        // see that the SOP list has been updated accordingly

        assertEquals(Arrays.asList("1.2.840.10008.1.2", "1.2.840.10008.1.2.1"),
                list.getTS(UID.CTImageStorage).asList());
        assertEquals(Arrays.asList("1.2.840.10008.1.2", "1.2.840.10008.1.2.1", "1.2.840.10008.1.2.4.70",
                "1.2.840.10008.1.2.4.50"), list.getTS(UID.UltrasoundMultiFrameImageStorage).asList());
        assertEquals(Collections.emptyList(), list.getTS(UID.UltrasoundMultiFrameImageStorageRetired).asList());

        // unmentioned SOP classes default to
        // accepting the default transfer syntaxes
        // (in this case, empty)
        assertEquals(Collections.emptyList(), list.getTS(UID.BreastTomosynthesisImageStorage).asList());
    }

    @Test
    public void testUpdateTransferOptions() throws IOException {
        // Load default settings and update transfer options

        // read the settings from our test config file
        ServerSettings settings = ServerSettingsManager.loadSettingsAt(this.testConfigTs);

        // update global SOP list based on settings
        SOPList list = SOPList.getInstance();
        list.readFromSettings(settings);

        SOPList sopList = SOPList.getInstance();

        TransfersStorage ts = sopList.getTS(UID.CTImageStorage);
        // make changes so that this SOP class is never accepted
        for (int i = 0; i < ts.getTS().length; i++) {
            ts.setTS(false, i);
        }
        // write changes back to settings
        sopList.writeToSettings(settings);

        // inspect changes
        final ServerSettings.DicomServices dcm = settings.getDicomServicesSettings();

        Collection<SOPClass> sopClasses = dcm.getSOPClasses();
        for (SOPClass sopClass : sopClasses) {
            Collection<String> tses = sopClass.getTransferSyntaxes();

            switch (sopClass.getUID()) {
                case UID.CTImageStorage:
                    assertEquals(Collections.emptyList(), tses);
                    break;
                case UID.EnhancedXAImageStorage:
                    assertEquals(Arrays.asList("1.2.840.10008.1.2", "1.2.840.10008.1.2.1", "1.2.840.10008.1.2.5"),
                            tses);
                    break;
                case UID.EnhancedCTImageStorage:
                    // fallthrough (same settings)
                case UID.UltrasoundMultiFrameImageStorage:
                    assertEquals(Arrays.asList("1.2.840.10008.1.2", "1.2.840.10008.1.2.1", "1.2.840.10008.1.2.4.70",
                            "1.2.840.10008.1.2.4.50"), tses);
                    break;
                default:
                    break;
            }
        }
    }

    @Test
    public void testMultiDim() throws IOException {
        // read the settings from our test config file
        ServerSettings settings = ServerSettingsManager.loadSettingsAt(this.testConfigDIM);
        final ServerSettings.Archive ar = settings.getArchiveSettings();
        assertTrue(settings instanceof ServerSettingsImpl);
        // assertions follow
        assertEquals("/opt/mydata", ar.getMainDirectory());
        assertEquals(Arrays.asList("lucene", "postgres"), ar.getDIMProviders());
        assertEquals(Arrays.asList("filestorage", "s3"), ar.getDefaultStorage());
    }


    @Test
    public void testSopClasses() throws IOException {
        // read the settings from our test config file
        ServerSettings settings = ServerSettingsManager.loadSettingsAt(this.testConfigSopClasses);
        final ServerSettings.DicomServices ds = settings.getDicomServicesSettings();
        assertTrue(settings instanceof ServerSettingsImpl);
        // assertions follow

        Collection<String> defaultTS = Arrays.asList("1.2.840.10008.1.2", "1.2.840.10008.1.2.1",
                "1.2.840.10008.1.2.4.80", "1.2.840.10008.1.2.4.50");
        assertSameContent(defaultTS, ((DicomServicesImpl) ds).getDefaultTransferSyntaxes());


    }

    @Test
    public void testAdditionals() throws IOException {
        // read the settings from our test config file
        ServerSettings settings = ServerSettingsManager.loadSettingsAt(this.testConfigAdditionals);
        final ServerSettings.DicomServices ds = settings.getDicomServicesSettings();
        assertTrue(settings instanceof ServerSettingsImpl);

        Logger logger = NOPLogger.NOP_LOGGER;
        // Filter (as in init)
        ds.setAdditionalTransferSyntaxes(ds.getAdditionalTransferSyntaxes().stream()
                .filter(ats -> AdditionalTransferSyntax.isValid(ats, logger)).collect(Collectors.toList()));
        ds.setAdditionalSOPClasses(ds.getAdditionalSOPClasses().stream()
                .filter(asc -> AdditionalSOPClass.isValid(asc, logger)).collect(Collectors.toList()));

        // assertions follow

        // assert the parsed additional transfer options are expected
        // SOP classes
        Collection<AdditionalSOPClass> additionalSOPClasses = new ArrayList<>();
        additionalSOPClasses
                .add(new AdditionalSOPClass("1.2.840.10008.5.1.4.1.1.131", "BasicStructuredDisplayStorage"));
        assertSameContent(additionalSOPClasses, ds.getAdditionalSOPClasses());

        // Transfer Syntaxes
        Collection<AdditionalTransferSyntax> additionalTransferSyntaxes = new ArrayList<>();
        additionalTransferSyntaxes.add(new AdditionalTransferSyntax("1.2.840.10008.1.2.4.95", "JPIPReferencedDeflated",
                null, false, null, true));
        assertSameContent(additionalTransferSyntaxes, ds.getAdditionalTransferSyntaxes());
    }


    private static void assertSameContent(Collection<?> o1, Collection<?> o2) {
        assertNotNull("left-hand collection is null", o1);
        assertNotNull("right-hand collection is null", o2);
        assertEquals("Collection sizes do not match", o1.size(), o2.size());
        for (Object o : o1) {
            if (!o2.contains(o)) {
                throw new ComparisonFailure("collection contents do not match", String.valueOf(o1), String.valueOf(o2));
            }
        }
        for (Object o : o2) {
            if (!o1.contains(o)) {
                throw new ComparisonFailure("collection contents do not match", String.valueOf(o1), String.valueOf(o2));
            }
        }
    }
}
